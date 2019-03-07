package sensorstatisticstask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sensorstatisticstask.component.StatisticsCalculator;
import sensorstatisticstask.component.StatisticsPrinter;
import sensorstatisticstask.entity.StatisticsReport;

import java.nio.file.Paths;


@SpringBootApplication
public class SensorStatisticsTaskApplication implements CommandLineRunner {

    @Autowired
    private StatisticsCalculator statisticsCalculator;
    @Autowired
    private StatisticsPrinter statisticsPrinter;

    public static void main(String[] args) {
        SpringApplication.run(SensorStatisticsTaskApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Application expects exactly one 'path to directory' argument to be passed");
        }

        StatisticsReport statisticsReport = statisticsCalculator.apply(Paths.get(args[0]));

        statisticsPrinter.accept(statisticsReport);
    }

}
