//package moe.plushie.armourers_workshop.core.skin.type.feet;
//
//import moe.plushie.armourers_workshop.core.api.common.skin.ISkinPartType;
//import moe.plushie.armourers_workshop.core.api.common.skin.ISkinProperties;
//import moe.plushie.armourers_workshop.core.api.common.skin.ISkinProperty;
//import moe.plushie.armourers_workshop.core.skin.data.property.SkinProperties;
//import moe.plushie.armourers_workshop.core.skin.data.property.SkinProperty;
//import moe.plushie.armourers_workshop.core.skin.type.AbstractSkinType;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SkinFeet extends AbstractSkinType {
//
//    private ArrayList<ISkinPartType> skinParts;
//
//    public SkinFeet() {
//        skinParts = new ArrayList<>();
//        skinParts.add(new SkinFeetPartLeftFoot());
//        skinParts.add(new SkinFeetPartRightFoot());
//    }
//
//    @Override
//    public List<? extends ISkinPartType> getParts() {
//        return this.skinParts;
//    }
//
//
//    @Override
//    public int getVanillaArmourSlotId() {
//        return 3;
//    }
//
//    @Override
//    public ArrayList<ISkinProperty<?>> getProperties() {
//        ArrayList<ISkinProperty<?>> properties = super.getProperties();
//        properties.add(SkinProperty.MODEL_OVERRIDE_LEG_LEFT);
//        properties.add(SkinProperty.MODEL_OVERRIDE_LEG_RIGHT);
//        properties.add(SkinProperty.MODEL_HIDE_OVERLAY_LEG_LEFT);
//        properties.add(SkinProperty.MODEL_HIDE_OVERLAY_LEG_RIGHT);
//        return properties;
//    }
//
//    @Override
//    public boolean haveBoundsChanged(ISkinProperties skinPropsOld, ISkinProperties skinPropsNew) {
//        if (SkinProperty.MODEL_OVERRIDE_LEG_LEFT.getValue(skinPropsOld) != SkinProperty.MODEL_OVERRIDE_LEG_LEFT.getValue(skinPropsNew)) {
//            return true;
//        }
//        if (SkinProperty.MODEL_OVERRIDE_LEG_RIGHT.getValue(skinPropsOld) != SkinProperty.MODEL_OVERRIDE_LEG_RIGHT.getValue(skinPropsNew)) {
//            return true;
//        }
//        return false;
//    }
//}
