package com.prog.gentlemens.cepstrumanalyzer.data;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

public class Data implements Serializable {
	private static final Logger logger = Logger.getLogger(Data.class.getName());
	private static final String AUDIO_EXTENSION = "_audio.pcm";
	private String name;
	private String absolutePath;
	private File currentFile;
	private long duration;
	
	public void setName(String... information) {
		if (name == null) {
			name = "";
		}
		if (information != null) {
			for (String singleInformation : information) {
				name = name.concat(singleInformation + "_");
			}
		}
		name = name + (DateFormat.getDateTimeInstance().format(new Date())) + AUDIO_EXTENSION;
	}
	
	public void setName(String fullPath) {
		name = fullPath;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDuration(long duration) {
		if(duration <= 0){
			throw new IllegalArgumentException("duration can not be less then zero");
		}
		this.duration = duration;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setPath(String absolutePath) {
		if (absolutePath != null) {
			this.absolutePath = absolutePath;
		}
	}
	
	public String getPath() {
		return absolutePath;
	}
	
	public void createNewFile() {
		if (name == null || absolutePath == null) {
			throw new IllegalArgumentException("name and absolutePath can not be null");
		}
		
		currentFile = new File(absolutePath, name);
		boolean result = false;
		
		try {
			result = currentFile.createNewFile();
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		if (!result) {
			throw new IllegalArgumentException("file name already exists");
		}
	}
	
	public File getCurrentFile() {
		return currentFile;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public byte[] returnByteData() {
		byte[] resultByteArray = new byte[(int) getCurrentFile().length()];
		
		try (InputStream inputStream = new FileInputStream(getCurrentFile())) {
			if (inputStream.read(resultByteArray) == -1) {
				return resultByteArray;
			}
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		return resultByteArray;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Data data = (Data) o;
		return Objects.equals(name, data.name) && Objects.equals(absolutePath, data.absolutePath) && Objects
				.equals(currentFile, data.currentFile);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public int hashCode() {
		return Objects.hash(name, absolutePath, currentFile);
	}
	
}
