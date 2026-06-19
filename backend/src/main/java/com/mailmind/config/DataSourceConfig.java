package com.mailmind.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
public class DataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String configured = env.getProperty("spring.datasource.url");
        String user = env.getProperty("spring.datasource.username");
        String pass = env.getProperty("spring.datasource.password");

        if (configured != null && configured.startsWith("jdbc:postgresql://")) {
            try {
                String hostPort = configured.substring("jdbc:postgresql://".length());
                int slashIdx = hostPort.indexOf('/');
                String hostPart = (slashIdx > 0) ? hostPort.substring(0, slashIdx) : hostPort;
                String host = hostPart;
                int colonIdx = hostPart.indexOf(':');
                if (colonIdx > 0)
                    host = hostPart.substring(0, colonIdx);
                // quick TCP probe (2000ms)
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(host, 5432), 2000);
                    log.info("Postgres host {} reachable — using configured datasource", host);
                    HikariConfig cfg = new HikariConfig();
                    cfg.setJdbcUrl(configured);
                    if (user != null)
                        cfg.setUsername(user);
                    if (pass != null)
                        cfg.setPassword(pass);
                    cfg.setMaximumPoolSize(10);
                    cfg.setConnectionTimeout(10000);
                    return new HikariDataSource(cfg);
                }
            } catch (Exception e) {
                log.warn(
                        "Configured Postgres appears unreachable ({}). Falling back to embedded H2 for local development.",
                        e.getMessage());
            }
        }

        // Fallback to file-based H2 database (Postgres compatibility mode)
        String h2Url = "jdbc:h2:file:./data/mailmind;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE;MODE=PostgreSQL";
        log.info("Using fallback H2 datasource: {}", h2Url);
        
        // Dynamically override Hibernate's DDL-auto to create/update tables in local H2 database
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
        System.setProperty("spring.jpa.properties.hibernate.hbm2ddl.auto", "update");
        
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(h2Url);
        cfg.setDriverClassName("org.h2.Driver");
        cfg.setMaximumPoolSize(5);
        cfg.setConnectionTimeout(10000);
        cfg.setLeakDetectionThreshold(20000);
        return new HikariDataSource(cfg);
    }
}
