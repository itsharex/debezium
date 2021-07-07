/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.testing.openshift;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.testing.openshift.fixtures.TestRuntimeFixture;
import io.debezium.testing.openshift.fixtures.TestSetupFixture;
import io.debezium.testing.openshift.tools.databases.DatabaseController;
import io.debezium.testing.openshift.tools.kafka.ConnectorConfigBuilder;
import io.debezium.testing.openshift.tools.kafka.KafkaConnectController;
import io.debezium.testing.openshift.tools.kafka.KafkaController;
import io.debezium.testing.openshift.tools.registry.OcpRegistryController;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ConnectorTest<D extends DatabaseController<?>>
        implements TestSetupFixture, TestRuntimeFixture<D> {

    static Logger LOGGER = LoggerFactory.getLogger(ConnectorTest.class);

    // Kafka control
    private KafkaController kafkaController;
    private KafkaConnectController kafkaConnectController;

    // Database control
    private D dbController;

    // Connector info
    private ConnectorConfigBuilder connectorConfig;

    // Registry control
    private OcpRegistryController registryController;

    @BeforeAll
    public void setupFixtures() throws Exception {
        LOGGER.info("Initializing test fixtures from {}", getClass().getName());
        setupKafka();
        setupRegistry();
        setupDatabase();
        setupConnector();
    }

    @AfterAll
    public void teardownFixtures() throws Exception {
        teardownConnector();
        teardownDatabase();
        teardownRegistry();
        teardownKafka();
    }

    @Override
    public KafkaConnectController getKafkaConnectController() {
        return this.kafkaConnectController;
    }

    @Override
    public void setKafkaConnectController(KafkaConnectController controller) {
        this.kafkaConnectController = controller;
    }

    @Override
    public KafkaController getKafkaController() {
        return this.kafkaController;
    }

    @Override
    public void setKafkaController(KafkaController controller) {
        this.kafkaController = controller;
    }

    @Override
    public D getDbController() {
        return this.dbController;
    }

    @Override
    public void setDbController(D controller) {
        this.dbController = controller;
    }

    @Override
    public ConnectorConfigBuilder getConnectorConfig() {
        return connectorConfig;
    }

    @Override
    public void setConnectorConfig(ConnectorConfigBuilder config) {
        this.connectorConfig = config;
    }

    @Override
    public Optional<OcpRegistryController> getRegistryController() {
        return Optional.ofNullable(registryController);
    }

    @Override
    public void setRegistryController(OcpRegistryController controller) {
        this.registryController = controller;
    }

}
