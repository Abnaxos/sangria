package ch.raffael.sangria.assembly;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class PackageProvision {

    private final String packageName;
    private final Feature feature;

    public PackageProvision(String packageName, Feature feature) {
        this.packageName = packageName;
        this.feature = feature;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + packageName + "->" + feature + "}";
    }

    public String getPackageName() {
        return packageName;
    }

    public Feature getFeature() {
        return feature;
    }
}
