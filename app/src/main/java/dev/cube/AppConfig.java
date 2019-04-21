package dev.cube;

public class AppConfig {
	public static Matrix4f gMatProject = new Matrix4f();
	public static Matrix4f gMatView = new Matrix4f();
	public static Matrix4f gMatModel = new Matrix4f();

    // +��ǰϵͳ��ͶӰ�����������
	public static float[] gpMatrixProjectArray = new float[16];

    // ��ǰϵͳ����ͼ�����������
	public static float[] gpMatrixViewArray = new float[16];

    public static int[] gpViewport = new int[4];

	public static float gScreenX, gScreenY;
	public static boolean Turning = false;

    // �Ƿ���Ҫ����ʰȡ��⣨�������¼�����ʱ��
	public static boolean gbNeedPick = false;

    // �Ƿ��������α�ѡ��
	public static boolean gbTrianglePicked = false;

	public static void setTouchPosition(float x, float y) {
		gScreenX = x;
		gScreenY = y;
	}
}
