package sensorstatisticstask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import sensorstatisticstask.component.StatisticsCalculator;
import sensorstatisticstask.config.AppConfig;
import sensorstatisticstask.entity.StatisticsReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
public class SensorMeasurementStatisticsTaskApplicationTests {

    private Path tmpDir;

    @Autowired
    private StatisticsCalculator statisticsCalculator;

    @Before
    public void prepareFiles() throws IOException {
        tmpDir = Files.createTempDirectory("sensors");

        Path file1 = Files.createTempFile(tmpDir, "leader-1", ".csv");
        List<String> content1 = Stream.of("sensorMeasurement-id,humidity", "s1,10", "s2,88", "s1,NaN").collect(Collectors.toList());
        Files.write(file1, content1);

        Path file2 = Files.createTempFile(tmpDir, "leader-2", ".csv");
        List<String> content2 = Stream.of("sensorMeasurement-id,humidity", "s2,80", "s3,NaN", "s2,78", "s1,98").collect(Collectors.toList());
        Files.write(file2, content2);
    }

    @Test
    public void statisticsCalculatorTest() {
        StatisticsReport report = statisticsCalculator.apply(tmpDir);

        assertEquals(2, report.getNumOfProcessedFiles());
        assertEquals(7, report.getNumOfProcessedMeasurements());
        assertEquals(2, report.getNumOfFailedMeasurements());

        assertEquals(3, report.getStatistics().size());
        assertEquals(1, report.getFailedSensors().size());

        assertEquals("s2", report.getStatistics().keySet().stream().findFirst().get());
        assertEquals("s3", report.getStatistics().keySet().stream().skip(report.getStatistics().size() - 1).findFirst().get());
    }

    @After
    public void deleteFiles() throws IOException {
        FileSystemUtils.deleteRecursively(tmpDir);
    }

}
