package sensorstatisticstask.component;

import sensorstatisticstask.entity.StatisticsReport;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.IntSummaryStatistics;

public class StatisticsPrinterImpl implements StatisticsPrinter {

    private OutputStream outputStream;

    public StatisticsPrinterImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void accept(StatisticsReport statistics) {
        try (PrintWriter pw = new PrintWriter(outputStream)) {
            pw.printf("Num of processed files: %d %n", statistics.getNumOfProcessedFiles());
            pw.printf("Num of processed measurements: %d %n", statistics.getNumOfProcessedMeasurements());
            pw.printf("Num of failed measurements: %d %n", statistics.getNumOfFailedMeasurements());
            pw.println();
            pw.println("Sensors with highest avg humidity:");
            pw.println();
            pw.println("sensorMeasurement-id,min,avg,max");
            statistics.getStatistics().forEach((key, value) -> {
                if (value.isPresent()) {
                    IntSummaryStatistics stats = value.get();
                    pw.printf("%s,%d,%.0f,%d %n", key, stats.getMin(), stats.getAverage(), stats.getMax());
                } else {
                    pw.printf("%s,NaN,NaN,NaN %n", key);
                }
            });
        }
    }
}
