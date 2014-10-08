package speak_pkg;

public class Data {
	private static byte[] dataBuffer;
	
	public static void setDataBuffer(byte[] data) {
		dataBuffer = data;
	}
	
	public static byte[] getDataBuffer() {
		return dataBuffer;
	}
	
	public static void clearDataBuffer() {
		dataBuffer = new byte[0];
	}
}
