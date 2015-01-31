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

package ch.raffael.sangria.logging.ext;

import org.slf4j.Logger;
import org.slf4j.Marker;

import static ch.raffael.sangria.logging.ext.LoggerInterceptor.Level.DEBUG;
import static ch.raffael.sangria.logging.ext.LoggerInterceptor.Level.ERROR;
import static ch.raffael.sangria.logging.ext.LoggerInterceptor.Level.INFO;
import static ch.raffael.sangria.logging.ext.LoggerInterceptor.Level.TRACE;
import static ch.raffael.sangria.logging.ext.LoggerInterceptor.Level.WARN;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("RedundantCast")
public abstract class LoggerInterceptor implements Logger {

    protected LoggerInterceptor() {
    }

    protected abstract Logger delegate();

    @Override
    public final String getName() {
        return delegate().getName();
    }

    /*
     * trace
     */
    
    @Override
    public boolean isTraceEnabled() {
        return delegate().isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate().isTraceEnabled(marker);
    }

    @Override
    public void trace(String msg) {
        if ( isTraceEnabled() ) {
            delegate().trace(
                    interceptMarker(TRACE, null, msg),
                    interceptMessage(TRACE, null, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if ( isTraceEnabled() ) {
            delegate().trace(
                    interceptMarker(TRACE, null, format),
                    interceptMessage(TRACE, null, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if ( isTraceEnabled() ) {
            delegate().trace(
                    interceptMarker(TRACE, null, format),
                    interceptMessage(TRACE, null, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if ( isTraceEnabled() ) {
            delegate().trace(
                    interceptMarker(TRACE, null, format),
                    interceptMessage(TRACE, null, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void trace(String msg, Throwable exception) {
        if ( isTraceEnabled() ) {
            delegate().trace(
                    interceptMarker(TRACE, null, msg),
                    interceptMessage(TRACE, null, msg),
                    exception);
        }
    }

    @Override
    public void trace(Marker marker, String msg) {
        delegate().trace(
                interceptMarker(TRACE, marker, msg),
                interceptMessage(TRACE, marker, msg),
                (Throwable)null);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if ( isTraceEnabled(marker) ) {
            delegate().trace(
                    interceptMarker(TRACE, marker, format),
                    interceptMessage(TRACE, marker, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if ( isTraceEnabled(marker) ) {
            delegate().trace(
                    interceptMarker(TRACE, marker, format),
                    interceptMessage(TRACE, marker, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments) {
        if ( isTraceEnabled(marker) ) {
            delegate().trace(
                    interceptMarker(TRACE, marker, format),
                    interceptMessage(TRACE, marker, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable exception) {
        if ( isTraceEnabled(marker) ) {
            delegate().trace(
                    interceptMarker(TRACE, marker, msg),
                    interceptMessage(TRACE, marker, msg),
                    exception);
        }
    }

    /*
     * debug
     */

    @Override
    public boolean isDebugEnabled() {
        return delegate().isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate().isDebugEnabled(marker);
    }

    @Override
    public void debug(String msg) {
        if ( isDebugEnabled() ) {
            delegate().debug(
                    interceptMarker(DEBUG, null, msg),
                    interceptMessage(DEBUG, null, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if ( isDebugEnabled() ) {
            delegate().debug(
                    interceptMarker(DEBUG, null, format),
                    interceptMessage(DEBUG, null, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if ( isDebugEnabled() ) {
            delegate().debug(
                    interceptMarker(DEBUG, null, format),
                    interceptMessage(DEBUG, null, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if ( isDebugEnabled() ) {
            delegate().debug(
                    interceptMarker(DEBUG, null, format),
                    interceptMessage(DEBUG, null, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void debug(String msg, Throwable exception) {
        if ( isDebugEnabled() ) {
            delegate().debug(
                    interceptMarker(DEBUG, null, msg),
                    interceptMessage(DEBUG, null, msg),
                    exception);
        }
    }

    @Override
    public void debug(Marker marker, String msg) {
        if ( isDebugEnabled(marker) ) {
            delegate().debug(
                    interceptMarker(DEBUG, marker, msg),
                    interceptMessage(DEBUG, marker, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if ( isDebugEnabled(marker) ) {
            delegate().debug(
                    interceptMarker(DEBUG, marker, format),
                    interceptMessage(DEBUG, marker, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if ( isDebugEnabled(marker) ) {
            delegate().debug(
                    interceptMarker(DEBUG, marker, format),
                    interceptMessage(DEBUG, marker, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        if ( isDebugEnabled(marker) ) {
            delegate().debug(
                    interceptMarker(DEBUG, marker, format),
                    interceptMessage(DEBUG, marker, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Throwable exception) {
        if ( isDebugEnabled(marker) ) {
            delegate().debug(
                    interceptMarker(DEBUG, marker, msg),
                    interceptMessage(DEBUG, marker, msg),
                    exception);
        }
    }

    /*
     * info
     */

    @Override
    public boolean isInfoEnabled() {
        return delegate().isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate().isInfoEnabled(marker);
    }

    @Override
    public void info(String msg) {
        if ( isInfoEnabled() ) {
            delegate().info(
                    interceptMarker(INFO, null, msg),
                    interceptMessage(INFO, null, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if ( isInfoEnabled() ) {
            delegate().info(
                    interceptMarker(INFO, null, format),
                    interceptMessage(INFO, null, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if ( isInfoEnabled() ) {
            delegate().info(
                    interceptMarker(INFO, null, format),
                    interceptMessage(INFO, null, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if ( isInfoEnabled() ) {
            delegate().info(
                    interceptMarker(INFO, null, format),
                    interceptMessage(INFO, null, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable exception) {
        if ( isInfoEnabled() ) {
            delegate().info(
                    interceptMarker(INFO, null, msg),
                    interceptMessage(INFO, null, msg),
                    exception);
        }
    }

    @Override
    public void info(Marker marker, String msg) {
        if ( isInfoEnabled(marker) ) {
            delegate().info(
                    interceptMarker(INFO, marker, msg),
                    interceptMessage(INFO, marker, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if ( isInfoEnabled(marker) ) {
            delegate().info(
                    interceptMarker(INFO, marker, format),
                    interceptMessage(INFO, marker, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if ( isInfoEnabled(marker) ) {
            delegate().info(
                    interceptMarker(INFO, marker, format),
                    interceptMessage(INFO, marker, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        if ( isInfoEnabled(marker) ) {
            delegate().info(
                    interceptMarker(INFO, marker, format),
                    interceptMessage(INFO, marker, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable exception) {
        if ( isInfoEnabled(marker) ) {
            delegate().info(
                    interceptMarker(INFO, marker, msg),
                    interceptMessage(INFO, marker, msg),
                    exception);
        }
    }

    /*
     * warn
     */

    @Override
    public boolean isWarnEnabled() {
        return delegate().isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate().isWarnEnabled(marker);
    }

    @Override
    public void warn(String msg) {
        if ( isWarnEnabled() ) {
            delegate().warn(
                    interceptMarker(WARN, null, msg),
                    interceptMessage(WARN, null, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if ( isWarnEnabled() ) {
            delegate().warn(
                    interceptMarker(WARN, null, format),
                    interceptMessage(WARN, null, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if ( isWarnEnabled() ) {
            delegate().warn(
                    interceptMarker(WARN, null, format),
                    interceptMessage(WARN, null, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if ( isWarnEnabled() ) {
            delegate().warn(
                    interceptMarker(WARN, null, format),
                    interceptMessage(WARN, null, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable exception) {
        if ( isWarnEnabled() ) {
            delegate().warn(
                    interceptMarker(WARN, null, msg),
                    interceptMessage(WARN, null, msg),
                    exception);
        }
    }

    @Override
    public void warn(Marker marker, String msg) {
        if ( isWarnEnabled(marker) ) {
            delegate().warn(
                    interceptMarker(WARN, marker, msg),
                    interceptMessage(WARN, marker, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if ( isWarnEnabled(marker) ) {
            delegate().warn(
                    interceptMarker(WARN, marker, format),
                    interceptMessage(WARN, marker, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if ( isWarnEnabled(marker) ) {
            delegate().warn(
                    interceptMarker(WARN, marker, format),
                    interceptMessage(WARN, marker, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        if ( isWarnEnabled(marker) ) {
            delegate().warn(
                    interceptMarker(WARN, marker, format),
                    interceptMessage(WARN, marker, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable exception) {
        if ( isWarnEnabled(marker) ) {
            delegate().warn(
                    interceptMarker(WARN, marker, msg),
                    interceptMessage(WARN, marker, msg),
                    exception);
        }
    }

    /*
     * error
     */

    @Override
    public boolean isErrorEnabled() {
        return delegate().isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate().isErrorEnabled(marker);
    }

    @Override
    public void error(String msg) {
        if ( isErrorEnabled() ) {
            delegate().error(
                    interceptMarker(ERROR, null, msg),
                    interceptMessage(ERROR, null, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if ( isErrorEnabled() ) {
            delegate().error(
                    interceptMarker(ERROR, null, format),
                    interceptMessage(ERROR, null, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if ( isErrorEnabled() ) {
            delegate().error(
                    interceptMarker(ERROR, null, format),
                    interceptMessage(ERROR, null, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if ( isErrorEnabled() ) {
            delegate().error(
                    interceptMarker(ERROR, null, format),
                    interceptMessage(ERROR, null, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void error(String msg, Throwable exception) {
        if ( isErrorEnabled() ) {
            delegate().error(
                    interceptMarker(ERROR, null, msg),
                    interceptMessage(ERROR, null, msg),
                    exception);
        }
    }

    @Override
    public void error(Marker marker, String msg) {
        if ( isErrorEnabled(marker) ) {
            delegate().error(
                    interceptMarker(ERROR, marker, msg),
                    interceptMessage(ERROR, marker, msg),
                    (Throwable)null);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if ( isErrorEnabled(marker) ) {
            delegate().error(
                    interceptMarker(ERROR, marker, format),
                    interceptMessage(ERROR, marker, format),
                    (Throwable)null, arg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if ( isErrorEnabled(marker) ) {
            delegate().error(
                    interceptMarker(ERROR, marker, format),
                    interceptMessage(ERROR, marker, format),
                    (Throwable)null, arg1, arg2);
        }
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if ( isErrorEnabled(marker) ) {
            delegate().error(
                    interceptMarker(ERROR, marker, format),
                    interceptMessage(ERROR, marker, format),
                    (Throwable)null, arguments);
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable exception) {
        if ( isErrorEnabled(marker) ) {
            delegate().error(
                    interceptMarker(ERROR, marker, msg),
                    interceptMessage(ERROR, marker, msg),
                    exception);
        }
    }

    /*
     * interceptions
     */

    protected String interceptMessage(Level level, Marker marker, String message) {
        return message;
    }
    
    protected Marker interceptMarker(Level level, Marker marker, String message) {
        return marker;
    }

    /**
     * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
     */
    public static enum Level {

        TRACE, DEBUG, INFO, WARN, ERROR

    }
}
