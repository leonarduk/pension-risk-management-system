package com.leonarduk.finance.api;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.joda.JodaModule;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;

@Configuration
public class AppConfig {
	@Inject
	DataSourceProperties dataSourceProperties;

	@Named
	static class JerseyConfig extends ResourceConfig {
		public JerseyConfig() {
			this.packages("com.leonarduk.finance.api");
			this.register(new JodaModule());
			this.register(CORSResponseFilter.class);
		}
	}

	@Bean
	DataSource dataSource() {
		final DataSource dataSource = DataSourceBuilder
		        .create(this.dataSourceProperties.getClassLoader())
		        .url(this.dataSourceProperties.getUrl())
		        .username(this.dataSourceProperties.getUsername())
		        .password(this.dataSourceProperties.getPassword()).build();
		return new DataSourceSpy(dataSource);
	}
}
