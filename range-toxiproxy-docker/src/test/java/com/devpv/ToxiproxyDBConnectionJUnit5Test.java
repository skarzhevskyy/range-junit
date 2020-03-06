/*
 * Pyx4j framework
 * Copyright (C) 2008-2019 pyx4j.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Created on Feb. 19, 2020
 * @author vlads
 */
package com.devpv;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.postgresql.PGProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @see https://www.testcontainers.org/
 */
@Testcontainers
public class ToxiproxyDBConnectionJUnit5Test {

    private static final Logger log = LoggerFactory.getLogger(ToxiproxyDBConnectionJUnit5Test.class);

    // Create a common docker network so that containers can communicate
    public Network network = Network.newNetwork();

    @AfterEach
    void networkClenup() {
        network.close();
    }

    // will be started before and stopped after each test method
    @Container
    @SuppressWarnings("rawtypes")
    private PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:10.12")
            .withDatabaseName("tdb")
            .withUsername("tester")
            .withPassword("secret")
            .withLogConsumer(new Slf4jLogConsumer(log).withMdc("process", "postgres"))
            .withNetwork(network);

    @Container
    public ToxiproxyContainer toxiproxyContainer = new ToxiproxyContainer()
            .withLogConsumer(new Slf4jLogConsumer(log).withMdc("process", "toxiproxy"))
            .withNetwork(network);

    @Test
    void testConnectionViaToxicProxy() throws Exception {
        assertThat(DockerClientFactory.instance().client().inspectNetworkCmd().withNetworkId(network.getId()).exec())
                .as("Network exists")
                .isNotNull();

        assertThat(postgreSQLContainer.isRunning()).as("DB Started")
                .isEqualTo(true);

        log.info("PostgreSQL port: {}", postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));

        String postgresUrl = postgreSQLContainer.getJdbcUrl();
        log.info("Direct PostgreSQL url: {}", postgresUrl);

        Connection conDirect = DriverManager.getConnection(postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword());

        assertThat(conDirect.createStatement().execute("SELECT 1"))
                .as("Direct Connection, SQL Returned somting")
                .isEqualTo(true);
        conDirect.close();

        assertThat(toxiproxyContainer.isRunning()).as("ToxiProxy Started")
                .isEqualTo(true);

        // Need to use internal port in network, e.g. Not exposed Port
        ToxiproxyContainer.ContainerProxy dbProxy = toxiproxyContainer.getProxy(postgreSQLContainer, PostgreSQLContainer.POSTGRESQL_PORT);

        String toxicPostgresUrl = "jdbc:postgresql://"
                + dbProxy.getContainerIpAddress() + ":" + dbProxy.getProxyPort() + "/" + postgreSQLContainer.getDatabaseName();

        log.info("Toxic  PostgreSQL url: {}", toxicPostgresUrl);

        Connection conToxic1 = DriverManager.getConnection(toxicPostgresUrl, postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
        conToxic1.createStatement().executeUpdate("CREATE TABLE tst_toxic (name varchar(300))");
        conToxic1.close();

        ComboPooledDataSource dataSourceC3PO = new ComboPooledDataSource(true);
        {
            dataSourceC3PO.setDriverClass(postgreSQLContainer.getDriverClassName());
            dataSourceC3PO.setJdbcUrl(toxicPostgresUrl);
            dataSourceC3PO.setUser(postgreSQLContainer.getUsername());
            dataSourceC3PO.setPassword(postgreSQLContainer.getPassword());

            dataSourceC3PO.setMaxPoolSize(5);

            dataSourceC3PO.setTestConnectionOnCheckout(true);

            dataSourceC3PO.setAcquireRetryAttempts(3); // Defines how many times c3p0 will try to acquire a new Connection from the database before giving up

            dataSourceC3PO.setCheckoutTimeout((int) Duration.ofSeconds(21).toMillis());

            PGProperty.CONNECT_TIMEOUT.set(dataSourceC3PO.getProperties(), (int) Duration.ofSeconds(10).getSeconds());
            PGProperty.SOCKET_TIMEOUT.set(dataSourceC3PO.getProperties(), (int) Duration.ofSeconds(2).getSeconds());
        }

        log.info("Cutting connection to DB");
        dbProxy.setConnectionCut(true);

        assertThatThrownBy(() -> dataSourceC3PO.getConnection())
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("has timed out");

        log.info("schedule connection restore in 20 seconds, see C3PO.setCheckoutTimeout");
        Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> {

                    log.info("Restore connection to DB");
                    dbProxy.setConnectionCut(false);

                }, 20, TimeUnit.SECONDS);

        Connection conToxic2;
        try {
            conToxic2 = dataSourceC3PO.getConnection();
        } catch (SQLException e) {
            log.error("Pool getConnection faild", e);
            throw new AssertionError("Connection poll shuld have been recovered", e);
        }

        assertThat(conToxic2.createStatement().execute("SELECT 1"))
                .as("Toxic Connection, SQL Returned somting after netwok connection was restored")
                .isEqualTo(true);

        conToxic2.close();

    }
}
