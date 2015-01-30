package ch.raffael.sangria.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.raffael.sangria.libs.args4j.Argument;
import ch.raffael.sangria.libs.guava.collect.ImmutableMap;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class GeneralOptions {

    private static final Pattern ENV_ENTRY_RE = Pattern.compile("\\s*(?<key>[^:=])\\s*([=:]\\s*(?<val>.*)\\s*)?");

    private final Map<String, String> environmentEntries = new LinkedHashMap<>();

    @Argument(
            usage = "Environment entries",
            metaVar = "KEY[=VALUE]",
            multiValued = true)
    public void setEnvironmentEntry(String spec) {
        Matcher m = ENV_ENTRY_RE.matcher(spec);
        if ( m.matches() ) {
            String key = m.group("key");
            String val = m.group("val");
            if ( val == null ) {
                environmentEntries.remove(key);
            }
            else {
                environmentEntries.put(key, val);
            }
        }
    }

    public Map<String, String> getEnvironmentEntries() {
        return ImmutableMap.copyOf(environmentEntries);
    }

}
