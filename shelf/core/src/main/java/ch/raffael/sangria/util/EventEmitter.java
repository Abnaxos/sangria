package ch.raffael.sangria.util;

import java.lang.reflect.Array;
import java.util.EventListener;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class EventEmitter<T extends EventListener> {

    private final Class<T> listenerClass;
    private final CopyOnWriteArrayList<T> listeners;
    private final T broadcaster;

    private EventEmitter(Class<T> listenerClass, CopyOnWriteArrayList<T> listeners, T broadcaster) {
        this.listenerClass = listenerClass;
        this.listeners = listeners;
        this.broadcaster = broadcaster;
    }

    public T broadcaster() {
        return broadcaster;
    }

    public void addListener(T listener) {
        listeners.add(listener);
    }

    public void removeListener(T listener) {
        listeners.remove(listener);
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public T[] getListeners() {
        Object[] array = this.listeners.toArray();
        Object[] listenerArray = (Object[])Array.newInstance(listenerClass, array.length);
        System.arraycopy(array, 0, listenerArray, 0, array.length);
        return (T[])listenerArray;
    }

    public static <T extends EventListener> EventEmitter.Builder<T> builder(Class<T> listenerClass) {
        return new Builder<T>();
    }

    private static final class SynchronizedEventEmitter<T extends EventListener> extends EventEmitter<T> {
        private final Object sync;
        private SynchronizedEventEmitter(Class<T> listenerClass, CopyOnWriteArrayList<T> listeners, T broadcaster, Object sync) {
            super(listenerClass, listeners, broadcaster);
            this.sync = sync;
        }
        @Override
        public void addListener(T listener) {
            synchronized ( sync ) {
                super.addListener(listener);
            }
        }
        @Override
        public void removeListener(T listener) {
            synchronized ( sync ) {
                super.removeListener(listener);
            }
        }
        @Override
        public boolean hasListeners() {
            synchronized ( sync ) {
                return super.hasListeners();
            }
        }
        @Override
        public T[] getListeners() {
            synchronized ( sync ) {
                return super.getListeners();
            }
        }
    }

    private static final class LockingEventEmitter<T extends EventListener> extends EventEmitter<T> {
        private final ReadWriteLock lock;
        private LockingEventEmitter(Class<T> listenerClass, CopyOnWriteArrayList<T> listeners, T broadcaster, ReadWriteLock lock) {
            super(listenerClass, listeners, broadcaster);
            this.lock = lock;
        }
        @Override
        public void addListener(T listener) {
            lock.writeLock().lock();
            try {
                super.addListener(listener);
            }
            finally {
                lock.writeLock().unlock();
            }
        }
        @Override
        public void removeListener(T listener) {
            lock.writeLock().lock();
            try {
                super.removeListener(listener);
            }
            finally {
                lock.writeLock().unlock();
            }
        }
        @Override
        public boolean hasListeners() {
            lock.readLock().lock();
            try {
                return super.hasListeners();
            }
            finally {
                lock.readLock().unlock();
            }
        }
        @Override
        public T[] getListeners() {
            lock.readLock().lock();
            try {
                return super.getListeners();
            }
            finally {
                lock.readLock().unlock();
            }
        }
    }

    private static final class ReadWriteLockAdapter implements ReadWriteLock {
        private final Lock lock;
        private ReadWriteLockAdapter(Lock lock) {
            this.lock = lock;
        }
        @Override
        public String toString() {
            return lock.toString();
        }
        @Override
        public Lock readLock() {
            return lock;
        }
        @Override
        public Lock writeLock() {
            return lock;
        }
    }

    public static class Builder<T extends EventListener> {
        // parallel (executor)
        // locking
        // exceptions
        // unique
    }

}
