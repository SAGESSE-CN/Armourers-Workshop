package moe.plushie.armourers_workshop.core.skin.geometry;

import moe.plushie.armourers_workshop.api.registry.IRegistryHolder;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.init.ModBlocks;
import moe.plushie.armourers_workshop.init.ModLog;
import moe.plushie.armourers_workshop.utils.ext.OpenResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class SkinGeometryTypes {

    private static final SkinGeometryType[] ALL_GEOMETRY_TYPE_MAPPING = new SkinGeometryType[256];
    private static final Map<String, SkinGeometryType> ALL_GEOMETRY_TYPES = new HashMap<>();

    public static final ISkinGeometryType BLOCK_SOLID = register("solid", 0, ModBlocks.SKIN_CUBE);
    public static final ISkinGeometryType BLOCK_GLOWING = register("glowing", 1, ModBlocks.SKIN_CUBE_GLOWING);
    public static final ISkinGeometryType BLOCK_GLASS = register("glass", 2, ModBlocks.SKIN_CUBE_GLASS);
    public static final ISkinGeometryType BLOCK_GLASS_GLOWING = register("glass_glowing", 3, ModBlocks.SKIN_CUBE_GLASS_GLOWING);

    public static final ISkinGeometryType CUBE = register("cube", 4, ModBlocks.BOUNDING_BOX);

    public static final ISkinGeometryType MESH = register("mesh", 5, ModBlocks.BOUNDING_BOX);

    public static ISkinGeometryType byName(String name) {
        var cube = ALL_GEOMETRY_TYPES.get(name);
        if (cube != null) {
            return cube;
        }
        return BLOCK_SOLID;
    }

    public static ISkinGeometryType byId(int index) {
        var cubeType = ALL_GEOMETRY_TYPE_MAPPING[index & 0xFF];
        if (cubeType != null) {
            return cubeType;
        }
        return BLOCK_SOLID;
    }

    public static ISkinGeometryType byBlock(Block block) {
        for (var cubeType : ALL_GEOMETRY_TYPES.values()) {
            if (cubeType.getBlock() == block) {
                return cubeType;
            }
        }
        return BLOCK_SOLID;
    }

    /**
     * Should this cube be rendered after the world?
     */
    public static boolean isGlass(ISkinGeometryType geometryType) {
        return geometryType == BLOCK_GLASS || geometryType == BLOCK_GLASS_GLOWING;
    }

    /**
     * Will this cube glow in the dark?
     */
    public static boolean isGlowing(ISkinGeometryType geometryType) {
        return geometryType == BLOCK_GLOWING || geometryType == BLOCK_GLASS_GLOWING;
    }

    private static SkinGeometryType register(String name, int id, IRegistryHolder<Block> block) {
        var geometryType = new SkinGeometryType(id, block);
        geometryType.setRegistryName(OpenResourceLocation.create("armourers", name));
        if (ALL_GEOMETRY_TYPES.containsKey(geometryType.getRegistryName().toString())) {
            ModLog.warn("A mod tried to register a geometry type with an id that is in use.");
            return geometryType;
        }
        ALL_GEOMETRY_TYPES.put(geometryType.getRegistryName().toString(), geometryType);
        ALL_GEOMETRY_TYPE_MAPPING[geometryType.getId() & 0xFF] = geometryType;
        ModLog.debug("Registering Skin Cube '{}'", geometryType.getRegistryName());
        return geometryType;
    }

    public static int getTotalCubes() {
        return ALL_GEOMETRY_TYPES.size();
    }
}
