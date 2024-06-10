module alphavantage4j {
    requires com.google.gson;
    requires jsr305;

    exports org.patriques;
    exports org.patriques.input.timeseries;
    exports org.patriques.output.exchange;
    exports org.patriques.output.exchange.data;
    exports org.patriques.output.timeseries;
    exports org.patriques.output.timeseries.data;
}