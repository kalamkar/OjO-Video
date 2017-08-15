package com.ojogaze.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Surface;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView.StereoRenderer;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.ojogaze.video.oogles20.EGLRenderTarget;
import com.ojogaze.video.oogles20.GlUtils;
import com.ojogaze.video.oogles20.Shader;
import com.ojogaze.video.oogles20.Sphere;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by abhi on 5/26/17.
 */

public class GlesVideoStereoRenderer implements StereoRenderer, OnFrameAvailableListener {
    private static final int SPHERE_SLICES = 180;
    private static final int SPHERE_INDICES_PER_VERTEX = 1;
    private static final float SPHERE_RADIUS = 500.0f;

    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 1000f;
    private static final float INITIAL_PITCH_DEGREES = 90.f;

    private final Context context;

    private Shader shader;
    private int aPositionLocation;
    private int uMVPMatrixLocation;
    private int uTextureMatrixLocation;
    private int aTextureCoordLocation;

    private Sphere sphere;

    private float[] textureMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];

    private EGLRenderTarget eglRenderTarget;

    private int textureHandle;
    private SurfaceTexture texture;
    private Surface surface;

    private boolean frameAvailable = false;

    public GlesVideoStereoRenderer(Context context) {
        this.context = context;
    }

    private void initShader() {
        aPositionLocation = shader.getAttribute("aPosition");
        uMVPMatrixLocation = shader.getUniform("uMVPMatrix");
        uTextureMatrixLocation = shader.getUniform("uTextureMatrix");
        aTextureCoordLocation = shader.getAttribute("aTextureCoord");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    private void initSphere() {
        GLES20.glUseProgram(shader.getHandle());

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GlUtils.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(), sphere.getVertices());

        GlUtils.checkGlError("glVertexAttribPointer");

        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        GlUtils.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(),
                sphere.getVertices().duplicate().position(3));
        GlUtils.checkGlError("glVertexAttribPointer");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        eglRenderTarget = new EGLRenderTarget();

        textureHandle = GlUtils.generateExternalTexture();
        texture = new SurfaceTexture(textureHandle);
        texture.setOnFrameAvailableListener(this);
        ((MainActivity) context).initMediaPlayer(new Surface(texture));

        shader = new Shader(
                Utils.readRawTextFile(R.raw.video_vertex_shader, context),
                Utils.readRawTextFile(R.raw.video_fragment_shader, context));
        initShader();

        sphere = new Sphere(SPHERE_SLICES, 0.f, 0.f, 0.f, SPHERE_RADIUS, SPHERE_INDICES_PER_VERTEX);
        initSphere();

        eglRenderTarget.createRenderSurface(texture);

        Matrix.setIdentityM(viewMatrix, 0);
        // Apply initial rotation
        Matrix.setRotateM(modelMatrix, 0, INITIAL_PITCH_DEGREES, 1, 0, 0);

        GLES20.glClearColor(1.0f, 0.f, 0.f, 1.f);
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        float[] headView = new float[16];
        headTransform.getHeadView(headView, 0);

        Matrix.setLookAtM(
                viewMatrix, 0,
                headView[0], headView[1], headView[2],  // camera[0], camera[1], camera[2],
                0, 0, 0,
                0, 1, 0
        );
    }

    @Override
    public void onDrawEye(Eye eye) {
        float[] projectionMatrix = eye.getPerspective(Z_NEAR, Z_FAR);

        if (!frameAvailable) {
            return;
        }

        Matrix.translateM(textureMatrix, 0, 0, 1, 0);

        float[] pvMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(pvMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, pvMatrix, 0, modelMatrix , 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindTexture(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                textureHandle);

        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, textureMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        for (int j = 0; j < sphere.getNumIndices().length; ++j) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    sphere.getNumIndices()[j], GLES20.GL_UNSIGNED_SHORT,
                    sphere.getIndices()[j]);
        }
        frameAvailable = false;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onRendererShutdown() {
        shader.release();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        frameAvailable = true;
        if (!eglRenderTarget.hasValidContext()) {
            return;
        }

        eglRenderTarget.makeCurrent();
        // Have to be sure to balance onFrameAvailable and updateTexImage calls so that
        // the internal queue buffers will be freed. For this example, we can rely on the
        // display refresh rate being higher or equal to the video frame rate (sample is 30fps).
        texture.updateTexImage();
        texture.getTransformMatrix(textureMatrix);
    }
}
