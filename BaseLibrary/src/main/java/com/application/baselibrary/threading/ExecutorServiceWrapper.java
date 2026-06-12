package com.application.baselibrary.threading;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExecutorServiceWrapper
 * A wrapper class over Java's ExecutorService API to manage background threads efficiently.
 */
public class ExecutorServiceWrapper {

    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    // Fixed thread pool for intensive background tasks
    private static final ExecutorService ioExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Cached thread pool for short-lived asynchronous tasks (e.g., Network)
    private static final ExecutorService networkExecutor = Executors.newCachedThreadPool();

    // Single thread executor for sequential background tasks
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    // Main thread executor to post results back to UI
    private static final Executor mainThreadExecutor = new MainThreadExecutor();

    private ExecutorServiceWrapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Runs a task on a fixed background thread pool.
     */
    public static void runOnIO(Runnable runnable) {
        ioExecutor.execute(runnable);
    }

    /**
     * Runs a task on a cached background thread pool (best for network).
     */
    public static void runOnNetwork(Runnable runnable) {
        networkExecutor.execute(runnable);
    }

    /**
     * Runs a task on a single background thread sequentially.
     */
    public static void runOnSingleThread(Runnable runnable) {
        singleThreadExecutor.execute(runnable);
    }

    /**
     * Runs a task on the Android Main (UI) thread.
     */
    public static void runOnMain(Runnable runnable) {
        mainThreadExecutor.execute(runnable);
    }

    public static ExecutorService getIoExecutor() {
        return ioExecutor;
    }

    public static ExecutorService getNetworkExecutor() {
        return networkExecutor;
    }

    public static ExecutorService getSingleThreadExecutor() {
        return singleThreadExecutor;
    }

    public static Executor getMainThreadExecutor() {
        return mainThreadExecutor;
    }

    /**
     * Shuts down all executors.
     */
    public static void shutdownAll() {
        ioExecutor.shutdown();
        networkExecutor.shutdown();
        singleThreadExecutor.shutdown();
    }

    /**
     * Inner class to handle Main Thread execution using Handler.
     */
    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
