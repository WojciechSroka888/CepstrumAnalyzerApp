package com.prog.gentlemens.cepstrumanalyzer.message;

/**
 * Abstract class to extend by threads that send data to other thread in BLockingQueue concurrent system.
 *
 */
public abstract  class ThreadMessage {
	public abstract boolean streaming();
	
}
