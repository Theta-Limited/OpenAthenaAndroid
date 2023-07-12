/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests {@link DiffResult}.
 */
public class DiffResultTest {

    private static final SimpleClass SIMPLE_FALSE = new SimpleClass(false);
    private static final SimpleClass SIMPLE_TRUE = new SimpleClass(true);
    private static final ToStringStyle SHORT_STYLE = ToStringStyle.SHORT_PREFIX_STYLE;

    private static class SimpleClass implements Diffable<SimpleClass> {
        private final boolean booleanField;

        SimpleClass(final boolean booleanField) {
            this.booleanField = booleanField;
        }

        static String getFieldName() {
            return "booleanField";
        }

        @Override
        public DiffResult diff(final SimpleClass obj) {
            return new DiffBuilder(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append(getFieldName(), booleanField, obj.booleanField)
                    .build();
        }
    }

    private static class EmptyClass {
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIsNonModifiable() {
        final SimpleClass lhs = new SimpleClass(true);
        final SimpleClass rhs = new SimpleClass(false);

        final List<Diff<?>> diffs = lhs.diff(rhs).getDiffs();

        final DiffResult list = new DiffResult(lhs, rhs, diffs, SHORT_STYLE);
        assertEquals(diffs, list.getDiffs());
        assertEquals(1, list.getNumberOfDiffs());
        list.getDiffs().remove(0);
    }

    @Test
    public void testIterator() {
        final SimpleClass lhs = new SimpleClass(true);
        final SimpleClass rhs = new SimpleClass(false);

        final List<Diff<?>> diffs = lhs.diff(rhs).getDiffs();
        final Iterator<Diff<?>> expectedIterator = diffs.iterator();

        final DiffResult list = new DiffResult(lhs, rhs, diffs, SHORT_STYLE);
        final Iterator<Diff<?>> iterator = list.iterator();

        while (iterator.hasNext()) {
            assertTrue(expectedIterator.hasNext());
            assertEquals(expectedIterator.next(), iterator.next());
        }
    }

    @Test
    public void testToStringOutput() {
        final DiffResult list = new DiffBuilder(new EmptyClass(), new EmptyClass(),
                ToStringStyle.SHORT_PREFIX_STYLE).append("test", false, true)
                .build();
        assertEquals(
                "DiffResultTest.EmptyClass[test=false] differs from DiffResultTest.EmptyClass[test=true]",
                list.toString());
    }

    @Test
    public void testToStringSpecifyStyleOutput() {
        final DiffResult list = SIMPLE_FALSE.diff(SIMPLE_TRUE);
        assertTrue(list.getToStringStyle().equals(SHORT_STYLE));

        final String lhsString = new ToStringBuilder(SIMPLE_FALSE,
                ToStringStyle.MULTI_LINE_STYLE).append(
                SimpleClass.getFieldName(), SIMPLE_FALSE.booleanField).build();

        final String rhsString = new ToStringBuilder(SIMPLE_TRUE,
                ToStringStyle.MULTI_LINE_STYLE).append(
                SimpleClass.getFieldName(), SIMPLE_TRUE.booleanField).build();

        final String expectedOutput = String.format("%s differs from %s", lhsString,
                rhsString);
        assertEquals(expectedOutput,
                list.toString(ToStringStyle.MULTI_LINE_STYLE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullLhs() {
        new DiffResult(null, SIMPLE_FALSE, SIMPLE_TRUE.diff(SIMPLE_FALSE)
                .getDiffs(), SHORT_STYLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRhs() {
        new DiffResult(SIMPLE_TRUE, null, SIMPLE_TRUE.diff(SIMPLE_FALSE)
                .getDiffs(), SHORT_STYLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullList() {
        new DiffResult(SIMPLE_TRUE, SIMPLE_FALSE, null, SHORT_STYLE);
    }

    @Test
    public void testNullStyle() {
        final DiffResult diffResult = new DiffResult(SIMPLE_TRUE, SIMPLE_FALSE, SIMPLE_TRUE
                .diff(SIMPLE_FALSE).getDiffs(), null);
        assertEquals(ToStringStyle.DEFAULT_STYLE, diffResult.getToStringStyle());
    }

    @Test
    public void testNoDifferencesString() {
        final DiffResult diffResult = new DiffBuilder(SIMPLE_TRUE, SIMPLE_TRUE,
                SHORT_STYLE).build();
        assertEquals(DiffResult.OBJECTS_SAME_STRING, diffResult.toString());
    }
}
