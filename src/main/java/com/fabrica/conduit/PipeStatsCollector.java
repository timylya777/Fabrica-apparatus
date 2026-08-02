package com.fabrica.conduit;

/**
 * Сборщик статистики сети (скорость передачи). Отвечает за усреднение
 * переданных за тик объёмов: каждые REFRESH_RATE тиков (20 = 1 секунда)
 * считает среднее арифметическое и запоминает его как актуальное значение.
 * Используется сетями (ElectricityNetwork, FluidNetwork) для показа
 * реальной скорости передачи в GUI (например, EU/тик или мВ/сек).
 */
public class PipeStatsCollector {
	// Частота обновления: пересчёт среднего раз в 20 тиков (1 секунда игры).
	private static final int REFRESH_RATE = 20;

	// Последнее вычисленное среднее значение за секунду (результат getValue()).
	private long lastStat = 0;
	// Накопленная сумма переданных объёмов за текущий период в 20 тиков.
	private long currentTot = 0;
	// Счётчик тиков внутри текущего периода.
	private int ticks = 0;

	// Добавляет объём, переданный сетью за один тик (newMoved).
	// Когда накоплено ровно REFRESH_RATE значений, вычисляет среднее
	// (currentTot / REFRESH_RATE) и сбрасывает счётчики для нового периода.
	public void addValue(long newMoved) {
		currentTot += newMoved;
		ticks++;

		if (ticks == REFRESH_RATE) {
			lastStat = currentTot / REFRESH_RATE;
			currentTot = 0;
			ticks = 0;
		}
	}

	// Возвращает последнее посчитанное среднее (мБ/тик или предметы и т.п.).
	public long getValue() {
		return lastStat;
	}
}
