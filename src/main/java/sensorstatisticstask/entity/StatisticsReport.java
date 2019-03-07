package sensorstatisticstask.entity;

import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Set;

public class StatisticsReport {
    private long numOfProcessedFiles;
    private long numOfProcessedMeasurements;
    private long numOfFailedMeasurements;
    private Map<String, IntSummaryStatistics> statistics;
    private Set<String> failedSensors;

    public StatisticsReport(long numOfProcessedFiles, long numOfProcessedMeasurements, long numOfFailedMeasurements, Map<String, IntSummaryStatistics> statistics, Set<String> failedSensors) {
        this.numOfProcessedFiles = numOfProcessedFiles;
        this.numOfProcessedMeasurements = numOfProcessedMeasurements;
        this.numOfFailedMeasurements = numOfFailedMeasurements;
        this.statistics = statistics;
        this.failedSensors = failedSensors;
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

    public Set<String> getFailedSensors() {
        return failedSensors;
    }
}
