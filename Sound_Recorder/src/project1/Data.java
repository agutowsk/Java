package project1;

public class Data {
	private static byte[] dataBuffer;
	
	public static void setDataBuffer(byte[] data) {
		dataBuffer = data;
	}
	
	public static byte[] getDataBuffer() {
		return dataBuffer;
	}
}
