package com.prog.gentlemens.cepstrumanalyzer.thread;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

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
	
	public WriteThread (){
	}
	
	public WriteThread(BlockingQueue<RecordMessage> queueWithRecordThread){
		this.queueWithRecordThread = queueWithRecordThread;
	}
	
	public WriteThread(BlockingQueue<RecordMessage> queueWithRecordThread, Data currentData) {
		this.queueWithRecordThread = queueWithRecordThread;
		this.currentData = currentData;
	}
	
	//TODO validate this data
	public void init(Data currentData){
		this.currentData = currentData;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public void run() {
		try(FileOutputStream fileOutputStream = new FileOutputStream(currentData.getCurrentFile())) {
			while (queueWithRecordThread.take().streaming()) {
				fileOutputStream.write(shortToByteConversion(queueWithRecordThread
						.take()
						.getContentArray(), queueWithRecordThread
						.take()
						.getContentArray().length));
			}
		} catch (InterruptedException | FileNotFoundException e) {
			logger.warning(e.getMessage());
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			logger.warning(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}
	
	public Data getCurrentData(){
		return  currentData;
	}
	
}
