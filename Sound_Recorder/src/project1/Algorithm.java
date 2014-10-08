package project1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Algorithm {
	private final String crossingsPath = "output/zero.data";
	private final String energyPath = "output/energy.data";
	private final String speechRawPath = "output/speech.raw";
	private final String speechDataPath = "output/speech.data";
	
	private List<Integer> zeroCrossings;
	private List<Integer> energyList;
	
	// in the speech piece we have to search back up to 250ms
	private double maxSearch = 0.250;

	//average of zero crossing rate
	private int IZC = 0;
	// standard deviation of the zero crossing rate
	private int stdDev = 0;
	//zero crossing threshold
	private int IZCT = 0;
	
	//max energy
	private int IMX = 0;
	//silence (energy related)
	private int IMN = 0;
	//3% of peak energy
	private int L1 = 0;
	//4x silent energy
	private int L2 = 0;
	// lower bound for energy threshold
	private int ITL = 0;
	// upper bound for energy threshold
	private int ITU = 0;
	
	public Algorithm() {
		super();
		this.zeroCrossings = new ArrayList<Integer>();
		this.energyList = new ArrayList<Integer>();
	}
	
	/*
	 * Function in charge of processing the
	 *  initial sound sample, and establishing the parameters used
	 *  to do speech activity detection
	 */
	public void analyzeIntialReading(byte[] sample) {
		// process zero crossing data first
		this.computeZeroCrossings(sample);
		this.setIZC();
		this.setStdDev();
		//calculate zero crossing threshold
		this.setIZCT();
		
		// process energy data
		this.computeEnergy(sample);
		this.setIMX();
		System.out.println("IMX: " + this.getIMX());
		this.setIMN();
		System.out.println("IMN: " + this.getIMN());
		this.setL1();
		System.out.println("L1: " + this.getL1());
		this.setL2();
		System.out.println("L2: " + this.getL2());
		//calculate energy thresholds
		this.setITL();
		System.out.println("ITL: " + this.getITL());
		this.setITU();
		System.out.println("ITU: " + this.getITU());
	}

	/****** ZERO CROSSINGS DATA FUNCTIONS *********/
	public List<Integer> getZeroCrossings() {
		return zeroCrossings;
	}

	public void setZeroCrossings(List<Integer> zeroCrossings) {
		this.zeroCrossings = zeroCrossings;
	}
	
	public int getIZC() {
		return IZC;
	}

	private void setIZC(int iZC) {
		IZC = iZC;
	}
	
	/*
	 * Function computes the average number of zero crossings
	 */
	private void setIZC() {
		int sum = 0;
		List<Integer> data = this.getZeroCrossings();
		for (int i : data) {
			sum = sum + i;
		}
		 this.setIZC(sum/data.size());
	}

	public int getStdDev() {
		return stdDev;
	}

	private void setStdDev(int stdDev) {
		this.stdDev = stdDev;
	}
	
	/*
	 * Function computes the standard deviation of zero crossings
	 */
	private void setStdDev() {
		int mean = this.getIZC();
		int sum = 0;
		List<Integer> data = this.getZeroCrossings();
		for (int i : data) {
			int diff = i - mean;
			sum = (int) (sum + (Math.pow(diff, 2)));
		}
		
		this.setStdDev(sum/ data.size());
	}

	public int getIZCT() {
		return IZCT;
	}
	
	private void setIZCT(int iZCT) {
		IZCT = iZCT;
	}

	/*
	 * Function computes the IZCT or zero crossing threshold,
	 * which is  min(25 / 10ms, IZC’ + 2*stdDev)
	 */
	private void setIZCT() {
		this.setIZCT( (int) Math.min((25/.01), (this.getIZC() + (2*this.getStdDev()))));
	}
	
	/****** Energy DATA FUNCTIONS *********/
	public List<Integer> getEnergyList() {
		return energyList;
	}

	private void setEnergyList(List<Integer> energyList) {
		this.energyList = energyList;
	}
	
	public int getIMX() {
		return IMX;
	}

	private void setIMX(int iMX) {
		IMX = iMX;
	}
	
	/*
	 * Function computes IMX or the max noise value
	 */
	private void setIMX() {
		int max = 0;
		for (int i : this.getEnergyList()) {
			max = Math.max(max, i);
		}
		this.setIMX(max);
	}

	public int getIMN() {
		return IMN;
	}

	private void setIMN(int iMN) {
		IMN = iMN;
	}
	
	/*
	 * Function computes the IMN or the minimum noise value
	 */
	
	private void setIMN() {
		int globalMin = 127;
		List<Integer> data = this.getEnergyList();
		for (int i : data) {
			globalMin = Math.min(globalMin, i);
		}
		this.setIMN(globalMin);
	}

	public int getL1() {
		return L1;
	}

	private void setL1(int l1) {
		L1 = l1;
	}
	
	/*
	 * Function computes l2 which is 3% of the peak energy level
	 */
	private void setL1() {
		int imn = this.getIMN();
		this.setL1((int) (0.03*(this.getIMX() - imn) + imn));
	}
	
	public int getL2() {
		return L2;
	}

	private void setL2(int l2) {
		L2 = l2;
	}
	
	/*
	 * Function computes the L2 or 4 times the silent energy level
	 */
	private void setL2() {
		this.setL2(4*this.getIMN());
	}
	
	public int getITL() {
		return ITL;
	}

	private void setITL(int iTL) {
		ITL = iTL;
	}
	
	/*
	 * Function computes the ITL or lower energy threshold,
	 * which is the lower value of L1 and L2 defined previously
	 */
	private void setITL() {
		this.setITL(Math.min(this.getL1(), this.getL2()));
	}

	public int getITU() {
		return ITU;
	}

	private void setITU(int iTU) {
		ITU = iTU;
	}
	
	/*
	 * Function computes the ITU or upper energy threshold,
	 * which is 5 times the lower threshold
	 */
	private void setITU() {
		this.setITU(5*this.getITL());
	}
	
	public double getMaxSearch() {
		return maxSearch;
	}

	public void setMaxSearch(double maxSearch) {
		this.maxSearch = maxSearch;
	}

	/****** Sample Processing FUNCTIONS *********/
	/*
	 * Function takes a byte and converts it to an unsigned int
	 * 
	 * @param dataBuffer- byte of data to convert
	 */
	public int convertByteToUInt(byte b){
		return (int) b & 0xff;
	}
	
	/*
	 * Function takes a data array and calculates the energy level
	 * by summing the magnitude of the sound in 10ms intervals, and outputs it 
	 * into a file (energy.data)
	 * 
	 * @param dataBuffer- byte[] data to analyze
	 */
	public void computeEnergy(byte[] dataBuffer) {
		List<Integer> energyList = new ArrayList<Integer>();
		int tailIndex = 0;
		// take a 10ms chunk from the dataBuffer (10ms = 80 samples so we can evenly split)
		for (int headIndex = 79; headIndex < dataBuffer.length; headIndex= headIndex+80) {
			byte[] currentBuffer = Arrays.copyOfRange(dataBuffer, tailIndex, headIndex);
			int sum = this.sumEnergyInSingleSegment(currentBuffer);
			energyList.add(sum);
			tailIndex = headIndex;
		}

		this.setEnergyList(energyList);
		audioAssistant.writeDataFile(this.energyPath, energyList, true);
	}
	
	/*
	 * Function takes a data array and calculates the energy level
	 * by summing the magnitude of the sound in 10ms intervals, and outputs it 
	 * into a file (energy.data)
	 * 
	 * @param dataBuffer- byte[] data to analyze
	 * @return int
	 */
	public int sumEnergyInSingleSegment(byte[] buffer) {
		int sum = 0;
		for (int i = 0; i < buffer.length; i++) {
			int magnitude = Math.abs(buffer[i]);
			sum = sum + magnitude;
		}
		
		return sum;
	}
	
	/*
	 * Function takes a data array and converts the raw 
	 * data into a amplitude between 0-255, and generates an output
	 * file with its converted data
	 * 
	 * @param dataBuffer- byte[] data to analyze
	 * @param filePath- String containing the file path where we write the file to
	 */
	public void computeAmplitudes(byte[] dataBuffer, String filePath) {
		List<Integer> amplitudes = new ArrayList<Integer>();
		for (int i = 0; i < dataBuffer.length; i++) {
			Integer conversion = this.convertByteToUInt(dataBuffer[i]);
			amplitudes.add(conversion);
		}
		audioAssistant.writeDataFile(filePath, amplitudes, false);
	}
	
	/*
	 * Function takes a data array and computes all the zero crossing
	 * points, and creates an output file of that data
	 * 
	 * @param dataBuffer- byte[] data to analyze
	 */
	public void computeZeroCrossings(byte[] dataBuffer) {
		List<Integer> listOfCrossingRates = new ArrayList<Integer>();
		int tailIndex = 0;
		// take a 10ms chunk from the dataBuffer (10ms = 80 samples)
		for (int headIndex = 79; headIndex < dataBuffer.length; headIndex= headIndex+80) {
			byte[] currentBuffer = Arrays.copyOfRange(dataBuffer, tailIndex, headIndex);
			int numberOfCrossings = this.getSingleZeroCrossing(currentBuffer);
			listOfCrossingRates.add(numberOfCrossings);
			tailIndex = headIndex;
		}
		
		this.setZeroCrossings(listOfCrossingRates);
		audioAssistant.writeDataFile(this.crossingsPath, listOfCrossingRates, true);
	}
	
	/*
	 * Function takes a data array and computes the number of zero crossing
	 * points in a single segment
	 * 
	 * @param dataBuffer- byte[] data to analyze
	 */
	private int getSingleZeroCrossing(byte[] currentBuffer) {
		int numberOfCrossings = 0;
		int indexValue = -1;
		for (int i = 0; i < currentBuffer.length; i++) {
			// calculate the sign of the current data point
			int nextPoint = (currentBuffer[i] > 0) ? 1 : -1;
			// if the signs don't match we know it crossed zero
			if (indexValue != nextPoint) {
				numberOfCrossings++;
			}
			// set the new value to the old value
			indexValue = nextPoint;
		}
		
		return numberOfCrossings;
	}
	
	/*
	 * Function in charge of processing the data values, using the already calculated
	 * values to apply the algorithm and do speech detection, eliminating silence.  This 
	 * function creates both speech.raw and speech.data values
	 *  
	 *  @param dataBuffer- byte[] data to analyze
	 */
	public void scanForSpeech(byte[] dataBuffer) {
		ByteArrayOutputStream speechData = new ByteArrayOutputStream();
		boolean isSpeech = false;
		int tailIndex = 0;
		for (int headIndex = 79; headIndex < dataBuffer.length; headIndex= headIndex+80) {
			// process this segment
			byte[] currentBuffer = Arrays.copyOfRange(dataBuffer, tailIndex, headIndex);
			int zeroCross = this.getSingleZeroCrossing(currentBuffer);
			int energy = this.sumEnergyInSingleSegment(currentBuffer);
			// check if we currently detect speech
			if (false == isSpeech) {
				// check the buffer for the start of a new word
				if (this.getIZCT() < zeroCross) {
					try {
						speechData.write(currentBuffer);
					} catch (IOException e) {
						System.out.println("Failed to write data to ByteArrayOutputStream");
						e.printStackTrace();
					}
					isSpeech = true;
				}
			}
			else {
				// check the buffer for the end of current word
				if (energy > this.getITU() && zeroCross > this.getIZCT()){
					try {
						speechData.write(currentBuffer);
					} catch (IOException e) {
						System.out.println("Failed to write data to ByteArrayOutputStream");
						e.printStackTrace();
					}
				}
				else {
					isSpeech = false;
				}
			}
			tailIndex = headIndex;
		}
		
		// create the output files with the reduced data set
		this.computeAmplitudes(speechData.toByteArray(), this.speechDataPath);
		audioAssistant.writeFile(speechData.toByteArray(), this.speechRawPath);
	}
	
}
