package moe.plushie.armourers_workshop.api.skin.geometry;

import moe.plushie.armourers_workshop.api.math.IVoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public interface ISkinGeometrySet<T extends ISkinGeometry> extends Iterable<T> {

    /**
     * Gets the geometry total.
     */
    int size();

    /**
     * Gets the geometry at index.
     */
    T get(int index);

    /**
     * Gets the combined geometry shape.
     */
    IVoxelShape getShape();

    /**
     * Gets the contains geometry types.
     */
    @Nullable
    Collection<ISkinGeometryType> getSupportedTypes();


    @NotNull
    @Override
    default Iterator<T> iterator() {
        return new Iterator<T>() {
            int index = 0;
            final int count = size();

            @Override
            public boolean hasNext() {
                return index < count;
            }

            @Override
            public T next() {
                var value = get(index);
                index += 1;
                return value;
            }
        };
    }

    default boolean isEmpty() {
        return size() == 0;
    }
}
