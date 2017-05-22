package com.leonarduk.finance.api;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;

@Configuration
public class AppConfig {
	@Inject
	private DataSourceProperties dataSourceProperties;

	@Named
	public static class JerseyConfig extends ResourceConfig {
		public JerseyConfig() {
			this.register(CORSResponseFilter.class);
			this.register(PortfolioFeedEndpoint.class);
			this.register(StockFeedEndpoint.class);
		}
	}

	@Bean
	public DataSource dataSource() {
		final DataSource dataSource = DataSourceBuilder
		        .create(this.dataSourceProperties.getClassLoader())
		        .url(this.dataSourceProperties.getUrl())
		        .username(this.dataSourceProperties.getUsername())
		        .password(this.dataSourceProperties.getPassword()).build();
		return new DataSourceSpy(dataSource);
	}
}
