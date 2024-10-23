package moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.document;

import com.apple.library.foundation.NSString;
import com.apple.library.uikit.UIColor;
import moe.plushie.armourers_workshop.api.common.IResultHandler;
import moe.plushie.armourers_workshop.api.skin.ISkinType;
import moe.plushie.armourers_workshop.core.client.gui.notification.UserNotificationCenter;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.core.skin.exception.TranslatableException;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.core.skin.serializer.SkinSerializer;
import moe.plushie.armourers_workshop.core.skin.transformer.blockbench.BlockBenchExporter;
import moe.plushie.armourers_workshop.core.skin.transformer.blockbench.BlockBenchPack;
import moe.plushie.armourers_workshop.core.skin.transformer.blockbench.BlockBenchPackReader;
import moe.plushie.armourers_workshop.init.environment.EnvironmentExecutor;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class DocumentImporter {

    private boolean keepItemTransforms = false;
    private final File inputFile;
    private final ISkinType targetType;
    private final DocumentBoneMapper boneMapper;

    public DocumentImporter(File inputFile, ISkinType targetType) {
        this.inputFile = inputFile;
        this.targetType = targetType;
        this.boneMapper = DocumentBoneMapper.of(targetType);
    }

    public boolean isKeepItemTransforms() {
        return keepItemTransforms;
    }

    public void setKeepItemTransforms(boolean keepItemTransforms) {
        this.keepItemTransforms = keepItemTransforms;
    }

    public void execute(Consumer<Skin> consumer) {
        generateSkin((skin, exception) -> {
            try {
                if (skin != null) {
                    consumer.accept(skin);
                } else {
                    throw exception;
                }
            } catch (TranslatableException e) {
                e.printStackTrace();
                var message = new NSString(e.getComponent());
                var title = NSString.localizedString("advanced-skin-builder.dialog.importer.title");
                UserNotificationCenter.showToast(message, UIColor.RED, title, null);
            } catch (Exception e) {
                e.printStackTrace();
                var message = NSString.localizedString("advanced-skin-builder.dialog.importer.unknownException");
                var title = NSString.localizedString("advanced-skin-builder.dialog.importer.title");
                UserNotificationCenter.showToast(message, UIColor.RED, title, null);
            }
        });
    }

    private void generateSkin(IResultHandler<Skin> resultHandler) {
        EnvironmentExecutor.runOnBackground(() -> () -> {
            try {
                if (!inputFile.exists()) {
                    throw new TranslatableException("inventory.armourers_workshop.skin-library.error.illegalModelFile");
                }
                var skin = readSkinFromFile(inputFile);
                if (skin == null || skin.getParts().isEmpty()) {
                    throw new TranslatableException("inventory.armourers_workshop.skin-library.error.illegalModelFormat");
                }
                Minecraft.getInstance().execute(() -> resultHandler.accept(apply(skin)));
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> resultHandler.throwing(e));
            }
        });
    }

    private Skin readSkinFromFile(File file) throws IOException {
        return readSkinFromReader(new BlockBenchPackReader(file));
    }

    private Skin readSkinFromReader(BlockBenchPackReader reader) throws IOException {
        var pack = reader.readPack();
        var exporter = new BlockBenchExporter(pack);

        var settings = exporter.getSettings();
        var properties = exporter.getProperties();

        var name = pack.getName();
        if (name != null && !name.isEmpty()) {
            properties.put(SkinProperty.ALL_CUSTOM_NAME, name);
        }

        var description = pack.getDescription();
        if (description != null && !description.isEmpty()) {
            properties.put(SkinProperty.ALL_FLAVOUR_TEXT, description);
        }

        var authors = pack.getAuthors();
        if (authors != null && !authors.isEmpty()) {
            var joiner = new StringJoiner(",");
            authors.forEach(joiner::add);
            properties.put(SkinProperty.ALL_AUTHOR_NAME, joiner.toString());
        }

        // a special author uuid to identity imported skin.
        properties.put(SkinProperty.ALL_AUTHOR_UUID, "generated by block bench importer");

        // the export skin must to editable.
        settings.setEditable(true);

        exporter.setOffset(getPackOrigin(pack));
        return exporter.export();
    }

    private Skin apply(Skin skin) {
        var settings = skin.getSettings().copy();
        var properties = skin.getProperties().copy();

        if (!isKeepItemTransforms()) {
            settings.setItemTransforms(null);
        }

        var rootParts = new ArrayList<>(skin.getParts());
        if (!boneMapper.isEmpty()) {
            skin.getParts().forEach(it -> extractToRootPart(it, new Stack<>(), rootParts));
        }

        var builder = new Skin.Builder(SkinTypes.ADVANCED);
        builder.parts(rootParts);
        builder.settings(settings);
        builder.properties(properties);
        builder.animations(skin.getAnimations());
        builder.version(SkinSerializer.Versions.V20);
        return builder.build();
    }


    private void extractToRootPart(SkinPart part, Stack<SkinPart> parent, List<SkinPart> rootParts) {
        // search all child part.
        var children = new ArrayList<>(part.getChildren());
        for (var child : children) {
            parent.push(part);
            extractToRootPart(child, parent, rootParts);
            parent.pop();
        }
        // the part is rewrite?
        var entry = boneMapper.get(part.getName());
        if (entry != null && entry.isRootPart()) {
            // remove from the part tree.
            if (parent.isEmpty()) {
                rootParts.remove(part);
            } else {
                var parentPart = parent.peek();
                parentPart.removePart(part);
            }
            var builder = new SkinPart.Builder(entry.getType());
            builder.name(part.getName());
            builder.transform(convertToLocal(part, entry, parent));
            builder.geometries(part.getGeometries());
            builder.children(part.getChildren());
            rootParts.add(builder.build());
        }
    }

    private SkinTransform convertToLocal(SkinPart part, DocumentBoneMapper.Entry entry, Stack<SkinPart> parent) {
        // TODO: @SAGESSE add built-in pivot support.
        //var origin = getParentOrigin(parent).adding(transform.getTranslate());
        //translate = origin;
        //rotation = transform.getRotation();
        //pivot = transform.getPivot();
        var translate = entry.getOffset(); // 0 + offset
        var rotation = Vector3f.ZERO; // never use rotation on the built-in part type.
        return SkinTransform.create(translate, rotation, Vector3f.ONE);
    }

    private Vector3f getPackOrigin(BlockBenchPack pack) {
        // relocation the block model origin to the bottom-center(0, 8, 0).
        if (targetType == SkinTypes.BLOCK) {
            // work in java_block.
            if (pack.getFormat().equals("java_block")) {
                return new Vector3f(8, 8, -8);
            }
            // work in bedrock_block/bedrock_entity/bedrock_entity_old/geckolib_block/generic_block/modded_entity/optifine_entity.
            return new Vector3f(0, 8, 0);
        }
        // relocation the entity model origin to the head bottom (0, 24, 0).
        if (targetType == SkinTypes.OUTFIT || targetType == SkinTypes.ARMOR_HEAD || targetType == SkinTypes.ARMOR_CHEST || targetType == SkinTypes.ARMOR_LEGS || targetType == SkinTypes.ARMOR_FEET || targetType == SkinTypes.ARMOR_WINGS) {
            return new Vector3f(0, 24, 0);
        }
        return Vector3f.ZERO;
    }

    private Vector3f getParentOrigin(Stack<SkinPart> parent) {
        var origin = Vector3f.ZERO;
        for (var part : parent) {
            if (part.getTransform() instanceof SkinTransform transform) {
                origin = origin.adding(transform.getTranslate());
            }
        }
        return origin;
    }
}
