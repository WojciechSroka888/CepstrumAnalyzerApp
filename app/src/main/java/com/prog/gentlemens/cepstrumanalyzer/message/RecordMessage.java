package com.prog.gentlemens.cepstrumanalyzer.message;

public class RecordMessage extends ThreadMessage{
	private boolean isStreamingData;
	private short[] contentArray;
	
	public RecordMessage(boolean isStreamingData, short[] inputArray) {
		this.isStreamingData = isStreamingData;
		this.contentArray = inputArray;
	}
	
	@Override
	public boolean streaming() {
		return isStreamingData;
	}
	
	public short[] getContentArray() {
		return contentArray;
	}
	
}
