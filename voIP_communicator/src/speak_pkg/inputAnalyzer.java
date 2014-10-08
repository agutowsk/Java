package speak_pkg;

/*
 * input params: 
 * 	1. socket type: -udp OR -tcp
 *  2. sample intervals: 20, 40, 60 ... all the way up to 1 second -i:20-1000
 *  3. speech detection enabled: -speech:true/false
 *  4. ip address: -ip:XX.XX.XX.XXX
 *  5. loss: -l:
 * 
 */
public class inputAnalyzer {
	public static configuration initializeConfig(String[] inputs) {
		// these initial variables are set to the defaults
		String ipAddress = "localhost";
		protocol socketType = protocol.TCP;
		int sampleInterval = 1000;
		boolean detectSpeech = false;
		int loss = 0;
		String client = "";
		
		if (inputs.length != 0) {
			for (String input : inputs) {
				if (input.contains("-clientA")) {
					client = "clientA";
				} else if (input.contains("-clientB")) {
					client = "clientB";
				} else if (input.contains("-udp")) {
					socketType = protocol.UDP;
				} else if (input.contains("-tcp")) {
					socketType = protocol.TCP;
				} else if (input.contains("-i:")) {
					String s = input.substring(input.indexOf(":") + 1, input.length());
					sampleInterval = Integer.parseInt(s);
				} else if (input.contains("-ip:")) {
					ipAddress = input.substring(input.indexOf(":") + 1, input.length());
				} else if (input.contains("-speech:")) {
					String s = input.substring(input.indexOf(":") + 1, input.length());
					detectSpeech = Boolean.parseBoolean(s);
				} else  if (input.contains("-l:")) {
					String s = input.substring(input.indexOf(":") + 1, input.length());
					loss = Integer.parseInt(s);
				}
			}
		}
		
//		// force the user to use a clientA or clientB input
//		if (client.isEmpty()) {
//			return null;
//		}
		
		return new configuration(socketType, detectSpeech, sampleInterval, ipAddress, loss, client);
	}
}
