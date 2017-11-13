package com.hackaton.bordarga.pruebasdemierda;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glView;
    private CameraView cameraView;

    private TextView text;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.activity_main);

        // When working with the camera, it's useful to stick to one orientation.
       // setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

        // Next, we disable the application's title bar...
        //requestWindowFeature( Window.FEATURE_NO_TITLE );
        // ...and the notification bar. That way, we can use the full screen.
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        // Now let's create an OpenGL surface.
        glView = findViewById(R.id.gl_surface);
        // To see the camera preview, the OpenGL surface has to be created translucently.
        // See link above.
        glView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
        glView.getHolder().setFormat( PixelFormat.RGBA_8888 );
        // The renderer will be implemented in a separate class, GLView, which I'll show next.
        glView.setZOrderOnTop(true);
        glView.setRenderer( new GLClearRenderer() );
        // Now set this as the main view.
        //setContentView( glView );


        // Now also create a view which contains the camera preview...
        /*if(checkCameraHardware(getApplicationContext())){
            Camera camera = getCameraInstance();
            cameraView = new CameraView( this , camera);
            // ...and add it, wrapping the full screen size.
            addContentView( cameraView, new WindowManager.LayoutParams( WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT ) );
        }
*/
        text = (TextView)findViewById(R.id.texto_mierda);
        //addContentView(text, new WindowManager.LayoutParams( WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT ) );
    }


    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
