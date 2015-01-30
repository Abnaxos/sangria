package ch.raffael.sangria.assembly;


import ch.raffael.sangria.libs.guava.base.Objects;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class SharedPackage {

    private final String name;
    private final Bundle provider;

    public SharedPackage(String name, Bundle provider) {
        this.name = name;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "package:" + name + "[" + provider + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, provider);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final SharedPackage other = (SharedPackage)obj;
        return Objects.equal(this.name, other.name) && Objects.equal(this.provider, other.provider);
    }

    public String getName() {
        return name;
    }

    public Bundle getProvider() {
        return provider;
    }
}
