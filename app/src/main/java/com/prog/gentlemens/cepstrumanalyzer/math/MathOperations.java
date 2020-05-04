package com.prog.gentlemens.cepstrumanalyzer.math;

import com.prog.gentlemens.cepstrumanalyzer.enums.NameOfMathFunction;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * In this class most of the data type is primitive - because in most cases the mathematical operations on primitives
 * are faster then on objects
 */
public final class MathOperations {
	
	private MathOperations() {
	}
	
	//TODO tests: calculateFFT()
	public static double calculateFFT(short[] inputData, int sampleRate, double maxFrequency, double minFrequency) {
		validateCalculateFFT(inputData, sampleRate, maxFrequency, minFrequency);
		
		double[] tempData = new double[inputData.length];
		double[] blackmannWindow = MathOperations.calculateBlackmannWindow(inputData.length);     // normally inputData.length should = 1024
		
		// FFT
		for (int i = 0; i < inputData.length; ++i) {
			tempData[i] = inputData[i] * blackmannWindow[i];
		}
		
		FFT.RealFT(tempData, 1);
		
		// LN and ABS
		for (int i = 0; i < tempData.length; i = i + 2) {
			if (tempData[i] == 0) {
				tempData[i + 1] = 0;
				continue;
			}
			
			tempData[i] = Math.sqrt(tempData[i] * tempData[i] + tempData[i + 1] * tempData[i + 1]);
			tempData[i + 1] = 0;
			
			tempData[i] = Math.log(tempData[i]);
		}
		
		// IFFT
		FFT.RealFT(tempData, -1);
		tempData[0] = tempData[1] = tempData[tempData.length - 1] = tempData[tempData.length - 2] = 0;
		
		// MAX
		double[] max = MathOperations.findMax(tempData, sampleRate, maxFrequency, minFrequency);
		//SEND RESULT
		
		//System.out.println("fft: " + max[0]);
		//[0] max | [1] shim
		return max[0];
	}
	
	private static void validateCalculateFFT(short[] inputDataToValidate, int sampleRateToValidate, double maxFrequencyToValidate, double minFrequencyToValidate) {
		String message = null;
		if (inputDataToValidate == null) {
			message += "short array can not be null ";
		}
		if (sampleRateToValidate <= 0) {
			message += " sampleRate has to be greater then zero ";
		}
		if (maxFrequencyToValidate <= 0) {
			message += " maxFrequency has to be greater then zero ";
		}
		if (minFrequencyToValidate <= 0) {
			message += " minFrequency has to be greater then zero ";
		}
		if (maxFrequencyToValidate < minFrequencyToValidate) {
			message += " maxFrequency has to be greater or equal the minFrequency ";
		}
		if (message != null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	//TODO tests: findMax()
	public static double[] findMax(double[] inputData, int fs, double maxFrequency, double minFrequency) {
		validateFindMax(inputData, fs, maxFrequency, minFrequency);
		
		double maxFreq = 0;
		double ampForMaxFreq = 0;
		int startIndex = round(fs / maxFrequency);        //appropriate starting index
		int endIndex = round(fs / minFrequency);
		int index = 0;                                 //number of maximum array
		//when only 0 instead of index = start
		double tempMaxFreq = inputData[startIndex];     //looking for maximum
		
		for (int i = startIndex; i < (endIndex > inputData.length - 1 ? inputData.length - 1 : endIndex); ++i) {
			if (tempMaxFreq < inputData[i + 1]) {
				tempMaxFreq = inputData[i + 1];
				index = i + 1;
			}
		}
		
		if (index != 0) {
			maxFreq = ((double) fs) / index;        // 1 / ( index * 1 / ( fs ) )
			ampForMaxFreq = inputData[index];    //ampForMaxFreq[i] = tempMaxFreq;
		} else {
			if (index == 0) {
				maxFreq = 0;
				ampForMaxFreq = 0;
			}
		}
		
		//System.out.println( index + '-' + maxFreq[i] + '-' + tempMaxFreq);
		return new double[]{maxFreq, ampForMaxFreq};
	}
	
	private static void validateFindMax(double[] inputDataToValidate, int sampleRateToValidate, double maxFrequencyToValidate, double minFrequencyToValidate) {
		String message = null;
		if (inputDataToValidate == null) {
			message += "double array can not be null ";
		}
		if (sampleRateToValidate <= 0) {
			message += " sampleRate has to be greater then zero ";
		}
		if (maxFrequencyToValidate <= 0) {
			message += " maxFrequency has to be greater then zero ";
		}
		if (minFrequencyToValidate <= 0) {
			message += " minFrequency has to be greater then zero ";
		}
		if (maxFrequencyToValidate < minFrequencyToValidate) {
			message += " maxFrequency has to be greater or equal the minFrequency ";
		}
		if (message != null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	public static double calculateMathFunctionInSteps(NameOfMathFunction nameOfMathFunction, short[] recordA, short[] recordB, int step, int fs, double maxFrequency, double minFrequency) {
		validateCalculateMathFunctionInSteps(nameOfMathFunction, recordA, recordB, step);
            /*
                This function copies values from recordA to recordB - d so as result it generates set of arrays
                that includes all recordA array and set of arrays with elements from recordA and recordB array
                -> it does not create array that includes only values from recordB array !
                -> it is created in the next iteration of averageOnFly() function
                 for recordA.length == recordB.length
             */
		
		double result = 0;
		double tempCalculate = 0;
		
		int numberOfParts = round((recordB.length) / (float) step);
		int counter = 0;
		
		short[] tempRecordArray = new short[recordB.length];
		
		for (int i = 0; i < numberOfParts; ++i) {
			try {
				// from first frame (array)
				System.arraycopy(recordA, i * step, tempRecordArray, 0, tempRecordArray.length - i * step);
				
				// from second frame (array)
				System.arraycopy(recordB, 0, tempRecordArray, tempRecordArray.length - i * step, i * step);
			} catch (IndexOutOfBoundsException e) {
				System.out.println(" exception in System.arraycopy() in averageOnFly()");
				continue;
			}
			
			if (nameOfMathFunction == NameOfMathFunction.CALCULATE_FFT) {
				tempCalculate = calculateFFT(tempRecordArray, fs, maxFrequency, minFrequency);
			} else {
				if (nameOfMathFunction == NameOfMathFunction.CALCULATE_RMS) {
					tempCalculate = calculateRMS(tempRecordArray);
				}
			}
			
			// do not add values from tempRecordArray that max is = 0 -> it is silence
			if (tempCalculate != 0) {
				result = tempCalculate + result;
				counter = counter + 1;
			}
		}
		
		return result / counter;
	}
	
	private static void validateCalculateMathFunctionInSteps(NameOfMathFunction nameOfMathFunction, short[] recordAToValidate, short[] recordBToValidate, int stepToValidate) {
		String message = null;
		if (nameOfMathFunction == null) {
			message += " nameOfMathFunction can not be null ";
		}
		if (recordAToValidate == null) {
			message += " short array recordA can not be null ";
		}
		if (recordBToValidate == null) {
			message += " short array recordB can not be null ";
		}
		if (recordAToValidate != null && recordBToValidate != null) {
			if (recordAToValidate.length != recordBToValidate.length) {
				message += " length of short array recordA and short array recordB has to be equal ";
			}
			if (recordAToValidate.length <= 0 || recordBToValidate.length <= 0) {
				message += " length of short array recordA and short array recordB has to be greater then 0 ";
			}
		}
		if (stepToValidate <= 0) {
			message += " step has to be greater then zero ";
		}
		if (message != null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * @param freqArray double [] - array that contains frequencies
	 * @return double - calculated jitter value
	 */
	//TODO tests: calculateJitter()
	public static double calculateJitter(double[] freqArray) {
		validateCalculateJitter(freqArray);
		
		// make new array with periods based on freqArray [] input
		List<Double> periodArray = new ArrayList<>();
		double jitter = 0;
		
		// validate data - without 0 and NaN values
		for (double singleDoubleElement : freqArray) {
			if (singleDoubleElement != 0 && !Double.isNaN(singleDoubleElement)) {
				periodArray.add(Math.pow(singleDoubleElement, -1));
			}
		}
		
		for (int i = 1; i < periodArray.size(); ++i) {
			jitter = jitter + Math.abs(periodArray.get(i) - periodArray.get(i - 1));
		}
		
		//jitter = jitter * 1 / (N - 1)
		jitter = jitter * Math.pow(periodArray.size() - 1, -1);
		
		// the previous value is in [s], change to [ms]
		jitter = jitter * 1000;
		
		return jitter;
	}
	
	private static void validateCalculateJitter(double[] freqArrayToValidate) {
		if (freqArrayToValidate == null) {
			throw new IllegalArgumentException(" freqArray can not be null ");
		}
	}
	
	/**
	 * @param ampArray double [] - array that contains amplitudes
	 * @return double - calculated shimmer value
	 */
	//TODO tests: calculateShimmer()
	public static double calculateShimmer(double[] ampArray) {
		validateCalculateShimmer(ampArray);
		
		double shimmer = 0;
		double counter = 0;
		
		for (int i = 0; i < ampArray.length - 1; ++i) {
			if (ampArray[i] != 0 && !Double.isNaN(ampArray[i])) {
				if (ampArray[i + 1] != 0 && !Double.isNaN(ampArray[i + 1])) {
					shimmer = shimmer + Math.abs(20 * Math.log10(ampArray[i + 1] / ampArray[i]));
					counter = counter + 1;
				}
			}
		}
		
		shimmer = shimmer * Math.pow(counter - 1, -1);
		return shimmer;
	}
	
	private static void validateCalculateShimmer(double[] freqArrayToValidate) {
		if (freqArrayToValidate == null) {
			throw new IllegalArgumentException(" freqArray can not be null ");
		}
	}
	
	public static double[] calculateBlackmannWindow(int size) {
		double[] windowOut = new double[size];
		
		for (int i = 0; i < size; ++i) {
			windowOut[i] = 0.42 - 0.5 * Math.cos(2 * Math.PI * i / (size - 1)) + 0.08 * Math.cos(4 * Math.PI * i / (size - 1));
		}
		
		return windowOut;
	}
	
	//TODO tests: dbMeter()
	public static double calculateDB(double inputData) {
		return 10 * log10(inputData);
	}
	
	//TODO tests: rmsMeter()
	public static double calculateRMS(short[] shortInputData) {
		validateCalculateRMS(shortInputData);
		
		double result = 0;
		
		for (short singleShortElement : shortInputData) {
			// + pow(2, 8) -> adds 2^8 to move -2^8 value to 0 -> move minimum value to 0 not to -2^8
			result = result + pow((singleShortElement + pow(2, 0)), 2);
		}
		
		return sqrt(result / shortInputData.length);
	}
	
	private static void validateCalculateRMS(short[] shortInputDataToValidate) {
		if (shortInputDataToValidate == null) {
			throw new IllegalArgumentException(" short array can not be null ");
		}
	}
	
	public static int round(double value) {
		if ((value - (int) value) >= 0.5)            //(int) value == floor(value)
		{
			return (int) Math.floor(value + 0.5);
		} else {
			return (int) Math.floor(value);
		}
	}
	
	public static float roundPrime(double value) {
		if ((value >= (Math.floor(value) + 0.25)) && (value <= (Math.floor(value) + 0.75))) {
			return ((float) (Math.floor(value) + 0.5));
		} else {
			if (value < Math.floor(value) + 0.25) {
				return (float) Math.floor(value);
			} else {
				return (float) Math.floor(value) + 1;
			}
		}
	}
	
	public static double arithmeticFrequencyAverage(double[] frequencies) {
		validateArithmeticFrequencyAverage(frequencies);
		
		double result = 0;
		
		int n = 0;
		
		for (int i = 0; i < frequencies.length; ++i, ++n) {
			if (frequencies[i] == 0)        //it means silence
			{
				n = n - 1;
				continue;
			}
			
			result = result + frequencies[i];
		}
		
		return n != 0 ? result / n : n;
	}
	
	private static void validateArithmeticFrequencyAverage(double[] frequencies){
		if(frequencies == null){
			throw new IllegalArgumentException(" frequencies can not be null");
		}
	}
	
	
	public static short[] byteToShortConversion(byte[] byteData) {
		short[] shortData = new short[byteData.length / 2];
		
		for (int i = 0; i < byteData.length / 2; i++) {
			shortData[i] = (short) ((byteData[2 * i + 1] << 8) + byteData[2 * i]);  //waga operatorów
		}
		
		return shortData;
	}
	
	public static void shortToByteConversion(short[] shortData, int n, byte[] byteData) {
		validateShortToByteConversion(shortData, n, byteData);
		for (int i = 0; i < n; i++) {
			byteData[i * 2] = (byte) (shortData[i] & 0x00FF);
			byteData[(i * 2) + 1] = (byte) (shortData[i] >> 8);
			//shortData[i] = 0;						//why ?
		}
	}
	
	private static void validateShortToByteConversion(short[] shortData, int n, byte[] byteData) {
		String message = null;
		
		if (shortData == null) {
			message += " shortData can not be null ";
		}
		if (byteData == null) {
			message += " byteData can not be null";
		}
		if (n <= 0) {
			message += " n has to be greater then 0";
		}
		if (message != null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	public static byte[] shortToByteConversion(short[] shortData) {
		validateShortToByteConversion(shortData);
		
		byte[] resultByteData = new byte[2 * shortData.length];
		for (int i = 0; i < shortData.length; i++) {
			resultByteData[i * 2] = (byte) (shortData[i] & 0x00FF);
			resultByteData[(i * 2) + 1] = (byte) (shortData[i] >> 8);
			//shortData[i] = 0;						//why ?
		}
		return resultByteData;
	}
	
	private static void validateShortToByteConversion(short[] shortData) {
		if (shortData == null) {
			throw new IllegalArgumentException(" shortData can not be null");
		}
	}
	
}
