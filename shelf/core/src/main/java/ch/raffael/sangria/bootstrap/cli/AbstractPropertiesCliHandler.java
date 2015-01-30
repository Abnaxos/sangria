package ch.raffael.sangria.bootstrap.cli;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import ch.raffael.sangria.libs.args4j.Option;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractPropertiesCliHandler {

    public void set(String property) {
        int pos = Math.min(property.indexOf('='), property.indexOf(':'));
        if ( pos < 0 ) {
            set(property, "");
        }
        else {
            set(property.substring(0, pos), property.substring(pos + 1));
        }
    }

    protected abstract void set(String key, String value);

    public static class SystemPropertiesCliHandler extends AbstractPropertiesCliHandler {

        @Override
        @Option(name = "-D",
                usage = "Set a system property",
                metaVar = "KEY=VALUE")
        public void set(String property) {
            super.set(property);
        }

        @Override
        protected void set(String key, String value) {
            System.setProperty(key, value);
        }
    }

}
