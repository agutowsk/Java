package speak_pkg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class voIPClient {
	public static void main(String[] args) {
		configuration config = inputAnalyzer.initializeConfig(args);
		if (config.getClient().isEmpty() && config.getSocketType() == protocol.TCP) {
			System.out.println("You must provide a clientA or clientB parameter");
			System.out.println("Please try again with one machine providing '-clientA' and the other providing '-clientB'");
			System.exit(0);
		}
		
		if (protocol.TCP == config.getSocketType()) {
			System.out.println("Setting up a TCP client");
			// TCP code
			try {
				// SETUP SOCKETS FOR TCP TRANSMISSION
				Socket sock = null;
				if ("clientA".equals(config.getClient())) {
					ServerSocket serverSocket = null;
					try {
						serverSocket = new ServerSocket(6789);
					} catch (IOException e) {
						System.out.println("Failed to open the socket");
						e.printStackTrace();
					}
					sock = serverSocket.accept();
				} else {
					sock = new Socket(config.getIpAddress(), 6789);
				}
				System.out.println("Sockets ready for transmission");
				
				// CREATE A RECORDING THREAD
				final recordingThread recorder = new recordingThread(sock, config);
				recorder.start();
				System.out.println("Recording begun");
				
				// CREATE A PLAY BACK THREAD
				final playbackThread playback = new playbackThread(sock, config);
				playback.start();
				System.out.println("Ready for playback");
				
				try {
					playback.thread.join();
					recorder.thread.join();
				} catch (InterruptedException e) {
					System.out.println("An error occured while opening the socket");
					e.printStackTrace();
				}
				
				//we joined already, so when we get this far, the recorder and playbackThread are dead
				recorder.stop();
				recorder.shutdown();
				playback.stop();
				playback.shutdown();
				
				//close socket
				if (null != sock) {
					sock.close();
				}
			} catch (UnknownHostException e) {
				System.out.println("There was an error with the socket");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("There was an error with the socket");
				e.printStackTrace();
			}
		} else {
			// UDP CODE
			System.out.println("Setting up a UDP client");
			// CREATE A RECORDING THREAD
			final recordingThread recorder = new recordingThread(config);
			recorder.start();
			
			// CREATE A PLAY BACK THREAD
			final playbackThread playback = new playbackThread(config);
			playback.start();
			
			try {
				recorder.thread.join();
			} catch (InterruptedException e) {
				System.out.println("There was an error joining the threads");
				e.printStackTrace();
			}
			
			try {
				playback.thread.join();
			} catch (InterruptedException e) {
				System.out.println("An error occured while joining the threads");
				e.printStackTrace();
			}
			
			//we joined already, so when we get this far, the recordingThread is dead
			recorder.stop();
			recorder.shutdown();
			
			//we joined already, so when we get this far, the playbackThread is dead
			playback.stop();
			playback.shutdown();
		}
		
		System.exit(0);
	}
}
