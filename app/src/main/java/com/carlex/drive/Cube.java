package com.carlex.drive;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final ShortBuffer indexBuffer;
    private final int mProgram;
    private final int COORDS_PER_VERTEX = 3;
    private final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
    private float[] rotationMatrix = new float[16];
    private float[] positionMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    static float cubeCoords[] = {
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
             1.0f, -1.0f, -1.0f,
             1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,
             1.0f, -1.0f,  1.0f,
             1.0f,  1.0f,  1.0f
    };

    static float colors[] = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    static short drawOrder[] = {
            0, 1, 2, 0, 2, 3,
            4, 5, 6, 4, 6, 7,
            0, 1, 5, 0, 5, 4,
            2, 3, 7, 2, 7, 6,
            0, 3, 7, 0, 7, 4,
            1, 2, 6, 1, 6, 5
    };

    public Cube() {
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        ByteBuffer ib = ByteBuffer.allocateDirect(drawOrder.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(positionMatrix, 0);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -5, 0, 0, 0, 0, 1, 0);
        Matrix.setIdentityM(projectionMatrix, 0);
    }

    public void draw() {
        GLES20.glUseProgram(mProgram);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int colorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, colorBuffer);

        int modelMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uModelMatrix");
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

        int viewMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uViewMatrix");
        GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0);

        int projectionMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uProjectionMatrix");
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }

    public void setRotation(float[] rotation) {
        Matrix.setRotateM(rotationMatrix, 0, rotation[0], 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(rotationMatrix, 0, rotation[1], 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(rotationMatrix, 0, rotation[2], 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(modelMatrix, 0, rotationMatrix, 0, modelMatrix, 0);
    }

    public void setPosition(float[] position) {
        Matrix.setIdentityM(positionMatrix, 0);
        Matrix.translateM(positionMatrix, 0, position[0], position[1], position[2]);
        Matrix.multiplyMM(modelMatrix, 0, positionMatrix, 0, modelMatrix, 0);
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        System.arraycopy(projectionMatrix, 0, this.projectionMatrix, 0, projectionMatrix.length);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private final String vertexShaderCode =
            "uniform mat4 uModelMatrix;" +
            "uniform mat4 uViewMatrix;" +
            "uniform mat4 uProjectionMatrix;" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 vColor;" +
            "varying vec4 outColor;" +
            "void main() {" +
            "  outColor = vColor;" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 outColor;" +
            "void main() {" +
            "  gl_FragColor = outColor;" +
            "}";
}
