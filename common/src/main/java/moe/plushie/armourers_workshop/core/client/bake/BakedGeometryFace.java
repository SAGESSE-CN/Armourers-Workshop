package moe.plushie.armourers_workshop.core.client.bake;

import com.google.common.collect.Lists;
import moe.plushie.armourers_workshop.api.client.IVertexConsumer;
import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.math.IPoseStack;
import moe.plushie.armourers_workshop.api.math.ITransformf;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.ISkinPartType;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderType;
import moe.plushie.armourers_workshop.core.client.texture.PlayerTextureLoader;
import moe.plushie.armourers_workshop.core.client.texture.TextureManager;
import moe.plushie.armourers_workshop.core.data.color.ColorDescriptor;
import moe.plushie.armourers_workshop.core.data.color.ColorScheme;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryFace;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCubeVertex;
import moe.plushie.armourers_workshop.core.skin.painting.SkinPaintTypes;
import moe.plushie.armourers_workshop.utils.MathUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BakedGeometryFace {

    private static final PaintColor RAINBOW_TARGET = PaintColor.of(0xff7f7f7f, SkinPaintTypes.RAINBOW);

    private final RenderType renderType;
    private final Collection<RenderType> renderTypeVariants;

    private final float priority;
    private final ITransformf transform;

    private final List<SkinGeometryVertex> vertices;

    private final SkinGeometryVertex defaultVertex;
    private final ITextureKey defaultTexture;

    public BakedGeometryFace(SkinGeometryFace geometryFace) {
        this.priority = geometryFace.getPriority();
        this.transform = geometryFace.getTransform();
        this.renderType = resolveRenderType(geometryFace);
        this.renderTypeVariants = resolveRenderTypeVariants(geometryFace);
        this.vertices = Lists.newArrayList(geometryFace.getVertices());
        this.defaultVertex = resolveDefaultVertex(vertices);
        this.defaultTexture = geometryFace.getTextureKey();
    }

    public void render(BakedSkinPart part, ColorScheme scheme, int lightmap, int overlay, IPoseStack poseStack, IVertexConsumer builder) {
        // when not any vertices found, we will ignore the rendering of this face.
        if (defaultVertex == null) {
            return;
        }

        // we need to blend the vertex color.
        // NOTE: we assume that all vertices use the same color, but in fact every vertex needs to be blended.
        var vertexColor = defaultVertex.getColor();
        var resolvedColor = resolveColor(vertexColor, scheme, part.getColorInfo(), part.getType(), 0);
        if (resolvedColor.getPaintType() == SkinPaintTypes.NONE) {
            return;
        }

        if (transform != SkinTransform.IDENTITY) {
            poseStack.pushPose();
            transform.apply(poseStack);
        }

        var entry = poseStack.last();

        var n = defaultTexture.getTotalWidth();
        var m = defaultTexture.getTotalHeight();

        var r = resolvedColor.getRed();
        var g = resolvedColor.getGreen();
        var b = resolvedColor.getBlue();
        var a = vertexColor.getAlpha();

        for (var vertex : vertices) {
            var position = vertex.getPosition();
            var normal = vertex.getNormal();
            var textureCoords = vertex.getTextureCoords();
            builder.vertex(entry, position.getX(), position.getY(), position.getZ())
                    .color(r, g, b, a)
                    .uv(textureCoords.getX() / n, textureCoords.getY() / m)
                    .overlayCoords(overlay)
                    .uv2(lightmap)
                    .normal(entry, normal.getX(), normal.getY(), normal.getZ())
                    .endVertex();
        }

        if (transform != SkinTransform.IDENTITY) {
            poseStack.popPose();
        }
    }

    private IPaintColor dye(IPaintColor source, IPaintColor destination, IPaintColor average) {
        if (destination.getPaintType() == SkinPaintTypes.NONE) {
            return PaintColor.CLEAR;
        }
        if (average == null) {
            return source;
        }
        int src = (source.getRed() + source.getGreen() + source.getBlue()) / 3;
        int avg = (average.getRed() + average.getGreen() + average.getBlue()) / 3;
        int r = MathUtils.clamp(destination.getRed() + src - avg, 0, 255);
        int g = MathUtils.clamp(destination.getGreen() + src - avg, 0, 255);
        int b = MathUtils.clamp(destination.getBlue() + src - avg, 0, 255);
        return PaintColor.of(r, g, b, destination.getPaintType());
    }


    private IPaintColor resolveTextureColor(IResourceLocation texture, ISkinPartType partType) {
        var bakedTexture = PlayerTextureLoader.getInstance().getTextureModel(texture);
        if (bakedTexture != null && defaultVertex instanceof SkinCubeVertex cubeVertex) {
            var shape = cubeVertex.getBoundingBox();
            var direction = cubeVertex.getDirection();
            int x = (int) shape.getX();
            int y = (int) shape.getY();
            int z = (int) shape.getZ();
            return bakedTexture.getColor(x, y, z, direction, partType);
        }
        return null;
    }

    private IPaintColor resolveColor(IPaintColor paintColor, ColorScheme scheme, ColorDescriptor descriptor, ISkinPartType partType, int deep) {
        var paintType = paintColor.getPaintType();
        if (paintType == SkinPaintTypes.NONE) {
            return PaintColor.CLEAR;
        }
        if (paintType == SkinPaintTypes.NORMAL) {
            return paintColor;
        }
        if (paintType == SkinPaintTypes.RAINBOW) {
            return dye(paintColor, RAINBOW_TARGET, descriptor.getAverageColor(paintType));
        }
        if (paintType == SkinPaintTypes.TEXTURE) {
            var paintColor1 = resolveTextureColor(scheme.getTexture(), partType);
            if (paintColor1 != null) {
                return paintColor1;
            }
            return paintColor;
        }
        if (paintType.getDyeType() != null && deep < 2) {
            var paintColor1 = scheme.getResolvedColor(paintType);
            if (paintColor1 == null) {
                return paintColor;
            }
            paintColor = dye(paintColor, paintColor1, descriptor.getAverageColor(paintType));
            return resolveColor(paintColor, scheme, descriptor, partType, deep + 1);
        }
        return paintColor;
    }

    private SkinGeometryVertex resolveDefaultVertex(List<SkinGeometryVertex> vertices) {
        if (!vertices.isEmpty()) {
            return vertices.get(0);
        }
        return null;
    }

    private RenderType resolveRenderType(SkinGeometryFace face) {
        var textureKey = face.getTextureKey();
        if (textureKey != null && textureKey.getProvider() != null) {
            return TextureManager.getInstance().register(textureKey.getProvider(), face.getType());
        }
        return SkinRenderType.by(face.getType());
    }

    private Collection<RenderType> resolveRenderTypeVariants(SkinGeometryFace face) {
        var texture = face.getTextureKey();
        if (texture == null || texture.getProvider() == null) {
            return null;
        }
        var parent = texture.getProvider();
        var renderTypes = new ArrayList<RenderType>();
        for (var variant : parent.getVariants()) {
            var properties = variant.getProperties();
            if (properties.isNormal() || properties.isSpecular()) {
                continue; // normal/specular map, only use from shader mod.
            }
            renderTypes.add(TextureManager.getInstance().register(variant, face.getType()));
        }
        return renderTypes;
    }

    public float getRenderPriority() {
        return priority;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public Collection<RenderType> getRenderTypeVariants() {
        return renderTypeVariants;
    }

    public SkinGeometryVertex getDefaultVertex() {
        return defaultVertex;
    }

    public List<SkinGeometryVertex> getVertices() {
        return vertices;
    }
}
