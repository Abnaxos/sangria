package ch.raffael.sangria.bootstrap.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import ch.raffael.sangria.libs.args4j.Option;

import ch.raffael.sangria.bootstrap.AssemblyBuilder;
import ch.raffael.sangria.bootstrap.BootstrapException;
import ch.raffael.sangria.bootstrap.BundleScanner;
import ch.raffael.sangria.logging.Logging;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class BundleCliHandler {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logging.logger();

    private final AssemblyBuilder assembly;

    private final Set<String> includes = new LinkedHashSet<>();
    private final Set<String> excludes = new LinkedHashSet<>();
    private final List<BundleScanner> scanners = new ArrayList<>();

    public BundleCliHandler(AssemblyBuilder assembly) {
        this.assembly = assembly;
    }

    @Option(name = "-include",
            usage = "Add an include pattern",
            metaVar = "PATTERN")
    public void include(String pattern) {
        includes.add(pattern);
    }

    @Option(name = "-exclude",
            usage = "Add an exclude pattern",
            metaVar = "PATTERN")
    public void exclude(String pattern) {
        excludes.add(pattern);
    }

    @Option(name = "-scan",
            usage="Scan a directory tree using the current patterns",
            metaVar = "DIR")
    public void scan(File path) {
        BundleScanner scanner = new BundleScanner(assembly, path.toPath()).include(includes).exclude(excludes);
        scanners.add(scanner);
    }

    @Option(name="-clear-patterns",
    usage="Clear the current include/exclude list")
    public void clearPatterns() {
        includes.clear();
        excludes.clear();
    }

    public void scanAll(AssemblyBuilder assembly) throws IOException, BootstrapException {
        for ( BundleScanner scanner : scanners ) {
            scanner.scan();
        }
    }

}

