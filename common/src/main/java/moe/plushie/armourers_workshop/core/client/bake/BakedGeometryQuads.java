package moe.plushie.armourers_workshop.core.client.bake;

import moe.plushie.armourers_workshop.api.skin.ISkinPartType;
import moe.plushie.armourers_workshop.api.skin.ISkinTransform;
import moe.plushie.armourers_workshop.core.data.color.ColorDescriptor;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.core.data.transform.SkinPartTransform;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryFace;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCubeFace;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCubeFaceCuller;
import moe.plushie.armourers_workshop.core.skin.painting.SkinPaintTypes;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartTypes;
import moe.plushie.armourers_workshop.core.texture.PlayerTextureModel;
import moe.plushie.armourers_workshop.core.texture.SkinPaintData;
import moe.plushie.armourers_workshop.core.texture.SkinPreviewData;
import moe.plushie.armourers_workshop.utils.math.OpenPoseStack;
import moe.plushie.armourers_workshop.utils.math.OpenVoxelShape;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Rectangle3i;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class BakedGeometryQuads {

    //private final HashMap<Direction, ArrayList<BakedCubeFace>> dirFaces = new HashMap<>();
    private final HashMap<RenderType, CompressedList<BakedGeometryFace>> splitFaces = new HashMap<>();

    private final OpenVoxelShape shape;
    private final ColorDescriptor colorInfo;

    public BakedGeometryQuads(OpenVoxelShape shape, ColorDescriptor colorInfo) {
        this.shape = shape;
        this.colorInfo = colorInfo;
    }

    public static QuadsList<ISkinPartType> from(SkinPart part) {
        var quads = new QuadsList<ISkinPartType>();
        var geometries = part.getGeometries();
        var shape = geometries.getShape();
        var bounds = new Rectangle3i(shape.bounds());
        SkinCubeFaceCuller.cullFaces2(geometries, bounds, part.getType()).forEach(result -> {
            // when has a different part type, it means the skin part was split.
            var newTransform = SkinTransform.createTranslateTransform(new Vector3f(result.getOrigin()));
            var newShape = shape;
            if (result.getPartType() != part.getType()) {
                var fixedBounds = result.getBounds().offset(bounds.getOrigin());
                newShape = OpenVoxelShape.box(fixedBounds);
            }
            var newQuads = new BakedGeometryQuads(newShape, new ColorDescriptor());
            newQuads.loadFaces(result.getFaces());
            quads.add(result.getPartType(), newTransform, newQuads);
        });
        return quads;
    }

    public static QuadsList<ISkinPartType> from(SkinPreviewData previewData) {
        var allQuads = new QuadsList<ISkinPartType>();
        if (previewData == null) {
            return allQuads;
        }
        previewData.forEach((transform, data) -> {
            var shape = data.getShape();
            var bounds = new Rectangle3i(shape.bounds());
            SkinCubeFaceCuller.cullFaces2(data, bounds, SkinPartTypes.BLOCK).forEach(result -> {
                var quad = new BakedGeometryQuads(shape, new ColorDescriptor());
                quad.loadFaces(result.getFaces());
                allQuads.add(result.getPartType(), transform, quad);
            });
        });
        return allQuads;
    }

    public static QuadsList<ISkinPartType> from(SkinPaintData paintData) {
        var allQuads = new QuadsList<ISkinPartType>();
        if (paintData == null) {
            return allQuads;
        }
        for (var entry : PlayerTextureModel.of(paintData.getWidth(), paintData.getHeight(), false).entrySet()) {
            var box = entry.getValue();
            var faces = new ArrayList<SkinGeometryFace>();
            box.forEach((texture, x, y, z, dir) -> {
                var paintColor = PaintColor.of(paintData.getColor(texture));
                if (paintColor.getPaintType() == SkinPaintTypes.NONE) {
                    return;
                }
                // in the vanilla's player textures are rendering without diffuse lighting.
                var shape = new Rectangle3f(x, y, z, 1, 1, 1);
                var transform = SkinTransform.IDENTITY;
                faces.add(new SkinCubeFace(SkinGeometryTypes.BLOCK_SOLID, transform, null, shape, dir, paintColor, 255));
            });
            if (!faces.isEmpty()) {
                var quads = new BakedGeometryQuads(OpenVoxelShape.box(box.getBounds()), new ColorDescriptor());
                quads.loadFaces(faces);
                allQuads.add(entry.getKey(), SkinTransform.IDENTITY, quads);
            }
        }
        return allQuads;
    }

    public static BakedGeometryQuads merge(BakedGeometryQuads parent, List<Pair<ISkinTransform, BakedGeometryQuads>> children) {
        // when children is empty, we no needs merge action.
        if (children.isEmpty()) {
            return parent;
        }
        // we need to recalculate the render bounds and shape.
        var mergedShape = parent.getShape().copy();
        children.forEach(pair -> {
            var transform = pair.getKey();
            var child = pair.getValue();
            if (child.getShape().isEmpty()) {
                return;
            }
            var shape = child.getShape().copy();
            var poseStack = new OpenPoseStack();
            transform.apply(poseStack);
            shape.mul(poseStack.last().pose());
            mergedShape.add(shape);
        });
        if (!mergedShape.isEmpty()) {
            mergedShape.optimize();
        }
        var result = new BakedGeometryQuads(mergedShape, parent.getColorInfo());
        parent.splitFaces.forEach((key, value) -> result.splitFaces.put(key, value.copy()));
        children.forEach(pair -> {
            var transform = pair.getKey();
            var child = pair.getValue();
            child.splitFaces.forEach((key, value) -> result.splitFaces.computeIfAbsent(key, CompressedList::new).addAll(transform, value));
        });
        return result;
    }

    public void forEach(BiConsumer<RenderType, CompressedList<BakedGeometryFace>> action) {
        splitFaces.forEach(action);
    }

//    public void forEach(OpenRay ray, Consumer<BakedCubeFace> recorder) {
//        if (dirFaces.isEmpty()) {
//            loadDirFaces();
//        }
//        dirFaces.forEach((dir, faces) -> {
//            for (var face : faces) {
//                if (face.intersects(ray)) {
//                    recorder.accept(face);
//                }
//            }
//        });
//    }

    private void loadFaces(Collection<? extends SkinGeometryFace> geometryFaces) {
        for (var geometryFace : geometryFaces) {
            if (!geometryFace.isVisible()) {
                continue;
            }
            var bakedFace = new BakedGeometryFace(geometryFace);
            addSplitFace(bakedFace.getRenderType(), bakedFace);
            if (bakedFace.getRenderTypeVariants() != null) {
                bakedFace.getRenderTypeVariants().forEach(renderType -> addSplitFace(renderType, bakedFace));
            }
            if (bakedFace.getDefaultVertex() != null) {
                colorInfo.add(bakedFace.getDefaultVertex().getColor());
            }
        }
        for (var filteredFaces : splitFaces.values()) {
            filteredFaces.sort(Comparator.comparingDouble(BakedGeometryFace::getRenderPriority));
        }
    }

//    private void loadDirFaces() {
//        splitFaces.values().forEach(faces -> faces.forEach(face -> {
//            dirFaces.computeIfAbsent(face.getDirection(), k -> new ArrayList<>()).add(face);
//        }));
//    }

    private void addSplitFace(RenderType renderType, BakedGeometryFace bakedFace) {
        splitFaces.computeIfAbsent(renderType, CompressedList::new).add(bakedFace);
    }

    public ColorDescriptor getColorInfo() {
        return colorInfo;
    }

    public OpenVoxelShape getShape() {
        return shape;
    }

    public int getFaceTotal() {
        int total = 0;
        for (var face : splitFaces.values()) {
            total += face.size();
        }
        return total;
    }

    public static class CompressedList<T> {

        private final RenderType renderType;
        private final ArrayList<T> values = new ArrayList<>();
        private final ArrayList<Pair<ISkinTransform, List<T>>> transformedValues = new ArrayList<>();

        public CompressedList(RenderType renderType) {
            this.renderType = renderType;
        }

        public void add(T face) {
            values.add(face);
        }

        public void addAll(ISkinTransform transform, CompressedList<T> compressedList) {
            transformedValues.add(Pair.of(transform, compressedList.values));
            for (var transformedValue : compressedList.transformedValues) {
                var combinedTransform = new SkinPartTransform();
                combinedTransform.addChild(transform);
                combinedTransform.addChild(transformedValue.getKey());
                transformedValues.add(Pair.of(combinedTransform, transformedValue.getRight()));
            }
        }

        public void sort(Comparator<? super T> comparator) {
            values.sort(comparator);
        }

        public void forEach(BiConsumer<ISkinTransform, List<T>> consumer) {
            consumer.accept(SkinTransform.IDENTITY, values);
            for (var transformedValue : transformedValues) {
                consumer.accept(transformedValue.getLeft(), transformedValue.getRight());
            }
        }

        public int size() {
            int total = values.size();
            for (var transformedValue : transformedValues) {
                total += transformedValue.getRight().size();
            }
            return total;
        }

        public RenderType renderType() {
            return renderType;
        }

        public CompressedList<T> copy() {
            var list = new CompressedList<T>(renderType);
            list.values.addAll(values);
            list.transformedValues.addAll(transformedValues);
            return list;
        }
    }

    public static class QuadsList<T> {

        private final ArrayList<Triple<T, ISkinTransform, BakedGeometryQuads>> quads = new ArrayList<>();

        public void add(T partType, ISkinTransform partTransform, BakedGeometryQuads quad) {
            quads.add(Triple.of(partType, partTransform, quad));
        }

        public void forEach(QuadsConsumer<T> consumer) {
            quads.forEach(pair -> consumer.accept(pair.getLeft(), pair.getMiddle(), pair.getRight()));
        }
    }

    public interface QuadsConsumer<T> {
        void accept(T key, ISkinTransform partTransform, BakedGeometryQuads quad);
    }
}
