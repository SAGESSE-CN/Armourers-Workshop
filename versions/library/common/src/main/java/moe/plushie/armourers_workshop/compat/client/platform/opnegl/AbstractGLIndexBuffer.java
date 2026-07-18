package moe.plushie.armourers_workshop.compat.client.platform.opnegl;

import moe.plushie.armourers_workshop.core.math.OpenMath;
import moe.plushie.armourers_workshop.init.ModLog;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.util.function.IntConsumer;

public class AbstractGLIndexBuffer {

    private final int id;
    private final int vertexStride;
    private final int indexStride;
    private final Generator generator;

    private int size;
    private int capacity;
    private Type type = Type.BYTE;

    public AbstractGLIndexBuffer(int vertexStride, int indexStride, Generator generator) {
        this.vertexStride = vertexStride;
        this.indexStride = indexStride;
        this.generator = generator;
        this.id = GL20.glGenBuffers();
    }

    public void unbind() {
        GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void bind() {
        GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, id);
        uploadStorageIfNeeded(capacity);
    }

    public void ensureCapacity(int i) {
        if (capacity < i) {
            capacity = i;
        }
    }

    private void uploadStorageIfNeeded(int total) {
        if (total <= size) {
            return;
        }
        total = OpenMath.roundToward(total * 2, 256);
        ModLog.debug("Growing index buffer from {} to {}.", size, total);
        var indexType = Type.least(total);
        var bufferSize = OpenMath.roundToward(total * indexType.bytes, 4);
        GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferSize, GL20.GL_DYNAMIC_DRAW);
        var buffer = GL20.glMapBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, GL20.GL_WRITE_ONLY);
        if (buffer == null) {
            throw new RuntimeException("Failed to map GL buffer");
        }
        type = indexType;
        var count = total / indexStride;
        var builder = indexType.builder(buffer);
        for (var i = 0; i < count; ++i) {
            generator.accept(builder, i * vertexStride);
        }
        GL20.glUnmapBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER);
        size = total;
    }

    public int stride(int count) {
        return (count / vertexStride) * indexStride;
    }

    public int type() {
        return type.asGLType;
    }

    public enum Type {
        BYTE(GL20.GL_UNSIGNED_BYTE, 1), SHORT(GL20.GL_UNSIGNED_SHORT, 2), INT(GL20.GL_UNSIGNED_INT, 4);

        public final int asGLType;
        public final int bytes;

        Type(int type, int bytes) {
            this.asGLType = type;
            this.bytes = bytes;
        }

        public static Type least(int i) {
            if ((i & 0xFFFF0000) != 0) {
                return INT;
            }
            if ((i & 0xFF00) != 0) {
                return SHORT;
            }
            return BYTE;
        }

        public IntConsumer builder(ByteBuffer buffer) {
            return switch (this) {
                case BYTE -> i -> buffer.put((byte) i);
                case SHORT -> i -> buffer.putShort((short) i);
                default -> buffer::putInt;
            };
        }
    }

    public interface Generator {
        void accept(IntConsumer var1, int var2);
    }
}
