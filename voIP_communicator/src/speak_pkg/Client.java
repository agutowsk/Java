package speak_pkg;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) {
		configuration config = inputAnalyzer.initializeConfig(args);
		
		if (protocol.TCP == config.getSocketType()) {
			// TCP code
			try {
				// open socket
				Socket socket = new Socket(config.getIpAddress(), 6789);
				System.out.println("Opened socket at port 6789");
				
				final recordingThread recorder = new recordingThread(socket, config);
				recorder.start();
				recorder.thread.join();
				
				//close socket
				if (null != socket) {
					socket.close();
				}
			} catch (UnknownHostException e) {
				System.out.println("There was an error with the socket");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("There was an error with the socket");
				e.printStackTrace();
			} catch (InterruptedException e1) {
				System.out.println("There was an error with the socket");
				e1.printStackTrace();
			}
		} else {
			final recordingThread recorder = new recordingThread(config);
			recorder.start();
			try {
				recorder.thread.join();
			} catch (InterruptedException e) {
				System.out.println("There was an error joining the threads");
				e.printStackTrace();
			}
			
			//we joined already, so when we get this far, the playbackThread is dead
			recorder.stop();
			recorder.shutdown();
		}
		
		System.exit(0);
	}

}
