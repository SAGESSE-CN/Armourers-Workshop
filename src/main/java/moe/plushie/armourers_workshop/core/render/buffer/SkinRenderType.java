package moe.plushie.armourers_workshop.core.render.buffer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import moe.plushie.armourers_workshop.core.api.ISkinCube;
import moe.plushie.armourers_workshop.core.utils.PaintingUtils;
import moe.plushie.armourers_workshop.core.utils.RenderUtils;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class SkinRenderType extends RenderType {

    private static final RenderState.TextureState COLORS = new TextureState(RenderUtils.TEX_CUBE, false, false);
    private static final RenderState.TexturingState COLORS_OFFSET = new TexturingState("offset_texturing", () -> {
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        float f = PaintingUtils.getPaintTextureOffset() / 256.0f;
        RenderSystem.translatef(0, f, 0.0F);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }, () -> {
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    });

    public static final SkinRenderType SOLID_FACE = new SkinRenderType("aw_quad_face", createSolidFace(false), true, false);
    public static final SkinRenderType LIGHTING_FACE = new SkinRenderType("aw_lighting_quad_face", createLightingPart(false), false, false);
    public static final SkinRenderType TRANSLUCENT_SOLID_FACE = new SkinRenderType("aw_translucent_quad_face", createSolidFace(true), true, true);
    public static final SkinRenderType TRANSLUCENT_LIGHTING_FACE = new SkinRenderType("aw_translucent_lighting_quad_face", createLightingPart(true), false, true);
    public static final SkinRenderType[] RENDER_ORDERING_FACES = {
            SOLID_FACE,
            LIGHTING_FACE,
            TRANSLUCENT_SOLID_FACE,
            TRANSLUCENT_LIGHTING_FACE
    };
    private final boolean enableLight;
    private final boolean enableTranslucent;

    public SkinRenderType(String name, RenderType delegate, boolean enableLight, boolean enableTranslucent) {
        super(name, delegate.format(), delegate.mode(), delegate.bufferSize(), delegate.affectsCrumbling(), false, delegate::setupRenderState, delegate::clearRenderState);
        this.enableLight = enableLight;
        this.enableTranslucent = enableTranslucent;
    }

    public static SkinRenderType by(ISkinCube cube) {
        if (cube.isGlass()) {
            if (cube.isGlowing()) {
                return TRANSLUCENT_LIGHTING_FACE;
            } else {
                return TRANSLUCENT_SOLID_FACE;
            }
        }
        if (cube.isGlowing()) {
            return LIGHTING_FACE;
        } else {
            return SOLID_FACE;
        }
    }

    public static RenderType createGUI(ResourceLocation r) {
        //            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        ;
        RenderType.State states = RenderType.State.builder()
                .setCullState(NO_CULL)
                .setTextureState(new TextureState(r, false, false))
//                .setTextureState(COLORS)
//                .setTexturingState(COLORS_OFFSET)
//                .setDiffuseLightingState(DIFFUSE_LIGHTING)
//                .setLightmapState(LIGHTMAP)
//                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                .setAlphaState(DEFAULT_ALPHA)
//                .setOutputState(alpha ? TRANSLUCENT_TARGET : MAIN_TARGET)
                .createCompositeState(false);
        return RenderType.create("aw_quad_face222", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 256, false, false, states);

    }


    private static RenderType createSolidFace(boolean alpha) {
        ImmutableList<VertexFormatElement> elements = ImmutableList.<VertexFormatElement>builder()
                .add(DefaultVertexFormats.ELEMENT_POSITION)
                .add(DefaultVertexFormats.ELEMENT_COLOR)
                .add(DefaultVertexFormats.ELEMENT_UV0)
                .add(DefaultVertexFormats.ELEMENT_NORMAL)
                .add(DefaultVertexFormats.ELEMENT_PADDING)
                .build();
        RenderType.State states = RenderType.State.builder()
                .setCullState(NO_CULL)
                .setTextureState(COLORS)
                .setTexturingState(COLORS_OFFSET)
                .setDiffuseLightingState(DIFFUSE_LIGHTING)
                .setLightmapState(LIGHTMAP)
                .setTransparencyState(alpha ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                .setOutputState(alpha ? TRANSLUCENT_TARGET : MAIN_TARGET)
                .createCompositeState(false);
        return RenderType.create("aw_quad_face", new VertexFormat(elements), GL11.GL_QUADS, 256, false, false, states);
    }

    private static RenderType createLightingPart(boolean hasAlpha) {
        ImmutableList<VertexFormatElement> elements = ImmutableList.<VertexFormatElement>builder()
                .add(DefaultVertexFormats.ELEMENT_POSITION)
                .add(DefaultVertexFormats.ELEMENT_COLOR)
                .add(DefaultVertexFormats.ELEMENT_UV0)
                .build();
        RenderType.State states = RenderType.State.builder()
                .setCullState(NO_CULL)
                .setTextureState(COLORS)
                .setTexturingState(COLORS_OFFSET)
                .setTransparencyState(hasAlpha ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                .setOutputState(hasAlpha ? TRANSLUCENT_TARGET : MAIN_TARGET)
                .createCompositeState(false);
        return RenderType.create("aw_light_quad_face", new VertexFormat(elements), GL11.GL_QUADS, 256, false, false, states);
    }

    public boolean usesLight() {
        return enableLight;
    }

    public boolean usesTranslucent() {
        return enableTranslucent;
    }
}