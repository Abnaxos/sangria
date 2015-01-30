package ch.raffael.sangria.annotations.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Index implements Set<Index.Entry> {

    public static final String RESOURCE_FILE = "ch.raffael.sangria.index";
    public static final String RESOURCE_PATH = "META-INF/" + RESOURCE_FILE;
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Pattern STRIP_SPACES_RE = Pattern.compile("\\s*\\.\\s*");
    private static final String JAVA_IDENT_RE_SRC = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern QNAME_RE = Pattern.compile(JAVA_IDENT_RE_SRC + "(\\." + JAVA_IDENT_RE_SRC + ")*");
    private static final Pattern NEWLINE_RE = Pattern.compile("\\r\\n?|\\n");
    private static final String NEWLINE = System.lineSeparator();

    private final Set<Entry> entries = new LinkedHashSet<>();

    public Index load(URL url) throws IOException {
        try ( InputStream input = url.openStream() ) {
            return load(input);
        }
    }

    public Index load(InputStream stream) throws IOException {
        return load(new InputStreamReader(stream, CHARSET));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public Index load(Reader reader) throws IOException {
        BufferedReader bufReader;
        if ( reader instanceof BufferedReader ) {
            bufReader = (BufferedReader)reader;
        }
        else {
            bufReader = new BufferedReader(reader);
        }
        int lineNumber = 0;
        String line;
        while ( (line = bufReader.readLine()) != null ) {
            lineNumber++;
            int pos = line.indexOf('#');
            if ( pos >= 0 ) {
                line = line.substring(0, pos);
            }
            line = line.trim();
            if ( line.isEmpty() ) {
                continue;
            }
            // when introducing a way to merge index files:
            //     boolean remove = line.startsWith("-");
            //     if ( remove ) {
            //         line = line.substring(1).trim();
            //     }
            Kind kind;
            String name;
            for ( pos = 0; pos < line.length() && !Character.isWhitespace(line.charAt(pos)); pos++ );
            if ( !(pos < line.length()) ) {
                throw new IndexSyntaxException(lineNumber, "No entry kind specified");
            }
            kind = Kind.forLabel(line.substring(0, pos));
            if ( kind == null ) {
                throw new IndexSyntaxException(lineNumber, "Unknown kind: '" + line.substring(0, pos) + "'");
            }
            name = line.substring(pos + 1).trim();
            for ( pos = 0; pos < name.length(); pos++ ) {
                if ( Character.isWhitespace(name.charAt(pos)) ) {
                    name = STRIP_SPACES_RE.matcher(name).replaceAll(".");
                }
            }
            if ( !QNAME_RE.matcher(name).matches() ) {
                throw new IndexSyntaxException(lineNumber, "Invalid Java name: '" + name + "'");
            }
            add(new Entry(kind, name));
            // when introducing a way to merge index files:
            //     if ( !remove ) {
            //         add(new Entry(kind, name));
            //     }
            //     else {
            //         remove(new Entry(kind, name));
            //     }
        }
        return this;
    }

    public Index write(OutputStream out) throws IOException {
        return write(null, out);
    }

    public Index write(String header, OutputStream out) throws IOException {
        return write(header, new OutputStreamWriter(out, CHARSET));
    }

    public Index write(Writer out) throws IOException {
        return write(null, out);
    }

    public Index write(String header, Writer out) throws IOException {
        if ( header != null ) {
            String[] headerLines = NEWLINE_RE.split(header);
            for ( String line : headerLines ) {
                out.write("# ");
                out.write(line);
                out.write(NEWLINE);
            }
            out.write(NEWLINE);
        }
        for ( Entry entry : entries ) {
            out.write(entry.toString());
            out.write(NEWLINE);
        }
        out.flush();
        return this;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return entries.contains(o);
    }

    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    @Override
    public Object[] toArray() {
        return entries.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return entries.toArray(a);
    }

    @Override
    public boolean add(Entry entry) {
        return entries.add(entry);
    }

    @Override
    public boolean remove(Object o) {
        return entries.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return entries.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Entry> c) {
        return entries.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return entries.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return entries.removeAll(c);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    @SuppressWarnings({ "EqualsWhichDoesntCheckParameterClass", "SimplifiableIfStatement" })
    @Override
    public boolean equals(Object o) {
        if ( o == null ) {
            return false;
        }
        if ( o == this ) {
            return true;
        }
        return entries.equals(o);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    public static final class Entry {

        private final Kind kind;
        private final String name;

        public Entry(Kind kind, String name) {
            this.kind = kind;
            this.name = name;
        }

        public static Entry forClass(String name) {
            return new Entry(Kind.CLASS, name);
        }

        public static Entry forPackage(String name) {
            return new Entry(Kind.PACKAGE, name);
        }

        @Override
        public String toString() {
            return kind.label() + " " + name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, name);
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null || getClass() != obj.getClass() ) {
                return false;
            }
            final Entry other = (Entry)obj;
            return Objects.equals(this.kind, other.kind) && Objects.equals(this.name, other.name);
        }

        public Kind kind() {
            return kind;
        }

        public String name() {
            return name;
        }

        public String resourceName() {
            return kind.resourceName(name);
        }

    }

    public static enum Kind {
        CLASS {
                    @Override
                    public String resourceName(String name) {
                        return name.replace('.', '/') + ".class";
                    }
                },
        PACKAGE {
                    @Override
                    public String resourceName(String name) {
                        return name.replace('.', '/') + "/package-info.class";
                    }
                };

        private static final Kind[] values = values();
        private final String label;

        Kind() {
            this.label = name().toLowerCase();
        }

        public abstract String resourceName(String name);

        public String label() {
            return label;
        }

        public static Kind forLabel(String label) {
            if ( label == null ) {
                return null;
            }
            for ( Kind kind : values ) {
                if ( kind.label().equals(label) ) {
                    return kind;
                }
            }
            return null;
        }

    }
}
