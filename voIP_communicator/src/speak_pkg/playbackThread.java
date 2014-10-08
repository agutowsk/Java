package speak_pkg;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class playbackThread implements Runnable {
	public Thread thread;
	private Socket connectionSocket = null;
	private configuration conf = null;
	private boolean readFromSocket = true;
	private DataInputStream dIn = null;
	private DatagramSocket serverSocket = null;
	
	private AudioFormat audioFormat = null;
	private SourceDataLine source = null;

	/*
	 * Constructor used to pass input parameters from the main
	 * 	thread and uses them in this thread, used for TCP
	 * 
	 */
	public playbackThread(Socket sock, configuration config) {
		this.connectionSocket = sock;
		this.conf = config;
		try {
			this.dIn = new DataInputStream(this.connectionSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("unable to open data stream properly");
			e.printStackTrace();
		}
	}
	
	/*
	 * Constructor used to pass input parameters from the main
	 * 	thread and uses them in this thread, used for UDP
	 * 
	 */
	public playbackThread(configuration config) {
		this.conf = config;
		try {
			this.serverSocket = new DatagramSocket(6789);
		} catch (SocketException e) {
			System.out.println("unable to open data stream properly");
			e.printStackTrace();
		}
	}

	/*
	 * Function allocates an internal thread and kicks its off
	 * 
	 */
	public void start() {
		thread = new Thread(this);
		readFromSocket = true;
		thread.start();
		System.out.println("playbackThread started");
	}

	/*
	 * Function stops the current thread so that it can be shutdown
	 * 
	 */
	public void stop() {
		readFromSocket = false;
	}

	/*
	 * Function nulls out the thread so that it is effectively shutdown
	 * 
	 */
	public void shutdown() {
		thread = null;
	}

	/*
	 * Function gets executed when the playbackThread
	 *  is kicked off, it is in charge of reading from the 
	 *  socket and playing the data back
	 * 
	 */
	@Override
	public void run() {
		// setup out the outputs prior to reading
		this.initPlayback();
		
		// now read from the socket until the thread is stopped
		while (true == readFromSocket) {
			byte[] data = null;
			if (this.conf.getSocketType() == protocol.TCP) {
				data = this.tcpReadFromSocket();
			} else {
				// must be UDP
				data = this.udpReadFromSocket();
			}
			
			// play out what we just read, if we got anything
			if (data != null && data.length > 0) {
				this.playBytes(data);
			}
		}
		
		// after the while we are done, so cleanup the sources
		this.cleanUpPlayback();
	}

	/*
	 * Function is in charge of reading a chunk of data from
	 * 	the UDP socket
	 * 
	 * @return data- byte array of sound data
	 */
	private byte[] udpReadFromSocket() {
		int bufferSize = (int) (this.conf.getSampleInterval()*8);
		byte[] receiveData = new byte[bufferSize];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
			this.serverSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.println("There was an error reading the packet");
			e.printStackTrace();
		}
        
		return receivePacket.getData();
	}

	/*
	 * Function is in charge of reading a chunk of data from
	 * 	the TCP socket
	 * 
	 * @return data- byte array of sound data
	 */
	private byte[] tcpReadFromSocket() {
		byte[] data = null;
		try {
			// only read if we have data available
			if (0 < dIn.available()) {
				// read the length of the incoming segment and allocate space
				int len = dIn.readInt();
				if (len > 0) {
					data = new byte[len];
					// now read the whole message
					dIn.readFully(data);
				}
			}
		} catch (IOException e) {
			System.out.println("The socket was closed during transmission");
		}

		return data;
	}
	
	/*
	 * Function sets up the source a single type before play back occurs
	 * 
	 */
	private void initPlayback() {
		// Initialize the audio format
		this.audioFormat = new AudioFormat(8000, /* sampleRate as a float */
											8, /* sampleSizeInBits */
											1, /* channels */
											true,/* signed */
											true); /* bigEndian */
	
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, this.audioFormat);
		try {
			// get the output and initialize it with the desired audio format
			this.source = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			this.source.open(this.audioFormat);
		} catch (LineUnavailableException e) {
			System.out.println("Could not obtain SourceDataLine");
			e.printStackTrace();
			return;
		}
		
		// open the speakers for play back
		this.source.start();
	}
	
	/*
	 * Function cleans up the source used during play back
	 * 
	 */
	private void cleanUpPlayback() {
		// wait until the queue is empty then close
		this.source.drain();
		this.source.close();
	}

	/*
	 * Function takes in a byte array and converts it into a playable format,
	 * 	then plays the byte array data out of the speakers
	 * 
	 * @param byte[]- byte array of sound data
	 */
	private void playBytes(byte[] data) {
		byte tempBuffer[] = new byte[10000];

		// now that the source file is open, we can read it
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		long streamLength = data.length / this.audioFormat.getFrameSize();
		AudioInputStream audioInputStream = new AudioInputStream(inputStream, this.audioFormat, streamLength);
		try {
			int index = 0;
			// -1 means end of file, so read till we hit the end
			while (-1 != index) {
				index = audioInputStream.read(tempBuffer, 0, tempBuffer.length);
				if (index > 0) {
					// write actually mean play to speakers in this case
					this.source.write(tempBuffer, 0, index);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading file");
			e.printStackTrace();
		}
	}
}
