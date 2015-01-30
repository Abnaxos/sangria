/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Raffael Herzog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ch.raffael.sangria.commons;

/**
 * Provides support for sneaking checked exceptions through code that can't throw checked
 * exceptions, re-catching and re-throwing them afterwards.
 *
 * A simple example using {@link java.util.stream}:
 *
 * ```java
 * Class[] loadClasses(String... classNames) throws ClassNotFoundException {
 *     return Unchecked.throwingCall(ClassNotFoundException.class,
 *             () -> Stream.of(classNames)
 *                     .map(n -> unchecked(() -> Class.forName(n)))
 *                     .toArray(Class[]::new));
 * }
 * ```
 *
 * As you can see, it keeps the usage of the
 * [sneakyThrow](https://www.google.com/search?q=java+sneakyThrow) hack under control by confining
 * it in a {@link Unchecked.Block} or {@link Unchecked.VoidBlock}, catching {@link Throwable},
 * re-throwing all declared exceptions and wrapping undeclared exceptions into an
 * {@link UnexpectedThrowableError}.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Unchecked {

    private Unchecked() {
    }

    public static <T> T unchecked(Block<T> block) {
        try {
            return block.call();
        }
        catch ( Throwable e ) {
            throw sneakyThrow(e);
        }
    }

    public static <T> T uncheckedCall(Block<T> block) {
        return unchecked(block);
    }

    public static void unchecked(VoidBlock block) {
        try {
            block.call();
        }
        catch ( Throwable e ) {
            throw sneakyThrow(e);
        }
    }

    public static void uncheckedRun(VoidBlock block) {
        unchecked(block);
    }

    public static <T> T throwing(Block<T> block) {
        return throwingCall(null, null, null, block);
    }

    public static <T, E extends Throwable> T throwing(Class<E> exception, Block<T> block) throws E {
        return throwingCall(exception, null, null, block);
    }

    public static <T, E1 extends Throwable, E2 extends Throwable> T throwing(Class<E1> exception1, Class<E2> exception2, Block<T> block) throws E1, E2 {
        return throwingCall(exception1, exception2, null, block);
    }

    public static <T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> T throwing(Class<E1> exception1, Class<E2> exception2, Class<E3> exception3, Block<T> block) throws E1, E2, E3 {
        return throwingCall(exception1, exception2, exception3, block);
    }

    public static <T> T throwingCall(Block<T> block) {
        return throwingCall(null, null, null, block);
    }

    public static <T, E1 extends Throwable> T throwingCall(Class<E1> exception, Block<T> block) throws E1 {
        return throwingCall(exception, null, null, block);
    }

    public static <T, E1 extends Throwable, E2 extends Throwable> T throwingCall(Class<E1> exception1, Class<E2> exception2, Block<T> block) throws E1, E2 {
        return throwingCall(exception1, exception2, null, block);
    }

    public static <T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> T throwingCall(Class<E1> exception1, Class<E2> exception2, Class<E3> exception3, Block<T> block) throws E1, E2, E3 {
        try {
            return block.call();
        }
        catch ( Throwable e ) {
            tryRethrow(exception1, e);
            tryRethrow(exception2, e);
            tryRethrow(exception3, e);
            if ( e instanceof Error ) {
                throw (Error)e;
            }
            if ( e instanceof RuntimeException ) {
                throw (RuntimeException)e;
            }
            throw undeclaredThrowable(e);
        }
    }

    public static void throwing(VoidBlock  block) {
          throwingRun(null, null, null, block);
    }

    public static <E extends Throwable> void throwing(Class<E> exception, VoidBlock  block) throws E {
          throwingRun(exception, null, null, block);
    }

    public static <E1 extends Throwable, E2 extends Throwable> void throwing(Class<E1> exception1, Class<E2> exception2, VoidBlock  block) throws E1, E2 {
          throwingRun(exception1, exception2, null, block);
    }

    public static <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> void throwing(Class<E1> exception1, Class<E2> exception2, Class<E3> exception3, VoidBlock  block) throws E1, E2, E3 {
          throwingRun(exception1, exception2, exception3, block);
    }

    public static void throwingRun(VoidBlock  block) {
          throwingRun(null, null, null, block);
    }

    public static <E1 extends Throwable> void throwingRun(Class<E1> exception, VoidBlock  block) throws E1 {
          throwingRun(exception, null, null, block);
    }

    public static <E1 extends Throwable, E2 extends Throwable> void throwingRun(Class<E1> exception1, Class<E2> exception2, VoidBlock  block) throws E1, E2 {
          throwingRun(exception1, exception2, null, block);
    }

    public static <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> void throwingRun(Class<E1> exception1, Class<E2> exception2, Class<E3> exception3, VoidBlock  block) throws E1, E2, E3 {
        try {
              block.call();
        }
        catch ( Throwable e ) {
            tryRethrow(exception1, e);
            tryRethrow(exception2, e);
            tryRethrow(exception3, e);
            if ( e instanceof Error ) {
                throw (Error)e;
            }
            if ( e instanceof RuntimeException ) {
                throw (RuntimeException)e;
            }
            throw undeclaredThrowable(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void tryRethrow(Class<E> exception, Throwable e) throws E {
        if ( exception == null ) {
            return;
        }
        if ( exception.isInstance(e) ) {
            throw (E)e;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneakyThrow(Throwable exception) throws E {
        throw (E)exception;
    }

    private static UnexpectedThrowableError undeclaredThrowable(Throwable e) {
        return new UnexpectedThrowableError(e);
    }

    public static interface Block<T> {
        T call() throws Exception;
    }

    public static interface VoidBlock {
        void call() throws Exception;
    }

}
