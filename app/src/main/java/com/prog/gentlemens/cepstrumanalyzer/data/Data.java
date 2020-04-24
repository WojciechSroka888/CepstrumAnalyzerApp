package com.prog.gentlemens.cepstrumanalyzer.data;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

//TODO check: remove this class - use File and try - with - resources
// TODO handle exceptions
public class Data {
	
	private final String audioExtension = "_audio.pcm";
	private String name;
	private String absolutePath;
	private File currentFile;
	
	public void setName(String... information) {
		if (name == null) {
			name = "";
		}
		if (information != null) {
			for (String singleInformation : information) {
				name = name.concat(singleInformation + "_");
			}
		}
		name = name + (DateFormat.getDateTimeInstance().format(new Date())) + audioExtension;
	}
	
	public String getName() {
		return name;
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
			// exception and information
		}
		
		currentFile = new File(absolutePath, name);
		boolean result = false;
		
		try {
			result = currentFile.createNewFile();
		} catch(IOException e){
			// TODO handle exeption
		} finally {
			if(!result){
				// TODO throw exception
			}
		}
	}
	
	public File getCurrentFile(){
		return currentFile;
	}
	
}
