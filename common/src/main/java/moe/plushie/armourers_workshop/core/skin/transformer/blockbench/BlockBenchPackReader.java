package moe.plushie.armourers_workshop.core.skin.transformer.blockbench;

import com.google.gson.JsonElement;
import moe.plushie.armourers_workshop.api.core.IResource;
import moe.plushie.armourers_workshop.api.data.IDataPackObject;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOConsumer;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOConsumer2;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOFunction;
import moe.plushie.armourers_workshop.utils.SkinFileUtils;
import moe.plushie.armourers_workshop.utils.StreamUtils;
import moe.plushie.armourers_workshop.utils.math.Rectangle2f;
import moe.plushie.armourers_workshop.utils.math.Size2f;
import moe.plushie.armourers_workshop.utils.math.Size3f;
import moe.plushie.armourers_workshop.utils.math.TexturePos;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/// https://www.blockbench.net/wiki/docs/bbmodel
/// https://github.com/JannisX11/blockbench/blob/master/js/io/formats/bbmodel.js
public class BlockBenchPackReader {

    protected final String name;
    protected final ResourceSet resourceSet;

    public BlockBenchPackReader(File file) throws IOException {
        this.name = file.getName();
        this.resourceSet = new ResourceSet(file);
    }

    public BlockBenchPack readPack() throws IOException {
        var modelObject = PackObject.from(resourceSet.findResource("(.*)\\.bbmodel"));
        if (modelObject != null) {
            return parsePackObject(modelObject);
        }
        throw new IOException("error.bb.loadModel.noModel");
    }

    protected BlockBenchPack parsePackObject(PackObject object) throws IOException {
        var builder = new BlockBenchPack.Builder();

        // pack info
        object.at("name", it -> builder.name(it.stringValue()));
//        object.at("description", it -> builder.description(it.stringValue()));
//        object.at("author", it -> builder.author(it.collect(IDataPackObject::stringValue)));
        object.at("meta.format_version", it -> builder.version(it.stringValue()));
        object.at("meta.model_format", it -> builder.format(it.stringValue()));

        object.at("resolution", it -> builder.resolution(it.size2fValue()));

        object.each("elements", it -> builder.addElement(parseElementObject(it)));
        object.each("textures", it -> builder.addTexture(parseTextureObject(it)));
        object.each("animations", it -> builder.addAnimation(parseAnimationObject(it)));

        object.each("outliner", it -> builder.addOutliner(parseChildOutlinerObject(it)));

        object.each("display", (name, it) -> builder.addDisplay(name, parseTransformObject(it)));

        return builder.build();
    }

    protected BlockBenchElement parseElementObject(PackObject object) throws IOException {
        var transformer = switch (object.get("type").stringValue()) {
            case "cube" -> parseElementObject(BlockBenchCube.Builder::new, builder -> {
                object.at("from", it -> builder.from(it.vector3fValue()));
                object.at("to", it -> builder.to(it.vector3fValue()));

                object.at("origin", it -> builder.origin(it.vector3fValue()));
                object.at("rotation", it -> builder.rotation(it.vector3fValue()));

                object.at("inflate", it -> builder.inflate(it.floatValue()));

                object.at("allow_mirror_modeling", it -> builder.allowMirrorModeling(it.boolValue()));
                object.at("box_uv", it -> builder.boxUV(it.boolValue()));
                object.at("mirror_uv", it -> builder.mirrorUV(it.boolValue()));
                object.at("uv_offset", it -> builder.uvOffset(it.vector2fValue()));

                object.at("faces", c1 -> {
                    c1.at("north", it -> builder.addFace(Direction.NORTH, parseFaceObject(it)));
                    c1.at("south", it -> builder.addFace(Direction.SOUTH, parseFaceObject(it)));
                    c1.at("east", it -> builder.addFace(Direction.EAST, parseFaceObject(it)));
                    c1.at("west", it -> builder.addFace(Direction.WEST, parseFaceObject(it)));
                    c1.at("up", it -> builder.addFace(Direction.UP, parseFaceObject(it)));
                    c1.at("down", it -> builder.addFace(Direction.DOWN, parseFaceObject(it)));
                });
            });
            case "mesh" -> parseElementObject(BlockBenchMesh.Builder::new, builder -> {
                object.at("origin", it -> builder.origin(it.vector3fValue()));
                //object.at("origin", it -> builder.origin(it.vector3fValue()));
                object.at("rotation", it -> builder.rotation(it.vector3fValue()));

                object.at("allow_mirror_modeling", it -> builder.allowMirrorModeling(it.boolValue()));
                object.at("box_uv", it -> builder.boxUV(it.boolValue()));
                object.at("mirror_uv", it -> builder.mirrorUV(it.boolValue()));
                object.at("uv_offset", it -> builder.uvOffset(it.vector2fValue()));

                object.each("vertices", (key, value) -> {
                    builder.addVertex(key, value.vector3fValue());
                });

                object.each("faces", (key, value) -> {
                    var builder2 = new BlockBenchMeshFace.Builder();
                    value.each("uv", (key2, value2) -> builder2.addUV(key2, value2.vector2fValue()));
                    value.each("vertices", it2 -> builder2.addVertex(it2.stringValue()));
                    value.at("texture", it2 -> {
                        if (!it2.isNull()) {
                            builder2.texture(it2.intValue());
                        }
                    });
                    builder.addFace(key, builder2.build());
                });
            });
            case "locator" -> parseElementObject(BlockBenchLocator.Builder::new, builder -> {
                object.at("position", it -> builder.position(it.vector3fValue()));
                object.at("rotation", it -> builder.rotation(it.vector3fValue()));
            });
            case "null_object" -> parseElementObject(BlockBenchNull.Builder::new, builder -> {
                object.at("position", it -> builder.position(it.vector3fValue()));
            });
            default -> parseElementObject(BlockBenchElement.Builder::new, null);
        };
        return transformer.apply(object);
    }

    protected <T extends BlockBenchElement.Builder> IOFunction<PackObject, BlockBenchElement> parseElementObject(Supplier<T> supplier, @Nullable IOConsumer<T> consumer) {
        return object -> {
            var builder = supplier.get();

            object.at("uuid", it -> builder.uuid(it.stringValue()));
            object.at("name", it -> builder.name(it.stringValue()));
            object.at("type", it -> builder.type(it.stringValue()));

            object.at("export", it -> builder.export(it.boolValue()));

            // ignore_inherited_scale
            // visibility
            // locked

            if (consumer != null) {
                consumer.accept(builder);
            }

            return builder.build();
        };
    }

    protected BlockBenchOutliner parseOutlinerObject(PackObject object) throws IOException {
        var builder = new BlockBenchOutliner.Builder();

        object.at("uuid", it -> builder.uuid(it.stringValue()));
        object.at("name", it -> builder.name(it.stringValue()));

        object.at("origin", it -> builder.origin(it.vector3fValue()));
        object.at("rotation", it -> builder.rotation(it.vector3fValue()));

        object.at("export", it -> builder.export(it.boolValue()));

        object.each("children", it -> builder.addChild(parseChildOutlinerObject(it)));

        return builder.build();
    }

    protected Object parseChildOutlinerObject(PackObject object) throws IOException {
        if (object.type() == IDataPackObject.Type.STRING) {
            return object.stringValue();
        }
        return parseOutlinerObject(object);
    }

    protected BlockBenchDisplay parseTransformObject(PackObject object) throws IOException {
        var builder = new BlockBenchDisplay.Builder();
        object.at("translation", it -> builder.translation(it.vector3fValue()));
        object.at("rotation", it -> builder.rotation(it.vector3fValue()));
        object.at("scale", it -> builder.scale(it.vector3fValue()));
        return builder.build();
    }

    protected BlockBenchTexture parseTextureObject(PackObject object) throws IOException {
        var builder = new BlockBenchTexture.Builder();
        object.at("name", it -> builder.name(it.stringValue()));
        object.at("uuid", it -> builder.uuid(it.stringValue()));
        object.at("source", it -> builder.source(it.stringValue()));
        object.at("render_mode", it -> builder.renderMode(it.stringValue()));
        object.at("frame_time", it -> builder.frameTime(it.intValue()));
        object.at("frame_order_type", it -> builder.frameOrderType(it.stringValue()));
        object.at("frame_order", it -> builder.frameOrder(it.stringValue()));
        object.at("frame_interpolate", it -> builder.frameInterpolate(it.boolValue()));
        object.at("width", width -> object.at("height", height -> builder.imageSize(new Size2f(width.floatValue(), height.floatValue()))));
        object.at("uv_width", width -> object.at("uv_height", height -> builder.textureSize(new Size2f(width.floatValue(), height.floatValue()))));
        return builder.build();
    }

    protected BlockBenchAnimation parseAnimationObject(PackObject object) throws IOException {
        var builder = new BlockBenchAnimation.Builder();
        object.at("name", it -> builder.name(it.stringValue()));
        object.at("uuid", it -> builder.uuid(it.stringValue()));
        object.at("loop", it -> builder.loop(it.stringValue()));
        object.at("length", it -> builder.duration(it.floatValue()));
        object.each("animators", (key, it) -> builder.addAnimator(parseAnimatorObject(key, it)));
        // "override"
        // "snapping"
        // "selected"
        // "anim_time_update"
        // "blend_weight"
        // "start_delay"
        // "loop_delay"
        return builder.build();
    }

    protected BlockBenchAnimator parseAnimatorObject(String uuid, PackObject object) throws IOException {
        var builder = new BlockBenchAnimator.Builder(uuid);
        object.at("name", it -> builder.name(it.stringValue()));
        object.at("uuid", it -> builder.uuid(it.stringValue()));
        object.at("type", it -> builder.type(it.stringValue()));
        object.each("keyframes", fo -> {
            var fb = new BlockBenchKeyFrame.Builder();
            fo.at("uuid", it -> fb.uuid(it.stringValue()));
            fo.at("channel", it -> fb.name(it.stringValue()));
            fo.at("time", it -> fb.time(it.floatValue()));
            fo.at("interpolation", it -> {
                fb.interpolation(it.stringValue());
                if (it.stringValue().equals("bezier")) {
                    var values = new ArrayList<Vector3f>();
                    var parameters = new ArrayList<Float>();
                    //fo.get("bezier_linked");
                    values.add(fo.get("bezier_left_time").vector3fValue());
                    values.add(fo.get("bezier_left_value").vector3fValue());
                    values.add(fo.get("bezier_right_time").vector3fValue());
                    values.add(fo.get("bezier_right_value").vector3fValue());
                    for (var parameter : values) {
                        parameters.add(parameter.getX());
                        parameters.add(parameter.getY());
                        parameters.add(parameter.getZ());
                    }
                    fb.parameters(parameters);
                }
            });
            fo.each("data_points", it -> {
                fb.add(it.get("x"));
                fb.add(it.get("y"));
                fb.add(it.get("z"));
            });
            builder.addFrame(fb.build());
        });
        return builder.build();
    }


    protected BlockBenchCubeFace parseFaceObject(PackObject object) throws IOException {
        var builder = new BlockBenchCubeFace.Builder();
        object.at("rotation", it -> builder.rotation(it.intValue()));
        object.at("uv", it -> builder.uv(it.rectangle2fValue()));
        object.at("texture", it -> {
            if (!it.isNull()) {
                builder.texture(it.intValue());
            }
        });
        return builder.build();
    }

    public String getName() {
        return name;
    }

    protected static class PackObject implements IDataPackObject {

        private final JsonElement element;

        public PackObject(IDataPackObject object) {
            this.element = object.jsonValue();
        }

        @Nullable
        public static PackObject from(IResource resource) throws IOException {
            if (resource == null) {
                return null;
            }
            var inputStream = new BufferedInputStream(resource.getInputStream());
            try {
                var object = StreamUtils.fromPackObject(inputStream);
                if (object == null) {
                    return null;
                }
                return new PackObject(object);
            } catch (Exception exception) {
                throw new IOException(exception);
            }
        }

        public TexturePos texturePosValue() {
            var values = allValues();
            if (values.size() >= 2) {
                var iterator = values.iterator();
                return new TexturePos(iterator.next().intValue(), iterator.next().intValue());
            }
            return TexturePos.ZERO;
        }

        public Vector2f vector2fValue() {
            var values = allValues();
            if (values.size() >= 2) {
                var iterator = values.iterator();
                return new Vector2f(iterator.next().floatValue(), iterator.next().floatValue());
            }
            return Vector2f.ZERO;
        }

        public Size2f size2fValue() {
            var values = allValues();
            if (values.size() >= 2) {
                var iterator = values.iterator();
                return new Size2f(iterator.next().floatValue(), iterator.next().floatValue());
            }
            return Size2f.ZERO;
        }

        public Rectangle2f rectangle2fValue() {
            var values = allValues();
            if (values.size() >= 4) {
                var iterator = values.iterator();
                var x1 = iterator.next().floatValue();
                var y1 = iterator.next().floatValue();
                var x2 = iterator.next().floatValue();
                var y2 = iterator.next().floatValue();
                return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
            }
            return Rectangle2f.ZERO;
        }

        public Size3f size3fValue() {
            var values = allValues();
            if (values.size() >= 3) {
                var iterator = values.iterator();
                return new Size3f(iterator.next().floatValue(), iterator.next().floatValue(), iterator.next().floatValue());
            }
            return Size3f.ZERO;
        }

        public Vector3f vector3fValue() {
            var values = allValues();
            if (values.size() >= 3) {
                var iterator = values.iterator();
                return new Vector3f(iterator.next().floatValue(), iterator.next().floatValue(), iterator.next().floatValue());
            }
            return Vector3f.ZERO;
        }

        public void at(String keyPath, IOConsumer<PackObject> consumer) throws IOException {
            var object = by(keyPath);
            if (object.isNull()) {
                return;
            }
            consumer.accept(new PackObject(by(keyPath)));
        }

        public void each(String keyPath, IOConsumer<PackObject> consumer) throws IOException {
            var object = by(keyPath);
            if (object.isNull()) {
                return;
            }
            for (var value : object.allValues()) {
                consumer.accept(new PackObject(value));
            }
        }

        public void each(String keyPath, IOConsumer2<String, PackObject> consumer) throws IOException {
            var object = by(keyPath);
            if (object.isNull()) {
                return;
            }
            for (var pair : object.entrySet()) {
                consumer.accept(pair.getKey(), new PackObject(pair.getValue()));
            }
        }

        @Override
        public PackObject get(String key) {
            return new PackObject(IDataPackObject.super.get(key));
        }

        public PackObject by(String keyPath) {
            // when this is a full key, ignore.
            if (has(keyPath)) {
                return get(keyPath);
            }
            var keys = keyPath.split("\\.");
            PackObject object = this;
            for (String key : keys) {
                object = object.get(key);
            }
            return object;
        }

        @Override
        public JsonElement jsonValue() {
            return element;
        }
    }

    protected static class ResourceSet {

        private final Collection<IResource> resources;

        public ResourceSet(File file) throws IOException {
            this.resources = getResourcesFromFile(file);
        }

        @Nullable
        public IResource findResource(String regex) {
            var pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            for (var resource : resources) {
                if (pattern.matcher(resource.getName()).find()) {
                    return resource;
                }
            }
            return null;
        }

        @Nullable
        public IResource getResource(String name) {
            for (var resource : resources) {
                if (resource.getName().equalsIgnoreCase(name)) {
                    return resource;
                }
            }
            return null;
        }

        public Collection<IResource> getResources() {
            return resources;
        }

        protected Collection<IResource> getResourcesFromFile(File file) throws IOException {
            if (file.isDirectory()) {
                return getResourcesFromDirectory(file);
            }
            if (file.getName().toLowerCase().endsWith(".zip")) {
                return getResourcesFromZip(file);
            }
            return getResourcesFromSet(file);
        }

        protected Collection<IResource> getResourcesFromZip(File zipFile) throws IOException {
            var resources = new ArrayList<IResource>();
            var file = new ZipFile(zipFile);
            var zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                var fileName = entry.getName();
                var fileEntry = entry;
                resources.add(new IResource() {
                    @Override
                    public String getName() {
                        return fileName;
                    }

                    @Override
                    public String getSource() {
                        return "";
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return file.getInputStream(fileEntry);
                    }
                });
            }
            return resources;
        }

        protected Collection<IResource> getResourcesFromDirectory(File rootPath) throws IOException {
            var resources = new ArrayList<IResource>();
            for (var entry : SkinFileUtils.listAllFiles(rootPath)) {
                if (entry.isDirectory()) {
                    continue;
                }
                var fileName = SkinFileUtils.getRelativePath(entry, rootPath, true).substring(1);
                resources.add(new IResource() {
                    @Override
                    public String getName() {
                        return fileName;
                    }

                    @Override
                    public String getSource() {
                        return "";
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(entry);
                    }
                });
            }
            return resources;
        }

        protected Collection<IResource> getResourcesFromSet(File... entries) throws IOException {
            var resources = new ArrayList<IResource>();
            for (var entry : entries) {
                if (entry.isDirectory()) {
                    continue;
                }
                var fileName = entry.getName();
                resources.add(new IResource() {
                    @Override
                    public String getName() {
                        return fileName;
                    }

                    @Override
                    public String getSource() {
                        return "";
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(entry);
                    }
                });
            }
            return resources;
        }
    }
}
