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
package com.tascape.reactor.task;

import com.tascape.reactor.ExecutionResult;
import com.tascape.reactor.Reactor;
import com.tascape.reactor.data.CaseIterationData;
import java.io.IOException;
import java.util.Random;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tascape.reactor.data.CaseDataProvider;
import java.util.stream.IntStream;

/**
 * Sample case class for JUnit4 test cases.
 *
 * @author linsong wang
 */
@Priority(level = 2)
public class JUnit4Case extends AbstractCase {
    private static final Logger LOG = LoggerFactory.getLogger(JUnit4Case.class);

    public JUnit4Case() {
    }

    @Before
    public void setUp() throws Exception {
        LOG.debug("Run something before case");
        LOG.debug("Please override");
        LOG.info("Case full name: {}", this.getCaseFullName());
    }

    @After
    public void tearDown() throws Exception {
        LOG.debug("Run something after case");
        LOG.debug("Please override");
    }

    @Override
    public String getApplicationUnderTask() {
        LOG.debug("Please override");
        return Reactor.class.getName();
    }

    @Test
    @Priority(level = 0)
    public void runPositive() throws Exception {
        LOG.info("Sample positive case");
        LOG.debug("Sample positive case");
        LOG.trace("Sample positive case");
        Random r = new Random();
        this.putResultMetric("JUnit4", "positive-1", r.nextInt(100));
        this.putResultMetric("JUnit4", "positive-2", r.nextInt(200));
        this.doSomethingGood();
    }

    @Test
    public void runFailure() throws Exception {
        LOG.info("Sample failure case");
        Assert.fail("case failed");
    }

    @Test
    public void runExternalId() throws Exception {
        LOG.info("Sample external id case, set to aaa");
        this.setExternalId("aaa");
        Random r = new Random();
        this.putResultMetric("JUnit4", "data-point-1", r.nextInt(100));
        this.putResultMetric("JUnit4", "data-point-2", r.nextInt(100));
        this.putResultMetric("JUnit4", "data-point-3", r.nextInt(100));
        this.putResultMetric("JUnit4", "data-point-4", r.nextInt(400));
        this.putResultMetric("JUnit4", "data-point-5", r.nextInt(100));
        this.putResultMetric("JUnit4", "data-point-6", r.nextInt(100));
        this.putResultMetric("JUnit4", "data-point-7", r.nextInt(100));
        this.putResultMetric("JUnit4", "data-point-8", r.nextInt(800));
        this.doSomethingGood();
    }

    @Test
    public void runMetrics() throws Exception {
        LOG.info("Sample case to collect metrics");
        this.doSomethingGood();
        Random r = new Random();
        IntStream.range(0, 10).forEach(i -> {
            this.putResultMetric("JUnit4", "homepage-load-second", 0.5 + r.nextFloat() * 2);
        });
    }

    @Test
    @Priority(level = 0)
    public void runNegative() throws Exception {
        LOG.info("Sample negative case");
        expectedException.expect(IOException.class);
        expectedException.expectMessage("something bad");
        Thread.sleep(3000);
        this.putResultMetric("JUnit4", "negative", new Random().nextInt(100));
        this.doSomethingBad();
    }

    @Test
    @Priority(level = 1)
    public void runNegativeAgain() throws Exception {
        LOG.info("Sample negative case again");
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Cannot resolve xyz");
        Thread.sleep(1000);
        this.doSomethingBadAgain();
    }

    @Test
    public void runMultiple() throws Exception {
        LOG.info("Sample multiple-result case");
        this.doSomething();
        ExecutionResult er = ExecutionResult.newMultiple();
        int total = 118;
        int pass = new Random().nextInt(9) + 110;
        LOG.debug("Mark case as {}", pass == total ? "PASS" : "FAIL");
        er.setPass(pass);
        er.setFail(total - pass);
        this.setExecutionResult(er);
    }

    @Test
    @CaseDataProvider(klass = SampleData.class)
    public void runDataProvider() throws Exception {
        SampleData d = this.getCaseData(SampleData.class);
        LOG.debug("case data '{}'", d.getCaseParameter());
        Thread.sleep(500);
    }

    @Test
    @CaseDataProvider(klass = CaseIterationData.class, method = "useIterations", parameter = "3")
    public void runIterations() throws Exception {
        LOG.debug("case iteration {}", new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(10));
        Thread.sleep(200);
    }

    @Test
    public void runCasePlaceholderForNotReadyFeature() throws Exception {
        Thread.sleep(500);
        super.markAsToBeImplemented();
    }

    @Test
    public void runRequeue() throws Exception {
        LOG.info("Sample requeue case");
        if (System.currentTimeMillis() % 2 == 0) {
            LOG.info("requeue case");
            this.requeue();
        } else {
            LOG.info("not requeue case");
        }
    }

    private void doSomethingGood() throws IOException {
        LOG.info("Do something good");
    }

    private void doSomething() throws IOException, InterruptedException {
        LOG.info("Do something");
        Thread.sleep(1000);
    }

    private void doSomethingBad() throws IOException {
        LOG.info("Do something bad");
        throw new IOException("something bad");
    }

    private void doSomethingBadAgain() throws IOException {
        LOG.info("Do something bad again");
        throw new IOException("Cannot resolve xyz");
    }
}
