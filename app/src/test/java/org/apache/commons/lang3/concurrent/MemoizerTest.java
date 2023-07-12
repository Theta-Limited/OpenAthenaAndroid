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

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(EasyMockRunner.class)
public class MemoizerTest {

    @Mock
    private Computable<Integer, Integer> computable;

    @Test
    public void testOnlyCallComputableOnceIfDoesNotThrowException() throws Exception {
        final Integer input = 1;
        final Memoizer<Integer, Integer> memoizer = new Memoizer<>(computable);
        expect(computable.compute(input)).andReturn(input);
        replay(computable);

        assertEquals("Should call computable first time", input, memoizer.compute(input));
        assertEquals("Should not call the computable the second time", input, memoizer.compute(input));
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultBehaviourNotToRecalculateExecutionExceptions() throws Exception {
        final Integer input = 1;
        final Memoizer<Integer, Integer> memoizer = new Memoizer<>(computable);
        final InterruptedException interruptedException = new InterruptedException();
        expect(computable.compute(input)).andThrow(interruptedException);
        replay(computable);

        try {
            memoizer.compute(input);
            fail("Expected Throwable to be thrown!");
        } catch (final Throwable expected) {
            // Should always be thrown the first time
        }

        memoizer.compute(input);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoesNotRecalculateWhenSetToFalse() throws Exception {
        final Integer input = 1;
        final Memoizer<Integer, Integer> memoizer = new Memoizer<>(computable, false);
        final InterruptedException interruptedException = new InterruptedException();
        expect(computable.compute(input)).andThrow(interruptedException);
        replay(computable);

        try {
            memoizer.compute(input);
            fail("Expected Throwable to be thrown!");
        } catch (final Throwable expected) {
            // Should always be thrown the first time
        }

        memoizer.compute(input);
    }

    @Test
    public void testDoesRecalculateWhenSetToTrue() throws Exception {
        final Integer input = 1;
        final Integer answer = 3;
        final Memoizer<Integer, Integer> memoizer = new Memoizer<>(computable, true);
        final InterruptedException interruptedException = new InterruptedException();
        expect(computable.compute(input)).andThrow(interruptedException).andReturn(answer);
        replay(computable);

        try {
            memoizer.compute(input);
            fail("Expected Throwable to be thrown!");
        } catch (final Throwable expected) {
            // Should always be thrown the first time
        }

        assertEquals(answer, memoizer.compute(input));
    }

    @Test(expected = RuntimeException.class)
    public void testWhenComputableThrowsRuntimeException() throws Exception {
        final Integer input = 1;
        final Memoizer<Integer, Integer> memoizer = new Memoizer<>(computable);
        final RuntimeException runtimeException = new RuntimeException("Some runtime exception");
        expect(computable.compute(input)).andThrow(runtimeException);
        replay(computable);

        memoizer.compute(input);
    }

    @Test(expected = Error.class)
    public void testWhenComputableThrowsError() throws Exception {
        final Integer input = 1;
        final Memoizer<Integer, Integer> memoizer = new Memoizer<>(computable);
        final Error error = new Error();
        expect(computable.compute(input)).andThrow(error);
        replay(computable);

        memoizer.compute(input);
    }
}
