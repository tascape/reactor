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

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class MysqlHandler extends MariadbHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlHandler.class);

    protected static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + "/" + DB_SCHEMA + "?useSSL=false";

    public static void main(String[] args) throws SQLException {
        MysqlHandler db = new MysqlHandler();
        TaskCase tc = new TaskCase();
        tc.setSuiteClass("a");
        LOG.debug("case id = {}", db.getCaseId(tc));
    }
}
