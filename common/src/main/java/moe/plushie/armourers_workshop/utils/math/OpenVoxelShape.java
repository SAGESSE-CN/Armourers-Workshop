package moe.plushie.armourers_workshop.utils.math;

import com.google.common.collect.Lists;
import moe.plushie.armourers_workshop.api.math.IMatrix4f;
import moe.plushie.armourers_workshop.api.math.IRectangle3f;
import moe.plushie.armourers_workshop.api.math.IRectangle3i;
import moe.plushie.armourers_workshop.api.math.IVector3f;
import moe.plushie.armourers_workshop.api.math.IVoxelShape;
import moe.plushie.armourers_workshop.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

@SuppressWarnings("unused")
public class OpenVoxelShape implements IVoxelShape, Iterable<Vector4f> {

    private OpenBoundingBox aabb;
    private Rectangle3f box;
    private ArrayList<Vector4f> vertexes;

    public OpenVoxelShape() {
    }

    public static OpenVoxelShape empty() {
        return new OpenVoxelShape();
    }


    public static OpenVoxelShape box(IRectangle3f bounds) {
        Rectangle3f rect = ObjectUtils.safeCast(bounds, Rectangle3f.class);
        if (rect != null) {
            return box(rect);
        }
        return box(new Rectangle3f(bounds));
    }

    public static OpenVoxelShape box(IRectangle3i bounds) {
        return box(new Rectangle3f(bounds));
    }

    public static OpenVoxelShape box(Rectangle3f bounds) {
        var shape = new OpenVoxelShape();
        shape.box = bounds;
        return shape;
    }

    public OpenBoundingBox aabb() {
        if (aabb != null) {
            return aabb;
        }
        aabb = new OpenBoundingBox(bounds());
        return aabb;
    }

    public Rectangle3f bounds() {
        if (box != null) {
            return box;
        }
        if (vertexes == null || vertexes.isEmpty()) {
            return Rectangle3f.ZERO;
        }
        var iterator = vertexes.iterator();
        var fp = iterator.next();
        float minX = fp.x(), minY = fp.y(), minZ = fp.z();
        float maxX = fp.x(), maxY = fp.y(), maxZ = fp.z();
        while (iterator.hasNext()) {
            Vector4f point = iterator.next();
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            minZ = Math.min(minZ, point.z());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
            maxZ = Math.max(maxZ, point.z());
        }
        box = new Rectangle3f(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ);
        return box;
    }

    public void mul(IMatrix4f matrix) {
        for (var vector : getVertexes()) {
            vector.transform(matrix);
        }
        box = null;
        aabb = null;
    }

    public void add(float x, float y, float z, float width, float height, float depth) {
        var list = getVertexes();
        list.add(new Vector4f(x, y, z, 1.0f));
        list.add(new Vector4f(x + width, y, z, 1.0f));
        list.add(new Vector4f(x + width, y + height, z, 1.0f));
        list.add(new Vector4f(x, y + height, z, 1.0f));
        list.add(new Vector4f(x, y, z + depth, 1.0f));
        list.add(new Vector4f(x + width, y, z + depth, 1.0f));
        list.add(new Vector4f(x + width, y + height, z + depth, 1.0f));
        list.add(new Vector4f(x, y + height, z + depth, 1.0f));
        box = null;
        aabb = null;
    }

    public void add(OpenVoxelShape shape1) {
        var list = getVertexes();
        list.addAll(shape1.getVertexes());
        box = null;
    }

    public void add(IRectangle3f rect) {
        add(rect.getX(), rect.getY(), rect.getZ(), rect.getWidth(), rect.getHeight(), rect.getDepth());
    }

    public void add(IVector3f vertex) {
        add(new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0f));
    }

    public void add(Vector4f vertex) {
        var list = getVertexes();
        list.add(vertex);
        box = null;
    }

    public boolean isEmpty() {
        return vertexes == null && box == null;
    }

    public void optimize() {
        if (vertexes == null || vertexes.size() <= 8) {
            return;
        }
        var list = getVertexes();
        var uniquesVertexes = new LinkedHashSet<Vector4f>(list.size());
        uniquesVertexes.addAll(list);
        vertexes = Lists.newArrayList(uniquesVertexes);
    }

    public OpenVoxelShape copy() {
        var shape = new OpenVoxelShape();
        shape.box = box;
        shape.aabb = aabb;
        if (vertexes != null) {
            var newVertexes = new ArrayList<Vector4f>();
            newVertexes.ensureCapacity(vertexes.size());
            for (var vector : vertexes) {
                newVertexes.add(vector.copy());
            }
            shape.vertexes = newVertexes;
        }
        return shape;
    }

    @Override
    public Iterator<Vector4f> iterator() {
        if (vertexes != null) {
            return vertexes.iterator();
        }
        return getVertexes(box).iterator();
    }

    private List<Vector4f> getVertexes() {
        if (vertexes == null) {
            vertexes = getVertexes(box);
        }
        return vertexes;
    }

    private ArrayList<Vector4f> getVertexes(IRectangle3f box) {
        if (box == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(
                new Vector4f(box.getMinX(), box.getMinY(), box.getMinZ(), 1.0f),
                new Vector4f(box.getMaxX(), box.getMinY(), box.getMinZ(), 1.0f),
                new Vector4f(box.getMaxX(), box.getMaxY(), box.getMinZ(), 1.0f),
                new Vector4f(box.getMinX(), box.getMaxY(), box.getMinZ(), 1.0f),
                new Vector4f(box.getMinX(), box.getMinY(), box.getMaxZ(), 1.0f),
                new Vector4f(box.getMaxX(), box.getMinY(), box.getMaxZ(), 1.0f),
                new Vector4f(box.getMaxX(), box.getMaxY(), box.getMaxZ(), 1.0f),
                new Vector4f(box.getMinX(), box.getMaxY(), box.getMaxZ(), 1.0f)
        );
    }
}
