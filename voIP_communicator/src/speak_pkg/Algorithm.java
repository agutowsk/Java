package speak_pkg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Algorithm {
	private List<Integer> energyList;
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
	private boolean initialized = false;

	public Algorithm() {
		this.energyList = new ArrayList<Integer>();
	}
	
	/*
	 * Function in charge of processing the
	 *  initial sound sample, and establishing the parameters used
	 *  to do speech activity detection
	 */
	public void analyzeIntialReading(byte[] sample) {
		// process energy data
		this.computeEnergy(sample);
		this.setIMX();
		this.setIMN();
		this.setL1();
		this.setL2();

		//calculate energy thresholds
		this.setITL();
		this.setITU();
		System.out.println("ITL: " + this.getITL());
		System.out.println("ITU: " + this.getITU());
		this.initialized = true;
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
	
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	/*
	 * Function computes the ITU or upper energy threshold,
	 * which is 5 times the lower threshold
	 */
	private void setITU() {
		this.setITU(5*this.getITL());
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
		for (int headIndex = 9; headIndex < dataBuffer.length; headIndex= headIndex+10) {
			byte[] currentBuffer = Arrays.copyOfRange(dataBuffer, tailIndex, headIndex);
			int sum = this.sumEnergyInSingleSegment(currentBuffer);
			energyList.add(sum);
			tailIndex = headIndex;
		}

		this.setEnergyList(energyList);
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
	 * Function in charge of processing the data values, using the already calculated
	 * values to apply the algorithm and do speech detection, eliminating silence.
	 *  
	 *  @param dataBuffer- byte[] data to analyze
	 */
	public boolean transmitSpeechSegment(byte[] buffer) {
		int energy = this.sumEnergyInSingleSegment(buffer);
		if (energy > this.getITU()) {
			return true;
		}
		return false;
	}
	
	/*
	 * Function in charge of processing the data values, using the already calculated
	 * values to apply the algorithm and do speech detection, eliminating silence.  This 
	 * function creates both speech.raw and speech.data values
	 *  
	 *  @param dataBuffer- byte[] data to analyze
	 */
	public byte[] scanForSpeech(byte[] dataBuffer) {
		ByteArrayOutputStream speechData = new ByteArrayOutputStream();
		boolean isSpeech = false;
		int tailIndex = 0;
		for (int headIndex = 9; headIndex < dataBuffer.length; headIndex= headIndex+10) {
			// process this segment
			byte[] currentBuffer = Arrays.copyOfRange(dataBuffer, tailIndex, headIndex);
			int energy = this.sumEnergyInSingleSegment(currentBuffer);
			// check if we currently detect speech
			if (false == isSpeech) {
				if (this.getITL() < energy) {
					try {
						speechData.write(currentBuffer);
					} catch (IOException e) {
						System.out.println("Failed to write data to ByteArrayOutputStream");
						e.printStackTrace();
					}
					isSpeech = true;
				}
			} else {
				// check the buffer for the end of current word
				if (energy > this.getITU()){
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
		
		return speechData.toByteArray();
	}
	
}
