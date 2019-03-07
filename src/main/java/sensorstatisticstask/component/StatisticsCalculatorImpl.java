package sensorstatisticstask.component;

import sensorstatisticstask.entity.SensorMeasurement;
import sensorstatisticstask.entity.StatisticsReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            return new StatisticsReport(files.size(), numOfMeasurements.sum(), numOfFailedMeasurements.sum(), map);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
