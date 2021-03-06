package com.hackaton.bordarga.easymaps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener, SensorEventListener{
    private Camera mCamera;
    private CameraPreview mPreview;
    private GLSurfaceView glView;

    private static final String LOGTAG = "android-localizacion";

    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_CONFIG_UBICACION = 201;

    private GoogleApiClient apiClient;

    private TextView logText;
    private ToggleButton btnActualizar;

    private LocationRequest locRequest;

    private Location userLocation;
    private Location lastLocation;


    double searchLat  = 40.514109;
    double searchLon = -3.664977;

    SensorManager mSensorManager;
    private Sensor mSensorGyr;
    private boolean firstCamera = true;



    private Renderer2 renderer;


    private float[] rMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logText = (TextView)findViewById(R.id.log_text);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Now let's create an OpenGL surface.
        glView = findViewById(R.id.gl_surface);
        // To see the camera preview, the OpenGL surface has to be created translucently.
        // See link above.
        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glView.getHolder().setFormat(PixelFormat.RGBA_8888);
        // The renderer will be implemented in a separate class, GLView, which I'll show next.
        glView.setZOrderOnTop(true);
        //.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        renderer = new Renderer2();
        renderer.addArrow(0.0f, -1.0f);
        glView.setRenderer(renderer);

/*
        if (checkCameraHardware(getApplicationContext())) {
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

        }*/

        //Construcción cliente API Google
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        enableLocationUpdates();



        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_NORMAL);


    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            String error = e.getMessage();
        }

        return c;
    }

    private void enableLocationUpdates() {

        locRequest = new LocationRequest();
        locRequest.setInterval(0);
        locRequest.setFastestInterval(0);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        apiClient, locSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        Log.i(LOGTAG, "Configuración correcta");
                        startLocationUpdates();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.i(LOGTAG, "Se requiere actuación del usuario");
                            status.startResolutionForResult(MainActivity.this, PETICION_CONFIG_UBICACION);
                        } catch (IntentSender.SendIntentException e) {
                            btnActualizar.setChecked(false);
                            Log.i(LOGTAG, "Error al intentar solucionar configuración de ubicación");
                        }

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(LOGTAG, "No se puede cumplir la configuración de ubicación necesaria");
                        btnActualizar.setChecked(false);
                        break;
                }
            }
        });
    }

    private void disableLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                apiClient, this);

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Ojo: estamos suponiendo que ya tenemos concedido el permiso.
            //Sería recomendable implementar la posible petición en caso de no tenerlo.

            Log.i(LOGTAG, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    apiClient, locRequest, MainActivity.this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Se ha producido un error que no se puede resolver automáticamente
        //y la conexión con los Google Play Services no se ha establecido.

        Log.e(LOGTAG, "Error grave al conectar con Google Play Services");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Conectado correctamente a Google Play Services

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PETICION_PERMISO_LOCALIZACION);
        } else {

            Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(apiClient);

            //updateUI(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Se ha interrumpido la conexión con Google Play Services

        Log.e(LOGTAG, "Se ha interrumpido la conexión con Google Play Services");
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Permiso concedido

                @SuppressWarnings("MissingPermission")
                Location lastLocation =
                        LocationServices.FusedLocationApi.getLastLocation(apiClient);

                //updateUI(lastLocation);

            } else {
                //Permiso denegado:
                //Deberíamos deshabilitar toda la funcionalidad relativa a la localización.

                Log.e(LOGTAG, "Permiso denegado");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PETICION_CONFIG_UBICACION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(LOGTAG, "El usuario no ha realizado los cambios de configuración necesarios");
                        btnActualizar.setChecked(false);
                        break;
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            lastLocation = userLocation;
            userLocation = location;

            //logText.setText("Latitud: " + userLocation.getLatitude() + "  Longitud:" + userLocation.getLongitude());
        }

        //Mostramos la nueva ubicación recibida
        if(userLocation != null && lastLocation != null)
        updatePath(userLocation);
    }

    private void updatePath(Location location){
        double result =  getDistance(userLocation.getLatitude(), userLocation.getLongitude(), searchLat, searchLon);

        double deltaLat = userLocation.getLatitude() - lastLocation.getLatitude();
        double deltaLon = userLocation.getLongitude() - lastLocation.getLongitude();

        deltaLat = Math.pow(10, 5);
        deltaLon *= Math.pow(10, 5);


        //renderer.applyLatitudeDisplacement(deltaLat);
        //renderer.applyLongitudeDisplacemente(deltaLon);
    }

    private double getDistance(double la1, double lo1, double la2, double lo2){
        double p = Math.PI / 180;
        double a = 0.5 - Math.cos((la2 - la1) * p) / 2 + Math.cos(la1 * p) * Math.cos(la2 * p) *
                (1 - Math.cos((lo2 - lo1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(a)) * 1000;
    }

    // Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "\n\nSensor: ";

        // Cada sensor puede lanzar un thread que pase por aqui
        // Para asegurarnos ante los accesos simult‡neos sincronizamos esto

        synchronized (this) {

            switch (event.sensor.getType()) {

                case Sensor.TYPE_ACCELEROMETER:

                    txt += "acelerometro\n";
                    txt += "\n x: " + event.values[0] + " m/s";
                    txt += "\n y: " + event.values[1] + " m/s";
                    txt += "\n z: " + event.values[2] + " m/s";
                    //acelerometro.setText(txt);



            break;

            case Sensor.TYPE_ROTATION_VECTOR:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                if (firstCamera)
                {
                    renderer.setLastX(x);
                    renderer.setLastY(y);
                    firstCamera = false;
                }


                float xoffset = x - renderer.getLastX();
                float yoffset = renderer.getLastY() - y; // reversed since y-coordinates go from bottom to top
                renderer.setLastX(x);
                renderer.setLastY(y);

                float sensitivity = 0.1f; // change this value to your liking
                xoffset *= sensitivity;
                yoffset *= sensitivity;

                renderer.setYaw(renderer.getYaw() + xoffset);
                renderer.setPitch(renderer.getPitch() + yoffset);

                // make sure that when pitch is out of bounds, screen doesn't get flipped
                if (renderer.getPitch() > 89.0f)
                    renderer.setPitch(89.0f);
                if (renderer.getPitch() < -89.0f)
                    renderer.setPitch(-89.0f);

                float[] front = new float[3];
                front[0] = (float) (Math.cos(Math.toRadians(renderer.getYaw())) * Math.cos(Math.toRadians(renderer.getPitch())));
                front[1] = (float) Math.sin(Math.toRadians(renderer.getPitch()));
                front[2] = (float) (Math.sin(Math.toRadians(renderer.getYaw())) * Math.cos(Math.toRadians(renderer.getPitch())));

                float[] cameraFront = new float[3];

                float length = (float) Math.sqrt(front[0] * front[0] + front[1] * front[1] + front[2] * front[2]);
                if(length > 0){
                    cameraFront[0] = front[0] / length;
                    cameraFront[1] = front[1] / length;
                    cameraFront[2] = front[2] / length;
                }

                renderer.setCameraFront(cameraFront[0], cameraFront[1], cameraFront[2]);

                break;

            case Sensor.TYPE_GRAVITY:
                txt += "Gravedad\n";
                txt += "\n x: " + event.values[0];
                txt += "\n y: " + event.values[1];
                txt += "\n z: " + event.values[2];


                break;

        }
    }
}
/*
    private void detectRotation(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            // Change background color if rate of rotation around any
            // axis and in any direction exceeds threshold;
            // otherwise, reset the color
            if (Math.abs(event.values[0]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[1]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[2]) > ROTATION_THRESHOLD) {
                soundGyro.start();
            }
        }
    } */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
