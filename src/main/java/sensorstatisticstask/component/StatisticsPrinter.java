package sensorstatisticstask.component;

import sensorstatisticstask.entity.StatisticsReport;

import java.util.function.Consumer;

public interface StatisticsPrinter extends Consumer<StatisticsReport> {
}
