package sensorstatisticstask.component;

import sensorstatisticstask.entity.SensorMeasurement;
import sensorstatisticstask.entity.StatisticsReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingInt;

public class StatisticsCalculatorImpl implements StatisticsCalculator {

    private MeasurementsReader measurementsReader;

    public StatisticsCalculatorImpl(MeasurementsReader measurementsReader) {
        this.measurementsReader = measurementsReader;
    }

    @Override
    public StatisticsReport apply(Path pathToDirectory) {
        try (Stream<Path> stream = Files.walk(pathToDirectory)) {

            List<Path> files = stream.filter(((Predicate<Path>) Files::isDirectory).negate())
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .collect(Collectors.toList());

            Map<String, Boolean> failedSensorsTracker = new ConcurrentHashMap<>();
            LongAdder numOfMeasurements = new LongAdder();
            LongAdder numOfFailedMeasurements = new LongAdder();

            Map<String, IntSummaryStatistics> map = files.parallelStream()
                    .flatMap(measurementsReader)
                    .peek(sensorMeasurement -> numOfMeasurements.increment())
                    .peek(sensorMeasurement -> {
                        if (sensorMeasurement.isFailed()) {
                            numOfFailedMeasurements.increment();
                        }
                    })
                    .peek(sensorMeasurement -> failedSensorsTracker.compute(sensorMeasurement.getSensorId(),
                            (k, v) -> v == null ? sensorMeasurement.isFailed() : v && sensorMeasurement.isFailed()))
                    .collect(groupingBy(SensorMeasurement::getSensorId, summarizingInt(SensorMeasurement::getAdjustedMeasurement)));

            Set<String> failedSensors = failedSensorsTracker.entrySet().stream()
                    .filter(Entry::getValue)
                    .map(Entry::getKey)
                    .collect(Collectors.toSet());

            map = sortStatistics(map, failedSensors);

            return new StatisticsReport(files.size(), numOfMeasurements.sum(), numOfFailedMeasurements.sum(), map, failedSensors);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, IntSummaryStatistics> sortStatistics(Map<String, IntSummaryStatistics> statistics, Set<String> failedSensors) {
        return statistics.entrySet().stream()
                .sorted((e1, e2) -> {
                    double d1 = failedSensors.contains(e1.getKey()) ? Double.MIN_VALUE : e1.getValue().getAverage();
                    double d2 = failedSensors.contains(e2.getKey()) ? Double.MIN_VALUE : e2.getValue().getAverage();
                    return Double.compare(d2, d1);
                })
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (s1, s2) -> s1, LinkedHashMap::new));
    }

}
