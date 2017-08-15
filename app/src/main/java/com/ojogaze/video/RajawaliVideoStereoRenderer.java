package com.ojogaze.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.view.MotionEvent;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.FieldOfView;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by abhi on 5/29/17.
 */

public class RajawaliVideoStereoRenderer extends Renderer implements GvrView.StereoRenderer{
    private final static String TAG = "RajawaliVideoStereoRenderer";

    /** position and rotation of eye camera in 3d space as matrix object */
    private Matrix4 eyeMatrix = new Matrix4();

    /** rotation of eye camera in 3d space */
    private Quaternion eyeOrientation = new Quaternion();

    private StreamingTexture videoTexture;

    RajawaliVideoStereoRenderer(Context context, MediaPlayer mediaPlayer) {
        super(context);

        // create texture from media player video
        videoTexture = new StreamingTexture("video", mediaPlayer);
    }

    @Override
    protected void initScene() {
        Sphere sphere = new Sphere(1, 24, 24);
        sphere.setPosition(0, 0, 0);

        // invert the sphere normals
        // factor "1" is two small and result in rendering glitches
        sphere.setScaleX(100);
        sphere.setScaleY(100);
        sphere.setScaleZ(-100);

        // set material with video texture
        Material material = new Material();
        material.setColorInfluence(0f);
        try {
            material.addTexture(videoTexture);
        } catch (ATexture.TextureException e){
            throw new RuntimeException(e);
        }
        sphere.setMaterial(material);

        // add sphere to scene
        getCurrentScene().addChild(sphere);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        super.onRenderSurfaceCreated(eglConfig, null, -1, -1);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onRenderSurfaceSizeChanged(null, width, height);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
//        float[] headView = new float[16];
//        headTransform.getHeadView(headView, 0);
//        eyeMatrix.setAll(headView);
    }

    @Override
    public void onDrawEye(Eye eye) {
        // Rajawali camera
        Camera currentCamera = getCurrentCamera();

        // cardboard field of view
        FieldOfView fov = eye.getFov();

        // update Rajawali camera from cardboard sdk
        currentCamera.updatePerspective(fov.getLeft(), fov.getRight(), fov.getBottom(), fov.getTop());
        eyeMatrix.setAll(eye.getEyeView());
        eyeOrientation.fromMatrix(eyeMatrix);
        currentCamera.setOrientation(eyeOrientation);
        Vector3 eyePosition = eyeMatrix.getTranslation().inverse();
        currentCamera.setPosition(eyePosition);

        super.onRenderFrame(null);
    }

    @Override
    public void onRendererShutdown() {
        super.onRenderSurfaceDestroyed(null);
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        super.onRender(elapsedRealTime, deltaTime);
        videoTexture.update();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }

    @Override
    public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {
    }
}
