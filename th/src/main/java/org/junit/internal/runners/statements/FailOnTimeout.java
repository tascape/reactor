package org.junit.internal.runners.statements;

import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on JUnit 4 source code.
 *
 * @author linsong wang
 */
public class FailOnTimeout extends Statement {
    private static final Logger LOG = LoggerFactory.getLogger(FailOnTimeout.class);

    private final Statement fOriginalStatement;

    private final long fTimeout;

    public FailOnTimeout(Statement originalStatement, long timeout) {
        fOriginalStatement = originalStatement;
        fTimeout = timeout;
        LOG.trace("Test timeout {} ms", timeout);
    }

    @Override
    public void evaluate() throws Throwable {
        StatementThread thread = evaluateStatement();
        if (!thread.fFinished) {
            throwExceptionForUnfinishedThread(thread);
        }
    }

    private StatementThread evaluateStatement() throws InterruptedException {
        StatementThread thread = new StatementThread(fOriginalStatement);

        // update the following line, so that thread-based log4j can work
        thread.setName(new StringBuilder(
                Thread.currentThread().getName()).append("-").append(fTimeout).append("ms").toString());
        thread.start();
        thread.join(fTimeout);
        if (!thread.fFinished) {
            thread.recordStackTrace();
        }
        thread.interrupt();
        return thread;
    }

    private void throwExceptionForUnfinishedThread(StatementThread thread) throws Throwable {
        if (thread.fExceptionThrownByOriginalStatement != null) {
            throw thread.fExceptionThrownByOriginalStatement;
        } else {
            throwTimeoutException(thread);
        }
    }

    private void throwTimeoutException(StatementThread thread) throws Exception {
        Exception exception = new Exception(String.format("test timed out after %d milliseconds", fTimeout));
        exception.setStackTrace(thread.getRecordedStackTrace());
        throw exception;
    }

    private static class StatementThread extends Thread {
        private final Statement fStatement;

        private boolean fFinished = false;

        private Throwable fExceptionThrownByOriginalStatement = null;

        private StackTraceElement[] fRecordedStackTrace = null;

        public StatementThread(Statement statement) {
            fStatement = statement;
        }

        public void recordStackTrace() {
            fRecordedStackTrace = getStackTrace();
        }

        public StackTraceElement[] getRecordedStackTrace() {
            return fRecordedStackTrace;
        }

        @Override
        public void run() {
            try {
                fStatement.evaluate();
                fFinished = true;
            } catch (InterruptedException e) {
                // don't log the InterruptedException
            } catch (Throwable e) {
                LOG.error("Failing", e);
                fExceptionThrownByOriginalStatement = e;
            }
        }
    }
}
