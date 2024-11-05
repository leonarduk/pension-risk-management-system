module timeseries.spring.boot.server {
    requires com.google.common;
    requires static lombok;
    requires org.apache.commons.lang3;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    requires ta4j.core;
    requires timeseries.stockfeed;
}
