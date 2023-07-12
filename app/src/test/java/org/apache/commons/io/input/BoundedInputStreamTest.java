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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BoundedInputStream}.
 */
public class BoundedInputStreamTest {

    private void compare(final String msg, final byte[] expected, final byte[] actual) {
        assertEquals(expected.length, actual.length, msg + " length");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], msg + " byte[" + i + "]");
        }
    }

    @Test
    public void testOnMaxLength() throws Exception {
        BoundedInputStream bounded;
        final byte[] helloWorld = "Hello World".getBytes();
        final byte[] hello = "Hello".getBytes();
        final AtomicBoolean boolRef = new AtomicBoolean();

        // limit = length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length) {
            @Override
            protected void onMaxLength(final long max, final long readCount) {
                boolRef.set(true);
            }
        };
        assertFalse(boolRef.get());
        for (int i = 0; i < helloWorld.length; i++) {
            assertEquals(helloWorld[i], bounded.read(), "limit = length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit = length end");
        assertTrue(boolRef.get());

        // limit > length
        boolRef.set(false);
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length + 1) {
            @Override
            protected void onMaxLength(final long max, final long readCount) {
                boolRef.set(true);
            }
        };
        assertFalse(boolRef.get());
        for (int i = 0; i < helloWorld.length; i++) {
            assertEquals(helloWorld[i], bounded.read(), "limit > length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit > length end");
        assertFalse(boolRef.get());

        // limit < length
        boolRef.set(false);
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), hello.length) {
            @Override
            protected void onMaxLength(final long max, final long readCount) {
                boolRef.set(true);
            }
        };
        assertFalse(boolRef.get());
        for (int i = 0; i < hello.length; i++) {
            assertEquals(hello[i], bounded.read(), "limit < length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit < length end");
        assertTrue(boolRef.get());
    }

    @Test
    public void testReadArray() throws Exception {

        BoundedInputStream bounded;
        final byte[] helloWorld = "Hello World".getBytes();
        final byte[] hello = "Hello".getBytes();

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld));
        compare("limit = -1", helloWorld, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), 0);
        compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length);
        compare("limit = length", helloWorld, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length + 1);
        compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length - 6);
        compare("limit < length", hello, IOUtils.toByteArray(bounded));
    }

    @Test
    public void testReadSingle() throws Exception {
        BoundedInputStream bounded;
        final byte[] helloWorld = "Hello World".getBytes();
        final byte[] hello = "Hello".getBytes();

        // limit = length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length);
        for (int i = 0; i < helloWorld.length; i++) {
            assertEquals(helloWorld[i], bounded.read(), "limit = length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit = length end");

        // limit > length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length + 1);
        for (int i = 0; i < helloWorld.length; i++) {
            assertEquals(helloWorld[i], bounded.read(), "limit > length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit > length end");

        // limit < length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), hello.length);
        for (int i = 0; i < hello.length; i++) {
            assertEquals(hello[i], bounded.read(), "limit < length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit < length end");
    }
}
