/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.reactor.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class PostgresqlHandler extends MysqlHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresqlHandler.class);

    private static final String DB_DRIVER = "org.postgresql.Driver";

    private static final String JDBC_URL = "jdbc:postgresql://" + DB_HOST + "/" + DB_SCHEMA;

    static {
        try {
            Class.forName(DB_DRIVER).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Cannot load database driver: " + DB_DRIVER, ex);
        }
    }

    @Override
    protected boolean acquireExecutionLock(Connection conn, String lock) throws SQLException {
        final String sqlLock = String.format("SELECT pg_advisory_lock(%d);", this.hash(lock));
        LOG.debug("Acquire lock {}", lock);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlLock)) {
            if (rs.next() && "1".equals(rs.getString(1))) {
                LOG.trace("{} is locked", lock);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean releaseExecutionLock(Connection conn, String lock) throws SQLException {
        final String sqlRelease = String.format("SELECT pg_advisory_unlock(%d);", this.hash(lock));
        LOG.debug("Release lock {}", lock);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlRelease)) {
            if (rs.next() && "1".equals(rs.getString(1))) {
                LOG.trace("{} is released", lock);
                return true;
            }
        } finally {
            conn.close();
        }
        return false;
    }

    public static void main(String[] args) throws SQLException {
        PostgresqlHandler db = new PostgresqlHandler();
        TaskCase tc = new TaskCase();
        tc.setSuiteClass("a");
        LOG.debug("case id = {}", db.getCaseId(tc));
    }
}
