package ch.raffael.sangria.testutil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.raffael.sangria.libs.guava.base.Function;
import ch.raffael.sangria.libs.guava.base.Predicate;
import ch.raffael.sangria.libs.guava.collect.Collections2;
import ch.raffael.sangria.libs.guava.collect.Iterables;
import ch.raffael.sangria.libs.guava.collect.Iterators;

import ch.raffael.sangria.annotations.index.Index;


/**
 * A class loader that caps the classes and resources in the specified packages. This is
 * used for testing the assembly without having to split the tests into several projects.
 * Use the `CappingClassLoader` as class loader for the assembly, then use {@link
 * TempFiles} to copy these capped classes and resources to a temporary directory to
 * make them loadable by child class loaders.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class CappingClassLoader extends ClassLoader {

    static {
        registerAsParallelCapable();
    }

    private final static Function<String, String> PREFIX = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input.replace('.', '/');
        }
    };

    private final Object capLock = new Object();
    private final Set<String> capPrefixes = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> nocapPrefixes = Collections.synchronizedSet(new HashSet<>());
    private boolean upToDate = true;

    private final Map<URL, URL> cappedIndexes = new HashMap<>();

    public CappingClassLoader(ClassLoader parent) {
        super(parent);
    }

    CappingClassLoader cap(Iterable<String> capPrefixes) {
        synchronized ( capLock ) {
            if ( capPrefixes instanceof Collection ) {
                upToDate = !this.capPrefixes.addAll(Collections2.transform((Collection<String>)capPrefixes, PREFIX));
            }
            else {
                upToDate = !Iterables.addAll(this.capPrefixes, Iterables.transform(capPrefixes, PREFIX));
            }
            return this;
        }
    }

    CappingClassLoader cap(String... capPrefixes) {
        return cap(Arrays.asList(capPrefixes));
    }

    CappingClassLoader nocap(Iterable<String> nocapPrefixes) {
        synchronized ( capLock ) {
            if ( nocapPrefixes instanceof Collection ) {
                upToDate = !this.nocapPrefixes.addAll(Collections2.transform((Collection<String>)nocapPrefixes, PREFIX));
            }
            else {
                upToDate = !Iterables.addAll(this.nocapPrefixes, Iterables.transform(nocapPrefixes, PREFIX));
            }
            return this;
        }
    }

    CappingClassLoader nocap(String... nocapPrefixes) {
        return nocap(Arrays.asList(nocapPrefixes));
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if ( isCapped(name.replace('.', '/')) ) {
            throw new ClassNotFoundException(name);
        }
        else {
            return super.loadClass(name);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if ( isCapped(name.replace('.', '/')) ) {
            throw new ClassNotFoundException(name);
        }
        else {
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public URL getResource(String name) {
        if ( name.equals(Index.RESOURCE_PATH) ) {
            try {
                return cappedIndex(super.getResource(name));
            }
            catch ( IOException e ) {
                return null;
            }
        }
        else if ( isCapped(name) ) {
            return null;
        }
        else {
            return super.getResource(name);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if ( name.equals(Index.RESOURCE_PATH) ) {
            List<URL> urls = new ArrayList<>();
            Enumeration<URL> urlEnum = super.getResources(name);
            while ( urlEnum.hasMoreElements() ) {
                urls.add(cappedIndex(urlEnum.nextElement()));
            }
            return Iterators.asEnumeration(urls.iterator());
        }
        else if ( isCapped(name) ) {
            return Collections.emptyEnumeration();
        }
        else {
            return super.getResources(name);
        }
    }

    //@Override
    //public InputStream getResourceAsStream(String name) {
    //    if ( name.equals(IndexReader.INDEX_RESOURCE_PATH) ) {
    //        URL url = getResource(name);
    //        if ( url == null ) {
    //            return null;
    //        }
    //        else {
    //            try {
    //                return url.openStream();
    //            }
    //            catch ( IOException e ) {
    //                return
    //            }
    //        }
    //    }
    //    else if ( isCapped(name) ) {
    //        return null;
    //    }
    //    else {
    //        return super.getResourceAsStream(name);
    //    }
    //}

    @Override
    protected Package getPackage(String name) {
        if ( isCapped(name.replace('.', '/')) ) {
            return null;
        }
        else {
            return super.getPackage(name);
        }
    }

    @Override
    protected Package[] getPackages() {
        Collection<Package> filtered = Collections2.filter(Arrays.asList(super.getPackages()), new Predicate<Package>() {
            @Override
            public boolean apply(Package input) {
                return !isCapped(input.getName().replace('.', '/'));
            }
        });
        return filtered.toArray(new Package[filtered.size()]);
    }

    private boolean isCapped(String name) {
        synchronized ( capLock ) {
            if ( !upToDate ) {
                capPrefixes.removeAll(nocapPrefixes);
                nocapPrefixes.removeAll(capPrefixes);
                upToDate = true;
            }
            return isContainedIn(name, capPrefixes) && !isContainedIn(name, nocapPrefixes);
        }
    }

    private boolean isContainedIn(String name, Set<String> prefixes) {
        for ( String prefix : prefixes ) {
            if ( name.startsWith(prefix + "/") || name.equals(prefix) ) {
                return true;
            }
        }
        return false;
    }

    private URL cappedIndex(URL indexUrl) throws IOException {
        if ( indexUrl == null ) {
            return null;
        }
        synchronized ( cappedIndexes ) {
            URL cappedUrl = cappedIndexes.get(indexUrl);
            if ( cappedUrl == null ) {
                Path cappedFile = TempFiles.createTempFile("index");
                Index index = new Index();
                index.load(indexUrl);
                index.removeIf(entry -> isCapped(entry.name().replace('.', '/')));
                try ( OutputStream output = new BufferedOutputStream(Files.newOutputStream(cappedFile)) ) {
                    index.write("capPrefixes: " + capPrefixes + "\nnocapPrefixes: " + nocapPrefixes, output);
                }
                cappedUrl = cappedFile.toUri().toURL();
                cappedIndexes.put(indexUrl, cappedUrl);
            }
            return cappedUrl;
        }
    }

}
