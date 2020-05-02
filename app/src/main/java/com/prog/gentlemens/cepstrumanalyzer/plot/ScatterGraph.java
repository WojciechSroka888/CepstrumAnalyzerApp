package com.prog.gentlemens.cepstrumanalyzer.plot;

import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
	private static final String DESCRIPTION_GRAPH = "Basic Frequency Line";
	private Logger logger = Logger.getLogger(ScatterGraph.class.getName());
	private ScatterChart scatterChart;
	private ScatterData scatterData;
	private ScatterDataSet scatterDataSet;
	private TextView legendTextView;
	private String label;
	private String descriptionUp;
	private Integer tempSelected;

	
	public ScatterGraph (){}
	
	public ScatterGraph (ScatterChart scatterChart, TextView legendTextView, String label){
		init(scatterChart, legendTextView, label);
	}
	
	public void init(ScatterChart scatterChart, TextView legendTextView, String label){
		this.scatterChart = scatterChart;
		this.legendTextView = legendTextView;
		this.label = label;
	}
	
	public void plotScatterGraph(int recordingTime, double jitter, double shimmer, double meanFrequency, double[] frequencies) {
		//labels - Oy, scatterdataset (entries)- Ox
		validateData(frequencies);
		
		List<Entry> entries = new ArrayList<>();
		createEntries(entries, frequencies);
		
		List<String> labels = new ArrayList<>();
		createLabels(labels, frequencies, recordingTime);
		//TODO create proper names convenction: legend, label, description, descriptionUp
		createLegend();
		
		scatterDataSet = new ScatterDataSet(entries, label);
		scatterChart.setDescription(DESCRIPTION_GRAPH);
		scatterChart.setDescriptionTextSize(17f);
		scatterDataSet.setScatterShapeSize(5);
		scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
		scatterDataSet.setColors(ColorTemplate.PASTEL_COLORS);
		scatterData = new ScatterData(labels, scatterDataSet);
		setDescriptionUp(jitter, shimmer, meanFrequency);
		setAxis();
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
	
	public Integer getTempSelected(){
		return tempSelected;
	}
	
	private void validateData(double[] frequencies){
		String message = null;
		
		if(scatterChart == null){
			message += " scatterChart can not be null";
		}
		if(legendTextView == null){
			message += " legendTextView can not be null";
		}
		if(label == null){
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
	
	private void createEntries(List<Entry> entries, double[] frequencies){
		for (int i = 0; i < frequencies.length; ++i) {
			entries.add(new Entry((float) frequencies[i], i));
		}
	}
	
	private void createLabels(List<String> labels, double[] frequencies, int recordingTime){
		for (int i = 0; i < frequencies.length; ++i) {
			labels.add(Integer.toString((int) (((double) 1 / frequencies.length) * i * recordingTime)));  //* 1000 -> ms
		}
	}
	
	private void createLegend(){
		Legend legend = scatterChart.getLegend();
		
		legend.setEnabled(false);
		legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
		legend.setTextSize(12f);
	}
	
	private void setDescriptionUp(Double jitter, Double shimmer, Double meanFrequency){
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		
		//TODO add type name to jitter and shimmers
		descriptionUp = "Jitter = " + decimalFormat.format(jitter) + " | " + //
		                "Shimmer = " + decimalFormat.format(shimmer) + " | " + //
		                "Mean frequency = " + decimalFormat.format(meanFrequency) + " [Hz]";
		legendTextView.setText(descriptionUp);
	}
	
	private void setAxis(){
		XAxis xAxis = scatterChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setTextSize(10f);
		xAxis.setTextColor(Color.BLACK);
		xAxis.setDrawAxisLine(true);
		xAxis.setDrawGridLines(true);
		
		YAxis left = scatterChart.getAxisLeft();
		left.setDrawLabels(true); // no axis labels
		left.setDrawAxisLine(true); // no axis line
		left.setDrawGridLines(true); // no grid lines
		scatterChart.getAxisRight().setEnabled(false); // no right axis
	}
	
}
