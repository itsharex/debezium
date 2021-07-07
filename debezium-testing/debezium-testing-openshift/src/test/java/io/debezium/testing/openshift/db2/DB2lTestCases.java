/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.testing.openshift.db2;

import static io.debezium.testing.openshift.assertions.KafkaAssertions.awaitAssert;
import static io.debezium.testing.openshift.tools.ConfigProperties.DATABASE_DB2_DBZ_DBNAME;
import static io.debezium.testing.openshift.tools.ConfigProperties.DATABASE_DB2_DBZ_PASSWORD;
import static io.debezium.testing.openshift.tools.ConfigProperties.DATABASE_DB2_DBZ_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.debezium.testing.openshift.fixtures.TestRuntimeFixture;
import io.debezium.testing.openshift.tools.databases.SqlDatabaseClient;
import io.debezium.testing.openshift.tools.databases.SqlDatabaseController;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public interface DB2lTestCases extends TestRuntimeFixture<SqlDatabaseController> {

    default void insertCustomer(String firstName, String lastName, String email) throws SQLException {
        SqlDatabaseClient client = getDbController().getDatabaseClient(DATABASE_DB2_DBZ_USERNAME, DATABASE_DB2_DBZ_PASSWORD);
        String sql = "INSERT INTO DB2INST1.CUSTOMERS(first_name,last_name,email) VALUES  ('" + firstName + "', '" + lastName + "', '" + email + "')";
        client.execute(DATABASE_DB2_DBZ_DBNAME, sql);
    }

    @Test
    @Order(1)
    default void shouldHaveRegisteredConnector() {
        Request r = new Request.Builder()
                .url(getKafkaConnectController().getApiURL().resolve("/connectors"))
                .build();

        awaitAssert(() -> {
            try (Response res = new OkHttpClient().newCall(r).execute()) {
                assertThat(res.body().string()).contains(getConnectorConfig().getConnectorName());
            }
        });
    }

    @Test
    @Order(2)
    default void shouldCreateKafkaTopics() {
        String prefix = getConnectorConfig().getDbServerName();
        assertions().assertTopicsExist(
                prefix + ".DB2INST1.CUSTOMERS",
                prefix + ".DB2INST1.ORDERS",
                prefix + ".DB2INST1.PRODUCTS",
                prefix + ".DB2INST1.PRODUCTS_ON_HAND");
    }

    @Test
    @Order(3)
    default void shouldContainRecordsInCustomersTopic() throws IOException {
        getKafkaConnectController().waitForDB2Snapshot(getConnectorConfig().getDbServerName());

        String topic = getConnectorConfig().getDbServerName() + ".DB2INST1.CUSTOMERS";
        awaitAssert(() -> assertions().assertRecordsCount(topic, 4));
    }

    @Test
    @Order(4)
    default void shouldStreamChanges() throws SQLException {
        insertCustomer("Tom", "Tester", "tom@test.com");

        String topic = getConnectorConfig().getDbServerName() + ".DB2INST1.CUSTOMERS";
        awaitAssert(() -> assertions().assertRecordsCount(topic, 5));
        awaitAssert(() -> assertions().assertRecordsContain(topic, "tom@test.com"));
    }

    @Test
    @Order(5)
    default void shouldBeDown() throws SQLException, IOException {
        getKafkaConnectController().undeployConnector(getConnectorConfig().getConnectorName());
        insertCustomer("Jerry", "Tester", "jerry@test.com");

        String topic = getConnectorConfig().getDbServerName() + ".DB2INST1.CUSTOMERS";
        awaitAssert(() -> assertions().assertRecordsCount(topic, 5));
    }

    @Test
    @Order(6)
    default void shouldResumeStreamingAfterRedeployment() throws IOException, InterruptedException {
        getKafkaConnectController().deployConnector(getConnectorConfig());

        String topic = getConnectorConfig().getDbServerName() + ".DB2INST1.CUSTOMERS";
        awaitAssert(() -> assertions().assertRecordsCount(topic, 6));
        awaitAssert(() -> assertions().assertRecordsContain(topic, "jerry@test.com"));
    }

    @Test
    @Order(7)
    default void shouldBeDownAfterCrash() throws SQLException {
        getKafkaConnectController().destroy();
        insertCustomer("Nibbles", "Tester", "nibbles@test.com");

        String topic = getConnectorConfig().getDbServerName() + ".DB2INST1.CUSTOMERS";
        awaitAssert(() -> assertions().assertRecordsCount(topic, 6));
    }

    @Test
    @Order(8)
    default void shouldResumeStreamingAfterCrash() throws InterruptedException {
        getKafkaConnectController().restore();

        String topic = getConnectorConfig().getDbServerName() + ".DB2INST1.CUSTOMERS";
        awaitAssert(() -> assertions().assertMinimalRecordsCount(topic, 7));
        awaitAssert(() -> assertions().assertRecordsContain(topic, "nibbles@test.com"));
    }
}
