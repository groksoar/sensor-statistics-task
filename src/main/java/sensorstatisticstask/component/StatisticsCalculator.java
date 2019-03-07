package sensorstatisticstask.component;

import sensorstatisticstask.entity.StatisticsReport;

import java.nio.file.Path;
import java.util.function.Function;

public interface StatisticsCalculator extends Function<Path, StatisticsReport> {
}
