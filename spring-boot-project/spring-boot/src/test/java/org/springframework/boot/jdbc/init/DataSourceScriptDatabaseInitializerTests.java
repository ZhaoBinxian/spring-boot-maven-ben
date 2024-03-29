/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.jdbc.init;

import java.util.UUID;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.AbstractScriptDatabaseInitializerTests;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.boot.testsupport.BuildOutput;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DataSourceScriptDatabaseInitializer}.
 *
 * @author Andy Wilkinson
 */
class DataSourceScriptDatabaseInitializerTests
        extends AbstractScriptDatabaseInitializerTests<DataSourceScriptDatabaseInitializer> {

    private final HikariDataSource embeddedDataSource = DataSourceBuilder.create().type(HikariDataSource.class)
            .url("jdbc:h2:mem:" + UUID.randomUUID()).build();

    private final HikariDataSource standloneDataSource = DataSourceBuilder.create().type(HikariDataSource.class)
            .url("jdbc:h2:file:" + new BuildOutput(DataSourceScriptDatabaseInitializerTests.class).getRootLocation()
                    .getAbsolutePath() + "/" + UUID.randomUUID())
            .build();

    @AfterEach
    void closeDataSource() {
        this.embeddedDataSource.close();
        this.standloneDataSource.close();
    }

    @Test
    void whenDatabaseIsInaccessibleThenItIsAssumedNotToBeEmbedded() {
        DataSourceScriptDatabaseInitializer initializer = new DataSourceScriptDatabaseInitializer(
                new HikariDataSource(), new DatabaseInitializationSettings());
        assertThat(initializer.isEmbeddedDatabase()).isFalse();
    }

    @Override
    protected DataSourceScriptDatabaseInitializer createEmbeddedDatabaseInitializer(
            DatabaseInitializationSettings settings) {
        return new DataSourceScriptDatabaseInitializer(this.embeddedDataSource, settings);
    }

    @Override
    protected DataSourceScriptDatabaseInitializer createStandaloneDatabaseInitializer(
            DatabaseInitializationSettings settings) {
        return new DataSourceScriptDatabaseInitializer(this.standloneDataSource, settings);
    }

    @Override
    protected int numberOfEmbeddedRows(String sql) {
        return numberOfRows(this.embeddedDataSource, sql);
    }

    @Override
    protected int numberOfStandaloneRows(String sql) {
        return numberOfRows(this.standloneDataSource, sql);
    }

    private int numberOfRows(DataSource dataSource, String sql) {
        return new JdbcTemplate(dataSource).queryForObject(sql, Integer.class);
    }

    @Override
    protected void assertDatabaseAccessed(boolean accessed, DataSourceScriptDatabaseInitializer initializer) {
        assertThat(((HikariDataSource) initializer.getDataSource()).isRunning()).isEqualTo(accessed);
    }

}
