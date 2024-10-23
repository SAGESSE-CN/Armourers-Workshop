package moe.plushie.armourers_workshop.core.skin;

import moe.plushie.armourers_workshop.api.skin.ISkin;
import moe.plushie.armourers_workshop.api.skin.ISkinType;
import moe.plushie.armourers_workshop.core.data.transform.SkinItemTransforms;
import moe.plushie.armourers_workshop.core.skin.animation.SkinAnimation;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.core.skin.serializer.SkinSerializer;
import moe.plushie.armourers_workshop.core.texture.SkinPaintData;
import moe.plushie.armourers_workshop.core.texture.SkinPreviewData;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.ThreadUtils;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Rectangle3i;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Skin implements ISkin {

    private final int id;
    private final int version;

    private final SkinSettings settings;
    private final SkinProperties properties;
    private final ISkinType type;
    private final List<SkinPart> parts;
    private final List<SkinAnimation> animations;

    private final SkinPaintData paintData;
    private final SkinPreviewData previewData;
    private final Object blobs;

    private Map<BlockPos, Rectangle3f> blockBounds;

    protected Skin(int id, int version, ISkinType type, SkinProperties properties, SkinSettings settings, SkinPaintData paintData, SkinPreviewData previewData, List<SkinAnimation> skinAnimations, List<SkinPart> skinParts, Object blobs) {
        this.id = id;
        this.version = version;
        this.type = type;
        this.properties = properties;
        this.settings = settings;
        this.blobs = blobs;
        this.paintData = paintData;
        this.previewData = previewData;
        this.animations = new ArrayList<>(skinAnimations);
        this.parts = new ArrayList<>(skinParts);
    }

    public int getId() {
        return id;
    }

    public SkinSettings getSettings() {
        return settings;
    }

    public SkinProperties getProperties() {
        return properties;
    }

    public Map<BlockPos, Rectangle3f> getBlockBounds() {
        if (blockBounds != null) {
            return blockBounds;
        }
        blockBounds = new HashMap<>();
        if (type != SkinTypes.BLOCK) {
            return blockBounds;
        }
        var collisionBox = settings.getCollisionBox();
        blockBounds.put(BlockPos.ZERO, Rectangle3f.ZERO);
        if (collisionBox != null) {
            for (var rect : collisionBox) {
                var rect1 = new Rectangle3i(rect);
                int bx = -Math.floorDiv(rect1.getMinX(), 16);
                int by = -Math.floorDiv(rect1.getMinY(), 16);
                int bz = Math.floorDiv(rect1.getMinZ(), 16);
                int tx = Math.floorMod(rect1.getMinX(), 16) - 8;
                int ty = Math.floorMod(rect1.getMinY(), 16) - 8;
                int tz = Math.floorMod(rect1.getMinZ(), 16) - 8;
                int tw = rect1.getWidth();
                int th = rect1.getHeight();
                int td = rect1.getDepth();
                blockBounds.put(new BlockPos(bx, by, bz), new Rectangle3f(-tx, -ty, tz, -tw, -th, td));
            }
            return blockBounds;
        }
        for (var part : getParts()) {
            var partBlockBounds = part.getBlockBounds();
            if (partBlockBounds != null) {
                blockBounds.putAll(partBlockBounds);
            }
        }
        return blockBounds;
    }

    public int getModelCount() {
        int count = 0;
        for (var part : parts) {
            count += part.getModelCount();
        }
        return count;
    }

    public int getPartCount() {
        return parts.size();
    }

    @Override
    public ISkinType getType() {
        return type;
    }

    @Nullable
    public SkinPaintData getPaintData() {
        return paintData;
    }

    public SkinPreviewData getPreviewData() {
        return previewData;
    }

    @Override
    public List<SkinPart> getParts() {
        return parts;
    }

    public List<SkinAnimation> getAnimations() {
        return animations;
    }

    public SkinItemTransforms getItemTransforms() {
        return settings.getItemTransforms();
    }

    public String getCustomName() {
        return properties.get(SkinProperty.ALL_CUSTOM_NAME);
    }

    public String getAuthorName() {
        return properties.get(SkinProperty.ALL_AUTHOR_NAME);
    }

    public String getAuthorUUID() {
        return properties.get(SkinProperty.ALL_AUTHOR_UUID);
    }

    public String getFlavourText() {
        return properties.get(SkinProperty.ALL_FLAVOUR_TEXT);
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return ObjectUtils.makeDescription(this, "type", type.getRegistryName().toString(), "properties", properties, "settings", settings, "animations", animations, "paintData", paintData, "previewData", previewData);
    }

    public List<SkinMarker> getMarkers() {
        var markers = new ArrayList<SkinMarker>();
        for (var part : parts) {
            markers.addAll(part.getMarkers());
        }
        return markers;
    }

    public Object getBlobs() {
        return blobs;
    }

    public static class Builder {

        private final ISkinType type;

        private ArrayList<SkinPart> skinParts = new ArrayList<>();
        private ArrayList<SkinAnimation> skinAnimations = new ArrayList<>();

        private SkinPaintData paintData;
        private SkinPreviewData previewData;
        private SkinSettings settings = new SkinSettings();
        private SkinProperties properties = SkinProperties.EMPTY;
        private Object blobs;

        private int id = -1;
        private int version = SkinSerializer.Versions.V13;

        public Builder(ISkinType type) {
            this.type = type;
            // for outfit skin, not allow edit by default.
            if (type == SkinTypes.OUTFIT) {
                this.settings.setEditable(false);
            }
        }

        public static int generateId() {
            return ThreadUtils.nextId(Skin.class);
        }

        public Builder properties(SkinProperties properties) {
            if (properties != null) {
                this.properties = properties;
            }
            return this;
        }

        public Builder settings(SkinSettings settings) {
            if (settings != null) {
                this.settings = settings;
            }
            return this;
        }

        public Builder paintData(SkinPaintData paintData) {
            this.paintData = paintData;
            return this;
        }

        public Builder previewData(SkinPreviewData previewData) {
            this.previewData = previewData;
            return this;
        }

        public Builder parts(List<SkinPart> parts) {
            if (parts != null) {
                this.skinParts = new ArrayList<>(parts);
            }
            return this;
        }

        public Builder animations(List<SkinAnimation> animations) {
            if (animations != null) {
                this.skinAnimations = new ArrayList<>(animations);
            }
            return this;
        }

        public Builder blobs(Object blobs) {
            this.blobs = blobs;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Skin build() {
            updateIdIfNeeded();
            updateSettingIfNeeded();
            updatePropertiesIfNeeded();
            return new Skin(id, version, type, properties, settings, paintData, previewData, skinAnimations, skinParts, blobs);
        }

        private void updateIdIfNeeded() {
            if (id == -1) {
                id = generateId();
            }
        }

        private void updateSettingIfNeeded() {
            // when skin only provided preview data and not any part data,
            // this indicates the skin is in preview mod
            settings.setPreviewMode(previewData != null && skinParts.isEmpty());
        }

        private void updatePropertiesIfNeeded() {
            // Update skin properties.
            if (properties.get(SkinProperty.OVERRIDE_MODEL_ALL)) {
                if (type == SkinTypes.ARMOR_HEAD) {
                    properties.put(SkinProperty.OVERRIDE_MODEL_HEAD, true);
                }
                if (type == SkinTypes.ARMOR_CHEST) {
                    properties.put(SkinProperty.OVERRIDE_MODEL_CHEST, true);
                    properties.put(SkinProperty.OVERRIDE_MODEL_LEFT_ARM, true);
                    properties.put(SkinProperty.OVERRIDE_MODEL_RIGHT_ARM, true);
                }
                if (type == SkinTypes.ARMOR_LEGS) {
                    properties.put(SkinProperty.OVERRIDE_MODEL_LEFT_LEG, true);
                    properties.put(SkinProperty.OVERRIDE_MODEL_RIGHT_LEG, true);
                }
                if (type == SkinTypes.ARMOR_FEET) {
                    properties.put(SkinProperty.OVERRIDE_MODEL_LEFT_LEG, true);
                    properties.put(SkinProperty.OVERRIDE_MODEL_RIGHT_LEG, true);
                }
                properties.remove(SkinProperty.OVERRIDE_MODEL_ALL);
            }
            if (properties.get(SkinProperty.OVERRIDE_OVERLAY_ALL)) {
                if (type == SkinTypes.ARMOR_HEAD) {
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_HAT, true);
                }
                if (type == SkinTypes.ARMOR_CHEST) {
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_JACKET, true);
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_LEFT_SLEEVE, true);
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_RIGHT_SLEEVE, true);
                }
                if (type == SkinTypes.ARMOR_LEGS) {
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_LEFT_PANTS, true);
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_RIGHT_PANTS, true);
                }
                if (type == SkinTypes.ARMOR_FEET) {
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_LEFT_PANTS, true);
                    properties.put(SkinProperty.OVERRIDE_OVERLAY_RIGHT_PANTS, true);
                }
                properties.remove(SkinProperty.OVERRIDE_OVERLAY_ALL);
            }
            if (!properties.get(SkinProperty.OVERRIDE_OVERLAY_COLOR)) {
                properties.put(SkinProperty.KEEP_OVERLAY_COLOR, true);
                properties.remove(SkinProperty.OVERRIDE_OVERLAY_COLOR);
            }
            // bind properties to part.
            for (var part : skinParts) {
                part.setProperties(properties);
            }
            var skinIndexes = properties.get(SkinProperty.OUTFIT_PART_INDEXS);
            if (skinIndexes != null && !skinIndexes.isEmpty()) {
                var split = skinIndexes.split(":");
                var partIndex = 0;
                for (var skinIndex = 0; skinIndex < split.length; ++skinIndex) {
                    var stub = properties.slice(skinIndex);
                    var count = Integer.parseInt(split[skinIndex]);
                    while (partIndex < count) {
                        if (partIndex < skinParts.size()) {
                            var skinPart = skinParts.get(partIndex);
                            skinPart.setProperties(stub);
                        }
                        partIndex += 1;
                    }
                    partIndex = count;
                }
            }
        }
    }
}
