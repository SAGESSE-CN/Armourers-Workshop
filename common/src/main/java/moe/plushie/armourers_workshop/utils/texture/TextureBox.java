package moe.plushie.armourers_workshop.utils.texture;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.common.ITextureProvider;
import moe.plushie.armourers_workshop.utils.math.Rectangle2f;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class TextureBox {

    private final Vector2f texturePos;
    private final ITextureProvider defaultTexture;

    private final float width;
    private final float height;
    private final float depth;

    private final boolean mirror;

    private EnumMap<Direction, Rectangle2f> variantRects;
    private EnumMap<Direction, ITextureProvider> variantTextures;

    public TextureBox(float width, float height, float depth, boolean mirror, @Nullable Vector2f baseUV, @Nullable ITextureProvider defaultTexture) {
        this.texturePos = baseUV;
        this.defaultTexture = defaultTexture;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.mirror = mirror;
    }

    public void put(Direction dir, ITextureProvider textureProvider) {
        if (variantTextures == null) {
            variantTextures = new EnumMap<>(Direction.class);
        }
        variantTextures.put(dir, textureProvider);
    }

    public void put(Direction dir, Rectangle2f rect) {
        if (variantRects == null) {
            variantRects = new EnumMap<>(Direction.class);
        }
        variantRects.put(dir, rect);
    }

    public TextureBox separated() {
        TextureBox box = new TextureBox(width, height, depth, mirror, null, defaultTexture);
        for (Direction dir : Direction.values()) {
            ITextureKey key = getTexture(dir);
            if (key == null) {
                continue;
            }
            box.put(dir, new Rectangle2f(key.getU(), key.getV(), key.getWidth(), key.getHeight()));
            if (key.getProvider() == defaultTexture) {
                continue;
            }
            box.put(dir, key.getProvider());
        }
        return box;
    }

    @Nullable
    public ITextureKey getTexture(Direction dir) {
        Direction dir1 = resolveDirection(dir);
        switch (dir1) {
            case UP: {
                return makeTexture(dir1, depth, 0, width, depth);
            }
            case DOWN: {
                return makeTexture(dir1, depth + width, 0, width, depth);
            }
            case NORTH: {
                return makeTexture(dir1, depth, depth, width, height);
            }
            case SOUTH: {
                return makeTexture(dir1, depth + width + depth, depth, width, height);
            }
            case WEST: {
                return makeTexture(dir1, depth + width, depth, depth, height);
            }
            case EAST: {
                return makeTexture(dir1, 0, depth, depth, height);
            }
        }
        return null;
    }

    private Direction resolveDirection(Direction dir) {
        // when mirroring occurs, the contents of the WEST and EAST sides will be swapped.
        if (mirror && dir.getAxis() == Direction.Axis.X) {
            return dir.getOpposite();
        }
        return dir;
    }

    @Nullable
    private ITextureKey makeTexture(Direction dir, float u, float v, float s, float t) {
        ITextureProvider texture = getTextureProvider(dir);
        if (texture == null) {
            return null;
        }
        // specifies the uv origin for the face.
        Rectangle2f rect = getTextureRect(dir);
        if (rect != null) {
            return new TextureKey(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), texture);
        }
        Vector2f pos = texturePos;
        if (pos != null) {
            return new Entry(pos.getX() + u, pos.getY() + v, s, t, texture, pos);
        }
        return null;
    }

    @Nullable
    private Rectangle2f getTextureRect(Direction dir) {
        if (variantRects != null) {
            return variantRects.get(dir);
        }
        return null;
    }

    private ITextureProvider getTextureProvider(Direction dir) {
        if (variantTextures != null) {
            return variantTextures.getOrDefault(dir, defaultTexture);
        }
        return defaultTexture;
    }

    public static class Entry extends TextureKey {

        protected final Vector2f parent;

        public Entry(float u, float v, float width, float height, ITextureProvider provider, Vector2f parent) {
            super(u, v, width, height, provider);
            this.parent = parent;
        }

        public Vector2f getParent() {
            return parent;
        }
    }
}