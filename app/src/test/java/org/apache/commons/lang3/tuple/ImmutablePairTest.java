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
package org.apache.commons.lang3.tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Test the Pair class.
 */
public class ImmutablePairTest {

    @Test
    public void testBasic() throws Exception {
        final ImmutablePair<Integer, String> pair = new ImmutablePair<>(0, "foo");
        assertEquals(0, pair.left.intValue());
        assertEquals(0, pair.getLeft().intValue());
        assertEquals("foo", pair.right);
        assertEquals("foo", pair.getRight());
        final ImmutablePair<Object, String> pair2 = new ImmutablePair<>(null, "bar");
        assertNull(pair2.left);
        assertNull(pair2.getLeft());
        assertEquals("bar", pair2.right);
        assertEquals("bar", pair2.getRight());
    }

    @Test
    public void testPairOf() throws Exception {
        final ImmutablePair<Integer, String> pair = ImmutablePair.of(0, "foo");
        assertEquals(0, pair.left.intValue());
        assertEquals(0, pair.getLeft().intValue());
        assertEquals("foo", pair.right);
        assertEquals("foo", pair.getRight());
        final ImmutablePair<Object, String> pair2 = ImmutablePair.of(null, "bar");
        assertNull(pair2.left);
        assertNull(pair2.getLeft());
        assertEquals("bar", pair2.right);
        assertEquals("bar", pair2.getRight());
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(ImmutablePair.of(null, "foo"), ImmutablePair.of(null, "foo"));
        assertFalse(ImmutablePair.of("foo", 0).equals(ImmutablePair.of("foo", null)));
        assertFalse(ImmutablePair.of("foo", "bar").equals(ImmutablePair.of("xyz", "bar")));

        final ImmutablePair<String, String> p = ImmutablePair.of("foo", "bar");
        assertTrue(p.equals(p));
        assertFalse(p.equals(new Object()));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(ImmutablePair.of(null, "foo").hashCode(), ImmutablePair.of(null, "foo").hashCode());
    }

    @Test
    public void testNullPairEquals() {
        assertEquals(ImmutablePair.nullPair(), ImmutablePair.nullPair());
    }

    @Test
    public void testNullPairSame() {
        assertSame(ImmutablePair.nullPair(), ImmutablePair.nullPair());
    }

    @Test
    public void testNullPairLeft() {
        assertNull(ImmutablePair.nullPair().getLeft());
    }

    @Test
    public void testNullPairKey() {
        assertNull(ImmutablePair.nullPair().getKey());
    }

    @Test
    public void testNullPairRight() {
        assertNull(ImmutablePair.nullPair().getRight());
    }

    @Test
    public void testNullPairValue() {
        assertNull(ImmutablePair.nullPair().getValue());
    }

    @Test
    public void testNullPairTyped() {
        // No compiler warnings
        // How do we assert that?
        final ImmutablePair<String, String> pair = ImmutablePair.nullPair();
        assertNotNull(pair);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("(null,null)", ImmutablePair.of(null, null).toString());
        assertEquals("(null,two)", ImmutablePair.of(null, "two").toString());
        assertEquals("(one,null)", ImmutablePair.of("one", null).toString());
        assertEquals("(one,two)", ImmutablePair.of("one", "two").toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        final ImmutablePair<Integer, String> origPair = ImmutablePair.of(0, "foo");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(origPair);
        final ImmutablePair<Integer, String> deserializedPair = (ImmutablePair<Integer, String>) new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray())).readObject();
        assertEquals(origPair, deserializedPair);
        assertEquals(origPair.hashCode(), deserializedPair.hashCode());
    }
}
