package moe.plushie.armourers_workshop.core.skin.geometry;

import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.registry.IRegistryHolder;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import net.minecraft.world.level.block.Block;

public class SkinGeometryType implements ISkinGeometryType {

    protected final int id;
    protected final IRegistryHolder<Block> block;

    protected IResourceLocation registryName;

    public SkinGeometryType(int id, IRegistryHolder<Block> block) {
        this.id = id;
        this.block = block;
    }

    @Override
    public IResourceLocation getRegistryName() {
        return registryName;
    }

    public void setRegistryName(IResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Block getBlock() {
        return block.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkinGeometryType that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return registryName.toString();
    }
}
