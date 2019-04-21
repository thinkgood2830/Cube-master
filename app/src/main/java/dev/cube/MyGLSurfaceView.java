package dev.cube;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import android.graphics.PixelFormat;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

public class MyGLSurfaceView extends GLSurfaceView { 
    private RayPickRenderer mRenderer; 
    private float mPreviousX, mPreviousY; // ��¼�ϴδ���λ�õ����� 
 
    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new RayPickRenderer(context); 
        setZOrderOnTop(true); // ͸����һ��View
        setEGLConfigChooser(8, 8, 8, 8, 16, 0); 
        getHolder().setFormat(PixelFormat.TRANSLUCENT); // ͸����һ��Activity
        setRenderer(mRenderer); 
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //this.setRendererPickedListener(new OnSurfacePickedListener());
    }

    /*public MyGLSurfaceView(Context context, AttributeSet attrs, OnSurfacePickedListener onSurfacePickedListener) {
        super(context, attrs);
        mRenderer = new RayPickRenderer(context); 
        setZOrderOnTop(true); 
        setEGLConfigChooser(8, 8, 8, 8, 16, 0); 
        getHolder().setFormat(PixelFormat.TRANSLUCENT); 
        setRenderer(mRenderer); 
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mRenderer.setOnSurfacePickedListener(onSurfacePickedListener);
        }*/

    public void setRendererPickedListener(OnSurfacePickedListener onSurfacePickedListener) {
        this.mRenderer.setOnSurfacePickedListener(onSurfacePickedListener);
    }
    
    public void onPause() { super.onPause();  } 
    public void onResume() { super.onResume();  } 

    @Override 
    public boolean onTouchEvent(MotionEvent e) { 
        float x = e.getX(); 
        float y = e.getY(); 
        AppConfig.setTouchPosition(x, y); 
        switch (e.getAction()) { 
        case MotionEvent.ACTION_MOVE:
            // �������ĵ�����Ʒ�����ʱ����ת90�������� 
            float dx = y - mPreviousY; 
            float dy = x - mPreviousX; 
            float d = (float) (Math.sqrt(dx * dx + dy * dy)); // ���ƾ��� 
            mRenderer.mfAngleX = dx; // ��ת�ᵥλ������x,yֵ��z=0��
            mRenderer.mfAngleY = dy; 
            mRenderer.gesDistance = d; 
            AppConfig.gbNeedPick = false; 
            break; 
        case MotionEvent.ACTION_DOWN: 
            AppConfig.gbNeedPick = false; 
            break; 
        case MotionEvent.ACTION_UP: 
            AppConfig.gbNeedPick = true; 
            break; 
        case MotionEvent.ACTION_CANCEL: 
            AppConfig.gbNeedPick = false; 
            break; 
        } 
        mPreviousX = x; 
        mPreviousY = y; 
        return true; 
    } 
} 
