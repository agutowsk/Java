package project1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

// must implement runnable so that we can execute Record() in a threaded
// manner
public class Record implements Runnable {
	public Thread thread;
	private byte[] data;
	private boolean record = true;

	private void setData(byte[] newData) {
		this.data = new byte[newData.length];
		this.data = newData;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public void start() {
		System.out.println("startRecording");
		thread = new Thread(this);
		record = true;
		thread.start();
	}

	public void stop() {
		System.out.println("stopRecording");
		record = false;
	}
	
	public void shutdown() {
		thread = null;
	}

	/*
	 * Run function, inherited from the Runnable class, gets called when
	 * we call the start function on this thread
	 * 
	 */
	@Override
	public void run() {
		System.out.println("run");
		
		// record sound
		byte[] audioInBytes = this.recordAudio();
		this.setData(audioInBytes);
	}

	/*
	 * Function in charge of opening the microphone port, recording data
	 * until told to stop from an external source, and then capturing
	 * all of it in a byte array
	 * 
	 * @return byte[] - all the data captured during the recording
	 */
	public byte[] recordAudio() {
		System.out.println("recordAudio");
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		AudioFormat audioFormat = new AudioFormat(8000, 	/* sampleRate as a float */
													8, 		/* sampleSizeInBits */
													1, 		/* channels */
													true,	/* signed */
													true); 	/* bigEndian */
		int frameSize = audioFormat.getFrameSize();
		DataLine.Info datalineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

		// make sure we can handle this info line before continuing
		if (!AudioSystem.isLineSupported(datalineInfo)) {
			System.out.println("datalineInfo is not supported" + datalineInfo);
			this.stop();
			return null;
		}

		TargetDataLine targetDataLine = null;
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(datalineInfo);
			int lineBufferSize = targetDataLine.getBufferSize();
			targetDataLine.open(audioFormat, lineBufferSize);
		} catch (LineUnavailableException e) {
			System.out.println("Failed to open targetDataLine");
			e.printStackTrace();
			this.stop();
		}

		// if we make it this far, the line is open and we have recorded data
		int bufferLength = (targetDataLine.getBufferSize() / 8) * frameSize;
		byte[] dataArray = new byte[bufferLength];

		targetDataLine.start();
		while (true == record) {
			// Keep reading till we reach the end of the targetDataLine
			int readIndex = targetDataLine.read(dataArray, 0, bufferLength);
			if (-1 == readIndex) {
				break;
			} else {
				outBuffer.write(dataArray, 0, readIndex);
				Data.setDataBuffer(outBuffer.toByteArray());
			}
		}

		// clean up the targetDataLine
		targetDataLine.stop();
		targetDataLine.close();

		// line is closed, now close the outputStream and process
		// process the it so then we can write it
		try {
			outBuffer.flush();
			outBuffer.close();
		} catch (IOException e) {
			System.out.println("Failed to close the outputStream");
			e.printStackTrace();
		}

		// return data
		System.out.println("completed recordAudio");
		return outBuffer.toByteArray();
	}
}
