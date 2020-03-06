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
 * Created on Feb. 20, 2020
 * @author vlads
 */
package com.devpv;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

import org.postgresql.PGProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * Demo example from @see https://www.testcontainers.org/
 */
public class ToxiproxyDBConnectionJUnit4Test {

    private static final Logger log = LoggerFactory.getLogger(ToxiproxyDBConnectionJUnit4Test.class);

    // Create a common docker network so that containers can communicate
    @org.junit.Rule
    public Network network = Network.newNetwork();

    // will be started before and stopped after each test method
    @org.junit.Rule
    @SuppressWarnings("rawtypes")
    public PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:10.12")
            .withDatabaseName("tdb")
            .withUsername("tester")
            .withPassword("secret")
            .withLogConsumer(new Slf4jLogConsumer(log).withMdc("process", "postgres"))
            .withNetwork(network);

    @org.junit.Rule
    public ToxiproxyContainer toxiproxyContainer = new ToxiproxyContainer()
            .withLogConsumer(new Slf4jLogConsumer(log).withMdc("process", "toxiproxy"))
            .withNetwork(network);

    @org.junit.Test
    public void testConnectionViaToxicProxy() throws SQLException {

        Properties conProperties = new Properties();
        PGProperty.USER.set(conProperties, postgreSQLContainer.getUsername());
        PGProperty.PASSWORD.set(conProperties, postgreSQLContainer.getPassword());
        PGProperty.CONNECT_TIMEOUT.set(conProperties, (int) Duration.ofSeconds(10).getSeconds());
        PGProperty.SOCKET_TIMEOUT.set(conProperties, (int) Duration.ofSeconds(2).getSeconds());

        log.info("Direct PostgreSQL url: {}", postgreSQLContainer.getJdbcUrl());

        Connection conDirect = DriverManager.getConnection(postgreSQLContainer.getJdbcUrl(), conProperties);

        assertThat(conDirect.createStatement().execute("SELECT 1"))
                .as("Direct Connection, SQL Returned somting")
                .isEqualTo(true);
        conDirect.close();

        // Need to use internal port in network, e.g. Not exposed Port
        ToxiproxyContainer.ContainerProxy dbProxy = toxiproxyContainer.getProxy(postgreSQLContainer, PostgreSQLContainer.POSTGRESQL_PORT);

        String toxicPostgresUrl = "jdbc:postgresql://"
                + dbProxy.getContainerIpAddress() + ":" + dbProxy.getProxyPort() + "/" + postgreSQLContainer.getDatabaseName();

        log.info("Toxic  PostgreSQL url: {}", toxicPostgresUrl);

        Connection conToxic1 = DriverManager.getConnection(toxicPostgresUrl, conProperties);

        assertThat(conToxic1.createStatement().execute("SELECT 1"))
                .as("Toxic Connection, SQL Returned somting")
                .isEqualTo(true);

        log.info("Cutting connection to DB");
        dbProxy.setConnectionCut(true);

        assertThatThrownBy(() -> conToxic1.createStatement().execute("SELECT 1"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("I/O error occurred");

        log.info("Restore connection to DB");
        dbProxy.setConnectionCut(false);

        //Old connection has been closed, close it any way
        conToxic1.close();

        Connection conToxic2 = DriverManager.getConnection(toxicPostgresUrl, conProperties);

        assertThat(conToxic2.createStatement().execute("SELECT 1"))
                .as("Toxic Connection, SQL Returned somting")
                .isEqualTo(true);

        conToxic2.close();

    }
}
