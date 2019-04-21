package dev.cube;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.*;

public class Cube { 
    public static final int VERTEX_BUFFER = 0; 
    public static final int TEXTURE_BUFFER = 1; 
    public int surface = -1; // 0-5

    private float one = 2.0f; 
    private float two = 1.0f; 
    private float[] vertices = new float[] { -one, -one, one,   one, -one, one,   one, one, one,   -one, one, one, // ǰ 0123
                                             -one, -one, -one,  -one, one, -one,  one, one, -one,  one, -one, -one,// �� 4567
                                             -one, one, -one,   -one, one, one,   one, one, one,   one, one, -one, // �� 5326
                                             -one, -one, -one,  one, -one, -one,  one, -one, one,  -one, -one, one,// �� 4710
                                             one, -one, -one,   one, one, -one,   one, one, one,   one, -one, one, // �� 7621
                                             -one, -one, -one,  -one, -one, one,  -one, one, one,  -one, one, -one // �� 4035
    }; 
    // ��������������
    private float[] texCoords = new float[] { two, 0, 0, 0, 0, two, two, two, // 8 each surface
                                              0, 0, 0, two, two, two, two, 0,
                                              two, two, two, 0, 0, 0, 0, two,
                                              0, two, two, two, two, 0, 0, 0,
                                              0, 0, 0, two, two, two, two, 0,
                                              two, 0, 0, 0, 0, two, two, two
    };
    // ����������˳�� 
    private byte[] indices0 = new byte[] { 0, 1, 3, 2};
    private byte[] indices = new byte[] { 0, 1, 3, 2, 4, 5, 7, 6, 8, 9, 11, 10, 12, 13, 15, 14, 16, 17, 19, 18, 20, 21, 23, 22 };
        
    // modify this function so that I could separate data into 6 faces
    public FloatBuffer getCoordinate(int coord_id, int idx) { 
        switch (coord_id) { 
        case VERTEX_BUFFER: 
            return getDirectBuffer(vertices, idx); 
        case TEXTURE_BUFFER: 
            return getDirectBuffer(texCoords, idx); 
        default: 
            throw new IllegalArgumentException(); 
        } 
    } 
    public FloatBuffer getDirectBuffer(float[] buffer, int idx) {
        int n = buffer.length / 6;
        ByteBuffer bb = ByteBuffer.allocateDirect(n * 4); 
        bb.order(ByteOrder.nativeOrder()); 
        FloatBuffer directBuffer = bb.asFloatBuffer();
        float [] tmpBuffer = new float[n];
        System.arraycopy(buffer, idx * n, tmpBuffer, 0, n);
        directBuffer.put(tmpBuffer); 
        directBuffer.position(0); 
        return directBuffer; 
    } 
    public ByteBuffer getIndices() {
        return ByteBuffer.wrap(indices0);
    }
    public Vector3f getSphereCenter() { return new Vector3f(0, 0, 0);  } // ��������������Բ�����ĵ�
    public float getSphereRadius() { return 1.732051f;  }                // ��������������Բ�İ뾶����3�� 

    
    private static Vector4f location = new Vector4f(); 
    /** 
     * ������ģ�͵ľ�ȷ��ײ��� 
     * @param ray - ת����ģ�Ϳռ��е����� 
     * @param trianglePosOut - ���ص�ʰȡ��������ζ���λ�� 
     * @return ����ཻ������true 
     */     
    public boolean intersect(Ray ray, Vector3f[] trianglePosOut) { 
        boolean bFound = false; 
        float closeDis = 0.0f; // �洢������ԭ�����������ཻ��ľ���, �������������������������һ�� 
        Vector3f v0, v1, v2; 

        for (int i = 0; i < 6; i++) { 
            for (int j = 0; j < 2; j++) { 
                /*if (0 == j) { 
                    v0 = getVector3f(indices[j]);     // 0
                    v1 = getVector3f(indices[j + 1]); // 1
                    v2 = getVector3f(indices[j + 2]); // 2
                } else { // �ڶ���������ʱ������˳�򣬲�Ȼ����Ⱦ���������ڲ�
                    v0 = getVector3f(indices[j]);     // 1
                    v1 = getVector3f(indices[j + 2]); // 3
                    v2 = getVector3f(indices[j + 1]); // 2
                }*/ 
                if (0 == j) { 
                    v0 = getVector3f(indices[i * 4 + j]);     // 0
                    v1 = getVector3f(indices[i * 4 + j + 1]); // 1
                    v2 = getVector3f(indices[i * 4 + j + 2]); // 2
                } else { // �ڶ���������ʱ������˳�򣬲�Ȼ����Ⱦ���������ڲ�
                    v0 = getVector3f(indices[i * 4 + j]);     // 1
                    v1 = getVector3f(indices[i * 4 + j + 2]); // 3
                    v2 = getVector3f(indices[i * 4 + j + 1]); // 2
                }
                // �������ߺ������е���ײ��� 
                if (ray.intersectTriangle(v0, v1, v2, location)) {
                    if (!bFound) { // ����ǳ��μ�⵽����Ҫ�洢����ԭ���������ν���ľ���ֵ 
                        bFound = true; 
                        closeDis = location.w; 
                        trianglePosOut[0].set(v0); 
                        trianglePosOut[1].set(v1); 
                        trianglePosOut[2].set(v2); 
                        surface = i; 
                    } else { // ���֮ǰ�Ѿ���⵽�ཻ�¼�������Ҫ�����ཻ����֮ǰ���ཻ������Ƚ�, ���ձ���������ԭ������� 
                        if (closeDis > location.w) { 
                            closeDis = location.w; 
                            trianglePosOut[0].set(v0); 
                            trianglePosOut[1].set(v1); 
                            trianglePosOut[2].set(v2); 
                            surface = i; 
                        } 
                    } 
                } 
            } 
        } 
        return bFound; 
    } 
 
    private Vector3f getVector3f(int start) { 
        return new Vector3f(vertices[3 * start], vertices[3 * start + 1], vertices[3 * start + 2]); 
    } 
} 
