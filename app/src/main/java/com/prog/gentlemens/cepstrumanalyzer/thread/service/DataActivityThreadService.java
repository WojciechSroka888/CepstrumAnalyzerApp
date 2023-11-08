package com.prog.gentlemens.cepstrumanalyzer.thread.service;

import android.media.AudioRecord;

import com.prog.gentlemens.cepstrumanalyzer.data.Data;
import com.prog.gentlemens.cepstrumanalyzer.message.RecordMessage;
import com.prog.gentlemens.cepstrumanalyzer.thread.RecordConfiguration;
import com.prog.gentlemens.cepstrumanalyzer.thread.RecordThread;
import com.prog.gentlemens.cepstrumanalyzer.thread.WriteThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class DataActivityThreadService {

    private static DataActivityThreadService dataActivityThreadServiceInstance;
    private Logger logger = Logger.getLogger(DataActivityThreadService.class.getName());
    private BlockingQueue<RecordMessage> queueWithRecordThread = new LinkedBlockingQueue<>();
    private RecordThread recordThread;
    private Thread threadForRecordThread;
    private WriteThread writeThread;
    private Thread threadForWriteThread;

    private DataActivityThreadService() {
    }

    public static DataActivityThreadService getInstance() {
        if (dataActivityThreadServiceInstance == null) {
            dataActivityThreadServiceInstance = new DataActivityThreadService();
        }
        return dataActivityThreadServiceInstance;
    }

    public void start() {
        if (threadForRecordThread == null || !threadForRecordThread.isAlive()) {
            initRecordThread();
            threadForRecordThread = new Thread(recordThread, "RecordThread");
            threadForRecordThread.setPriority(7);
            threadForRecordThread.start();
        }
        if (threadForWriteThread == null || !threadForWriteThread.isAlive()) {
            initWriteThread();
            threadForWriteThread = new Thread(writeThread, "WriteThread");
            threadForWriteThread.setPriority(7);
            threadForWriteThread.start();
        }
    }

    public void initRecordThread() {
        if (recordThread == null) {
            recordThread = new RecordThread(queueWithRecordThread);
        }
    }

    public void initRecordThread(RecordConfiguration recordConfiguration) {
        if (recordThread != null) {
            logger.info("the recordThread is already initialized -> updating this recordThread configuration");
            recordThread.init(recordConfiguration);
        } else {
            recordThread = new RecordThread(queueWithRecordThread, recordConfiguration);
        }
    }

    public void initWriteThread() {
        if (writeThread == null) {
            writeThread = new WriteThread(queueWithRecordThread);
        }
    }

    public void initWriteThread(Data currentData) {
        if (writeThread != null) {
            logger.info("the writeThread is already initialized -> updating this writeThread configuration");
            writeThread.init(currentData);
        } else {
            writeThread = new WriteThread(queueWithRecordThread, currentData);
        }
    }

    public void stopRecording() {
        if (recordThread != null) {
            recordThread.stopRecording();
        }
    }

    public boolean getAudioRecordState() {
        if (recordThread != null) {
            if (recordThread.getAudioRecord() != null) {
                if (recordThread.getAudioRecord()
                        .getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    return true;
                }
            } else {
                logger.info("the audioRecord is not initialized");
            }
        } else {
            logger.info("the recordThread is not initialized");
        }
        return false;
    }

    public Data getCurrentData() {
        if (writeThread != null) {
            return writeThread.getCurrentData();
        } else {
            logger.info("the writeThread is not initialized");
        }
        return null;
    }

}
