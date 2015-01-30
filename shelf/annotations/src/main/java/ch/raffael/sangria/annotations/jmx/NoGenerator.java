package ch.raffael.sangria.annotations.jmx;

import java.util.function.Function;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
final class NoGenerator implements Function<Object, String> {
    private NoGenerator() {
    }
    @Override
    public String apply(Object o) {
        return "";
    }
}
