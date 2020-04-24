package com.prog.gentlemens.cepstrumanalyzer.thread;

import com.prog.gentlemens.cepstrumanalyzer.data.Data;
import com.prog.gentlemens.cepstrumanalyzer.message.RecordMessage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.shortToByteConversion;

public class WriteThread implements Runnable {
	private BlockingQueue<RecordMessage> queueWithRecordThread;
	private Logger logger = Logger.getLogger(WriteThread.class.getName());
	private Data currentData;
	private FileOutputStream fileOutputStream;
	
	public WriteThread(BlockingQueue<RecordMessage> queueWithRecordThread, Data currentData) {
		this.queueWithRecordThread = queueWithRecordThread;
		this.currentData = currentData;
	}
	
	@Override
	public void run() {
		setFileOutputStream();
		try {
			while (true) {
				if (queueWithRecordThread.take().streaming()) {
					fileOutputStream.write(shortToByteConversion(queueWithRecordThread.take().getContentArray(), queueWithRecordThread.take().getContentArray().length
));
				}
			}
		} catch (InterruptedException | IOException e) {
			logger.warning(e.getMessage());
			Thread.currentThread().interrupt();
		} finally {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}
	}
	
	private void setFileOutputStream() {
		try {
			fileOutputStream = new FileOutputStream(currentData.getCurrentFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
