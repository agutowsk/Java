package speak_pkg;

public class configuration {
	private protocol socketType;
	private boolean detectSpeech;
	private int sampleInterval;
	private String ipAddress;
	private int loss;
	private String client;

	public configuration(protocol socket, boolean detectSpeech, int sample, String addr, int loss, String client) {
		this.setSocketType(socket);
		this.setDetectSpeech(detectSpeech);
		this.setSampleInterval(sample);
		this.setIpAddress(addr);
		this.setLoss(loss);
		this.setClient(client);
	}
	
	public protocol getSocketType() {
		return socketType;
	}

	public void setSocketType(protocol socketType) {
		this.socketType = socketType;
	}

	public boolean isDetectSpeech() {
		return detectSpeech;
	}

	public void setDetectSpeech(boolean detectSpeech) {
		this.detectSpeech = detectSpeech;
	}

	public int getSampleInterval() {
		return sampleInterval;
	}

	public void setSampleInterval(int sampleInterval) {
		this.sampleInterval = sampleInterval;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public int getLoss() {
		return loss;
	}

	public void setLoss(int loss) {
		this.loss = loss;
	}
	
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}
}
