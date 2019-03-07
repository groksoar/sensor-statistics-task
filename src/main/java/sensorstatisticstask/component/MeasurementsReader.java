package sensorstatisticstask.component;

import sensorstatisticstask.entity.SensorMeasurement;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

public interface MeasurementsReader extends Function<Path, Stream<SensorMeasurement>> {
}
