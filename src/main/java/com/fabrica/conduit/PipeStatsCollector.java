package com.fabrica.conduit;

public class PipeStatsCollector {
	private static final int REFRESH_RATE = 20;

	private long lastStat = 0;
	private long currentTot = 0;
	private int ticks = 0;

	public void addValue(long newMoved) {
		currentTot += newMoved;
		ticks++;

		if (ticks == REFRESH_RATE) {
			lastStat = currentTot / REFRESH_RATE;
			currentTot = 0;
			ticks = 0;
		}
	}

	public long getValue() {
		return lastStat;
	}
}
