module org.patriques.alphavantage4j {
    requires com.google.gson;
    requires javax.annotation.api;
    requires jsr305;
    exports org.patriques;
    exports org.patriques.input;
    exports org.patriques.input.digitalcurrencies;
    exports org.patriques.input.exchange;
    exports org.patriques.input.symbol;
    exports org.patriques.input.technicalindicators;
    exports org.patriques.input.timeseries;
    exports org.patriques.output;
    exports org.patriques.output.digitalcurrencies;
    exports org.patriques.output.exchange;
    exports org.patriques.output.quote;
    exports org.patriques.output.sectorperformances;
    exports org.patriques.output.technicalindicators;
    exports org.patriques.output.timeseries;
}
