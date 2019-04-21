package dev.cube;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.IOException;
import android.opengl.GLUtils;

public class RayPickRenderer implements Renderer { 
    private OnSurfacePickedListener onSurfacePickedListener; 
    private Context mContext; 
    private Cube cube;
    private int[] texture = new int[6]; 
    public float mfAngleX = 0.0f; 
    public float mfAngleY = 0.0f; 
    public float gesDistance = 0.0f;
    private Vector3f mvEye = new Vector3f(5.0f, 8.0f, -4.0f);
    private Vector3f mvCenter = new Vector3f(0, 0, 0);
    private Vector3f mvUp = new Vector3f(0, 1, 0);

    public RayPickRenderer(Context context) { 
        mContext = context; 
        cube = new Cube();
    } 

    public void setOnSurfacePickedListener(OnSurfacePickedListener onSurfacePickedListener) { 
        this.onSurfacePickedListener = onSurfacePickedListener; 
    } 
    
    private void loadTexture(GL10 gl) { 
        gl.glClearDepthf(1.0f); // ��������ӳ�� 
        IntBuffer intBuffer = IntBuffer.allocate(6); // 1 change to use an array  // ����2D��ͼ,���� 
        gl.glGenTextures(6, intBuffer); // GLuint, ��������, ��һ������ָʾ��Ҫ���ɼ�������
        for (int i = 0; i < 6; i++) {
            texture[i] = intBuffer.get(i); // 1 2 3 4 5 6
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[i]);
            gl.glEnable(GL10.GL_TEXTURE_2D); 
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.bitmap[i], 0); 
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); 
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            //GLImage.bitmap[i].recycle();
        }
    } 
  
    private void drawModel(GL10 gl) { // ��Ⱦģ��
        // ���·�ʽ��ȫ�������Ʒ�����ת 
        matRot.setIdentity(); 
        point = new Vector3f(mfAngleX, mfAngleY, 0); 
        try { 
            // ת����ģ���ڲ��ĵ㣬��Ҫ����             
            matInvertModel.set(AppConfig.gMatModel); 
            matInvertModel.invert();
            //matInvertModel.printMatrix44(); // happen to be I
            matInvertModel.transform(point, point); // point still changes sometimes
            float d = Vector3f.distance(new Vector3f(), point); // (0, 0, 0) ==> Point 
            // �ټ������
            if (Math.abs(d - gesDistance) <= 1E-4) {
                // �������λ������ת�����������ܻ�������Ŷ�ʹ��ģ����ʧ������
                matRot.glRotatef((float) (gesDistance * Math.PI / 180), point.x / d, point.y / d, point.z / d);
                //matRot.printMatrix44(); 
                // ��ת����ԭ��������ת
                if (0 != gesDistance) 
                    AppConfig.gMatModel.mul(matRot); 
            } 
        } catch (Exception e) { // �������������������ʧ�� 
        } 

        gesDistance = 0; 
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer()); // ��������ϵ����������ϵ�ı任����
        gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);    // show the textures

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
        gl.glEnable(GL10.GL_TEXTURE_2D);               // ��������ӳ�� 
        for (int i = 0; i < 6; i++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[i]); 
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cube.getCoordinate(Cube.VERTEX_BUFFER, i)); 
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, cube.getCoordinate(Cube.TEXTURE_BUFFER, i)); 
            gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, cube.getIndices());
        }

        gl.glDisable(GL10.GL_TEXTURE_2D); 
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 

        //drawCoordinateSystem(gl); 
    } 

    private void drawCoordinateSystem(GL10 gl) { 
        gl.glDisable(GL10.GL_DEPTH_TEST); // ��ʱ������Ȳ��� 
        gl.glLineWidth(2.0f);             // ���õ���ߵĿ�� 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); // �������ö������� 
        FloatBuffer fb = IBufferFactory.newFloatBuffer(3 * 2); 
        fb.put(new float[] { 0, 0, 0, 2.5f, 0, 0 }); 
        fb.position(0); 
        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb); 
        gl.glDrawArrays(GL10.GL_LINES, 0, 2); 
        fb.clear(); 
        fb.put(new float[] { 0, 0, 0, 0, 2.5f, 0 }); 
        fb.position(0); 
        gl.glColor4f(1.0f, 1.0f, 0.0f, 1.0f); // yellow
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb); 
        gl.glDrawArrays(GL10.GL_LINES, 0, 2); 
        fb.clear(); 
        fb.put(new float[] { 0, 0, 0, 0, 0, 2.5f }); 
        fb.position(0); 
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb); 
        gl.glDrawArrays(GL10.GL_LINES, 0, 2); 

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glLineWidth(1.0f); 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
    }

    @Override 
    public void onDrawFrame(GL10 gl) { // ��֡��Ⱦ 
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // �����Ļ����Ȼ���
        gl.glLoadIdentity(); // ���õ�ǰ��ģ�͹۲����
        setUpCamera(gl);     // ����������ģ����ͼ����

        gl.glPushMatrix();
        drawModel(gl);      // ��Ⱦ����
        gl.glPopMatrix();

        gl.glPushMatrix();
        drawPickedTriangle(gl); // ��Ⱦѡ�е�������
        gl.glPopMatrix(); 

        updatePick(); 
    } 
 
    private void setUpCamera(GL10 gl) { 
        gl.glMatrixMode(GL10.GL_MODELVIEW); // ����ģ����ͼ����
        gl.glLoadIdentity();
        //gl.glTranslatef(0, 0, -4.5f); // look weird, frame supposed to move down to feel better
        Matrix4f.gluLookAt(mvEye, mvCenter, mvUp, AppConfig.gMatView); // static [4][4]
        gl.glLoadMatrixf(AppConfig.gMatView.asFloatBuffer());          // FloatBuffer [16]
    } 
 
    private Matrix4f matRot = new Matrix4f(); 
    private Vector3f point; 
    private Vector3f transformedSphereCenter = new Vector3f(); 
    private Ray transformedRay = new Ray(); 
    private Matrix4f matInvertModel = new Matrix4f(); 
    private Vector3f[] mpTriangle = {
        new Vector3f(),
        new Vector3f(), 
        new Vector3f()
    }; 
    private FloatBuffer mBufPickedTriangle = IBufferFactory.newFloatBuffer(3 * 3); 
 
    /** 
     * ��Ⱦѡ�е������� 
     */ 
    private void drawPickedTriangle(GL10 gl) { 
        if (!AppConfig.gbTrianglePicked) return; 
        // ���ڷ��ص�ʰȡ�����������ǳ���ģ������ϵ�� 
        // �����Ҫ����ģ�ͱ任�������Ǳ任����������ϵ�н�����Ⱦ 
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer());  // ����ģ�ͱ任���� 
        gl.glColor4f(1.0f, 1.0f, 0.0f, 0.7f);  // set to be yellow
        
        // ����Blend���ģʽ 
        gl.glEnable(GL10.GL_BLEND); 
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); 

        // �����޹����ԣ�����ʹ�ô�ɫ��� 
        gl.glDisable(GL10.GL_DEPTH_TEST); 
        gl.glDisable(GL10.GL_TEXTURE_2D); 

        // ��ʼ����Ⱦ�������� 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufPickedTriangle); // ѡ����������������ϵ��������
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3); 

        // ����������� 
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
        gl.glDisable(GL10.GL_BLEND); 
    } 
 
    /** 
     * ������ͼ����ʱ���� 
     */ 
    @Override 
    public void onSurfaceCreated(GL10 gl, EGLConfig config) { 
        // ȫ�������� 
        gl.glEnable(GL10.GL_DITHER); 
 
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
        // ��������������ɫ 
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        //gl.glClearColor(0f, 0f, 0f, 1); 
        // ������ɫģ��Ϊƽ����ɫ 
        gl.glShadeModel(GL10.GL_SMOOTH); 
 
        // ���ñ������ 
        gl.glEnable(GL10.GL_CULL_FACE); 
        gl.glCullFace(GL10.GL_BACK); 
        // ������Ȳ��� 
        gl.glEnable(GL10.GL_DEPTH_TEST); 
        // ���ù��պͻ�� 
        gl.glDisable(GL10.GL_LIGHTING); 
        gl.glDisable(GL10.GL_BLEND); 
 
        loadTexture(gl); 
 
        AppConfig.gMatModel.setIdentity(); 
    } 
 
    // ����ʰȡ�¼�
    private void updatePick() {
        // ��ʰȡ������ģ�͵İ�Χ�岻�ཻ�������������һ����Ϊ��ʱ��������ģ�͵ľ�ȷ�������ཻ��� 
        if (!AppConfig.gbNeedPick) return; 
            
        AppConfig.gbNeedPick = false;
        // �������µ�ʰȡ���� 
        PickFactory.update(AppConfig.gScreenX, AppConfig.gScreenY); 
        // ������µ�ʰȡ���� 
        Ray ray = PickFactory.getPickRay();

        // ���Ȱ�ģ�͵İ���ͨ��ģ�;�����ģ�;ֲ��ռ�任������ռ� 
        AppConfig.gMatModel.transform(cube.getSphereCenter(), transformedSphereCenter); // (0, 0, 0)
        
        // ��������������ı��Ϊ�� 
        cube.surface = -1; 

        // ���ȼ��ʰȡ�����Ƿ���ģ�Ͱ������ཻ 
        // ������ܿ죬���Կ����ų�����Ҫ�ľ�ȷ�ཻ��� 
        if (ray.intersectSphere(transformedSphereCenter, cube.getSphereRadius())) {
            // ���������������ཻ����ô����Ҫ���о�ȷ�������漶����ཻ��� 
            // �������ǵ�ģ����Ⱦ���ݣ�������ģ�;ֲ�����ϵ�� 
            // ��ʰȡ����������������ϵ�� 
            // �����Ҫ������ת����ģ������ϵ�� 
            // �������ȼ���ģ�;���������             
            matInvertModel.set(AppConfig.gMatModel); 
            matInvertModel.invert();
            // �����߱任��ģ������ϵ�У��ѽ���洢��transformedRay�� 
            ray.transform(matInvertModel, transformedRay);

            // ��������ģ������ȷ�ཻ���             
            if (cube.intersect(transformedRay, mpTriangle)) {
                // ����ҵ����ཻ������������� 
                AppConfig.gbTrianglePicked = true;
                // ��������һ���� 
                Log.i("the surface touched ", "mark: " + cube.surface);

                if (onSurfacePickedListener != null) 
                    onSurfacePickedListener.onSurfacePicked(cube.surface); 
                // ������ݵ���ѡȡ�����ε���Ⱦ������ 
                mBufPickedTriangle.clear(); 
                for (int i = 0; i < 3; i++) { 
                    IBufferFactory.fillBuffer(mBufPickedTriangle, mpTriangle[i]); // ��ѡѡ����������������ϵ�������� 
                    //Log.i("point: " + i, mpTriangle[i].x + "\t" + mpTriangle[i].y + "\t" + mpTriangle[i].z); 
                }
                mBufPickedTriangle.position(0); 
            } 
        } else 
            AppConfig.gbTrianglePicked = false; 
    } 

    /** 
     * ����ͼ����ߴ緢���ı�ʱ���� 
     */ 
    @Override 
    public void onSurfaceChanged(GL10 gl, int width, int height) { 
        // �����ӿ� 
        gl.glViewport(0, 0, width, height); 
        AppConfig.gpViewport[0] = 0; 
        AppConfig.gpViewport[1] = 0; 
        AppConfig.gpViewport[2] = width; 
        AppConfig.gpViewport[3] = height; 
 
        // ����ͶӰ���� 
        float ratio = (float) width / height; // ��Ļ��߱� 0.62827224

        gl.glMatrixMode(GL10.GL_PROJECTION); 
        gl.glLoadIdentity(); 

        // GLU.gluPerspective(gl, 45.0f, ratio, 1, 5000);ϵͳ�ṩ 
        Matrix4f.gluPersective(45.0f, ratio, 0.1f, 100.0f, AppConfig.gMatProject);
        gl.glLoadMatrixf(AppConfig.gMatProject.asFloatBuffer()); 
        AppConfig.gMatProject.fillFloatArray(AppConfig.gpMatrixProjectArray); 
        // ÿ���޸���GL_PROJECTION����ý���ǰ����ģ�����û�GL_MODELVIEW 
        gl.glMatrixMode(GL10.GL_MODELVIEW); 
    }
} 
