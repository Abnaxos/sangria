package ch.raffael.sangria.core;

import org.slf4j.Logger;

//import ch.raffael.sangria.bootstrap.Bootstrapper;
import ch.raffael.sangria.logging.Logging;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Sangria {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logging.logger();

    //public Injector bootstrap(String[] args, ClassLoader loader) {
    //    ServiceLoader<BootstrapModule> bootstrapModuleLoader = ServiceLoader.load(BootstrapModule.class, Classes.classLoader(loader, Classes.callerClass()));
    //    LinkedList<Module> modules = new LinkedList<>();
    //    InternalBootstrapper bootstrapper = new InternalBootstrapper(bootstrapModuleLoader, args);
    //    modules.addFirst(bootstrapper);
    //    for ( BootstrapModule bootstrapModule : bootstrapModuleLoader ) {
    //        modules.add(bootstrapModule);
    //    }
    //    Injector bootstrapInjector = Guice.createInjector(modules);
    //    Assembly assembly = bootstrapper.bootstrap(bootstrapInjector);
    //}

    //public Assembly bootstrap(ClassLoader loader, Bootstrapper... bootstrappers) {
    //}
    //
    //public Assembly bootstrap(ClassLoader loader, Iterable<? extends Bootstrapper> bootstrappers) {
    //    Classes.classLoader(loader, Classes.callerClass());
    //}

    //private static final class InternalBootstrapper extends AbstractModule {
    //
    //    private final ServiceLoader<BootstrapModule> bootstrapModuleLoader;
    //    private final Collection<Class<?>> optionsBeanClasses = new LinkedHashSet<>();
    //    private final List<String> cmdLineArgs;
    //
    //    private InternalBootstrapper(ServiceLoader<BootstrapModule> bootstrapModuleLoader, String[] cmdLineArgs) {
    //        this(bootstrapModuleLoader, ImmutableList.copyOf(cmdLineArgs));
    //    }
    //
    //    private InternalBootstrapper(ServiceLoader<BootstrapModule> bootstrapModuleLoader, List<String> cmdLineArgs) {
    //        this.bootstrapModuleLoader = bootstrapModuleLoader;
    //        optionsBeanClasses.add(GeneralOptions.class);
    //        this.cmdLineArgs = ImmutableList.copyOf(cmdLineArgs);
    //    }
    //
    //    @Override
    //    protected void configure() {
    //        bind(Phase.class).toInstance(Phase.BOOTSTRAP);
    //        bind(new TypeLiteral<List<String>>(){}).annotatedWith(CmdLineOptions.class).toInstance(cmdLineArgs);
    //        bootstrapModuleLoader.iterator().forEachRemaining(m -> {
    //            CmdLineOptions.OptionTargets optionsTargets = m.getClass().getAnnotation(CmdLineOptions.OptionTargets.class);
    //            Arrays.stream(optionsTargets.value())
    //                    .filter(c -> {
    //                        if ( c.isInterface() || c.isEnum() ) {
    //                            addError("Invalid options receiver: %s", c);
    //                            return true;
    //                        }
    //                        else {
    //                            return false;
    //                        }
    //                    })
    //                    .forEach( c -> {
    //                        if ( optionsBeanClasses.add(c) ) {
    //                            bind(c).in(Singleton.class);
    //                        }
    //                    });
    //        });
    //    }
    //
    //    @Provides
    //    @Singleton
    //    @CmdLineOptions
    //    private Collection<Object> provideOptionsReceiver(Injector injector) {
    //        return Collections.unmodifiableCollection(
    //                Collections2.transform(
    //                        optionsBeanClasses,
    //                        (Class<?> c) -> injector.getInstance(Key.get(c, CmdLineOptions.class))));
    //    }
    //
    //    private Assembly bootstrap(Injector injector) {
    //        CmdLineParser cmdLineParser = new CmdLineParser(null);
    //        for ( Object bean : injector.getInstance(Key.get(Collection.class, CmdLineOptions.class)) ) {
    //            new ClassParser().parse(bean, cmdLineParser);
    //        }
    //        injector.getInstance(Key.get(new TypeLiteral<Set<Bootstrapper>>(){})).forEach(Bootstrapper::bootstrap);
    //    }
    //
    //}

}
