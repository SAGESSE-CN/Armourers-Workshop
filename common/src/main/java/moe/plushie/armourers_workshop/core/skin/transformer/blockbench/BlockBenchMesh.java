package moe.plushie.armourers_workshop.core.skin.transformer.blockbench;

import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;

/// https://github.com/JannisX11/blockbench/blob/master/js/outliner/mesh.js
public class BlockBenchMesh extends BlockBenchElement {

    private final boolean boxUV;
    private final boolean mirrorUV;
    private final boolean allowMirrorModeling;

    private final Vector2f uvOffset;

    private final Vector3f origin;
    private final Vector3f rotation;

    private final Map<String, BlockBenchMeshFace> faces;
    private final Map<String, Vector3f> vertices;

    public BlockBenchMesh(String uuid, String name, String type, boolean allowExport, boolean allowMirrorModeling, boolean boxUV, boolean mirrorUV, Vector2f uvOffset, Vector3f origin, Vector3f rotation, Map<String, BlockBenchMeshFace> faces, Map<String, Vector3f> vertices) {
        super(uuid, name, type, allowExport);
        this.origin = origin;
        this.rotation = rotation;
        this.boxUV = boxUV;
        this.mirrorUV = mirrorUV;
        this.allowMirrorModeling = allowMirrorModeling;
        this.uvOffset = uvOffset;
        this.faces = faces;
        this.vertices = vertices;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public boolean isBoxUV() {
        return boxUV;
    }

    public boolean isMirrorUV() {
        return mirrorUV;
    }

    public Vector2f getUVOffset() {
        return uvOffset;
    }

    public Map<String, BlockBenchMeshFace> getFaces() {
        return faces;
    }

    public Map<String, Vector3f> getVertices() {
        return vertices;
    }

    public static class Builder extends BlockBenchElement.Builder {

        protected boolean boxUV = false;
        protected boolean mirrorUV = false;
        protected boolean allowMirrorModeling = false;

        protected Vector2f uvOffset = Vector2f.ZERO;

        protected Vector3f origin = Vector3f.ZERO;
        protected Vector3f rotation = Vector3f.ZERO;

        protected final Map<String, BlockBenchMeshFace> faces = new LinkedHashMap<>();
        protected final Map<String, Vector3f> vertices = new LinkedHashMap<>();

        public void boxUV(boolean boxUV) {
            this.boxUV = boxUV;
        }

        public void mirrorUV(boolean mirrorUV) {
            this.mirrorUV = mirrorUV;
        }

        public void allowMirrorModeling(boolean allowMirrorModeling) {
            this.allowMirrorModeling = allowMirrorModeling;
        }

        public void origin(Vector3f origin) {
            this.origin = origin;
        }

        public void rotation(Vector3f rotation) {
            this.rotation = rotation;
        }

        public void uvOffset(Vector2f uvOffset) {
            this.uvOffset = uvOffset;
        }

        public void addFace(String key, BlockBenchMeshFace face) {
            this.faces.put(key, face);
        }

        public void addVertex(String key, Vector3f vertex) {
            this.vertices.put(key, vertex);
        }

        @Override
        public BlockBenchMesh build() {
            return new BlockBenchMesh(uuid, name, type, allowExport, allowMirrorModeling, boxUV, mirrorUV, uvOffset, origin, rotation, faces, vertices);
        }
    }
}
