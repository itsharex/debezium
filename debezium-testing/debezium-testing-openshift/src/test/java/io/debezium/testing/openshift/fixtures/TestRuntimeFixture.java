/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.testing.openshift.fixtures;

import io.debezium.testing.openshift.fixtures.connectors.ConnectorRuntimeFixture;
import io.debezium.testing.openshift.fixtures.databases.DatabaseRuntimeFixture;
import io.debezium.testing.openshift.fixtures.kafka.KafkaRuntimeFixture;
import io.debezium.testing.openshift.fixtures.registry.RegistryRuntimeFixture;
import io.debezium.testing.openshift.tools.databases.DatabaseController;

public interface TestRuntimeFixture<D extends DatabaseController<?>>
        extends KafkaRuntimeFixture, ConnectorRuntimeFixture, DatabaseRuntimeFixture<D>,
        RegistryRuntimeFixture, Assertions {
}
