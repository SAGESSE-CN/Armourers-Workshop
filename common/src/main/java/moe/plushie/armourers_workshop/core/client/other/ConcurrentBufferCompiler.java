package moe.plushie.armourers_workshop.core.client.other;

import com.mojang.blaze3d.vertex.VertexFormat;
import moe.plushie.armourers_workshop.api.client.IRenderedBuffer;
import moe.plushie.armourers_workshop.api.skin.ISkinPartType;
import moe.plushie.armourers_workshop.compatibility.client.AbstractBufferBuilder;
import moe.plushie.armourers_workshop.compatibility.client.AbstractVertexArrayObject;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkinPart;
import moe.plushie.armourers_workshop.core.client.texture.TextureManager;
import moe.plushie.armourers_workshop.core.data.cache.AutoreleasePool;
import moe.plushie.armourers_workshop.core.data.cache.CacheQueue;
import moe.plushie.armourers_workshop.core.data.color.ColorScheme;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import moe.plushie.armourers_workshop.utils.ThreadUtils;
import moe.plushie.armourers_workshop.utils.math.OpenPoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class ConcurrentBufferCompiler {

    private static final ExecutorService QUEUE = ThreadUtils.newFixedThreadPool(1, "AW-SKIN-VB");
    private static final CacheQueue<Object, Group> CACHING = new CacheQueue<>(30000, Group::release);
    private static final VertexIndexObject INDEXER = new VertexIndexObject(4, 6, (builder, index) -> {
        builder.accept(index);
        builder.accept(index + 1);
        builder.accept(index + 2);
        builder.accept(index + 2);
        builder.accept(index + 3);
        builder.accept(index);
    });

    private ArrayList<Group> pendingTasks;

    public static void clearAllCache() {
        CACHING.clearAll();
    }

    @Nullable
    public Group compile(BakedSkinPart part, BakedSkin skin, ColorScheme scheme) {
        var key = Key.of(skin.getId(), part.getId(), part.requirements(scheme));
        var group = CACHING.get(key);
        if (group != null) {
            if (group.isCompiled()) {
                return group;
            }
            return null; // wait compile

        }
        group = new Group(part, skin, scheme);
        CACHING.put(key.copy(), group);
        startBatch(group);
        return null; // wait compile
    }

    private synchronized void startBatch(Group value) {
        var tasks = pendingTasks;
        if (tasks == null) {
            tasks = new ArrayList<>();
            pendingTasks = tasks;
            QUEUE.execute(this::compile);
        }
        tasks.add(value);
    }

    private synchronized ArrayList<Group> endBatch() {
        var tasks = pendingTasks;
        pendingTasks = null;
        return tasks;
    }

    private void compile() {
        var pendingTasks = endBatch();
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return;
        }
//        long startTime = System.currentTimeMillis();
        var poseStack1 = new OpenPoseStack();
        var buildingTasks = new ArrayList<Pass>();
        for (var task : pendingTasks) {
            var part = task.part;
            var scheme = task.scheme;
            var usingTypes = new HashSet<RenderType>();
            var mergedTasks = new ArrayList<Pass>();
            part.getQuads().forEach((renderType, quads) -> {
                var builder = new AbstractBufferBuilder(quads.size() * 8 * renderType.format().getVertexSize());
                builder.begin(renderType);
                quads.forEach((transform, faces) -> {
                    poseStack1.pushPose();
                    transform.apply(poseStack1);
                    faces.forEach(face -> face.render(part, scheme, 0xf000f0, OverlayTexture.NO_OVERLAY, poseStack1, builder));
                    poseStack1.popPose();
                });
                var renderedBuffer = builder.end();
                var compiledTask = new Pass(renderType, renderedBuffer, part.getRenderPolygonOffset(), part.getType());
                usingTypes.add(renderType);
                mergedTasks.add(compiledTask);
                buildingTasks.add(compiledTask);
            });
            task.mergedTasks = mergedTasks;
            task.usingTypes = usingTypes;
        }
        link(pendingTasks, buildingTasks);
//        long totalTime = System.currentTimeMillis() - startTime;
//        ModLog.debug("compile pendingTasks {}, times: {}ms", pendingTasks.size(), totalTime);
    }

    private void link(ArrayList<Group> cachedTasks, ArrayList<Pass> buildingTasks) {
        var totalRenderedBytes = 0;
        var byteBuffers = new ArrayList<ByteBuffer>();

        for (var compiledTask : buildingTasks) {
            var renderedBuffer = compiledTask.bufferBuilder;
            var format = renderedBuffer.format();
            var byteBuffer = renderedBuffer.vertexBuffer().duplicate();
            compiledTask.vertexCount = renderedBuffer.vertexCount();
            compiledTask.vertexOffset = totalRenderedBytes;
            compiledTask.bufferBuilder = null;
            compiledTask.format = format;
            byteBuffers.add(byteBuffer);
            totalRenderedBytes += byteBuffer.remaining();
            renderedBuffer.release();
            // make sure the index buffer is of sufficient size.
            INDEXER.ensureStorage(compiledTask.vertexCount * 2);
        }

        var mergedByteBuffer = ByteBuffer.allocateDirect(totalRenderedBytes);
        for (var byteBuffer : byteBuffers) {
            mergedByteBuffer.put(byteBuffer);
        }
        mergedByteBuffer.rewind();

        // upload only be called in the render thread !!!
        RenderSystem.recordRenderCall(() -> upload(mergedByteBuffer, cachedTasks));
    }

    private void upload(ByteBuffer byteBuffer, ArrayList<Group> cachedTasks) {
        var vertexBuffer = new VertexBufferObject();
        vertexBuffer.upload(byteBuffer);
        for (var cachedTask : cachedTasks) {
            cachedTask.upload(vertexBuffer);
        }
        vertexBuffer.release();
//        var renderState = new SkinRenderState();
//        renderState.save();
//        renderState.load();
    }

    public static class Group {

        private final BakedSkinPart part;
        private final BakedSkin skin;
        private final ColorScheme scheme;

        private HashSet<RenderType> usingTypes;
        private ArrayList<Pass> mergedTasks;

        private VertexBufferObject bufferObject;

        public Group(BakedSkinPart part, BakedSkin skin, ColorScheme scheme) {
            this.skin = skin;
            this.part = part;
            this.scheme = scheme;
        }

        public void upload(VertexBufferObject bufferObject) {
            if (mergedTasks == null) {
                return; // is released or not init.
            }
            this.bufferObject = bufferObject;
            this.bufferObject.retain();
            this.usingTypes.forEach(TextureManager.getInstance()::open);
            this.mergedTasks.forEach(it -> it.upload(bufferObject, INDEXER));
        }

        public void release() {
            if (bufferObject == null) {
                return;
            }
            this.mergedTasks.forEach(Pass::release);
            this.usingTypes.forEach(TextureManager.getInstance()::close);
            this.bufferObject.release();
            this.bufferObject = null;
            this.usingTypes = null;
            this.mergedTasks = null;
        }

        public List<Pass> getPasses() {
            return mergedTasks;
        }

        public boolean isEmpty() {
            return mergedTasks == null || mergedTasks.isEmpty();
        }

        public boolean isCompiled() {
            return bufferObject != null;
        }
    }

    public static class Pass {

        final boolean isGrowing;
        final float polygonOffset;
        final ISkinPartType partType;
        final RenderType renderType;

        int vertexCount;
        int vertexOffset;

        IRenderedBuffer bufferBuilder;
        VertexFormat format;

        VertexArrayObject arrayObject;
        VertexBufferObject bufferObject;
        VertexIndexObject indexObject;

        Pass(RenderType renderType, IRenderedBuffer bufferBuilder, float polygonOffset, ISkinPartType partType) {
            this.partType = partType;
            this.renderType = renderType;
            this.bufferBuilder = bufferBuilder;
            this.polygonOffset = polygonOffset;
            this.isGrowing = SkinRenderType.isGrowing(renderType);
        }

        public void upload(VertexBufferObject bufferObject, VertexIndexObject indexObject) {
            this.arrayObject = AbstractVertexArrayObject.create(format, vertexOffset, bufferObject, indexObject);
            this.bufferObject = bufferObject;
            this.indexObject = indexObject;
        }

        public void release() {
            this.arrayObject.close();
            this.bufferObject = null;
            this.indexObject = null;
        }
    }

    public static class Key {

        protected static final AutoreleasePool<Key> POOL = AutoreleasePool.create(Key::new);

        private int p1;
        private int p2;
        private Object p3;
        private int hash;

        private Key set(int hash, int p1, int p2, Object p3) {
            this.hash = hash;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            return this;
        }

        public static Key of(int p1, int p2, Object p3) {
            int hash = p1;
            hash = 31 * hash + p2;
            hash = 31 * hash + (p3 == null ? 0 : p3.hashCode());
            return POOL.get().set(hash, p1, p2, p3);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key that)) return false;
            return p1 == that.p1 && p2 == that.p2 && Objects.equals(p3, that.p3);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        public Key copy() {
            return new Key().set(hash, p1, p2, p3);
        }
    }
}
