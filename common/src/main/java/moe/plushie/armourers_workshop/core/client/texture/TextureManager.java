package moe.plushie.armourers_workshop.core.client.texture;

import moe.plushie.armourers_workshop.api.common.ITextureProperties;
import moe.plushie.armourers_workshop.api.common.ITextureProvider;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderType;
import moe.plushie.armourers_workshop.core.data.cache.ReferenceCounted;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.texture.TextureAnimation;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.utils.DataContainer;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import moe.plushie.armourers_workshop.utils.SkinFileUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class TextureManager {

    private static final AtomicInteger ID = new AtomicInteger(0);
    private static final TextureManager INSTANCE = new TextureManager();

    private final IdentityHashMap<Object, Entry> textures = new IdentityHashMap<>();

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        ID.set(0);
    }

    public synchronized void stop() {
        textures.values().forEach(Entry::unbind);
        textures.clear();
    }

    public void open(RenderType renderType) {
        var entry = Entry.of(renderType);
        if (entry != null) {
            entry.retain();
        }
    }

    public void close(RenderType renderType) {
        var entry = Entry.of(renderType);
        if (entry != null) {
            entry.release();
        }
    }

    public synchronized RenderType register(ITextureProvider provider, ISkinGeometryType type) {
        var entry = textures.get(provider);
        if (entry == null) {
            entry = new Entry(provider);
            textures.put(provider, entry);
        }
        return entry.getRenderType(type);
    }

    public static class Entry extends ReferenceCounted {

        private final IResourceLocation location;
        private final ITextureProperties properties;

        private final TextureAnimationController animationController;

        private final Map<IResourceLocation, ByteBuffer> textureBuffers;

        private RenderType cubeRenderType;
        private RenderType meshRenderType;

        public Entry(ITextureProvider provider) {
            this.location = ModConstants.key("textures/dynamic/" + ID.getAndIncrement() + ".png");
            this.properties = provider.getProperties();
            this.textureBuffers = resolveTextureBuffers(location, provider);
            this.animationController = new TextureAnimationController((TextureAnimation) provider.getAnimation());
        }

        @Nullable
        public static TextureManager.Entry of(RenderType renderType) {
            return DataContainer.get(renderType, null);
        }

        @Override
        protected void init() {
            RenderSystem.recordRenderCall(() -> {
                textureBuffers.forEach(SmartResourceManager.getInstance()::register);
                Minecraft.getInstance().getTextureManager().register(location.toLocation(), new SimpleTexture(location.toLocation()));
            });
        }

        @Override
        protected void dispose() {
            RenderSystem.recordRenderCall(() -> {
                textureBuffers.keySet().forEach(SmartResourceManager.getInstance()::unregister);
                Minecraft.getInstance().getTextureManager().unregister(location.toLocation());
            });
        }

        public RenderType getRenderType(ISkinGeometryType type) {
            if (type == SkinGeometryTypes.MESH) {
                if (meshRenderType == null) {
                    meshRenderType = SkinRenderType.meshFace(location, properties.isEmissive());
                    DataContainer.set(meshRenderType, this);
                }
                return meshRenderType;
            } else {
                if (cubeRenderType == null) {
                    cubeRenderType = SkinRenderType.cubeFace(location, properties.isEmissive());
                    DataContainer.set(cubeRenderType, this);
                }
                return cubeRenderType;
            }
        }

        public IResourceLocation getLocation() {
            return location;
        }

        public TextureAnimationController getAnimationController() {
            return animationController;
        }

        @Override
        public String toString() {
            return location.toString();
        }

        protected void unbind() {
            if (cubeRenderType != null) {
                DataContainer.set(cubeRenderType, null);
            }
            if (meshRenderType != null) {
                DataContainer.set(meshRenderType, null);
            }
        }

        private Map<IResourceLocation, ByteBuffer> resolveTextureBuffers(IResourceLocation location, ITextureProvider provider) {
            var path = SkinFileUtils.removeExtension(location.getPath());
            var results = new LinkedHashMap<IResourceLocation, ByteBuffer>();
            results.put(location, provider.getBuffer());
            for (var variant : provider.getVariants()) {
                if (variant.getProperties().isNormal()) {
                    results.put(ModConstants.key(path + "_n.png"), variant.getBuffer());
                }
                if (variant.getProperties().isSpecular()) {
                    results.put(ModConstants.key(path + "_s.png"), variant.getBuffer());
                }
            }
            return results;
        }
    }
}
