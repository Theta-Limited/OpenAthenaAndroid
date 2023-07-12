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
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link AutoCloseInputStream}.
 */
public class AutoCloseInputStreamTest {

    private byte[] data;

    private AutoCloseInputStream stream;

    private boolean closed;

    @BeforeEach
    public void setUp() {
        data = new byte[] { 'x', 'y', 'z' };
        stream = new AutoCloseInputStream(new ByteArrayInputStream(data) {
            @Override
            public void close() {
                closed = true;
            }
        });
        closed = false;
    }

    @Test
    public void testClose() throws IOException {
        stream.close();
        assertTrue(closed, "closed");
        assertEquals(-1, stream.read(), "read()");
    }

    @Test
    public void testFinalize() throws Throwable {
        stream.finalize();
        assertTrue(closed, "closed");
        assertEquals(-1, stream.read(), "read()");
    }

    @Test
    public void testRead() throws IOException {
        for (final byte element : data) {
            assertEquals(element, stream.read(), "read()");
            assertFalse(closed, "closed");
        }
        assertEquals(-1, stream.read(), "read()");
        assertTrue(closed, "closed");
    }

    @Test
    public void testReadBuffer() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b)) {
            assertFalse(closed, "closed");
            for (int i = 0; i < n; i++) {
                assertEquals(data[total + i], b[i], "read(b)");
            }
            total += n;
        }
        assertEquals(data.length, total, "read(b)");
        assertTrue(closed, "closed");
        assertEquals(-1, stream.read(b), "read(b)");
    }

    @Test
    public void testReadBufferOffsetLength() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b, total, b.length - total)) {
            assertFalse(closed, "closed");
            total += n;
        }
        assertEquals(data.length, total, "read(b, off, len)");
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b[i], "read(b, off, len)");
        }
        assertTrue(closed, "closed");
        assertEquals(-1, stream.read(b, 0, b.length), "read(b, off, len)");
    }

    private void testResetBeforeEnd(final AutoCloseInputStream inputStream) throws IOException {
        inputStream.mark(1);
        assertEquals('1', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        assertEquals('3', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        assertEquals('3', inputStream.read());
        assertEquals('4', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
    }

    @Test
    public void testResetBeforeEndCtor() throws IOException {
        try (final AutoCloseInputStream inputStream = new AutoCloseInputStream(new ByteArrayInputStream("1234".getBytes()))) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetBeforeEndSetByteArray() throws IOException {
        try (final AutoCloseInputStream inputStream = AutoCloseInputStream.builder().setByteArray("1234".getBytes()).get()) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetBeforeEndSetCharSequence() throws IOException {
        try (final AutoCloseInputStream inputStream = AutoCloseInputStream.builder().setCharSequence("1234").get()) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetBeforeEndSetInputStream() throws IOException {
        try (final AutoCloseInputStream inputStream = AutoCloseInputStream.builder().setInputStream(new ByteArrayInputStream("1234".getBytes())).get()) {
            testResetBeforeEnd(inputStream);
        }
    }

}
