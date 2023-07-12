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
package org.apache.commons.lang3.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * JUnit tests for ContextedException.
 */
public class ContextedExceptionTest extends AbstractExceptionContextTest<ContextedException> {

    @Override
    public void setUp() throws Exception {
        exceptionContext = new ContextedException(new Exception(TEST_MESSAGE));
        super.setUp();
    }

    @Test
    public void testContextedException() {
        exceptionContext = new ContextedException();
        final String message = exceptionContext.getMessage();
        final String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.contains("ContextedException"));
        assertTrue(StringUtils.isEmpty(message));
    }

    @Test
    public void testContextedExceptionString() {
        exceptionContext = new ContextedException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, exceptionContext.getMessage());

        final String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.contains(TEST_MESSAGE));
    }

    @Test
    public void testContextedExceptionThrowable() {
        exceptionContext = new ContextedException(new Exception(TEST_MESSAGE));
        final String message = exceptionContext.getMessage();
        final String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.contains("ContextedException"));
        assertTrue(trace.contains(TEST_MESSAGE));
        assertTrue(message.contains(TEST_MESSAGE));
    }

    @Test
    public void testContextedExceptionStringThrowable() {
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE));
        final String message = exceptionContext.getMessage();
        final String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.contains("ContextedException"));
        assertTrue(trace.contains(TEST_MESSAGE));
        assertTrue(trace.contains(TEST_MESSAGE_2));
        assertTrue(message.contains(TEST_MESSAGE_2));
    }

    @Test
    public void testContextedExceptionStringThrowableContext() {
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        final String message = exceptionContext.getMessage();
        final String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.contains("ContextedException"));
        assertTrue(trace.contains(TEST_MESSAGE));
        assertTrue(trace.contains(TEST_MESSAGE_2));
        assertTrue(message.contains(TEST_MESSAGE_2));
    }

    @Test
    public void testNullExceptionPassing() {
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), null)
        .addContextValue("test1", null)
        .addContextValue("test2", "some value")
        .addContextValue("test Date", new Date())
        .addContextValue("test Nbr", Integer.valueOf(5))
        .addContextValue("test Poorly written obj", new ObjectWithFaultyToString());

        final String message = exceptionContext.getMessage();
        assertTrue(message != null);
    }

    @Test
    public void testRawMessage() {
        assertEquals(Exception.class.getName() + ": " + TEST_MESSAGE, exceptionContext.getRawMessage());
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        assertEquals(TEST_MESSAGE_2, exceptionContext.getRawMessage());
        exceptionContext = new ContextedException(null, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        assertNull(exceptionContext.getRawMessage());
    }
}
