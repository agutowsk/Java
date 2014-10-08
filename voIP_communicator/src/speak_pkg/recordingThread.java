package speak_pkg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class recordingThread implements Runnable {
	public Thread thread;
	private Socket socket = null;
	private configuration conf = null;
	private boolean record = true;
	private int chunkSize = 0;
	private DataOutputStream dOut = null;
	private DatagramSocket udpClientSocket = null;
	private Algorithm alg = null;
	
	/*
	 * Constructor used to pass input parameters from the main
	 * 	thread and uses them in this thread, used for TCP
	 * 
	 */
	public recordingThread(Socket sock, configuration config) {
		this.socket = sock;
		this.conf = config;
		
		// calculate the users transmission chunk size in frames
		this.chunkSize = (int) (config.getSampleInterval()*8);
		
		try {
			dOut = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("There was an error opening the socket");
			e.printStackTrace();
		} 
	}
	
	/*
	 * Constructor used to pass input parameters from the main
	 * 	thread and uses them in this thread, used for UDP
	 * 
	 */
	public recordingThread(configuration config) {
		this.conf = config;
		
		// calculate the users transmission chunk size in frames
		this.chunkSize = (int) (config.getSampleInterval()*8);
		
		try {
			this.udpClientSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("There was an error opening the DatagramSocket");
			e.printStackTrace();
		}
	}

	/*
	 * Function allocates an internal thread and kicks its off
	 * 
	 */
	public void start() {
		thread = new Thread(this);
		record = true;
		thread.start();
	}

	/*
	 * Function stops the current thread so that it can be shutdown
	 * 
	 */
	public void stop() {
		record = false;
	}

	/*
	 * Function nulls out the thread so that it is effectively shutdown
	 * 
	 */
	public void shutdown() {
		thread = null;
	}
	
	/*
	 * Function reads in an specified audio file and converts it into a byte array
	 * 	this was only used of testing, to decouple the network transmission and recording pieces
	 * 
	 */
	public byte[] readFile() {
		//Read audio file and create byte array
		String fileName = "/Users/alexgutowski/Desktop/CS529 Multimedia networking/Project 1 code/output/sound.raw";
		File incomingData = new File(fileName);
		if (!incomingData.exists()) { 
			System.out.println("Could not find file " + fileName);
			return null;
		}

		FileInputStream inputSteam = null;
		byte[]soundArray = null;
		try {
			// create an input stream out of the file path
			inputSteam = new FileInputStream(incomingData);
			
			//initialize the sound buffer to be the size of the available data in the input stream
			soundArray = new byte[inputSteam.available()];
			
			//copy the data to the input stream
			inputSteam.read(soundArray);
		} catch (FileNotFoundException e) {
			System.out.println("Unable to find file to read");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error reading the input file");
			e.printStackTrace();
		}
		
		try {
			inputSteam.close();
		} catch (IOException e) {
			System.out.println("Error closing the input stream");
			e.printStackTrace();
		}
		
		return soundArray;
	}

	/*
	 * Function gets called when this thread is started.  It acts as a "main"
	 * 	for this single thread, kicking off all the recording functions
	 * 
	 */
	@Override
	public void run() {		
		// record sound (and which also sends it)
		this.alg = new Algorithm();
		this.recordSound();
	}
	
	/*
	 * Function sets up the input to record data, and once it has enough
	 * 	data available, it writes it to the network
	 * 
	 */
	private void recordSound() {
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
			return;
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
		
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		int writeIndex = 0;
		while (true == record) {
			// Keep reading till we reach the end of the targetDataLine
			int readIndex = targetDataLine.read(dataArray, 0, bufferLength);
			if (-1 == readIndex) {
				break;
			} else {
				outBuffer.write(dataArray, 0, readIndex);
				Data.setDataBuffer(outBuffer.toByteArray());
				
				// if we have a chunks worth of data, write it to the socket
				if (outBuffer.toByteArray().length >= (writeIndex + this.chunkSize)) {
					byte[] chunk = Arrays.copyOfRange(outBuffer.toByteArray(), writeIndex, (writeIndex + this.chunkSize));
					this.writeToSocket(chunk);
					writeIndex = writeIndex + this.chunkSize;
				}
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
	}
	
	/*
	 * Wrapper function that takes in a byte array of data, and sends
	 * 	it to the correct protocols write function
	 * 
	 * @param byte[] from the recording module
	 */
	private void writeToSocket(byte[] data) {
		// before processing the packet, determine if we are going to
		// simulate loss and drop the packet
		if (true == this.dropPacketToSimulateLoss()) {
			return;
		}
		
		// if speech is enabled, run the algorithm prior to sending
		if (true == this.conf.isDetectSpeech()) {
			if (this.alg.isInitialized() == false) {
				byte[] buffer = Arrays.copyOfRange(data, 0, 20);
				this.alg.analyzeIntialReading(buffer);
			}
			
			if (false == this.alg.transmitSpeechSegment(data)) {
				//no speech so drop packet
				return;
			}
		}
		if (conf.getSocketType() == protocol.TCP) {
			this.writeToTcpSocket(data);
		} else {
			this.writeToUdpSocket(data);
		}
	}
	
	/*
	 * Function takes in a byte array and write is to the TCP socket
	 * 
	 * @param byte[] from the recording module
	 */
	private void writeToTcpSocket(byte[] soundArray) {
		try {
			// first write the size of the data being sent, so the receiver can read it first
			// and allocate a buffer to put the data in
			dOut.writeInt(soundArray.length);
		    if (soundArray.length > 0) {
		    	// now actually write the data
		    	dOut.write(soundArray, 0, soundArray.length);
		    }
		} catch (IOException e) {
			System.out.println("The socket was closed during transmission");
			System.exit(0);
		}
	}

	/*
	 * Function takes in a byte array and write is to the UDP socket
	 * 
	 * @param byte[] from the recording module
	 */
	private void writeToUdpSocket(byte[] soundArray) {
		try {
			// each data gram requires the IP address sent with it, so fetch it based on input parameters
			InetAddress ipAddress = InetAddress.getByName(this.conf.getIpAddress());
			
			//create data gram with data and send it
			DatagramPacket sendPacket = new DatagramPacket(soundArray, soundArray.length, ipAddress, 6789);
			udpClientSocket.send(sendPacket);
		} catch (UnknownHostException e) {
			System.out.println("There was an error writing to the socket");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("The socket was closed during transmission");
		}
	}
	
	/*
	 * Function determines if this packet gets "lost" in the network
	 * 
	 * @return boolean stating whether to transmit packet or not
	 *   returning true = drop packet
	 *   returning false = transmit packet
	 */
	private boolean dropPacketToSimulateLoss() {
		// generate a random number between 1 and 100
		Random random = new Random();
		int chance = random.nextInt(100) + 1;
		return (this.conf.getLoss() >= chance) ? true : false;		
	}
	
}
