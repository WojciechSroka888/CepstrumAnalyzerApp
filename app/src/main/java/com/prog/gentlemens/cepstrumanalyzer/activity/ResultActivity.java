package com.prog.gentlemens.cepstrumanalyzer.activity;

import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.arithmeticFrequencyAverage;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.byteToShortConversion;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.calculateBlackmannWindow;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.calculateJitter;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.calculateShimmer;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.round;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.prog.gentlemens.cepstrumanalyzer.R;
import com.prog.gentlemens.cepstrumanalyzer.data.Data;
import com.prog.gentlemens.cepstrumanalyzer.math.FFT;
import com.prog.gentlemens.cepstrumanalyzer.media.PlayMedia;
import com.prog.gentlemens.cepstrumanalyzer.plot.ScatterGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class ResultActivity extends AppCompatActivity {
    private Logger logger = Logger.getLogger(ResultActivity.class.getName());
    private Data currentData;
    private Button playButton;
    private Button analyzeButton;
    private Button selectAButton;
    private Button selectBButton;
    private Button backButton;

    private TextView legendTextView;
    private PlayMedia playMedia;
    private ScatterChart scatterChart;
    private ScatterGraph scatterGraph;

    private Integer selectedA;
    private Integer selectedB;
    private double[] frequencies;
    private double[] amplitudes;
    private boolean isAnalyzed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        legendTextView = findViewById(R.id.legend_text_view);
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
        playMedia = new PlayMedia(this);
        scatterGraph = new ScatterGraph(scatterChart,//
                legendTextView, //
                "Basic Frequency Line");
    }

    private void setBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DataActivity.class);
                intent.putExtra("currentData", currentData);
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
                if (playMedia.isPlaying()) {
                    stopPlaying();
                } else {
                    startPlaying();
                }
            }
        });
    }

    private void setAnalyzeButton() {
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                startAnalyzing();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startPlaying() {
        playButton.setText("stop");
        playMedia.startPlaying(currentData.returnByteData());
    }

    private void stopPlaying() {
        playButton.setText("play");
        playMedia.stopPlaying();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startAnalyzing() {
        logger.info("*****startAnalyzing()*******");

        if ((selectedA == null) && (selectedB == null)) {
            cepstrumAll();
            isAnalyzed = true;
        } else {
            logger.info("*****startAnalyzing() --> again()*******");
            cepstrumPartly();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void cepstrumAll() {
        byte[] byteData = currentData.returnByteData();
        short[] shortData = byteToShortConversion(byteData);

        final int d = 64;                                            //step shift window
        final int n = 2 * 512;                                    //size of frame
        final int N = round((shortData.length - n) / d);            //number of frames
        final int fs = 22050;                                        //sampling frequency

        double[][] tabl = new double[N][n];
        double[] temp = new double[n];
        double[] blackmannWindow = calculateBlackmannWindow(n);

        logger.info("*****FFT*****");
        for (int i = 0; i < N; ++i) {
            //part of the window
            for (int j = 0; j < n; ++j) {
                temp[j] = shortData[j + i * d] * blackmannWindow[j];
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

        frequencies = new double[N];
        amplitudes = new double[N];

        findMax(frequencies, amplitudes, tabl, N, fs);

        scatterGraph.plotScatterGraph(currentData.getDuration(),//
                calculateJitter(frequencies),//
                calculateShimmer(amplitudes),//
                arithmeticFrequencyAverage(frequencies), //
                frequencies);
    }

    private void cepstrumPartly() {
        setSelectedValues();

        double[] partOfFrequencies = new double[selectedB - selectedA];
        double[] partOfAmplitudes = new double[selectedB - selectedA];

        for (int i = 0; i < selectedB - selectedA; ++i) {
            partOfFrequencies[i] = this.frequencies[i + selectedA];
            partOfAmplitudes[i] = this.amplitudes[i + selectedA];
        }

        scatterGraph.plotScatterGraph(currentData.getDuration(),//
                calculateJitter(partOfFrequencies),//
                calculateShimmer(partOfAmplitudes),//
                arithmeticFrequencyAverage(partOfFrequencies),//
                this.frequencies);
    }

    private void setSelectedValues() {
        if (selectedA == null) {
            selectedA = 0;
        }
        if (selectedB == null) {
            selectedB = frequencies.length;
        }
        if (selectedA.equals(selectedB)) {
            selectedA = 0;
            selectedB = frequencies.length;
        } else {
            if (selectedA > selectedB) {
                int temp = selectedA;
                selectedA = selectedB;
                selectedB = temp;
            }
        }
    }

    private void findMax(double[] frequencies, double[] amplitudes, double[][] tabl, int N, final int samplingFrequency) {
        double tempMax;                    //looking for maximum
        final double maxFrequency = 400;
        final double minFrequency = 40;

        int start = round(samplingFrequency / maxFrequency);        //appropriate starting index
        int end = round(samplingFrequency / minFrequency);
        int index;                                 //number of maximum array

        for (int i = 0; i < N; ++i) {
            index = 0;                        //when only 0 instead of index = start

            tempMax = tabl[i][start];

            for (int j = start; j < end; ++j) {
                if (tempMax < tabl[i][j + 1]) {
                    tempMax = tabl[i][j + 1];
                    index = j + 1;
                }
            }

            if (index != 0) {
                frequencies[i] = samplingFrequency / index;        // 1 / ( index * 1 / ( samplingFrequency ) )
                amplitudes[i] = tabl[i][index];    //amplitudes[i] = tempMax;
            } else {
                if (index == 0) {
                    frequencies[i] = 0;
                    amplitudes[i] = 0;
                }
            }

            logger.info(index + "-" + frequencies[i] + "-" + tempMax);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Bundle extrasBundle = intent.getExtras();

        if (extrasBundle != null && !extrasBundle.isEmpty()) {
            if (extrasBundle.containsKey("currentData")) {
                currentData = (Data) extrasBundle.getSerializable("currentData");
            }
            if (extrasBundle.containsKey("isAnalyzed")) {
                isAnalyzed = extrasBundle.getBoolean("isAnalyzed");
                if (isAnalyzed) {
                    startAnalyzing();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onPause() {
        super.onPause();

        Bundle bundle = new Bundle();
        bundle.putBoolean("isAnalyzed", isAnalyzed);
        bundle.putSerializable("currentData", currentData);

        Intent intent = getIntent();
        intent.putExtras(bundle);
    }

    @Override
    public void onStop() {
        super.onStop();

        Bundle bundle = new Bundle();
        bundle.putBoolean("isAnalyzed", isAnalyzed);
        bundle.putSerializable("currentData", currentData);

        Intent intent = getIntent();
        intent.putExtras(bundle);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing() && currentData != null && currentData.getCurrentFile() != null) {
            try {
                Files.delete(currentData.getCurrentFile().toPath());
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }

}
