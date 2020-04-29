package com.prog.gentlemens.cepstrumanalyzer.plot;

import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ScatterGraph {
	private Logger logger = Logger.getLogger(ScatterGraph.class.getName());
	private ScatterChart scatterChart;
	private ScatterData scatterData;
	private ScatterDataSet scatterDataSet;
	private TextView legendTextView;
	private String legend;
	private String description;
	private Integer tempSelected;
	private double[] frequencies;
	private double jitter;
	private double shimmer;
	private double meanFrequency;
	private int recordingTime;
	
	public ScatterGraph (){}
	
	public ScatterGraph (ScatterChart scatterChart, TextView legendTextView, String legend, double[] frequencies, double jitter, double shimmer, double meanFrequency, int recordingTime, Integer tempSelected){
		init(scatterChart, legendTextView, legend, frequencies, jitter, shimmer, meanFrequency, recordingTime, tempSelected);
	}
	
	public void init(ScatterChart scatterChart, TextView legendTextView, String legend, double[] frequencies, double jitter, double shimmer, double meanFrequency, int recordingTime, Integer tempSelected){
		this.scatterChart = scatterChart;
		this.legendTextView = legendTextView;
		this.legend = legend;
		this.frequencies = frequencies;
		this.jitter = jitter;
		this.shimmer = shimmer;
		this.meanFrequency = meanFrequency;
		this.recordingTime = recordingTime;
		this.tempSelected = tempSelected;
	}
	
	public void plotScatterGraph() {
		validateData();
		List<Entry> entries = new ArrayList<>();
		createEntries(entries);
		
		List<String> labels = new ArrayList<>();
		createLabels(labels);
		//labels - Oy, scatterdataset (entries)- Ox
		
		scatterDataSet = new ScatterDataSet(entries, legend);
		scatterDataSet.setScatterShapeSize(5);
		scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
		scatterDataSet.setColors(ColorTemplate.PASTEL_COLORS);
		scatterData = new ScatterData(labels, scatterDataSet);
		setDescription();
		scatterChart.setHighlightEnabled(true);
		scatterChart.setHighlightIndicatorEnabled(true);
		scatterChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
			@Override
			public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
				logger.info("selected [x]: " + e.getXIndex());
				tempSelected = e.getXIndex();
			}
			
			@Override
			public void onNothingSelected() {
			}
		});
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				scatterChart.setData(scatterData);
				scatterChart.postInvalidate();
			}
		}).start();
	}
	
	private void validateData(){
		String message = null;
		
		if(scatterChart == null){
			message += " scatterChart can not be null";
		}
		if(legendTextView == null){
			message += " legendTextView can not be null";
		}
		if(legend == null){
			message += " legend can not be null";
		}
		if(frequencies == null){
			message += " frequencies can not be null";
		} else if(frequencies.length == 0){
			message += " frequencies length can not be zero";
		}
		if(message != null){
			throw new IllegalArgumentException(message);
		}
	}
	
	private void createEntries(List<Entry> entries){
		for (int i = 0; i < frequencies.length; ++i) {
			entries.add(new Entry((float) frequencies[i], i));
		}
	}
	
	private void createLabels(List<String> labels){
		for (int i = 0; i < frequencies.length; ++i) {
			labels.add(Integer.toString((int) (((double) 1 / frequencies.length) * i * recordingTime)));  //* 1000 -> ms
		}
	}
	
	private void setDescription(){
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		
		description = "Jitter = " + decimalFormat.format(jitter) + " | " + //
		                     "Shimmer = " + decimalFormat.format(shimmer) + " | " + //
		                     "Fmean = " + decimalFormat.format(meanFrequency) + " [Hz]";
		legendTextView.setText(description);
	}
	
}
