package com.hackaton.bordarga.easymaps;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by botarga on 13/11/2017.
 */

public class Arrow {
    private List<String> verticesList;
    private List<String> facesList;

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;

    public float xCord, zCord;
    public boolean initialized = false;


    private final String vertexShaderCode = "attribute vec4 position;\n" +
            "uniform mat4 matrix;\n" +
            " \n" +
            "void main() {\n" +
            "    gl_Position = matrix * position;\n" +
            "}";

    private final String fragmentShaderCode = "precision mediump float;\n" +
            " \n" +
            "void main() {\n" +
            "    gl_FragColor = vec4(1, 0.5, 0.5, 1.0);\n" +
            "}";


    public int program;

    public void init(){
        initialized = true;

        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
        buffer1.order(ByteOrder.nativeOrder());
        verticesBuffer = buffer1.asFloatBuffer();

        // Create buffer for faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2);
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();

        for(String vertex: verticesList) {
            String coords[] = vertex.split(" "); // Split by space
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);
        }
        verticesBuffer.position(0);

        for(String face: facesList) {
            String vertexIndices[] = face.split(" ");
            short vertex1 = Short.parseShort(vertexIndices[1]);
            short vertex2 = Short.parseShort(vertexIndices[2]);
            short vertex3 = Short.parseShort(vertexIndices[3]);
            facesBuffer.put((short)(vertex1 - 1));
            facesBuffer.put((short)(vertex2 - 1));
            facesBuffer.put((short)(vertex3 - 1));
        }
        facesBuffer.position(0);

        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
    }

    public Arrow(float anX, float aZ) {
        this.xCord = anX;
        this.zCord = aZ;
        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();

        // More code goes here



        verticesList.add("v 0.000000 0.200000 -1.000000"); // 1
        verticesList.add("v -1.000000 0.200000 0.000000"); // 2
        verticesList.add("v -0.800000 0.200000 0.000000"); // 3
        verticesList.add("v 0.000000 0.200000 -0.800000");  // 4
        verticesList.add("v 0.800000 0.200000 0.000000");  // 5
        verticesList.add("v 1.000000 0.200000 0.000000");  // 6

        verticesList.add("v 0.000000 -0.200000 -1.000000"); // 7
        verticesList.add("v -1.000000 -0.200000 0.000000"); // 8
        verticesList.add("v -0.800000 -0.200000 0.000000"); // 9
        verticesList.add("v 0.000000 -0.200000 -0.800000");  // 10
        verticesList.add("v 0.800000 -0.200000 0.000000");  // 11
        verticesList.add("v 1.000000 -0.200000 0.000000");  // 12




        facesList.add("f 1 2 3");
        facesList.add("f 3 1 4");

        facesList.add("f 5 6 1");
        facesList.add("f 1 4 5");

         facesList.add("f 7 8 9");
       facesList.add("f 9 7 10");

        facesList.add("f 11 12 7");
        facesList.add("f 7 10 11");


        facesList.add("f 1 2 8");
        facesList.add("f 7 8 1");


        facesList.add("f 8 9 3");
        facesList.add("f 8 2 3");


        facesList.add("f 10 9 3");
        facesList.add("f 4 10 3");


        facesList.add("f 10 11 4");
        facesList.add("f 11 4 5");

        facesList.add("f 11 12 5");
        facesList.add("f 5 6 12");

        facesList.add("f 1 12 6");
        facesList.add("f 1 12 7");

    }

    public void draw() {
        int position = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(position);

        GLES20.glVertexAttribPointer(position,
                3, GLES20.GL_FLOAT, false, 3 * 4, verticesBuffer);



        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);

        GLES20.glDisableVertexAttribArray(position);
    }

}
