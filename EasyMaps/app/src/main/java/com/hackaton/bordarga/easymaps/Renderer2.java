package com.hackaton.bordarga.easymaps;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by botarga on 13/11/2017.
 */

class Renderer2 implements GLSurfaceView.Renderer {

    ArrayList<Arrow> arrows = new ArrayList<>();
    float[] projectionMatrix = new float[16];

    float[] lightPos = new float[]{1.2f, 1.0f, 2.0f};

    float[] cameraPos   = new float[]{0.0f, 2.0f, 3.0f};
    float[] cameraFront = new float[]{0.0f, 0.0f, -1.0f};
    float[] cameraUp    = new float[]{0.0f, 1.0f, 0.0f};

    float yaw   = -90.0f;	// yaw is initialized to -90.0 degrees since a yaw of 0.0 results in a direction vector pointing to the right so we initially rotate a bit to the left.
    float pitch =  0.0f;
    float lastX;
    float lastY;
    float fov   =  45.0f;



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //arrows.add(new Arrow(0.0f, 3.0f));
    }


    public void addArrow(float x, float z){
        arrows.add(new Arrow(x, z));
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        lastX = width / 2.0f;
        lastY = height / 2.0f;

        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT |GLES20.GL_DEPTH_BUFFER_BIT);


        float[] viewMatrix = new float[16];

        float[] productMatrix = new float[16];



       Matrix.setLookAtM(viewMatrix, 0,
                cameraPos[0], cameraPos[1], cameraPos[2],
                cameraPos[0] + cameraFront[0], cameraPos[1] + cameraFront[1], cameraPos[2] + cameraFront[2],
                cameraUp[0], cameraUp[1], cameraUp[2]);




        for(Arrow a : arrows) {
            if(!a.initialized)
                a.init();

            Matrix.multiplyMM(productMatrix, 0,
                    projectionMatrix, 0,
                    viewMatrix, 0);

            float[] scratch = new float[16];
            Matrix.setIdentityM(scratch, 0);
            Matrix.translateM(scratch, 0, a.xCord, 0.0f, a.zCord);


            Matrix.multiplyMM(productMatrix, 0, productMatrix, 0, scratch, 0);

            int matrix = GLES20.glGetUniformLocation(a.program, "matrix");
            GLES20.glUniformMatrix4fv(matrix, 1, false, productMatrix, 0);

            a.draw();
        }


    }

    public void setLastX(float x){
        lastX =  x;
    }

    public void setLastY(float y){
        lastY = y;
    }

    public float getLastX(){
        return lastX;
    }

    public float getLastY(){
        return lastY;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setCameraFront(float x, float y, float z){
        cameraFront[0] = x;
        cameraFront[1] = y;
        cameraFront[2] = z;
    }

    public void applyLatitudeDisplacement(double delta){
        for(Arrow a : arrows){
            a.xCord += delta;
        }
    }

    public void applyLongitudeDisplacemente(double delta){
         for(Arrow a : arrows){
            a.zCord += delta;
        }
    }
}
