package sensorstatisticstask.entity;

public class SensorMeasurement {

    private String sensorId;
    private byte measurement;

    public SensorMeasurement(String sensorId, byte measurement) {
        this.sensorId = sensorId;
        this.measurement = measurement;
    }

    public String getSensorId() {
        return sensorId;
    }

    public byte getMeasurement() {
        return measurement;
    }

    public boolean isFailed() {
        return measurement < 0 || measurement > 100;
    }
}
