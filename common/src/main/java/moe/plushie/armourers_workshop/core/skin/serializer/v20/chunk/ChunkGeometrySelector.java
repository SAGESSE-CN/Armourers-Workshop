package moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk;

import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;

import java.io.IOException;

public class ChunkGeometrySelector implements ChunkVariable {

    private final int index;
    private final int count;

    private final ChunkGeometrySection section;

    ChunkGeometrySelector(ChunkGeometrySection section, int fromIndex, int toIndex) {
        this.section = section;
        this.index = fromIndex;
        this.count = toIndex - fromIndex;
    }

    @Override
    public void writeToStream(IOutputStream stream) throws IOException {
        stream.writeInt(section.getIndex() + index);
        stream.writeInt(count);
    }

    @Override
    public boolean freeze() {
        return section.isResolved();
    }


    public int getIndex() {
        return index;
    }

    public int getCount() {
        return count;
    }

    public ChunkGeometrySection getSection() {
        return section;
    }
}
