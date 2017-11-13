package com.hackaton.bordarga.pruebasdemierda;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by botarga on 13/11/2017.
 */

class GLClearRenderer implements GLSurfaceView.Renderer {
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport( 0, 0, width, height );
    }

    @Override
    public void onDrawFrame(GL10 gl) {
// This method is called per frame, as the name suggests.
        // For demonstration purposes, I simply clear the screen with a random translucent gray.
        //float c = 1.0f / 256 * ( System.currentTimeMillis() % 256 );
        float c = 0.0f;
        gl.glClearColor( 0.0f, 0.0f, 1.0f, 0.5f );
        gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
    }
}
