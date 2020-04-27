package com.prog.gentlemens.cepstrumanalyzer;

import android.content.Intent;
import android.graphics.Color;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.prog.gentlemens.cepstrumanalyzer.math.FFT;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.arithmeticFrequencyAverage;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.byteToShortConversion;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.calculateBlackmannWindow;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.round;

public class ResultActivity extends AppCompatActivity {
	
	private Logger logger = Logger.getLogger(ResultActivity.class.getName());
	private Button playButton;
	private Button analyzeButton;
	private Button selectAButton;
	private Button selectBButton;
	private Button backButton;
	
	private TextView legendTextView;
	private TextView streamTextView;
	
	private boolean startPlaying = true;
	
	private AudioTrack audioTrack = null;
	
	private int selectedA = -1;
	private int selectedB = -1;
	private int tempSelected = -1;
	
	private double[] maksy;
	private double[] shim;
	private int N;
	private double JT;
	private double SH;
	private double fMean;
	
	private String pathMainActivity = null;     //string psth for this activity
	private boolean newOrOld;                   //showing if it is new recording
	
	private boolean isAnalyzed = false;
	private byte[] savedEnterMusicTemp = null;
	
	private String descriptionGraph = "Basic Frequency Line";
	
	private ScatterChart scatterchart = null;
	private ScatterDataSet scatterdataset = null;
	private ScatterData scatterdata = null;
	private String description = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		newOrOld = mainWelcome();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		scatterchart = findViewById(R.id.scatter_chart);
		
		XAxis xAxis = scatterchart.getXAxis();
		xAxis.setPosition(XAxisPosition.BOTTOM);
		xAxis.setTextSize(10f);
		xAxis.setTextColor(Color.BLACK);
		xAxis.setDrawAxisLine(true);
		xAxis.setDrawGridLines(true);
		
		YAxis left = scatterchart.getAxisLeft();
		left.setDrawLabels(true); // no axis labels
		left.setDrawAxisLine(true); // no axis line
		left.setDrawGridLines(true); // no grid lines
		scatterchart.getAxisRight().setEnabled(false); // no right axis
		
		scatterchart.setDescription(descriptionGraph);
		scatterchart.setDescriptionTextSize(17f);
		//scatterchart.setDescriptionPosition(690f, 50f);
		
		Legend legend = scatterchart.getLegend();
		
		legend.setEnabled(false);
		legend.setPosition(LegendPosition.RIGHT_OF_CHART_INSIDE);
		legend.setTextSize(12f);
		
		scatterchart.setData(scatterdata);
		scatterchart.invalidate();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		legendTextView = findViewById(R.id.legend_text_view);
		streamTextView = findViewById(R.id.stream_text_view);
		backButton = findViewById(R.id.back_button);
		playButton = findViewById(R.id.play_button);
		analyzeButton = findViewById(R.id.analyze_button);
		selectAButton = findViewById(R.id.a_button);
		selectBButton = findViewById(R.id.b_button);
		
		setAnalyzeButton();
		setBackButton();
		setPlayButton();
		setSelectAButton();
		setSelectBButton();
	}
	
	private void setBackButton() {
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DataActivity.class);
				intent.putExtra("pathMainActivity", pathMainActivity);
				intent.putExtra("comeBack", true);
				
				startActivity(intent);
			}
		});
	}
	
	private void setSelectAButton() {
		selectAButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedA = tempSelected;
				System.out.print("*****SelectedA = ");
				System.out.println(selectedA);
				
				if (isAnalyzed == false) {
					selectAButton.setText("analyze first");
				} else {
					if (isAnalyzed == true) {
						selectAButton.setText("select left");
						
						if (selectedA >= 0) {
							selectAButton.setText(Integer.toString(selectedA));
						}
					}
				}
			}
		});
	}
	
	private void setSelectBButton() {
		selectBButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedB = tempSelected;
				
				if (!isAnalyzed) {
					selectBButton.setText("analyze first");
				} else {
					if (isAnalyzed) {
						selectBButton.setText("select right");
						
						if (selectedB >= 0) {
							selectBButton.setText("" + selectedB);
						}
					}
				}
			}
		});
	}
	
	private void setPlayButton() {
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPlay(startPlaying);
				startPlaying = !startPlaying;
			}
		});
	}
	
	private void setAnalyzeButton() {
		analyzeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLineTestFunction();
				startAnalyzing();
			}
		});
	}
	
	private boolean mainWelcome() {
		//new recording, new data, temp always has sth from -> FaceActivity
		if (getIntent().getStringExtra("pathFaceActivity") != null) {
			if (pathMainActivity.equals(getIntent().getStringExtra("pathFaceActivity"))) {
				return false;        //old
			} else {
				pathMainActivity = getIntent().getStringExtra("pathFaceActivity");
				return true;       //new
			}
			//if different - can be new or first recording or new or new different recording
			//if the same -> back
		}
		return false;
	}
	
	private boolean fileExist(String pathCheck) {
		File fileCheck = new File(pathCheck);
		return fileCheck.exists();
	}
	
	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}
	
	private void startPlaying() {
		//check if there is loaded audio file - from onSavedInstantState()
		final byte enter_music[];
		
		if (fileExist(pathMainActivity))         //old
		{
			enter_music = readDataFromFile();
		} else {
			enter_music = savedEnterMusicTemp;  //from saved temp, after inverting
		}
		//final byte [] enter_music = readDataFromFile();
		
		int minBufferSize = AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
		audioTrack.play();
		
		new Thread(new Runnable() {
			public void run() {
				System.out.println("start playing");
				audioTrack.write(enter_music, 0, enter_music.length);
			}
		}).start();
		
		playButton.setText("Stop");
	}
	
	private byte[] readDataFromFile() {
		File enterFile = new File(pathMainActivity);
		
		byte[] enterMusic = new byte[(int) enterFile.length()];
		
		try {
			InputStream enter_stream = new FileInputStream(enterFile);
			enter_stream.read(enterMusic);
			enter_stream.close();
		} catch (Exception e) {
			System.out.println("FILE NOT FOUND");
		}
		return enterMusic;
	}
	
	private void stopPlaying() {
		if (audioTrack != null) {
			audioTrack.release();
			audioTrack = null;
			System.out.println("stop playing!!");
			
			// mStartPlaying = false;
			playButton.setText("Play");
		}
	}
	
	private void startAnalyzing() {
		logger.info("*****startAnalyzing()*******");
		
		if ((selectedA < 0) && (selectedB < 0)) {
			cepstrumAll();
		} else {
			logger.info("*****startAnalyzing() --> again()*******");
			cepstrumPartly();
		}
	}
	
	private void cepstrumAll() {
		byte[] dane;
		
		if (fileExist(pathMainActivity)) {
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
		System.out.println("*****AFTER FFT***** ");
		
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
		ScatterGraph(maksy, 22050, JT, SH, fMean, "Basic Frequency Line");
		
		//*******************************************************************
		logger.info("*****END runFFT()*****");
	}
	
	private void cepstrumPartly() {
		logger.info("*****START cepstrumPartly()*****");
		if (selectedA < 0) {
			selectedA = 0;
		}
		
		if (selectedB < 0) {
			selectedB = maksy.length;
		}
		
		if (selectedA == selectedB) {
			selectedA = 0;
			selectedB = maksy.length;
		} else {
			if (selectedA > selectedB) {
				int temp = selectedA;
				
				selectedA = selectedB;
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
		
		ScatterGraph(maksy, 22050, JT, SH, fMean, "Basic Frequency Line");
		
		logger.info("*****END cepstrumPartly()*****");
	}
	
	private double[] cepstrumStreamPart(short[] part) {
		int n = part.length;         //2 * 512 size of frame
		int fs = 22050;                                        //sampling frequency
		
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
		
		double temp_max;                        //for maximum searching
		double fhigh = 400;
		double fmin = 40;
		int fs = 22050;
		double tablOut[] = {0, 0};
		//tablOut[0] -> maksy - fmax
		//tablOut[1] -> shim -  fmax value
		
		int start = round(fs / fhigh);        //providing start from appropraite index
		int koniec = round(fs / fmin);
		int index = 0;                            //number of maximum array
		
		temp_max = tablIn[start];
		
		for (int j = start; j < koniec; ++j) {
			if (temp_max < tablIn[j + 1]) {
				temp_max = tablIn[j + 1];
				index = j + 1;
			}
		}
		
		if (index != 0) {
			tablOut[0] = fs / index;        // 1 / ( index * 1 / ( fs ) )
			tablOut[1] = tablIn[index];        //shim[i] = temp_max;
		} else {
			if (index == 0) {
				tablOut[0] = 0;
				tablOut[1] = 0;
			}
		}
		
		logger.info("*****END fMeanPart()*****");
		
		return tablOut;
	}
	
	private void cepstrumStreamSum(short[] part) {
		//suma fmean, obliczenie jitter i shimmer
		//oblicz pojedyńcze cepstrum
		//może w nowym wątku ? -> zamiast
		
		//************************przed streamowaniem lub zmienna globalna**************************
		
		double frequencyMean = 0;
		double jitter = 0;
		double shimmer = 0;
		
		//*****************************przed streamowaniem******************************************
		
		int counter = 0;
		
		//***************************w miejscu streamowania audio **********************************
		
		double frequencyMaxTemp;
		double frequencyMaxAmplitudeTemp;
		double tablTemp[] = {0, 0};
		
		tablTemp = frequencyMaxStreamPart(cepstrumStreamPart(part));
		frequencyMaxTemp = tablTemp[0];
		frequencyMaxAmplitudeTemp = tablTemp[1];
		
		frequencyMean = (frequencyMean + frequencyMaxAmplitudeTemp) / counter;
		counter = counter + 1;
		
		//******************obsługa graficzna********************************
		
	}
	
	private void onLineTestFunction() {
		byte[] dane;
		
		if (fileExist(pathMainActivity)) {
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
		
		short sendTemp[] = new short[n];
		
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
	
	public void ScatterGraph(double[] tab_x, int fs, double jt, double sh, double fmean, String legenda) {
		logger.info("*****START ScatterGraph()*****");
		
		ArrayList<Entry> entries = new ArrayList<Entry>();
		
		for (int i = 0; i < tab_x.length; ++i) {
			entries.add(new Entry((float) tab_x[i], i));
		}
		
		scatterdataset = new ScatterDataSet(entries, legenda);        //legenda - dół
		scatterdataset.setScatterShapeSize(5);
		scatterdataset.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
		scatterdataset.setColors(ColorTemplate.PASTEL_COLORS);
		
		ArrayList<String> labels = new ArrayList<String>();
		
		int tempInt = getIntent().getIntExtra("recordingTime", 5000);
		//calculating of recording degree PL:"przelicza skalę trwania nagrania"
		for (int i = 0; i < tab_x.length; ++i) {
			labels.add(Integer.toString((int) (((double) 1 / tab_x.length) * i * tempInt)));  //* 1000 -> ms
		}
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
		//System.getProperty("line.separator");
		description = "Jitter = " + df.format(jt) + " | " + "Shimmer = " + df.format(sh) + " | " + "Fmean = " + df
				.format(fmean) + " [Hz]";
		legendTextView.setText(description);
		
		logger.info("*****before SCATTERDATA*****");
		
		scatterdata = new ScatterData(labels, scatterdataset);
		//labels - Oy, scatterdataset (entries)- Ox
		
		logger.info("*****chart -> new Runnable()*****");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//scatterchart.setDescription( opis );
				scatterchart.setData(scatterdata);
				scatterchart.invalidate();
			}
		});
		logger.info("*****END Runnable()*****");
		
		isAnalyzed = true;      //to service buttona and buttonB
		
		scatterchart.setHighlightEnabled(true);
		scatterchart.setHighlightIndicatorEnabled(true);
		scatterchart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
			@Override
			public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
				logger.info("selected [x]: " + e.getXIndex());
				tempSelected = e.getXIndex();
				//scatterchart.setHighlightColor(Color.BLACK);
			}
			
			@Override
			public void onNothingSelected() {
				logger.info("onNothingSelected()");
			}
		});
		
		logger.info("*****END ScatterGraph()*****");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public void onPause() {
		logger.info("*****MainActivity onPause()*****");
		super.onPause();
		
		try (FileOutputStream fos = openFileOutput("tempFile1", MODE_PRIVATE)) {
			if (fileExist(pathMainActivity)) {
				fos.write(readDataFromFile());
			} else {
				fos.write(savedEnterMusicTemp);
			}
		}  catch (IOException e) {
			logger.warning(e.getMessage());
		}
		
		Bundle bundle = new Bundle();
		bundle.putBoolean("isAnalyzed", isAnalyzed);
		bundle.putString("pathMainActivity", pathMainActivity);
		
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
				pathMainActivity = extrasBundle.getString("pathMainActivity");
			}
		}
		
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
		}
		
		logger.info("*****MainActivity onResume()*****");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		logger.info("*****START onDestroy()*****");
		if (fileExist(pathMainActivity)) {
			File file = new File(pathMainActivity);
			file.delete();
		}
		
		logger.info("*****END onDestroy()*****");
	}
}
//******************CODE UNDERLINE IS IMPORTANT FOR CODE IMPROVEMENT********************************
/*
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        System.out.println( "*****MainActivity onSaveInstanceState()***** " );

        if(fileExist(pathMainActivity) == true)
        {
            savedInstanceState.putByteArray("enter_music", readDataFromFile());
        }

        else
        {
            savedInstanceState.putByteArray("enter_music", savedEnterMusicTemp);
        }

        savedInstanceState.putBoolean("isAnalyzed", isAnalyzed);
        savedInstanceState.putString("pathMainActivity", pathMainActivity);

        System.out.println( "*****END MainActivity onSaveInstanceState()***** " );

        /*
        try {
                FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
         */
// }
/*
    public void onRestoreInstanceState(Bundle savedInstantState) {
        super.onRestoreInstanceState(savedInstantState);

        System.out.println( "*****MainActivity onRestoreInstanceState()***** " );

        isAnalyzed = savedInstantState.getBoolean("isAnalyzed");
        savedEnterMusicTemp = savedInstantState.getByteArray("enter_music");
        pathMainActivity = savedInstantState.getString("pathMainActivity");

        if(isAnalyzed == true) {
            cepstrumAll();
        }

        System.out.println("*****END MainActivity onRestoreInstanceState()***** ");
        //w przyszłości lepiej to zamienić na onStop() / onPause() / onStart()
    }
*/
