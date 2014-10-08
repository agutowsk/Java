package speak_pkg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	/*
	 * input params: 
	 * 	1. socket type: -udp OR -tcp
	 *  2. sample intervals: 20, 40, 60 ... all the way up to 1 second -i:20-1000
	 *  3. speech detection enabled: -speech:true/false
	 *  4. ip address: -ip:XX.XX.XX.XXX
	 * 
	 */
	public static void main(String[] args) {
		configuration config = inputAnalyzer.initializeConfig(args);

		if (protocol.TCP == config.getSocketType()) {
			// TCP Server code
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(6789);
				System.out.println("Opened socket at port 6789");
			} catch (IOException e) {
				System.out.println("Failed to open the socket");
				e.printStackTrace();
			}
			
			try {
				Socket socket = serverSocket.accept();
				final playbackThread playback = new playbackThread(socket, config);
				playback.start();
				
				
				playback.thread.join();
				
				//we joined already, so when we get this far, the playbackThread is dead
				playback.stop();
				playback.shutdown();
				socket.close();
			} catch (IOException e) {
				System.out.println("An error occured while opening the socket");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println("An error occured while opening the socket");
				e.printStackTrace();
			}
			
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Failed to close the socket");
				e.printStackTrace();
			}
		} else {
			//UDP server code
			final playbackThread playback = new playbackThread(config);
			playback.start();
			try {
				playback.thread.join();
			} catch (InterruptedException e) {
				System.out.println("An error occured while joining the threads");
				e.printStackTrace();
			}
			
			//we joined already, so when we get this far, the playbackThread is dead
			playback.stop();
			playback.shutdown();
		}

		System.exit(0);
	}

}
