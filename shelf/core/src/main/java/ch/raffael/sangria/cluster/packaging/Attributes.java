package ch.raffael.sangria.cluster.packaging;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import ch.raffael.sangria.libs.guava.base.MoreObjects;
import ch.raffael.sangria.libs.guava.collect.ForwardingMap;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;
import ch.raffael.sangria.libs.guava.collect.Maps;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Attributes {

    private static final Attributes EMPTY = new Attributes(ImmutableMap.of());

    private final ImmutableMap<String, String> map;

    private Attributes(Map<String, String> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    public static Attributes empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Map<String, String> presets) {
        return new Builder(presets);
    }

    public static Attributes copyOf(Map<String, String> map) {
        return new Attributes(map);
    }

    public static Attributes copyOf(java.util.jar.Attributes jarAttributes) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        jarAttributes.entrySet().forEach(entry -> builder.put((String)entry.getKey(), (String)entry.getValue()));
        return new Attributes(builder.build());
    }

    public String get(String key) {
        return map.get(key);
    }

    public String get(String key, String fallback) {
        return MoreObjects.firstNonNull(map.get(key), fallback);
    }

    public Iterable<String> attributeNames() {
        return map.keySet();
    }

    public Stream<String> attributeNameStream() {
        return map.keySet().stream();
    }

    public Iterable<Map.Entry<String, String>> entries() {
        return map.entrySet();
    }

    public Stream<Map.Entry<String, String>> entryStream() {
        return map.entrySet().stream();
    }

    public static final class Builder extends ForwardingMap<String, String> {

        private final Map<String, String> map;

        private Builder() {
            map = new LinkedHashMap<>();
        }

        private Builder(Map<String, String> presets) {
            map = new LinkedHashMap<>(presets);
        }

        @Override
        protected Map<String, String> delegate() {
            return map;
        }

        public Builder set(String key, String value) {
            put(key, value);
            return this;
        }

        public Builder setAll(Map<String, String> map) {
            putAll(map);
            return this;
        }

        public Attributes build() {
            return new Attributes(map);
        }

    }

}
