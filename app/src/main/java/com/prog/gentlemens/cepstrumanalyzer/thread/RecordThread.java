package com.prog.gentlemens.cepstrumanalyzer.thread;

import android.media.AudioRecord;

import com.prog.gentlemens.cepstrumanalyzer.message.RecordMessage;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class RecordThread implements Runnable {
	private BlockingQueue<RecordMessage> queueToSendRecordData;
	private RecordConfiguration recordConfiguration;
	private AudioRecord audioRecord;
	private short[] recordArray;
	private Logger logger = Logger.getLogger(RecordThread.class.getName());
	
	public RecordThread(BlockingQueue<RecordMessage> queueToSendRecordData, RecordConfiguration recordConfiguration) {
		this.queueToSendRecordData = queueToSendRecordData;
		this.recordConfiguration = recordConfiguration;
	}
	
	public RecordThread(BlockingQueue<RecordMessage> queueToSendRecordData) {
		this.queueToSendRecordData = queueToSendRecordData;
	}
	
	@Override
	public void run() {
		if (initialize()) {
			audioRecord.startRecording();
		}
		try {
			while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
				int numberShortsRead = audioRecord.read(recordArray, 0, recordConfiguration.getBufferElementsRec() * recordConfiguration.getBytesPerElement());
				if (numberShortsRead > 0) {
					queueToSendRecordData.put(new RecordMessage(true, recordArray));
				}
			}
			// inform that there is no more streaming
			queueToSendRecordData.put(new RecordMessage(false, null));
		} catch (InterruptedException e) {
			logger.warning(e.getMessage());
			stopRecording();
			Thread.currentThread().interrupt();
		}
	}
	
	public void stopRecording() {
		if (audioRecord != null) {
			logger.info(audioRecord.toString() + " has finished recording");
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
	}
	
	public void init(RecordConfiguration recordConfiguration){
		this.recordConfiguration = recordConfiguration;
	}
	
	public AudioRecord getAudioRecord() {
		return audioRecord;
	}
	
	private boolean initialize() {
		boolean isDefault = false;
		if (recordConfiguration == null) {
			recordConfiguration = new RecordConfiguration();
			isDefault = true;
		}
		recordArray = new short[recordConfiguration.getBufferElementsRec() * recordConfiguration.getBytesPerElement()];
		try {
			audioRecord = new AudioRecord(recordConfiguration.getAudioSource(), //
					recordConfiguration.getSampleRateInHz(), //
					recordConfiguration.getChannelConfig(), //
					recordConfiguration.getAudioFormat(), //
					recordConfiguration.getBufferElementsRec() * recordConfiguration.getBytesPerElement());
		}catch(IllegalArgumentException e){
			if(!isDefault){
				logger.warning(e.getMessage() + " -> unable to initialized AudioRecord, trying with default values");
				recordConfiguration = null;
				// try initialize with default values
				return initialize();
			}else{
				return false;
			}
		}
		if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED && !isDefault) {
			logger.warning("Unable to initialized AudioRecord, trying with default values");
			recordConfiguration = null;
			// try initialize with default values
			return initialize();
		} else {
			if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
				logger.warning("Unable to initialized AudioRecord");
				return false;
			}
		}
		return true;
	}
	
}
