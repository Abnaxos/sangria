package ch.raffael.sangria.modules.shutdown;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.raffael.sangria.libs.guava.collect.ImmutableList;
import ch.raffael.sangria.libs.guava.util.concurrent.SettableFuture;
import ch.raffael.sangria.libs.guava.util.concurrent.ThreadFactoryBuilder;
import ch.raffael.sangria.libs.guava.util.concurrent.Uninterruptibles;

import ch.raffael.sangria.util.EventEmitter;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class ShutdownCoordinatorImpl implements ShutdownCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ShutdownCoordinatorImpl.class);
    private final String appId;

    private EventEmitter<VetoableShutdownListener> vetoableShutdownEmitter;
    private final CopyOnWriteArrayList<ShutdownListener> shutdownListeners = new CopyOnWriteArrayList<>();

    private final List<Runnable> shutdownFinalizers;

    ShutdownCoordinatorImpl(String appId, Iterable<Runnable> shutdownFinalizers) {
        this.appId = appId;
        this.shutdownFinalizers = ImmutableList.copyOf(shutdownFinalizers);
    }

    @Override
    public boolean isVetoable() {
        return false;
    }

    @Override
    public Future<ShutdownVetoException> shutdown() {
        final SettableFuture<ShutdownVetoException> future = SettableFuture.create();
        shutdownThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if ( isVetoable() ) {
                        log.info("Initiating vetoable shutdown");
                        try {
                            vetoableShutdownEmitter.broadcaster().shutdownRequested();
                        }
                        catch ( ShutdownVetoException veto ) {
                            log.info("Shutdown vetoed", veto);
                            vetoableShutdownEmitter.broadcaster().shutdownVetoed(veto);
                            future.set(veto);
                            return;
                        }
                    }
                    performShutdown();
                    future.set(null);
                }
                catch ( Throwable e ) {
                    log.error("Uncaught exception during shutdown", e);
                    future.setException(e);
                }
            }
        });
        return future;
    }

    @Override
    public Future<Void> forceShutdown() {
        final SettableFuture<Void> future = SettableFuture.create();
        shutdownThread(new Runnable() {
            @Override
            public void run() {
                try {
                    performShutdown();
                    future.set(null);
                }
                catch ( Throwable e ) {
                    log.error("Uncaught exception during shutdown", e);
                    future.setException(e);
                }
            }
        });
        return future;
    }

    private void shutdownThread(Runnable runnable) {
        new Thread(runnable, "Shutdown-" + appId).start();
    }

    private void performShutdown() {
        log.info("Performing shutdown");
        ExecutorService executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setNameFormat("Shutdown-" + appId + "-worker-%d")
                        .build());
        invokeListeners(executor, new Invoker("prepareShutdown") {
            @Override
            protected void invoke(ShutdownListener listener) {
                listener.prepareShutdown();
            }
        });
        invokeListeners(executor, new Invoker("performShutdown") {
            @Override
            protected void invoke(ShutdownListener listener) {
                listener.performShutdown();
            }
        });
        invokeListeners(executor, new Invoker("postShutdown") {
            @Override
            protected void invoke(ShutdownListener listener) {
                listener.postShutdown();
            }
        });
        executor.shutdown();
        for ( Runnable runnable : shutdownFinalizers ) {
            runnable.run();
        }
    }

    private void invokeListeners(ExecutorService executor, final Invoker invoker) {
        Object[] listeners = shutdownListeners.toArray();
        final CountDownLatch latch = new CountDownLatch(listeners.length);
        for ( final Object listener : listeners ) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        invoker.invoke((ShutdownListener)listener);
                    }
                    catch ( Throwable e ) {
                        log.error("Error in {} of {}", invoker.name, listener);
                    }
                    finally {
                        latch.countDown();
                    }
                }
            });
        }
        Uninterruptibles.awaitUninterruptibly(latch);
    }

    @Override
    public void addShutdownListener(ShutdownListener listener) {
        shutdownListeners.add(listener);
    }

    @Override
    public void removeShutdownListener(ShutdownListener listener) {
        shutdownListeners.remove(listener);
    }

    @Override
    public void addVetoableShutdownListener(VetoableShutdownListener listener) {
        vetoableShutdownEmitter.addListener(listener);
    }

    @Override
    public void removeVetoableShutdownListener(VetoableShutdownListener listener) {
        vetoableShutdownEmitter.removeListener(listener);
    }

    private static abstract class Invoker {
        private final String name;
        protected Invoker(String name) {
            this.name = name;
        }
        protected abstract void invoke(ShutdownListener listener);
    }

}
