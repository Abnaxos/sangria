package ch.raffael.sangria.assembly.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.raffael.sangria.libs.guava.base.Joiner;
import ch.raffael.sangria.libs.guava.collect.Iterators;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Path<T> implements Iterable<T> {

    private static final Joiner JOINER = Joiner.on("->");

    private final Set<T> contained = new HashSet<>();
    private final List<T> path = new ArrayList<>();

    @Override
    public String toString() {
        return JOINER.join(path);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        return path.equals(((Path)o).path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public boolean enter(T element) {
        if ( !contained.add(element) ) {
            return false;
        }
        path.add(element);
        return true;
    }

    public T leave() {
        if ( isEmpty() ) {
            throw new IllegalStateException("Path is empty");
        }
        T element = path.remove(path.size() - 1);
        contained.remove(element);
        return element;
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public int size() {
        return path.size();
    }

    public boolean contains(Object o) {
        return path.contains(o);
    }

    public T get(int index) {
        return path.get(index);
    }

    public T getLast() {
        if ( isEmpty() ) {
            throw new IllegalStateException("Path is empty");
        }
        return path.get(path.size() - 1);
    }

    public Object[] toArray() {
        return path.toArray();
    }

    public <T1> T1[] toArray(T1[] a) {
        return path.toArray(a);
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(path.iterator());
    }

    public void clear() {
        path.clear();
    }
}
