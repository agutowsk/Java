package project1;

public class voiceRecoder {
	private final static String RECORD = "record";
	private final static String PLAY = "play";
	private final static String TEST = "test";

	public static void main(String[] args) {
		if (0 == args.length) {
			System.out.println("Invalid input parameter");
			return;
		}
		
		String param = args[0];
		if (param.equals(RECORD)) {
			System.out.println("User selected RECORD");

			/****** RECORD SOUND *********/
			final Record dataRecorder = new Record();
			dataRecorder.start();
			
			// register the shutdown hook so we stop recording at some point, and
			// then process the data
			Runtime.getRuntime().addShutdownHook(new ShutdownThread(dataRecorder));
		} else if (param.equals(PLAY)) {
			System.out.println("User selected PLAY");
			if (2 != args.length || args[1].isEmpty()) {
				System.out.println("Must provide a filename for playback option");
				return;
			}
			
			//play file
			String fileName = args[1];
			System.out.println("Attempting to play file " + fileName);
			audioAssistant.readFromFile(fileName);

		} else if (param.equals(TEST)) {
			System.out.println("User selected TEST");
			System.out.println("Hello World");
		} else {
			System.out.println("Unknown input parameter");
		}
	}
}
