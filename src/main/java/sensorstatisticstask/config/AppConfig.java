package sensorstatisticstask.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sensorstatisticstask.component.*;

@Configuration
public class AppConfig {

    @Bean
    public MeasurementsReader reportReader() {
        return new MeasurementsReaderImpl();
    }

    @Bean
    public StatisticsCalculator statisticsCalculator() {
        return new StatisticsCalculatorImpl(reportReader());
    }

    @Bean
    public StatisticsPrinter statisticsPrinter() {
        return new StatisticsPrinterImpl(System.out);
    }
}
