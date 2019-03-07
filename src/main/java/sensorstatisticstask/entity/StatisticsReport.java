package sensorstatisticstask.entity;

import java.util.IntSummaryStatistics;
import java.util.Map;

public class StatisticsReport {
    private long numOfProcessedFiles;
    private long numOfProcessedMeasurements;
    private long numOfFailedMeasurements;
    private Map<String, IntSummaryStatistics> statistics;

    public StatisticsReport(long numOfProcessedFiles, long numOfProcessedMeasurements, long numOfFailedMeasurements, Map<String, IntSummaryStatistics> statistics) {
        this.numOfProcessedFiles = numOfProcessedFiles;
        this.numOfProcessedMeasurements = numOfProcessedMeasurements;
        this.numOfFailedMeasurements = numOfFailedMeasurements;
        this.statistics = statistics;
    }

    public long getNumOfProcessedFiles() {
        return numOfProcessedFiles;
    }

    public long getNumOfProcessedMeasurements() {
        return numOfProcessedMeasurements;
    }

    public long getNumOfFailedMeasurements() {
        return numOfFailedMeasurements;
    }

    public Map<String, IntSummaryStatistics> getStatistics() {
        return statistics;
    }

}
