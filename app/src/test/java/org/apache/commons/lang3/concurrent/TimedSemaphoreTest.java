/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for TimedSemaphore.
 */
public class TimedSemaphoreTest {
    /** Constant for the time period. */
    private static final long PERIOD = 500;

    /** Constant for the time unit. */
    private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    /** Constant for the default limit. */
    private static final int LIMIT = 10;

    /**
     * Tests creating a new instance.
     */
    @Test
    public void testInit() {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        EasyMock.replay(service);
        final TimedSemaphore semaphore = new TimedSemaphore(service, PERIOD, UNIT,
                LIMIT);
        EasyMock.verify(service);
        assertEquals("Wrong service", service, semaphore.getExecutorService());
        assertEquals("Wrong period", PERIOD, semaphore.getPeriod());
        assertEquals("Wrong unit", UNIT, semaphore.getUnit());
        assertEquals("Statistic available", 0, semaphore
                .getLastAcquiresPerPeriod());
        assertEquals("Average available", 0.0, semaphore
                .getAverageCallsPerPeriod(), .05);
        assertFalse("Already shutdown", semaphore.isShutdown());
        assertEquals("Wrong limit", LIMIT, semaphore.getLimit());
    }

    /**
     * Tries to create an instance with a negative period. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitInvalidPeriod() {
        new TimedSemaphore(0L, UNIT, LIMIT);
    }

    /**
     * Tests whether a default executor service is created if no service is
     * provided.
     */
    @Test
    public void testInitDefaultService() {
        final TimedSemaphore semaphore = new TimedSemaphore(PERIOD, UNIT, LIMIT);
        final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) semaphore
                .getExecutorService();
        assertFalse("Wrong periodic task policy", exec
                .getContinueExistingPeriodicTasksAfterShutdownPolicy());
        assertFalse("Wrong delayed task policy", exec
                .getExecuteExistingDelayedTasksAfterShutdownPolicy());
        assertFalse("Already shutdown", exec.isShutdown());
        semaphore.shutdown();
    }

    /**
     * Tests starting the timer.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testStartTimer() throws InterruptedException {
        final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(PERIOD,
                UNIT, LIMIT);
        final ScheduledFuture<?> future = semaphore.startTimer();
        assertNotNull("No future returned", future);
        Thread.sleep(PERIOD);
        final int trials = 10;
        int count = 0;
        do {
            Thread.sleep(PERIOD);
            if (count++ > trials) {
                fail("endOfPeriod() not called!");
            }
        } while (semaphore.getPeriodEnds() <= 0);
        semaphore.shutdown();
    }

    /**
     * Tests the shutdown() method if the executor belongs to the semaphore. In
     * this case it has to be shut down.
     */
    @Test
    public void testShutdownOwnExecutor() {
        final TimedSemaphore semaphore = new TimedSemaphore(PERIOD, UNIT, LIMIT);
        semaphore.shutdown();
        assertTrue("Not shutdown", semaphore.isShutdown());
        assertTrue("Executor not shutdown", semaphore.getExecutorService()
                .isShutdown());
    }

    /**
     * Tests the shutdown() method for a shared executor service before a task
     * was started. This should do pretty much nothing.
     */
    @Test
    public void testShutdownSharedExecutorNoTask() {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        EasyMock.replay(service);
        final TimedSemaphore semaphore = new TimedSemaphore(service, PERIOD, UNIT,
                LIMIT);
        semaphore.shutdown();
        assertTrue("Not shutdown", semaphore.isShutdown());
        EasyMock.verify(service);
    }

    /**
     * Prepares an executor service mock to expect the start of the timer.
     *
     * @param service the mock
     * @param future the future
     */
    private void prepareStartTimer(final ScheduledExecutorService service,
            final ScheduledFuture<?> future) {
        service.scheduleAtFixedRate((Runnable) EasyMock.anyObject(), EasyMock
                .eq(PERIOD), EasyMock.eq(PERIOD), EasyMock.eq(UNIT));
        EasyMock.expectLastCall().andReturn(future);
    }

    /**
     * Tests the shutdown() method for a shared executor after the task was
     * started. In this case the task must be canceled.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testShutdownSharedExecutorTask() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.expect(Boolean.valueOf(future.cancel(false))).andReturn(Boolean.TRUE);
        EasyMock.replay(service, future);
        final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(service,
                PERIOD, UNIT, LIMIT);
        semaphore.acquire();
        semaphore.shutdown();
        assertTrue("Not shutdown", semaphore.isShutdown());
        EasyMock.verify(service, future);
    }

    /**
     * Tests multiple invocations of the shutdown() method.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testShutdownMultipleTimes() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.expect(Boolean.valueOf(future.cancel(false))).andReturn(Boolean.TRUE);
        EasyMock.replay(service, future);
        final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(service,
                PERIOD, UNIT, LIMIT);
        semaphore.acquire();
        for (int i = 0; i < 10; i++) {
            semaphore.shutdown();
        }
        EasyMock.verify(service, future);
    }

    /**
     * Tests the acquire() method if a limit is set.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testAcquireLimit() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.replay(service, future);
        final int count = 10;
        final CountDownLatch latch = new CountDownLatch(count - 1);
        final TimedSemaphore semaphore = new TimedSemaphore(service, PERIOD, UNIT, 1);
        final SemaphoreThread t = new SemaphoreThread(semaphore, latch, count,
                count - 1);
        semaphore.setLimit(count - 1);

        // start a thread that calls the semaphore count times
        t.start();
        latch.await();
        // now the semaphore's limit should be reached and the thread blocked
        assertEquals("Wrong semaphore count", count - 1, semaphore
                .getAcquireCount());

        // this wakes up the thread, it should call the semaphore once more
        semaphore.endOfPeriod();
        t.join();
        assertEquals("Wrong semaphore count (2)", 1, semaphore
                .getAcquireCount());
        assertEquals("Wrong acquire() count", count - 1, semaphore
                .getLastAcquiresPerPeriod());
        EasyMock.verify(service, future);
    }

    /**
     * Tests the acquire() method if more threads are involved than the limit.
     * This method starts a number of threads that all invoke the semaphore. The
     * semaphore's limit is set to 1, so in each period only a single thread can
     * acquire the semaphore.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testAcquireMultipleThreads() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.replay(service, future);
        final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(service,
                PERIOD, UNIT, 1);
        semaphore.latch = new CountDownLatch(1);
        final int count = 10;
        final SemaphoreThread[] threads = new SemaphoreThread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new SemaphoreThread(semaphore, null, 1, 0);
            threads[i].start();
        }
        for (int i = 0; i < count; i++) {
            semaphore.latch.await();
            assertEquals("Wrong count", 1, semaphore.getAcquireCount());
            semaphore.latch = new CountDownLatch(1);
            semaphore.endOfPeriod();
            assertEquals("Wrong acquire count", 1, semaphore
                    .getLastAcquiresPerPeriod());
        }
        for (int i = 0; i < count; i++) {
            threads[i].join();
        }
        EasyMock.verify(service, future);
    }

    /**
     * Tests the acquire() method if no limit is set. A test thread is started
     * that calls the semaphore a large number of times. Even if the semaphore's
     * period does not end, the thread should never block.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testAcquireNoLimit() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.replay(service, future);
        final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(service,
                PERIOD, UNIT, TimedSemaphore.NO_LIMIT);
        final int count = 1000;
        final CountDownLatch latch = new CountDownLatch(count);
        final SemaphoreThread t = new SemaphoreThread(semaphore, latch, count, count);
        t.start();
        latch.await();
        EasyMock.verify(service, future);
    }

    /**
     * Tries to call acquire() after shutdown(). This should cause an exception.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test(expected = IllegalStateException.class)
    public void testPassAfterShutdown() throws InterruptedException {
        final TimedSemaphore semaphore = new TimedSemaphore(PERIOD, UNIT, LIMIT);
        semaphore.shutdown();
        semaphore.acquire();
    }

    /**
     * Tests a bigger number of invocations that span multiple periods. The
     * period is set to a very short time. A background thread calls the
     * semaphore a large number of times. While it runs at last one end of a
     * period should be reached.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testAcquireMultiplePeriods() throws InterruptedException {
        final int count = 1000;
        final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(
                PERIOD / 10, TimeUnit.MILLISECONDS, 1);
        semaphore.setLimit(count / 4);
        final CountDownLatch latch = new CountDownLatch(count);
        final SemaphoreThread t = new SemaphoreThread(semaphore, latch, count, count);
        t.start();
        latch.await();
        semaphore.shutdown();
        assertTrue("End of period not reached", semaphore.getPeriodEnds() > 0);
    }

    /**
     * Tests the methods for statistics.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testGetAverageCallsPerPeriod() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.replay(service, future);
        final TimedSemaphore semaphore = new TimedSemaphore(service, PERIOD, UNIT,
                LIMIT);
        semaphore.acquire();
        semaphore.endOfPeriod();
        assertEquals("Wrong average (1)", 1.0, semaphore
                .getAverageCallsPerPeriod(), .005);
        semaphore.acquire();
        semaphore.acquire();
        semaphore.endOfPeriod();
        assertEquals("Wrong average (2)", 1.5, semaphore
                .getAverageCallsPerPeriod(), .005);
        EasyMock.verify(service, future);
    }

    /**
     * Tests whether the available non-blocking calls can be queried.
     *
     * @throws java.lang.InterruptedException so we don't have to catch it
     */
    @Test
    public void testGetAvailablePermits() throws InterruptedException {
        final ScheduledExecutorService service = EasyMock
                .createMock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = EasyMock.createMock(ScheduledFuture.class);
        prepareStartTimer(service, future);
        EasyMock.replay(service, future);
        final TimedSemaphore semaphore = new TimedSemaphore(service, PERIOD, UNIT,
                LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            assertEquals("Wrong available count at " + i, LIMIT - i, semaphore
                    .getAvailablePermits());
            semaphore.acquire();
        }
        semaphore.endOfPeriod();
        assertEquals("Wrong available count in new period", LIMIT, semaphore
                .getAvailablePermits());
        EasyMock.verify(service, future);
    }

    /**
     * Tests the tryAcquire() method. It is checked whether the semaphore can be acquired
     * by a bunch of threads the expected number of times and not more.
     */
    @Test
    public void testTryAcquire() throws InterruptedException {
        final TimedSemaphore semaphore = new TimedSemaphore(PERIOD, TimeUnit.SECONDS,
                LIMIT);
        final TryAcquireThread[] threads = new TryAcquireThread[3 * LIMIT];
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TryAcquireThread(semaphore, latch);
            threads[i].start();
        }

        latch.countDown();
        int permits = 0;
        for (final TryAcquireThread t : threads) {
            t.join();
            if (t.acquired) {
                permits++;
            }
        }
        assertEquals("Wrong number of permits granted", LIMIT, permits);
    }

    /**
     * Tries to call tryAcquire() after shutdown(). This should cause an exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testTryAcquireAfterShutdown() {
        final TimedSemaphore semaphore = new TimedSemaphore(PERIOD, UNIT, LIMIT);
        semaphore.shutdown();
        semaphore.tryAcquire();
    }

    /**
     * A specialized implementation of {@code TimedSemaphore} that is easier to
     * test.
     */
    private static class TimedSemaphoreTestImpl extends TimedSemaphore {
        /** A mock scheduled future. */
        ScheduledFuture<?> schedFuture;

        /** A latch for synchronizing with the main thread. */
        volatile CountDownLatch latch;

        /** Counter for the endOfPeriod() invocations. */
        private int periodEnds;

        TimedSemaphoreTestImpl(final long timePeriod, final TimeUnit timeUnit,
                final int limit) {
            super(timePeriod, timeUnit, limit);
        }

        TimedSemaphoreTestImpl(final ScheduledExecutorService service,
                final long timePeriod, final TimeUnit timeUnit, final int limit) {
            super(service, timePeriod, timeUnit, limit);
        }

        /**
         * Returns the number of invocations of the endOfPeriod() method.
         *
         * @return the endOfPeriod() invocations
         */
        int getPeriodEnds() {
            synchronized (this) {
                return periodEnds;
            }
        }

        /**
         * Invokes the latch if one is set.
         *
         * @throws java.lang.InterruptedException because it is declared that way in TimedSemaphore
         */
        @Override
        public synchronized void acquire() throws InterruptedException {
            super.acquire();
            if (latch != null) {
                latch.countDown();
            }
        }

        /**
         * Counts the number of invocations.
         */
        @Override
        protected synchronized void endOfPeriod() {
            super.endOfPeriod();
            periodEnds++;
        }

        /**
         * Either returns the mock future or calls the super method.
         */
        @Override
        protected ScheduledFuture<?> startTimer() {
            return schedFuture != null ? schedFuture : super.startTimer();
        }
    }

    /**
     * A test thread class that will be used by tests for triggering the
     * semaphore. The thread calls the semaphore a configurable number of times.
     * When this is done, it can notify the main thread.
     */
    private static class SemaphoreThread extends Thread {
        /** The semaphore. */
        private final TimedSemaphore semaphore;

        /** A latch for communication with the main thread. */
        private final CountDownLatch latch;

        /** The number of acquire() calls. */
        private final int count;

        /** The number of invocations of the latch. */
        private final int latchCount;

        SemaphoreThread(final TimedSemaphore b, final CountDownLatch l, final int c, final int lc) {
            semaphore = b;
            latch = l;
            count = c;
            latchCount = lc;
        }

        /**
         * Calls acquire() on the semaphore for the specified number of times.
         * Optionally the latch will also be triggered to synchronize with the
         * main test thread.
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    semaphore.acquire();

                    if (i < latchCount) {
                        latch.countDown();
                    }
                }
            } catch (final InterruptedException iex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * A test thread class which invokes {@code tryAcquire()} on the test semaphore and
     * records the return value.
     */
    private static class TryAcquireThread extends Thread {
        /** The semaphore. */
        private final TimedSemaphore semaphore;

        /** A latch for communication with the main thread. */
        private final CountDownLatch latch;

        /** Flag whether a permit could be acquired. */
        private boolean acquired;

        TryAcquireThread(final TimedSemaphore s, final CountDownLatch l) {
            semaphore = s;
            latch = l;
        }

        @Override
        public void run() {
            try {
                if (latch.await(10, TimeUnit.SECONDS)) {
                    acquired = semaphore.tryAcquire();
                }
            } catch (final InterruptedException iex) {
                // ignore
            }
        }
    }
}
