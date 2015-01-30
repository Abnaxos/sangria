package ch.raffael.sangria.bootstrap_old;

import ch.raffael.sangria.annotations.Install;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Install
public class MultiplexBundleLoader {//implements BundleLoader {

    //private final List<BundleLoader> loaders;
    //
    //@Inject
    //public MultiplexBundleLoader(Set<BundleLoaderBinder.BundleLoaderBinding> loaders) {
    //    this.loaders = Lists.transform(BundleLoaderBinder.sortByPriority(loaders), BundleLoaderBinder.BundleLoaderBinding.TO_LOADER);
    //}
    //
    //@Override
    //public boolean loadBundles(URI uri) throws IOException {
    //    for ( BundleLoader loader : loaders ) {
    //        if ( loader.loadBundles(uri) ) {
    //            return true;
    //        }
    //    }
    //    return false;
    //}
}
