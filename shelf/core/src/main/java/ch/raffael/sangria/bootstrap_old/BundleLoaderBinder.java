package ch.raffael.sangria.bootstrap_old;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class BundleLoaderBinder {//extends AbstractExtensionPoint {

    //private BundleLoaderBinder(Binder binder) {
    //    super(binder);
    //}
    //
    //public static BundleLoaderBinder newBundleLoaderBinder(Binder binder) {
    //    return new BundleLoaderBinder(binder);
    //}
    //
    //public BundleLoaderBindingBuilder addBundleLoader() {
    //    BundleLoaderBindingBuilder builder = new BundleLoaderBindingBuilder();
    //    multibinder(BundleLoaderBinding.class).addBinding().toInstance(builder.binding);
    //    return builder;
    //}
    //
    //public static List<BundleLoaderBinding> sortByPriority(Collection<? extends BundleLoaderBinding> unsortedBinders) {
    //    BundleLoaderBinding[] sortedBinders = unsortedBinders.toArray(new BundleLoaderBinding[unsortedBinders.size()]);
    //    Arrays.sort(sortedBinders, new Comparator<BundleLoaderBinding>() {
    //        @Override
    //        public int compare(BundleLoaderBinding left, BundleLoaderBinding right) {
    //            if ( left.priority > right.priority ) {
    //                return 1;
    //            }
    //            else if ( left.priority < right.priority ) {
    //                return -1;
    //            }
    //            else {
    //                return 0;
    //            }
    //        }
    //    });
    //    return ImmutableList.copyOf(sortedBinders);
    //}
    //
    //static class BundleLoaderBinding {
    //
    //    static final Function<BundleLoaderBinding, BundleLoader> TO_LOADER = new Function<BundleLoaderBinding, BundleLoader>() {
    //        @Override
    //        public BundleLoader apply(BundleLoaderBinding input) {
    //            return input.loader.get();
    //        }
    //    };
    //
    //    private int priority = 0;
    //    private final Provider<BundleLoader> loader;
    //
    //    BundleLoaderBinding(Provider<BundleLoader> loader) {
    //        this.loader = loader;
    //    }
    //}
    //
    //public class BundleLoaderBindingBuilder implements LinkedBindingBuilder<BundleLoader> {
    //
    //    private final ForwardLinkedBindingBuilder<BundleLoader> loaderBindingBuilder = new ForwardLinkedBindingBuilder<>(binder());
    //    private final BundleLoaderBinding binding = new BundleLoaderBinding(loaderBindingBuilder.provider());
    //
    //    private BundleLoaderBindingBuilder() {
    //    }
    //
    //    public BundleLoaderBindingBuilder withPriority(int priority) {
    //        binding.priority = priority;
    //        return this;
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder to(Class<? extends BundleLoader> implementation) {
    //        return loaderBindingBuilder.to(implementation);
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder to(TypeLiteral<? extends BundleLoader> implementation) {
    //        return loaderBindingBuilder.to(implementation);
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder to(Key<? extends BundleLoader> targetKey) {
    //        return loaderBindingBuilder.to(targetKey);
    //    }
    //
    //    @Override
    //    public void toInstance(BundleLoader instance) {
    //        loaderBindingBuilder.toInstance(instance);
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder toProvider(Provider<? extends BundleLoader> provider) {
    //        return loaderBindingBuilder.toProvider(provider);
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends BundleLoader>> providerType) {
    //        return loaderBindingBuilder.toProvider(providerType);
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends BundleLoader>> providerType) {
    //        return loaderBindingBuilder.toProvider(providerType);
    //    }
    //
    //    @Override
    //    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends BundleLoader>> providerKey) {
    //        return loaderBindingBuilder.toProvider(providerKey);
    //    }
    //
    //    public <S extends BundleLoader> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
    //        return loaderBindingBuilder.toConstructor(constructor);
    //    }
    //
    //    public <S extends BundleLoader> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
    //        return loaderBindingBuilder.toConstructor(constructor, type);
    //    }
    //
    //    @Override
    //    public void in(Class<? extends Annotation> scopeAnnotation) {
    //        loaderBindingBuilder.in(scopeAnnotation);
    //    }
    //
    //    @Override
    //    public void in(Scope scope) {
    //        loaderBindingBuilder.in(scope);
    //    }
    //
    //    @Override
    //    public void asEagerSingleton() {
    //        loaderBindingBuilder.asEagerSingleton();
    //    }
    //}

}
