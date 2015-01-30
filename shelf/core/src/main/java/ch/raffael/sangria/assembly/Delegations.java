package ch.raffael.sangria.assembly;

import java.util.Map;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Delegations {

    public static Merger merge(Map<String, ClassLoader> delegations) {
        return new Merger(delegations);
    }

    public static final class Merger {
        private final Map<String, ClassLoader> delegations;
        private Merger(Map<String, ClassLoader> delegations) {
            this.delegations = delegations;
        }
        public void into(Map<String, ClassLoader> destination) {
            for ( Map.Entry<String, ClassLoader> delegation : delegations.entrySet() ) {
                ClassLoader prev = destination.get(delegation.getKey());
                if ( prev != null ) {
                    if ( !prev.equals(delegation.getValue()) ) {
                        throw new AmbiguousPackageException(String.format("Package %s delegated to both %s and %s", delegation.getKey(), prev, delegation.getValue()));
                    }
                    destination.put(delegation.getKey(), delegation.getValue());
                }
            }
        }
    }

}
