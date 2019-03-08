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
import sensorstatisticstask.component.StatisticsPrinter;
import sensorstatisticstask.component.StatisticsPrinterImpl;
import sensorstatisticstask.config.AppConfig;
import sensorstatisticstask.entity.StatisticsReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

        assertEquals("s2", report.getStatistics().keySet().stream().findFirst().get());
        assertEquals("s3", report.getStatistics().keySet().stream().skip(report.getStatistics().size() - 1).findFirst().get());
    }

    @Test
    public void statisticsPrinterTest() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StatisticsPrinter printer = new StatisticsPrinterImpl(baos);

        IntSummaryStatistics s3Stats= new IntSummaryStatistics();
        s3Stats.accept(1);
        s3Stats.accept(3);

        Map<String, Optional<IntSummaryStatistics>> stats = new LinkedHashMap<>();
        stats.put("s3", Optional.of(s3Stats));
        stats.put("s1", Optional.empty());
        stats.put("s5", Optional.empty());

        printer.accept(new StatisticsReport(2,4, 2, stats));

        String expected = String.format("Num of processed files: 2 %n" +
                "Num of processed measurements: 4 %n" +
                "Num of failed measurements: 2 %n%n" +
                "Sensors with highest avg humidity:%n%n" +
                "sensorMeasurement-id,min,avg,max%n" +
                "s3,1,2,3 %n" +
                "s1,NaN,NaN,NaN %n" +
                "s5,NaN,NaN,NaN %n");

        assertEquals(expected, new String(baos.toByteArray()));
    }

    @After
    public void deleteFiles() throws IOException {
        FileSystemUtils.deleteRecursively(tmpDir);
    }

}
