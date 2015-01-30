package ch.raffael.sangria.modules.lifecycle;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
final class AgendaException extends RuntimeException implements Iterable<AgendaException> {

    private final LifecycleFacet facet;
    private AgendaException next = null;

    AgendaException(LifecycleFacet facet, Throwable exception) {
        super(facet.toString(), exception);
        this.facet = facet;
    }

    LifecycleFacet getFacet() {
        return facet;
    }

    AgendaException getNext() {
        return next;
    }

    void append(AgendaException exception) {
        AgendaException current = this;
        while ( current.next != null ) {
            current = current.next;
        }
        current.next = exception;
    }

    @Override
    public Iterator<AgendaException> iterator() {
        return new Iterator<AgendaException>() {
            private AgendaException current = null;
            @Override
            public boolean hasNext() {
                return current == null || current.next != null;
            }

            @Override
            public AgendaException next() {
                if ( current == null ) {
                    current = AgendaException.this;
                }
                else if ( current.next == null ) {
                    throw new NoSuchElementException();
                }
                else {
                    current = current.next;
                }
                return current;
            }
        };
    }
}
