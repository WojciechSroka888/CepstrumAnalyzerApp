package com.prog.gentlemens.cepstrumanalyzer;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.prog.gentlemens.cepstrumanalyzer.data.Data;
import com.prog.gentlemens.cepstrumanalyzer.math.FFT;
import com.prog.gentlemens.cepstrumanalyzer.plot.ScatterGraph;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.arithmeticFrequencyAverage;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.byteToShortConversion;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.calculateBlackmannWindow;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.round;

public class ResultActivity extends AppCompatActivity {
	
	private Logger logger = Logger.getLogger(ResultActivity.class.getName());
	private Data currentData;
	private Button playButton;
	private Button analyzeButton;
	private Button selectAButton;
	private Button selectBButton;
	private Button backButton;
	
	private TextView legendTextView;
	private TextView streamTextView;
	
	private boolean startPlaying = true;
	
	private AudioTrack audioTrack = null;
	
	private Integer selectedA;
	private Integer selectedB;
	
	private double[] maksy;
	private double[] shim;
	private int N;
	private double JT;
	private double SH;
	private double fMean;
	
	private boolean isAnalyzed = false;
	private byte[] savedEnterMusicTemp = null;
	
	private ScatterChart scatterChart;
	private ScatterGraph scatterGraph;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		legendTextView = findViewById(R.id.legend_text_view);
		streamTextView = findViewById(R.id.stream_text_view);
		backButton = findViewById(R.id.back_button);
		playButton = findViewById(R.id.play_button);
		analyzeButton = findViewById(R.id.analyze_button);
		selectAButton = findViewById(R.id.a_button);
		selectBButton = findViewById(R.id.b_button);
		scatterChart = findViewById(R.id.scatter_chart);
		
		setBackButton();
		setPlayButton();
		setAnalyzeButton();
		setSelectAButton();
		setSelectBButton();
		
		checkCurrentData();
	}
	
	private void setBackButton() {
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DataActivity.class);
				intent.putExtra("current_data", currentData);
				startActivity(intent);
			}
		});
	}
	
	private void setSelectAButton() {
		selectAButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedA = scatterGraph.getTempSelected();
				logger.info("*****SelectedA = " + selectedA);
				
				if (!isAnalyzed) {
					selectAButton.setText("analyze first");
				} else {
					selectAButton.setText("select left");
					
					if (selectedA != null) {
						selectAButton.setText(Integer.toString(selectedA));
					}
				}
			}
		});
	}
	
	private void setSelectBButton() {
		selectBButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedB = scatterGraph.getTempSelected();
				
				if (!isAnalyzed) {
					selectBButton.setText("analyze first");
				} else {
					selectBButton.setText("select right");
					
					if (selectedB != null) {
						selectBButton.setText("" + selectedB);
					}
				}
			}
		});
	}
	
	private void setPlayButton() {
		playButton.setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void onClick(View v) {
				onPlay(startPlaying);
				startPlaying = !startPlaying;
			}
		});
	}
	
	private void setAnalyzeButton() {
		analyzeButton.setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void onClick(View v) {
				onLineTestFunction();
				startAnalyzing();
				isAnalyzed = true;
			}
		});
	}
	
	private void checkCurrentData() {
		Serializable serializable = getIntent().getSerializableExtra("current_data");
		if ((serializable != null) && (!serializable.equals(currentData))) {
			currentData = (Data) serializable;
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void startPlaying() {
		final byte[] musicFile;
		
		if (currentData.getCurrentFile() != null) {
			musicFile = readDataFromFile();
		} else {
			musicFile = savedEnterMusicTemp;  //from saved temp, after inverting
		}
		
		int minBufferSize = AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
		audioTrack.play();
		
		new Thread(new Runnable() {
			public void run() {
				logger.info("start playing");
				audioTrack.write(musicFile, 0, musicFile.length);
			}
		}).start();
		
		playButton.setText("Stop");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private byte[] readDataFromFile() {
		byte[] enterMusic = new byte[(int) currentData.getCurrentFile().length()];
		
		try (InputStream inputStream = new FileInputStream(currentData.getCurrentFile())) {
			inputStream.read(enterMusic);
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		return enterMusic;
	}
	
	private void stopPlaying() {
		if (audioTrack != null) {
			audioTrack.release();
			audioTrack = null;
			logger.info("stop playing");
			
			// mStartPlaying = false;
			playButton.setText("Play");
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void startAnalyzing() {
		logger.info("*****startAnalyzing()*******");
		
		if ((selectedA == null) && (selectedB == null)) {
			cepstrumAll();
		} else {
			logger.info("*****startAnalyzing() --> again()*******");
			cepstrumPartly();
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void cepstrumAll() {
		byte[] dane;
		
		if (currentData.getCurrentFile() != null) {
			dane = readDataFromFile();
		} else {
			dane = savedEnterMusicTemp;
		}
		
		short[] dane2 = new short[dane.length / 2];
		dane2 = byteToShortConversion(dane);
		
		int d = 64;                                            //step shift window
		int n = 2 * 512;                                    //size of frame
		N = round((dane2.length - n) / d);            //number of frames
		int fs = 22050;                                        //sampling frequency
		
		double[][] tabl = new double[N][n];
		double[] temp = new double[n];
		
		double[] oknoBlackmana = calculateBlackmannWindow(n);
		
		logger.info("*****FFT*****");
		for (int i = 0; i < N; ++i) {
			//part of the window
			for (int j = 0; j < n; ++j) {
				temp[j] = dane2[j + i * d] * oknoBlackmana[j];
			}
			
			FFT.RealFT(temp, 1);
			
			for (int j = 0; j < temp.length; ++j) {
				tabl[i][j] = temp[j];
			}
		}
		logger.info("*****AFTER FFT*****");
		
		//****************************************************************************************
		
		logger.info("*****LN i ABS*****");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < n; j = j + 2) {
				if (tabl[i][j] == 0)                    //because log(0) = NaN
				{
					tabl[i][j + 1] = 0;                    //we need to 0 the imaginary part
					continue;
				}
				
				tabl[i][j] = Math.sqrt(tabl[i][j] * tabl[i][j] + tabl[i][j + 1] * tabl[i][j + 1]);
				tabl[i][j + 1] = 0;
				
				tabl[i][j] = Math.log(tabl[i][j]);
			}
		}
		logger.info("*****END LN i ABS*****");
		
		logger.info("*****IFFT*****");
		for (int i = 0; i < N; ++i) {
			FFT.RealFT(tabl[i], -1);
			
			tabl[i][tabl[i].length - 1] = 0;
			tabl[i][tabl[i].length - 2] = 0;
			
			tabl[i][0] = 0;
			tabl[i][1] = 0;
		}
		logger.info("*****END IFFT*****");
		
		//****************************************************************************************
		
		maksy = new double[N];         //? frequency value
		shim = new double[N];          //? amplitude value
		
		findMax(maksy, shim, tabl, N, n, fs);
		
		//fmean
		fMean = arithmeticFrequencyAverage(maksy);
		
		//JITTER
		//double JT = jitter( maksy, f_mean );
		JT = 0;
		//SHIMMER
		//double SH = shimmer( shim, maksy.length, f_mean );
		SH = 0;
		//plotScatterGraph(maksy, 22050, JT, SH, fMean, "Basic Frequency Line");
		
		//*******************************************************************
		logger.info("*****END runFFT()*****");
		
		scatterGraph = new ScatterGraph(scatterChart, legendTextView, "Basic Frequency Line", maksy, JT, SH, fMean, 1000);
		scatterGraph.plotScatterGraph();
	}
	
	private void cepstrumPartly() {
		logger.info("*****START cepstrumPartly()*****");
		if (selectedA == null) {
			selectedA = 0;
		}
		if (selectedB == null) {
			selectedB = maksy.length;
		}
		if (selectedA.equals(selectedB)) {
			selectedA = 0;
			selectedB = maksy.length;
		} else {
			if (selectedA > selectedB) {
				int temp = selectedA;
				selectedA = selectedB;
				// TODO check "java is passing by value"
				selectedB = temp;
			}
		}
		
		double[] temp_maksy = new double[selectedB - selectedA];
		double[] temp_shim = new double[selectedB - selectedA];
		
		for (int i = 0; i < selectedB - selectedA; ++i) {
			temp_maksy[i] = maksy[i + selectedA];
			temp_shim[i] = shim[i + selectedA];
		}
		//fmean
		fMean = arithmeticFrequencyAverage(temp_maksy);
		
		//JITTER
		//JT = jitter( temp_maksy, f_mean );
		JT = 0;
		
		//SHIMMER
		//SH = shimmer( temp_shim, temp_maksy.length, f_mean );
		SH = 0;
		
		scatterGraph = new ScatterGraph(scatterChart, legendTextView, "Basic Frequency Line", maksy, JT, SH, fMean, 1000);
		scatterGraph.plotScatterGraph();
		
		logger.info("*****END cepstrumPartly()*****");
	}
	
	private double[] cepstrumStreamPart(short[] part) {
		int n = part.length;         //2 * 512 size of frame
		
		double[] tabl = new double[n];
		double[] oknoBlackmana = calculateBlackmannWindow(n);
		
		logger.info("*****FFT*****");
		//window application
		for (int j = 0; j < n; ++j) {
			tabl[j] = part[j] * oknoBlackmana[j];
		}
		
		//Fourier transform
		FFT.RealFT(tabl, 1);
		
		logger.info("*****AFTER FFT*****");
		//****************************************************************************************
		
		logger.info("*****LN i ABS*****");
		for (int j = 0; j < n; j = j + 2) {
			if (tabl[j] == 0)                    //because log(0) = NaN
			{
				tabl[j + 1] = 0;                    //we need to 0 imaginary part
				continue;
			}
			
			tabl[j] = Math.sqrt(tabl[j] * tabl[j] + tabl[j + 1] * tabl[j + 1]);
			tabl[j + 1] = 0;
			
			tabl[j] = Math.log(tabl[j]);
		}
		logger.info("*****AFTER LN i ABS*****");
		
		logger.info("*****IFFT*****");
		FFT.RealFT(tabl, -1);
		
		tabl[tabl.length - 1] = 0;
		tabl[tabl.length - 2] = 0;
		
		tabl[0] = 0;
		tabl[1] = 0;
		logger.info("*****AFTER IFFT*****");
		logger.info("*****END runFFT()*****");
		return tabl;
	}
	
	private double[] frequencyMaxStreamPart(double[] tablIn) {
		logger.info("*****START fMeanPart()*****");
		
		double tempMax;                        //for maximum searching
		double fhigh = 400;
		double fmin = 40;
		int fs = 22050;
		double[] tablOut = {0, 0};
		//tablOut[0] -> maksy - fmax
		//tablOut[1] -> shim -  fmax value
		
		int start = round(fs / fhigh);        //providing start from appropraite index
		int koniec = round(fs / fmin);
		int index = 0;                            //number of maximum array
		
		tempMax = tablIn[start];
		
		for (int j = start; j < koniec; ++j) {
			if (tempMax < tablIn[j + 1]) {
				tempMax = tablIn[j + 1];
				index = j + 1;
			}
		}
		
		if (index != 0) {
			tablOut[0] = fs / index;        // 1 / ( index * 1 / ( fs ) )
			tablOut[1] = tablIn[index];        //shim[i] = tempMax;
		} else {
			if (index == 0) {
				tablOut[0] = 0;
				tablOut[1] = 0;
			}
		}
		
		logger.info("*****END fMeanPart()*****");
		return tablOut;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void onLineTestFunction() {
		byte[] dane;
		
		if (currentData.getCurrentFile() != null) {
			dane = readDataFromFile();
		} else {
			dane = savedEnterMusicTemp;
		}
		
		short[] dane2 = byteToShortConversion(dane);
		
		//BEFORE STREAMING
		
		//************************before streaming or global value globalna**************************
		
		double frequencyMean = 0;
		double jitter = 0;
		double shimmer = 0;
		
		//*****************************before streaming******************************************
		
		int counter = 0;
		
		//STREAMING
		
		//***************************in place of streaming audio **********************************
		
		double frequencyMaxTemp;
		double frequencyMaxAmplitudeTemp;
		double tablTemp[] = {0, 0};
		
		int d = 64;                                            //shift step
		int n = 2 * 512;                                    //size of frame
		N = round((dane2.length - n) / d);            //number of frames
		
		short[] sendTemp = new short[n];
		
		logger.info("*****FFT*****");
		
		for (int i = 0; i < N; ++i, ++counter) {
			//wycinek
			for (int j = 0; j < n; ++j) {
				sendTemp[j] = dane2[j + i * d];
			}
			
			tablTemp = frequencyMaxStreamPart(cepstrumStreamPart(sendTemp));
			frequencyMaxTemp = tablTemp[0];
			frequencyMaxAmplitudeTemp = tablTemp[1];
			
			if (frequencyMaxTemp == 0) {
				counter = counter - 1;
				continue;
			}
			
			frequencyMean = frequencyMean + frequencyMaxTemp;
			
			streamTextView.setText(Integer.toString((int) (frequencyMaxTemp)));
			
			selectBButton.setText(Integer.toString((int) (frequencyMean / counter)));
			
		}
		
		frequencyMean = frequencyMean / counter;
		
		
		logger.info("*****END onLineTestFunction()*****");
		//******************graphics********************************
	}
	
	private void findMax(double[] maksy, double[] shim, double[][] tabl, int N, int n, int fs) {
		logger.info("*****START findMax()*****");
		
		double temp_max = 0;                    //looking for maximum
		double fhigh = 400;
		double fmin = 40;
		
		int start = round(fs / fhigh);        //appropraite starting index
		int koniec = round(fs / fmin);
		int index;                                 //number of maximum array
		
		for (int i = 0; i < N; ++i) {
			index = 0;                        //when only 0 instead of index = start
			
			temp_max = tabl[i][start];
			
			for (int j = start; j < koniec; ++j) {
				if (temp_max < tabl[i][j + 1]) {
					temp_max = tabl[i][j + 1];
					index = j + 1;
				}
			}
			
			if (index != 0) {
				maksy[i] = fs / index;        // 1 / ( index * 1 / ( fs ) )
				shim[i] = tabl[i][index];    //shim[i] = temp_max;
			} else {
				if (index == 0) {
					maksy[i] = 0;
					shim[i] = 0;
				}
			}
			
			//System.out.println( index + '-' + maksy[i] + '-' + temp_max);
		}
		//roundJ(3.44);
		logger.info("*****END findMax()*****");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public void onPause() {
		logger.info("*****MainActivity onPause()*****");
		super.onPause();
		
		try (FileOutputStream fos = openFileOutput("tempFile1", MODE_PRIVATE)) {
			if (currentData.getCurrentFile() != null) {
				fos.write(readDataFromFile());
			} else {
				fos.write(savedEnterMusicTemp);
			}
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		
		Bundle bundle = new Bundle();
		bundle.putBoolean("isAnalyzed", isAnalyzed);
		bundle.putString("pathMainActivity", currentData.getPath());
		
		Intent intent = getIntent();
		intent.putExtras(bundle);
		
		logger.info("*****MainActivity onPause()*****");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public void onResume()          //onResume() vs onRestoreInstanceState()
	{
		logger.info("*****MainActivity onResume()*****");
		super.onResume();
		
		Intent intent = getIntent();
		Bundle extrasBundle = intent.getExtras();
		
		if (extrasBundle != null && !extrasBundle.isEmpty()) {
			if (extrasBundle.containsKey("isAnalyzed")) {
				isAnalyzed = extrasBundle.getBoolean("isAnalyzed");
			}
			if (extrasBundle.containsKey("enter_music")) {
				savedEnterMusicTemp = extrasBundle.getByteArray("enter_music");
			}
			if (extrasBundle.containsKey("pathMainActivity")) {
				//TODO pathMainActivity = extrasBundle.getString("pathMainActivity");
			}
		}
		/*
		if (fileExist(getFilesDir() + "/tempFile1")) {
			File file = new File(getFilesDir() + "/tempFile1");
			
			int size = (int) file.length();
			byte[] bytes = new byte[size];
			try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file))) {
				buf.read(bytes, 0, bytes.length);
				savedEnterMusicTemp = bytes;
				if (isAnalyzed) {
					cepstrumAll();
				}
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}*/
		
		logger.info("*****MainActivity onResume()*****");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		logger.info("*****START onDestroy()*****");
		if (currentData.getCurrentFile() != null) {
			File file = new File(currentData.getPath());
			file.delete();
		}
		
		logger.info("*****END onDestroy()*****");
	}
	
}
