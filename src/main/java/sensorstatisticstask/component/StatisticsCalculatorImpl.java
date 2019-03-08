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

            List<Path> files = getCsvFiles(stream);

            Map<String, Boolean> failedSensorsTracker = new ConcurrentHashMap<>();
            LongAdder numOfMeasurements = new LongAdder();
            LongAdder numOfFailedMeasurements = new LongAdder();

            Map<String, IntSummaryStatistics> sensors = files.parallelStream()
                    .flatMap(measurementsReader)
                    .peek(sensorMeasurement -> numOfMeasurements.increment())
                    .peek(sensorMeasurement -> {
                        if (sensorMeasurement.isFailed()) {
                            numOfFailedMeasurements.increment();
                        }
                    })
                    .peek(sensorMeasurement -> failedSensorsTracker.compute(sensorMeasurement.getSensorId(),
                            (k, v) -> v == null ? sensorMeasurement.isFailed() : v && sensorMeasurement.isFailed()))
                    .filter(sensorMeasurement -> !sensorMeasurement.isFailed())
                    .collect(groupingBy(SensorMeasurement::getSensorId, summarizingInt(SensorMeasurement::getMeasurement)));

            Map<String, Optional<IntSummaryStatistics>> failedSensors = getFailedSensors(failedSensorsTracker);

            Map<String, Optional<IntSummaryStatistics>> sortedSensors = sortSensors(sensors);
            sortedSensors.putAll(failedSensors);

            return new StatisticsReport(files.size(), numOfMeasurements.sum(), numOfFailedMeasurements.sum(), sortedSensors);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Path> getCsvFiles(Stream<Path> stream) {
        return stream.filter(((Predicate<Path>) Files::isDirectory).negate())
                .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                .collect(Collectors.toList());
    }

    private Map<String, Optional<IntSummaryStatistics>> getFailedSensors(Map<String, Boolean> failedSensorsTracker) {
        return failedSensorsTracker.entrySet().stream()
                .filter(Entry::getValue)
                .map(Entry::getKey)
                .collect(Collectors.toMap(e -> e, e -> Optional.empty()));
    }

    private Map<String, Optional<IntSummaryStatistics>> sortSensors(Map<String, IntSummaryStatistics> statistics) {
        return statistics.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAverage(), e1.getValue().getAverage()))
                .collect(Collectors.toMap(Entry::getKey, e -> Optional.of(e.getValue()), (s1, s2) -> s1, LinkedHashMap::new));
    }

}
