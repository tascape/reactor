/*
 * Copyright 2015 - 2016 Nebula Bay.
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
package com.tascape.qa.th.driver;

import java.io.IOException;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class JacocoClient extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(JacocoClient.class);

    private final ExecDumpClient edc = new ExecDumpClient();

    private String address = "localhost";

    private int port = 8522;

    public JacocoClient setAddress(final String address) {
        this.address = address;
        return this;
    }

    public JacocoClient setPort(final int port) {
        this.port = port;
        return this;
    }

    public ExecFileLoader dump() throws IOException {
        return edc.dump(address, port);
    }

    @Override
    public String getName() {
        return JacocoClient.class.getSimpleName();
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public void reset() throws Exception {
        LOG.debug("na");
    }
}
