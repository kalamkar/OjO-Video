package com.ojogaze.video.oogles20;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by abhi on 5/26/17.
 */

public class GlUtils {
    private static final String TAG = "Utils";

    public static int generateExternalTexture() {
        int externalTextureId = -1;
        int[] textures = new int[1];
        try {
            GLES20.glGenTextures(1, textures, 0);
            externalTextureId = textures[0];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    externalTextureId);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
            if (externalTextureId != -1) {
                GLES20.glDeleteTextures(1, textures, 0);
            }
            return -1;
        }
        return externalTextureId;
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    public static void checkGlError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}
