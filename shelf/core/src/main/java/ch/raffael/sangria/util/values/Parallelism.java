package ch.raffael.sangria.util.values;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Parallelism {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final Pattern REGEX = Pattern.compile("(?<core>(\\d+|\\d*\\.\\d+)\\s*[cC]?)\\s*((?<fixed>!)|(-\\s*(?<max>(\\d+|\\d*\\.\\d+)\\s*[cC]?)))?");

    private final int coreThreads;
    private final int maxThreads;

    private Parallelism(int coreThreads, int maxThreads) {
        this.coreThreads = coreThreads;
        this.maxThreads = maxThreads;
    }

    public static Parallelism get(int core, int max) {
        if ( core <= 0 ) {
            throw new IllegalArgumentException("Invalid core thread count: " + core);
        }
        else if ( max < 0 ) {
            throw new IllegalArgumentException("Invalid core thread count: " + core);
        }
        if ( max > 0 && max < core ) {
            throw new IllegalArgumentException("maxThreads(" + max + ") < coreThreads(" + core + ")");
        }
        return new Parallelism(core, max);
    }

    public static Parallelism get(int core) {
        return get(core, 0);
    }

    public static Parallelism fixed(int count) {
        return get(count, count);
    }

    public static Parallelism parse(String string) {
        Matcher matcher = REGEX.matcher(string.trim());
        if ( !matcher.matches() ) {
            throw invalidParallelism(string);
        }
        int core = threadCount(matcher.group("core"));
        if ( core < 0 ) {
            throw invalidParallelism(string);
        }
        int max;
        if ( matcher.group("fixed") != null ) {
            max = core;
        }
        else if ( matcher.group("max") != null ) {
            max = threadCount(matcher.group("max"));
            if ( max < 0 ) {
                throw invalidParallelism(string);
            }
        }
        else {
            max = 0;
        }
        return get(core, max);
    }

    private static int threadCount(String string) {
        if ( Character.toUpperCase(string.charAt(string.length() - 1)) == 'C' ) {
            string = string.substring(0, string.length() - 1).trim();
            int pos = string.indexOf('.');
            int multiplier = 0;
            if ( pos > 0 ) {
                multiplier = Integer.valueOf(string.substring(0, pos))*1000;
            }
            else if ( pos < 0 ) {
                multiplier = Integer.valueOf(string) * 1000;
            }
            if ( pos >= 0 ) {
                String dec = string.substring(pos, string.length());
                if ( dec.length() > 3 ) {
                    dec = dec.substring(0, 3);
                }
                multiplier += Integer.valueOf(dec);
            }
            int count = (CORES * 1000 * multiplier) / 1000;
            if ( count == 0 ) {
                count = 1;
            }
            return count;
        }
        else {
            if ( string.indexOf('.') >= 0 ) {
                return -1;
            }
            return Integer.valueOf(string);
        }
    }

    private static IllegalArgumentException invalidParallelism(String string) {
        return new IllegalArgumentException("Invalid parallelism string: '" + string + "'");
    }

    public int getCoreThreads() {
        return coreThreads;
    }

    public int getMaxThreads() {
        return getMaxThreads(coreThreads);
    }

    public int getMaxThreads(int fallback) {
        return maxThreads == 0 ? fallback : maxThreads;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coreThreads, maxThreads);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final Parallelism other = (Parallelism)obj;
        return Objects.equals(this.coreThreads, other.coreThreads) && Objects.equals(this.maxThreads, other.maxThreads);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + coreThreads + "-" + maxThreads + "}";
    }

    public static class Converter implements TypeConverter {
        private static final Converter INSTANCE = new Converter();
        @Override
        public Object convert(String value, TypeLiteral<?> toType) {
            if ( toType.getRawType().equals(Parallelism.class) ) {
                return parse(value);
            }
            else {
                throw new IllegalArgumentException("Cannot convert to " + toType);
            }
        }
        public static Converter getInstance() {
            return INSTANCE;
        }
        public static void bind(Binder binder) {
            binder.convertToTypes(Matchers.identicalTo(Parallelism.class), getInstance());
        }
    }

}
