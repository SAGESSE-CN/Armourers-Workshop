package moe.plushie.armourers_workshop.compat.client.platform.opnegl;

import moe.plushie.armourers_workshop.api.annotation.Dist;
import moe.plushie.armourers_workshop.api.annotation.OnlyIn;
import moe.plushie.armourers_workshop.init.ModLog;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

@OnlyIn(Dist.CLIENT)
public class AbstractGLObjectState {

    public int vao = -1;
    public int vbo = -1;
    public int ibo = -1;
    public int fbo = -1;

    public int programId = -1;

    public void push() {
        programId = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (AbstractGLCapabilities.framebuffer_object) {
            //fbo = GL30.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        }
        if (AbstractGLCapabilities.vertex_buffer_object) {
            vbo = GL20.glGetInteger(GL20.GL_ARRAY_BUFFER_BINDING);
            ibo = GL20.glGetInteger(GL20.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        }
        if (AbstractGLCapabilities.vertex_array_object) {
            vao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        }
    }

    public void pop() {
        reset();
        if (AbstractGLCapabilities.vertex_buffer_object) {
            if (GL20.glIsBuffer(vbo)) {
                GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo);
            }
            if (GL20.glIsBuffer(ibo)) {
                GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            }
        }
        if (AbstractGLCapabilities.vertex_array_object) {
            if (GL30.glIsVertexArray(vao)) {
                GL30.glBindVertexArray(vao);
            }
        }
        if (AbstractGLCapabilities.framebuffer_object) {
            //if (GL30.glIsFramebuffer(fbo)) {
            //    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            //}
        }
        vao = -1;
        vbo = -1;
        ibo = -1;
        programId = -1;
    }

    public void reset() {
        if (AbstractGLCapabilities.vertex_array_object) {
            GL30.glBindVertexArray(0);
        }
        if (AbstractGLCapabilities.vertex_buffer_object) {
            GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
            GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    private void printError() {
        var error = GL30.glGetError();
        if (error != 0) {
            ModLog.info("gl error {}", error);
        }
    }
}
