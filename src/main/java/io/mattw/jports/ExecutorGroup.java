package io.mattw.jports;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Structure to make managing a group of threads running the same task in an ExecutorService easier.
 *
 * @author mattwright324
 */
public class ExecutorGroup {

    private final ExecutorService service;
    private final List<Future<?>> futures = new ArrayList<>();
    private final int threadCount;

    /**
     * Default constructor
     *
     * @param threadCount number of threads to create
     */
    public ExecutorGroup(final int threadCount) {
        this.threadCount = threadCount;

        service = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * Submits the same runnable {@link ExecutorGroup#threadCount} times and shuts the service down.
     *
     * @param runnable stateless runnable object
     */
    public void submitAndShutdown(final Runnable runnable) {
        for (int i = 0; i < threadCount; i++) {
            futures.add(service.submit(runnable));
        }

        service.shutdown();
    }

    /**
     * Waits for all threads to complete.
     */
    public void await() throws InterruptedException {
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    /**
     * Check if any threads are still working.
     */
    public boolean isStillWorking() {
        for (Future<?> future : futures) {
            if (!future.isDone()) {
                return true;
            }
        }
        return false;
    }

}
