package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.raffael.sangria.dynamic.asm.AnnotationVisitor;
import ch.raffael.sangria.dynamic.asm.ClassReader;
import ch.raffael.sangria.dynamic.asm.ClassVisitor;
import ch.raffael.sangria.dynamic.asm.Opcodes;
import ch.raffael.sangria.dynamic.asm.Type;
import ch.raffael.sangria.libs.guava.collect.ImmutableSet;
import ch.raffael.sangria.libs.guava.collect.MapMaker;

import ch.raffael.guards.Nullable;
import ch.raffael.sangria.annotations.DependsOn;
import ch.raffael.sangria.annotations.Extends;
import ch.raffael.sangria.annotations.OutboundLinks;
import ch.raffael.sangria.annotations.LinkTo;
import ch.raffael.sangria.annotations.Phase;
import ch.raffael.sangria.annotations.Provides;
import ch.raffael.sangria.annotations.Using;
import ch.raffael.sangria.annotations.index.Index;

import static ch.raffael.sangria.libs.guava.base.Objects.firstNonNull;
import static ch.raffael.sangria.libs.guava.base.Objects.toStringHelper;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class AssemblyAdvice {

    private static final int ASM = Opcodes.ASM4;

    private static final Logger log = LoggerFactory.getLogger(AssemblyAdvice.class);
    private final static ConcurrentMap<URL, AssemblyAdvice> CACHE = new MapMaker().concurrencyLevel(1).makeMap();

    private final URL url;
    private final Index.Entry key;

    private final Object loadLock = new Object();
    private volatile boolean loaded = false;

    // @Feature
    private boolean feature;
    private Set<String> extend;
    private Set<String> provides;
    // @LinkTo
    private Set<String> linkTo;
    // ExternalLinks
    private Set<String> outboundLinks;
    // @Use
    private Set<String> usedFeatures;
    // @Install
    private Set<Phase> install;
    // @DependsOn
    private Set<String> dependencies;

    private AssemblyAdvice(URL url, Index.Entry key) {
        this.url = url;
        this.key = key;
    }

    @Override
    public String toString() {
        return toStringHelper(this).addValue(url).toString();
    }

    @Nullable
    public static AssemblyAdvice forClass(ResourceLocator resourceLocator, String name) throws IOException {
        return forKey(resourceLocator, Index.Entry.forClass(name));
    }

    @Nullable
    public static AssemblyAdvice forPackage(ResourceLocator resourceLocator, String name) throws IOException {
        return forKey(resourceLocator, Index.Entry.forPackage(name));
    }

    public static AssemblyAdvice forKey(ResourceLocator resourceLocator, Index.Entry key) throws IOException {
        URL url = resourceLocator.getResource(key.resourceName());
        if ( url == null ) {
            return null;
        }
        AssemblyAdvice advice = CACHE.get(url);
        if ( advice == null ) {
            advice = new AssemblyAdvice(url, key);
            AssemblyAdvice prev = CACHE.putIfAbsent(url, advice);
            if ( prev != null ) {
                advice = prev;
            }
        }
        advice.load();
        return advice;
    }

    public URL url() {
        return url;
    }

    public String name() {
        return key.name();
    }

    public Index.Kind kind() {
        return key.kind();
    }

    public boolean feature() {
        return feature;
    }

    public Set<Phase> install() {
        return install;
    }

    public Set<String> extend() {
        return extend;
    }

    public Set<String> provides() {
        return provides;
    }

    public Set<String> linkTo() {
        return linkTo;
    }

    public Set<String> outboundLinks() {
        return outboundLinks;
    }

    public Set<String> usedFeatures() {
        return usedFeatures;
    }

    public Set<String> dependencies() {
        return dependencies;
    }

    private void load() throws IOException {
        if ( loaded ) {
            return;
        }
        synchronized ( loadLock ) {
            if ( loaded ) {
                return;
            }
            log.trace("Loading assembly advice for {} from {}", key.name(), url);
            try ( InputStream classInput = url.openStream() ) {
                ClassReader reader = new ClassReader(classInput);
                reader.accept(new ReadClass(), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
            }
            extend = firstNonNull(extend, ImmutableSet.<String>of());
            provides = firstNonNull(provides, ImmutableSet.<String>of());
            linkTo = firstNonNull(linkTo, ImmutableSet.<String>of());
            outboundLinks = firstNonNull(outboundLinks, ImmutableSet.<String>of());
            usedFeatures = firstNonNull(usedFeatures, ImmutableSet.<String>of());
            dependencies = firstNonNull(dependencies, ImmutableSet.<String>of());
            loaded = true;
        }
    }

    private <T> T setOnce(String name, T prevValue, T newValue) {
        if ( prevValue != null ) {
            //log.warn("{}: {}: Ignoring duplicate value", url, name);
            return prevValue;
        }
        else {
            return newValue;
        }
    }

    private class ReadClass extends ClassVisitor {

        private ReadClass() {
            this(null);
        }

        private ReadClass(ClassVisitor cv) {
            super(Opcodes.ASM4, cv);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor av = super.visitAnnotation(desc, visible);
            switch ( desc ) {
                case "Lch/raffael/sangria/annotations/Feature;":
                    feature = true;
                    return av;
                case "Lch/raffael/sangria/annotations/Extends$List;":
                case "Lch/raffael/sangria/annotations/Extends;":
                    return new RepeatableReader<Extends, String>(av, Extends.class, String.class) {
                        @Override
                        public void visitEnd() {
                            extend = setOnce("@Extends", extend, values());
                        }
                    }.visitorFor(desc);
                case "Lch/raffael/sangria/annotations/Provides$List;":
                case "Lch/raffael/sangria/annotations/Provides;":
                    return new RepeatableReader<Provides, String>(av, Provides.class, String.class) {
                        @Override
                        public void visitEnd() {
                            provides = setOnce("@Provides", provides, values());
                        }
                    }.visitorFor(desc);
                case "Lch/raffael/sangria/annotations/LinkTo$List;":
                case "Lch/raffael/sangria/annotations/LinkTo;":
                    return new RepeatableReader<LinkTo, String>(av, LinkTo.class, String.class) {
                        @Override
                        public void visitEnd() {
                            linkTo = setOnce("linkTo", linkTo, values());
                        }
                    }.visitorFor(desc);
                case "Lch/raffael/sangria/annotations/OutboundLinks$List;":
                case "Lch/raffael/sangria/annotations/OutboundLinks;":
                    return new RepeatableReader<OutboundLinks, String>(av, OutboundLinks.class, String.class) {
                        @Override
                        public void visitEnd() {
                            outboundLinks = setOnce("outboundLinks", outboundLinks, values());
                        }
                    }.visitorFor(desc);
                case "Lch/raffael/sangria/annotations/Install;":
                    return new ReadInstall(av);
                case "Lch/raffael/sangria/annotations/Using$List;":
                case "Lch/raffael/sangria/annotations/Using;":
                    return new RepeatableReader<Using, String>(av, Using.class, String.class) {
                        @Override
                        public void visitEnd() {
                            usedFeatures = setOnce("@Using", usedFeatures, values());
                        }
                    }.visitorFor(desc);
                case "Lch/raffael/sangria/annotations/DependsOn$List;":
                case "Lch/raffael/sangria/annotations/DependsOn;":
                    return new RepeatableReader<DependsOn, Type>(av, DependsOn.class, Type.class) {
                        @Override
                        public void visitEnd() {
                            dependencies = setOnce("@DependsOn", dependencies, ImmutableSet.copyOf(values().stream().map(Type::getClassName).iterator()));
                        }
                    }.visitorFor(desc);
                default:
                    return av;
            }
        }
    }

    private abstract class RepeatableReader<R extends Annotation, T> extends AnnotationVisitor {

        private final Type annotationType;
        private final Type containerType;
        private final Class<T> valueType;

        private final AnnotationVisitor parent;

        private final ImmutableSet.Builder<T> valueBuilder = ImmutableSet.builder();

        protected RepeatableReader(AnnotationVisitor annotationVisitor, Class<R> annotationType, Class<T> valueType) {
            super(Opcodes.ASM5, annotationVisitor);
            this.parent = annotationVisitor;
            Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
            if ( repeatable == null ) {
                throw new IllegalArgumentException("Annotation type " + annotationType.getName() + " is not repeatable");
            }
            this.annotationType = Type.getType(annotationType);
            containerType = Type.getType(repeatable.value());
            this.valueType = valueType;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            AnnotationVisitor av = super.visitArray(name);
            if ( "value".equals(name) ) {
                return new AnnotationVisitor(Opcodes.ASM5, av) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String name, String desc) {
                        AnnotationVisitor av = super.visitAnnotation(name, desc);
                        if ( annotationType.getDescriptor().equals(desc) ) {
                            return new ValueVisitor(av);
                        }
                        else {
                            return av;
                        }
                    }
                };
            }
            else {
                return av;
            }
        }

        protected Iterable<? extends T> collect(T value) {
            return null;
        }

        protected Set<T> values() {
            return valueBuilder.build();
        }

        public AnnotationVisitor visitorFor(String desc) {
            if ( desc.equals(containerType.getDescriptor()) ) {
                return this;
            }
            else if ( desc.equals(annotationType.getDescriptor()) ) {
                return new ValueVisitor(parent) {
                    @Override
                    public void visitEnd() {
                        RepeatableReader.this.visitEnd();
                    }
                };
            }
            else {
                throw new IllegalArgumentException("Illegal descriptor: " + desc);
            }
        }

        private class ValueVisitor extends AnnotationVisitor {

            private ValueVisitor(AnnotationVisitor annotationVisitor) {
                super(Opcodes.ASM5, annotationVisitor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void visit(String name, Object value) {
                super.visit(name, value);
                if ( "value".equals(name) && valueType.isInstance(value) ) {
                    T val = (T)value;
                    Iterable<? extends T> iter = collect(val);
                    if ( iter == null ) {
                        valueBuilder.add(val);
                    }
                    else {
                        valueBuilder.addAll(iter);
                    }
                }
                else {
                    throw new RuntimeException();
                }
            }
        }

    }

    private class ReadInstall extends AnnotationVisitor {

        private final EnumSet<Phase> phases = EnumSet.noneOf(Phase.class);

        private ReadInstall() {
            this(null);
        }

        private ReadInstall(AnnotationVisitor av) {
            super(ASM, av);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            AnnotationVisitor av = super.visitArray(name);
            if ( name.equals("value") ) {
                return new AnnotationVisitor(ASM, av) {
                    @Override
                    public void visitEnum(String name, String desc, String value) {
                        super.visitEnum(name, desc, value);
                        phases.add(Phase.valueOf(value));
                    }
                };
            }
            else {
                return av;
            }
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if ( phases.isEmpty() ) {
                phases.add(Phase.RUNTIME);
            }
            install = setOnce("@Install.value", install, phases);
        }
    }

}
