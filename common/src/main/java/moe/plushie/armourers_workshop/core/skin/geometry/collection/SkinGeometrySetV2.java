package moe.plushie.armourers_workshop.core.skin.geometry.collection;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometry;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometrySet;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCube;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCubeFace;
import moe.plushie.armourers_workshop.core.skin.geometry.mesh.SkinMesh;
import moe.plushie.armourers_workshop.core.texture.TextureBox;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SkinGeometrySetV2 extends SkinGeometrySet<SkinGeometry> {

    private final ArrayList<SkinGeometry> entities = new ArrayList<>();

    public void addBox(Box box) {
        entities.add(box);
    }

    public void addMesh(Mesh mesh) {
        entities.add(mesh);
    }

    @Override
    public int size() {
        return entities.size();
    }

    @Override
    public SkinGeometry get(int index) {
        return entities.get(index);
    }

    @Override
    public Collection<ISkinGeometryType> getSupportedTypes() {
        return Collections.singleton(SkinGeometryTypes.CUBE);
    }

    public static class Box extends SkinCube {

        private final TextureBox skyBox;

        public Box(Rectangle3f boundingBox, SkinTransform transform, TextureBox skyBox) {
            this.transform = transform;
            this.boundingBox = boundingBox;
            this.skyBox = skyBox;
        }

        @Override
        public ISkinGeometryType getType() {
            return SkinGeometryTypes.CUBE;
        }

        @Override
        public IPaintColor getPaintColor(Direction dir) {
            return PaintColor.WHITE;
        }

        @Override
        public ITextureKey getTexture(Direction dir) {
            return skyBox.getTexture(dir);
        }

        @Override
        public SkinCubeFace getFace(Direction dir) {
            if (getTexture(dir) != null) {
                return super.getFace(dir);
            }
            return null;
        }
    }

    public static class Mesh extends SkinMesh {

        private final List<SkinGeometryVertex> vertices;

        public Mesh(SkinTransform transform, ITextureKey textureKey, List<SkinGeometryVertex> vertices) {
            this.transform = transform;
            this.textureKey = textureKey;
            this.vertices = vertices;
        }

        @Override
        public List<SkinGeometryVertex> getVertices() {
            return vertices;
        }
    }
}
