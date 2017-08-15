package com.ojogaze.video.oogles20;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by abhi on 5/26/17.
 */

public class Shader {

    public static final String TAG = "Shader";

    private int shaderProgramHandle;

    public Shader(String vertexShader, String fragmentShader) {
        shaderProgramHandle = createProgram(vertexShader, fragmentShader);
    }

    public int getHandle() {
        return shaderProgramHandle;
    }

    public void release() {
        GLES20.glDeleteProgram(shaderProgramHandle);
        shaderProgramHandle = -1;
    }

    private static void checkLocation(int location, String name) {
        if (location >= 0) {
            return;
        }
        throw new RuntimeException("Could not find location for " + name);
    }

    public int getAttribute(String name) {
        int loc = GLES20.glGetAttribLocation(shaderProgramHandle, name);
        checkLocation(loc, name);
        return loc;
    }

    public int getUniform(String name) {
        int loc = GLES20.glGetUniformLocation(shaderProgramHandle, name);
        checkLocation(loc, name);
        return loc;
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GlUtils.checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        GlUtils.checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        GlUtils.checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GlUtils.checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }
}
