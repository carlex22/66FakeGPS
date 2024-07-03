package com.carlex.drive;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class logActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private GLSurfaceView glSurfaceView;
    private MyGLRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Setup OpenGL ES view
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new MyGLRenderer();
        glSurfaceView.setRenderer(renderer);
        setContentView(glSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            renderer.setAcceleration(event.values);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            renderer.setGyroscope(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private static class MyGLRenderer implements GLSurfaceView.Renderer {
        private Cube cube;
        private float[] accelData = new float[3];
        private float[] gyroData = new float[3];

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            cube = new Cube();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            cube.setRotation(gyroData);
            cube.setPosition(accelData);
            cube.draw();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            float[] projectionMatrix = new float[16];
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
            cube.setProjectionMatrix(projectionMatrix);
        }

        public void setAcceleration(float[] values) {
            System.arraycopy(values, 0, accelData, 0, values.length);
        }

        public void setGyroscope(float[] values) {
            System.arraycopy(values, 0, gyroData, 0, values.length);
        }
    }
}
