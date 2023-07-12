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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Test class for {@code CallableBackgroundInitializer}
 */
public class CallableBackgroundInitializerTest  {
    /** Constant for the result of the call() invocation. */
    private static final Integer RESULT = Integer.valueOf(42);

    /**
     * Tries to create an instance without a Callable. This should cause an
     * exception.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInitNullCallable() {
        new CallableBackgroundInitializer<>(null);
    }

    /**
     * Tests whether the executor service is correctly passed to the super
     * class.
     */
    @Test
    public void testInitExecutor() throws InterruptedException {
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        final CallableBackgroundInitializer<Integer> init = new CallableBackgroundInitializer<>(
                new TestCallable(), exec);
        assertEquals("Executor not set", exec, init.getExternalExecutor());
        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * Tries to pass a null Callable to the constructor that takes an executor.
     * This should cause an exception.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInitExecutorNullCallable() throws InterruptedException {
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            new CallableBackgroundInitializer<Integer>(null, exec);
        } finally {
            exec.shutdown();
            exec.awaitTermination(1, TimeUnit.SECONDS);
        }

    }

    /**
     * Tests the implementation of initialize().
     *
     * @throws java.lang.Exception so we don't have to catch it
     */
    @Test
    public void testInitialize() throws Exception {
        final TestCallable call = new TestCallable();
        final CallableBackgroundInitializer<Integer> init = new CallableBackgroundInitializer<>(
                call);
        assertEquals("Wrong result", RESULT, init.initialize());
        assertEquals("Wrong number of invocations", 1, call.callCount);
    }

    /**
     * A test Callable implementation for checking the initializer's
     * implementation of the initialize() method.
     */
    private static class TestCallable implements Callable<Integer> {
        /** A counter for the number of call() invocations. */
        int callCount;

        /**
         * Records this invocation and returns the test result.
         */
        @Override
        public Integer call() throws Exception {
            callCount++;
            return RESULT;
        }
    }
}
