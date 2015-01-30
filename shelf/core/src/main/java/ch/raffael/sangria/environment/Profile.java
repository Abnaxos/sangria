package ch.raffael.sangria.environment;

import java.lang.annotation.Annotation;
import java.util.Optional;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class Profile {

    private final boolean initialState;
    private volatile Optional<Boolean> state = Optional.empty();

    protected Profile() {
        this.initialState = initialState();
    }

    protected boolean initialState() {
        return false;
    }

    public final boolean state() {
        return state.orElse(initialState);
    }

    public boolean isExplicitState() {
        return state.isPresent();
    }

    public void activate() {
        state = Optional.of(true);
    }

    public void deactivate() {
        state = Optional.of(false);
    }

    @Override
    public String toString() {
        Optional<Boolean> state = this.state;
        return "Profile:" + getClass().getName() + "[" + (state.isPresent() ? "explicit:" : "implicit:") + state.orElse(initialState)+"]";
    }
}
