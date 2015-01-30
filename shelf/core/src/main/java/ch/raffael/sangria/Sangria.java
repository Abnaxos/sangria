package ch.raffael.sangria;

import java.io.IOException;

import com.google.inject.Module;

import ch.raffael.sangria.libs.guava.base.Charsets;
import ch.raffael.sangria.libs.guava.io.Resources;

import ch.raffael.sangria.bootstrap_old.Bootstrapper;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Sangria {

    private static final String VERSION;
    static {
        try {
            VERSION = Resources.toString(Sangria.class.getResource("version.txt"), Charsets.UTF_8).trim();
        }
        catch ( IOException e ) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Bootstrapper bootstrapper() {
        return Bootstrapper.create();
    }

    public static Bootstrapper bootstrapper(Module... modules) {
        return Bootstrapper.create(modules);
    }

    public static Bootstrapper bootstrapper(Iterable<Module> modules) {
        return Bootstrapper.create(modules);
    }

    public static String version() {
        return VERSION;
    }

}
