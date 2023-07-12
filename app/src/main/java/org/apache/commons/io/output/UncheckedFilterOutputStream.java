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

package org.apache.commons.io.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

/**
 * A {@link FilterOutputStream} that throws {@link UncheckedIOException} instead of {@link UncheckedIOException}.
 * <p>
 * To build an instance, see {@link Builder}.
 * </p>
 *
 * @see FilterOutputStream
 * @see UncheckedIOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public final class UncheckedFilterOutputStream extends FilterOutputStream {

    /**
     * Builds a new {@link UncheckedFilterOutputStream} instance.
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UncheckedFilterOutputStream s = UncheckedFilterOutputStream.builder()
     *   .setFile(file)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UncheckedFilterOutputStream s = UncheckedFilterOutputStream.builder()
     *   .setPath(path)
     *   .get();}
     * </pre>
     */
    public static class Builder extends AbstractStreamBuilder<UncheckedFilterOutputStream, Builder> {

        /**
         * Constructs a new instance.
         * <p>
         * This builder use the aspect OutputStream and OpenOption[].
         * </p>
         * <p>
         * You must provide an origin that can be converted to an OutputStream by this builder, otherwise, this call will throw an
         * {@link UnsupportedOperationException}.
         * </p>
         *
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide an OutputStream.
         * @see #getOutputStream()
         */
        @SuppressWarnings("resource")
        @Override
        public UncheckedFilterOutputStream get() throws IOException {
            return new UncheckedFilterOutputStream(getOutputStream());
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs an output stream filter built on top of the specified underlying output stream.
     *
     * @param outputStream the underlying output stream, or {@code null} if this instance is to be created without an
     *        underlying stream.
     */
    private UncheckedFilterOutputStream(final OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void close() throws UncheckedIOException {
        Uncheck.run(super::close);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void flush() throws UncheckedIOException {
        Uncheck.run(super::flush);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final byte[] b) throws UncheckedIOException {
        Uncheck.accept(super::write, b);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws UncheckedIOException {
        Uncheck.accept(super::write, b, off, len);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final int b) throws UncheckedIOException {
        Uncheck.accept(super::write, b);
    }

}
