package moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk;

import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometry;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometrySet;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.SliceRandomlyAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChunkGeometrySliceSet extends SkinGeometrySet<SkinGeometry> {

    private final int total;

    private final ThreadLocal<SliceRandomlyAccessor<SkinGeometry>> accessor;
    private final List<ChunkGeometrySelector> selectors;
    private final ChunkPaletteData palette;

    public ChunkGeometrySliceSet(int id, List<ChunkGeometrySelector> selectors, ChunkPaletteData palette) {
        this.id = id;
        this.palette = palette;
        this.selectors = selectors;
        this.accessor = ThreadLocal.withInitial(() -> build(selectors));
        this.total = ObjectUtils.sum(selectors, ChunkGeometrySelector::getCount);
    }

    @Override
    public int size() {
        return total;
    }

    @Override
    public SkinGeometry get(int index) {
        return accessor.get().get(index);
    }

    @Override
    public Collection<ISkinGeometryType> getSupportedTypes() {
        return ObjectUtils.map(selectors, it -> it.getSection().getGeometryType());
    }

    public ChunkPaletteData getPalette() {
        return palette;
    }

    public Collection<ChunkGeometrySelector> getSelectors() {
        return selectors;
    }

    private static SliceRandomlyAccessor<SkinGeometry> build(List<ChunkGeometrySelector> selectors) {
        var providers = new ArrayList<SliceRandomlyAccessor.Provider<? extends SkinGeometry>>();
        int startIndex = 0;
        int endIndex = 0;
        for (var selector : selectors) {
            endIndex += selector.getCount();
            providers.add(new ChunkGeometrySlice(startIndex, endIndex, selector, (ChunkGeometrySection.Immutable) selector.getSection()));
            startIndex = endIndex;
        }
        return new SliceRandomlyAccessor<>(providers);
    }
}
