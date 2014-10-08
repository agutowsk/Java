package project1;

import java.util.Arrays;

public class ShutdownThread extends Thread {
	public Record existing = null;
	
	private final static String SOUND_NAME = "output/sound.raw";
	private final static String SOUND_DATA_NAME = "output/sound.data";

	/*
	 * This program does all the data processing once the signal has been completely
	 * recorded.  This means we have to catch the Ctrl + C call and then grab the 
	 * recorded data and run the algorithm over it, ShutdownThread does this task
	 * for us
	 * 
	 */
	public ShutdownThread(Record dataRecorder) {
		this.existing = dataRecorder;
	}
	
	// 		  open sound device
	//		  set up sound device parameters
	//		  record silence
	//		  set algorithm parameters
	//		  while (1)
	//		     record sound
	//		     compute energy
	//		     compute zero crossings
	//		     search for beginning/end of speech
	//		     write data to file
	//		     write sound to file
	//		     if speech, write speech to file
	//		  end while
	@Override
	public void run() {
		System.out.println("Inside Run");
		existing.stop();
		
		/****** FETCH DATA *********/
		byte[] audioInBytes = Data.getDataBuffer();
    	if (null == audioInBytes) {
    		System.out.println("Failed to read audioInBytes");
    		return;
    	}
    	
    	// take the first 100ms which is 800 frames, to process it and setup algorithm
		byte[] silence = Arrays.copyOfRange(audioInBytes, 0, (int) (.1*8000));

		Algorithm algorithm = new Algorithm();
		algorithm.analyzeIntialReading(silence);
		
		// Write the data as the sound.raw file
		audioAssistant.writeFile(audioInBytes, SOUND_NAME);
		
		// Translate the data we just wrote in the sound.raw file into the sound.data file
		algorithm.computeAmplitudes(audioInBytes, SOUND_DATA_NAME);
	
		/****** COMPUTE ENERGY *********/
		algorithm.computeEnergy(audioInBytes);
		
		/****** COMPUTE ZERO CROSSINGS *********/
		algorithm.computeZeroCrossings(audioInBytes);
		
		/****** SEARCH FOR BEGINNING AND END OF SPEECH *********/
		/****** GENERATES SPEECH FILES ********/
		algorithm.scanForSpeech(audioInBytes);

		/****** CLEAN UP ********/
    	existing.shutdown();
    	existing = null;
    	Runtime.getRuntime().halt(0);
	}
}
