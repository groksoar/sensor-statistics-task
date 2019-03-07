package sensorstatisticstask.component;

import sensorstatisticstask.entity.SensorMeasurement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class MeasurementsReaderImpl implements MeasurementsReader {

    @Override
    public Stream<SensorMeasurement> apply(Path path) {
        try {
            return Files.lines(path)
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> line.split(","))
                    .map(arr -> new SensorMeasurement(arr[0], getMeasurement(arr)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte getMeasurement(String[] arr) {
        if (arr.length != 2) {
            return -1;
        }
        try {
            return Byte.valueOf(arr[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
