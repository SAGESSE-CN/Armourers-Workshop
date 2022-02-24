package moe.plushie.armourers_workshop.core.skin.part.chest;

import moe.plushie.armourers_workshop.core.api.common.skin.ISkinPartTypeTextured;
import moe.plushie.armourers_workshop.core.api.common.skin.ISkinProperties;
import moe.plushie.armourers_workshop.core.skin.data.property.SkinProperty;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartType;
import moe.plushie.armourers_workshop.core.utils.Rectangle3i;
import net.minecraft.util.math.vector.Vector3i;

import java.awt.*;

public class ChestPartType extends SkinPartType implements ISkinPartTypeTextured {

    public ChestPartType() {
        super();
        this.buildingSpace = new Rectangle3i(-6, -24, -30, 12, 38, 60);
        this.guideSpace = new Rectangle3i(-4, -12, -2, 8, 12, 4);
        this.offset = new Vector3i(0, -1, 0);
    }

    @Override
    public void renderBuildingGuide(float scale, ISkinProperties skinProps, boolean showHelper) {
//        GL11.glTranslated(0, this.buildingSpace.getY() * scale, 0);
//        GL11.glTranslated(0, -this.guideSpace.getY() * scale, 0);
//        ModelChest.MODEL.renderChest(scale);
//        GL11.glTranslated(0, this.guideSpace.getY() * scale, 0);
//        GL11.glTranslated(0, -this.buildingSpace.getY() * scale, 0);
    }

    @Override
    public Point getTextureSkinPos() {
        return new Point(16, 16);
    }

    @Override
    public boolean isTextureMirrored() {
        return false;
    }

    @Override
    public Point getTextureBasePos() {
        return new Point(16, 16);
    }

    @Override
    public Point getTextureOverlayPos() {
        return new Point(16, 32);
    }

    @Override
    public Vector3i getTextureModelSize() {
        return new Vector3i(8, 12, 4);
    }

    @Override
    public Vector3i getRenderOffset() {
        return new Vector3i(0, 0, 0);
    }

    @Override
    public Rectangle3i getItemRenderTextureBounds() {
        return new Rectangle3i(-4, 0, -2, 8, 12, 4);
    }

    @Override
    public boolean isModelOverridden(ISkinProperties skinProps) {
        return skinProps.get(SkinProperty.MODEL_OVERRIDE_CHEST);
    }

    @Override
    public boolean isOverlayOverridden(ISkinProperties skinProps) {
        return skinProps.get(SkinProperty.MODEL_HIDE_OVERLAY_CHEST);
    }

//    @Override
//    public Collection<ISkinProperty<?>> getProperties() {
//        return Arrays.asList(
//                SkinProperty.MODEL_OVERRIDE_CHEST,
//                SkinProperty.MODEL_HIDE_OVERLAY_CHEST
//        );
//    }
}