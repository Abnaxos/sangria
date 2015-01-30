package ch.raffael.sangria.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.collect.Iterables;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class MessageBroadcaster extends AbstractMessager {

    private final List<Messager> messagers = new ArrayList<>();

    public MessageBroadcaster() {
    }

    public MessageBroadcaster(Iterable<? extends Messager> messagers) {
        if ( messagers != null ) {
            Iterables.addAll(this.messagers, messagers);
        }
    }

    public MessageBroadcaster(Messager... messagers) {
        if ( messagers != null ) {
            this.messagers.addAll(Arrays.asList(messagers));
        }
    }

    @Override
    public void error(Source source, String message, Throwable cause) {
        for ( Messager messager : messagers ) {
            messager.error(source, message, cause);
        }
    }

    @Override
    public void warning(Source source, String message, Throwable cause) {
        for ( Messager messager : messagers ) {
            messager.warning(source, message, cause);
        }
    }

    public void add(Iterable<Messager> messagers) {
        if ( messagers != null ) {
            Iterables.addAll(this.messagers, messagers);
        }
    }

    public void add(Messager... messagers) {
        if ( messagers != null ) {
            this.messagers.addAll(Arrays.asList(messagers));
        }
    }

    public List<Messager> getMessagers() {
        return ImmutableList.copyOf(getMessagers());
    }

}
