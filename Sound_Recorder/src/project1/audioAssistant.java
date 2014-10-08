package project1;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

// helper class for functions that do not need to be part spanned in a new thread
public class audioAssistant {

	/*
	 * Function takes in a byte array and generates an output file saved as a
	 * .wav file
	 * 
	 * @param inputStream- AudioInputStream of sound data
	 * 
	 * @param filename - String containing the file name to save
	 */
	public static void writeSoundAsWav(AudioInputStream inputStream, String filepath) {
		System.out.println("attempting to writeSoundAsWav");
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.createNewFile();
			}
			
			if (-1 == AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE,
					file)) {
				System.out.println("An error occured trying to write the file");
				return;
			}
		} catch (IOException e) {
			System.out.println("Failed to write outputStream");
			e.printStackTrace();
		}
		System.out.println("completed writeSoundAsWav");
	}

	/*
	 * Function takes in a byte array and generates an output file saved as a
	 * .raw file, this is used to generate both required output files
	 * 
	 * @param data- byte array of sound data
	 * 
	 * @param filepath - String containing the file name to save
	 */
	public static void writeFile(byte[] data, String filepath) {
		System.out.println("attempting to writeFile");
		FileOutputStream output = null;
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.createNewFile();
			}

			output = new FileOutputStream(file);
			output.write(data);
			output.flush();
			output.close();
		} catch (IOException e) {
			System.out.println("Failed to write outputStream");
			e.printStackTrace();
		} finally {
			try {
				// make sure we completely clean up
				if (null != output) {
					output.close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close outputStream");
				e.printStackTrace();
			}
		}

		System.out.println("completed writeFile");
	}
	
	/*
	 * Function takes in a List<Integer> and generates an output file
	 * 
	 * @param dataList- List<Integer> data to write to a file
	 * 
	 * @param filename - String containing the file name to save
	 * 
	 * @param newLine - boolean stating whether to print 1 audio frame per line or not
	 */
	public static void writeDataFile(String filename, List<Integer> dataList, boolean newLine) {
		try {
			Writer writer = new FileWriter(filename);
			for(Integer i : dataList) {
				writer.write(i.toString());
				if (newLine) {
					writer.write("\n");
				}
				else {
					writer.write("\t");
				}
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("An error occured while writing the file");
			e.printStackTrace();
		}
	}
	
	/*
	 * Function takes in a byte[] and generates an output file
	 * 
	 * @param array- byte[] data to write to a file
	 * 
	 * @param filename - String containing the file name to save
	 * 
	 * @param newLine - boolean stating whether to print 1 audio frame per line or not
	 */
	public static void writeDataFile(String filename, byte[] array, boolean newLine) {
		try {
			Writer writer = new FileWriter(filename);
			for(int i = 0; i < array.length; i++) {
				writer.write(String.valueOf(array[i]));
				if (newLine) {
					writer.write("\n");
				}
				else {
					writer.write("\t");
				}
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("An error occured while writing the file");
			e.printStackTrace();
		}
	}
	
	/*
	 * Function takes in a file name, opens it and plays the data
	 * out loud on the speakers
	 * 
	 * @param fileName - String containing the file name to read
	 */
	public static void readFromFile(String fileName) {
		System.out.println("readFromFile");
		// create and open fileName
		File incomingData = new File(fileName);
		if (!incomingData.exists()) { 
			System.out.println("Could not find file " + fileName);
			return;
		}

		FileInputStream inputSteam;
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
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(soundArray);
        AudioFormat audioFormat = new AudioFormat(8000, /* sampleRate as a float */
													8,	/* sampleSizeInBits */
													1, 	/* channels */
													true,/* signed */
													true); /* bigEndian */
        
        long streamLength = soundArray.length / audioFormat.getFrameSize();
        AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFormat, streamLength);
        audioAssistant.playAudioStream(audioInputStream);
        System.out.println("completed readFromFile");
	}
	
	/*
	 * Function takes in audio data and plays it out loud, used for
	 * playing sound data that has never been written/saved externally
	 * 
	 * @param audioStream - sound stream to play
	 */
	public static void playAudioStream(AudioInputStream audioStream) {
		System.out.println("playAudioStream");
		AudioFormat format = audioStream.getFormat();
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		SourceDataLine source = null;
		try {
			source = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			source.open(format);
		} catch (LineUnavailableException e) {
			System.out.println("Could not obtain SourceDataLine");
			e.printStackTrace();
			return;
		}

		source.start();
		byte tempBuffer[] = new byte[10000];
		
		// now that the source file is open, we can read it
		try {
			int index = 0;
			// -1 means end of file, so read till we hit the end
			while (-1 != index) {
				index = audioStream.read(tempBuffer, 0, tempBuffer.length);
				if (index > 0) {
					// write actually mean play to speakers in this case
					source.write(tempBuffer, 0, index);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading file");
			e.printStackTrace();
			return;
		} finally {
			// wait until the queue is empty then close
			source.drain();
			source.close();
		}
		System.out.println("completed playAudioStream");
	}
}
