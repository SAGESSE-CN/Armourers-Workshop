package moe.plushie.armourers_workshop.compat.client.platform.opnegl;

import org.lwjgl.opengl.GL;

public class AbstractGLCapabilities {

    public static boolean framebuffer_object;
    public static boolean vertex_buffer_object;
    public static boolean vertex_array_object;
    public static boolean uniform_buffer_object;

    static {
        var caps = GL.createCapabilities();
        framebuffer_object = caps.OpenGL30 || caps.GL_ARB_framebuffer_object;
        vertex_array_object = caps.OpenGL30 || caps.GL_ARB_vertex_array_object;
        vertex_buffer_object = caps.OpenGL20 || caps.GL_ARB_vertex_buffer_object;
        uniform_buffer_object = caps.OpenGL30 || caps.GL_ARB_uniform_buffer_object;
    }
}
