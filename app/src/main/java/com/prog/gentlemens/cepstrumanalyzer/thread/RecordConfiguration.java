package com.prog.gentlemens.cepstrumanalyzer.thread;

import android.media.AudioFormat;
import android.media.MediaRecorder;

public class RecordConfiguration {
    private int audioSource;
    private int sampleRateInHz;
    private int channelConfig;
    private int audioFormat;
    private int bufferElementsRec;
    private int bytesPerElement;

    public RecordConfiguration() {
        setDefault();
    }

    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getBufferElementsRec() {
        return bufferElementsRec;
    }

    public void setBufferElementsRec(int bufferElementsRec) {
        this.bufferElementsRec = bufferElementsRec;
    }

    public int getBytesPerElement() {
        return bytesPerElement;
    }

    public void setBytesPerElement(int bytesPerElement) {
        this.bytesPerElement = bytesPerElement;
    }

    private void setDefault() {
        this.audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        this.sampleRateInHz = 22050;
        this.channelConfig = AudioFormat.CHANNEL_IN_MONO;
        this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        this.bufferElementsRec = 512;
        this.bytesPerElement = 2;
    }

    public static final class RecordConfigurationBuilder {
        private int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        private int sampleRateInHz = 22050;
        private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        private int bufferElementsRec = 512;
        private static final int BYTES_PER_ELEMENT = 2;

        public RecordConfigurationBuilder withAudioSource(int audioSource) {
            this.audioSource = audioSource;
            return this;
        }

        public RecordConfigurationBuilder withSampleRateInHz(int sampleRateInHz) {
            this.sampleRateInHz = sampleRateInHz;
            return this;
        }

        public RecordConfigurationBuilder withChannelConfig(int channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        public RecordConfigurationBuilder withAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }

        public RecordConfigurationBuilder withBufferElementsRec(int bufferElementsRec) {
            this.bufferElementsRec = bufferElementsRec;
            return this;
        }

        public RecordConfiguration build() {
            RecordConfiguration recordConfiguration = new RecordConfiguration();
            recordConfiguration.setAudioSource(audioSource);
            recordConfiguration.setSampleRateInHz(sampleRateInHz);
            recordConfiguration.setChannelConfig(channelConfig);
            recordConfiguration.setAudioFormat(audioFormat);
            recordConfiguration.setBufferElementsRec(bufferElementsRec);
            recordConfiguration.setBytesPerElement(BYTES_PER_ELEMENT);
            return recordConfiguration;
        }
    }

}
