package com.application.baselibrary.threading.scheduler;


import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * RxSchedulerProvider
 * A utility class to provide central access to RxJava3 Schedulers.
 * Useful for switching between threads and facilitating unit testing.
 */
public class RxSchedulerProvider {

    /**
     * Returns a scheduler intended for IO-bound work (network, disk, etc.).
     */
    public static Scheduler io() {
        return Schedulers.io();
    }

    /**
     * Returns the scheduler for the Android Main Thread.
     */
    public static Scheduler main() {
        return AndroidSchedulers.mainThread();
    }

    /**
     * Returns a scheduler intended for computational work (loops, processing, etc.).
     */
    public static Scheduler computation() {
        return Schedulers.computation();
    }

    /**
     * Returns a scheduler that executes work on a single background thread.
     */
    public static Scheduler single() {
        return Schedulers.single();
    }

    /**
     * Returns a scheduler that starts a new thread for each unit of work.
     */
    public static Scheduler newThread() {
        return Schedulers.newThread();
    }

    /**
     * Executes a task on the IO scheduler.
     */
    public static void runOnIO(Runnable runnable) {
        Schedulers.io().scheduleDirect(runnable);
    }

    /**
     * Executes a task on the Main thread.
     */
    public static void runOnMain(Runnable runnable) {
        AndroidSchedulers.mainThread().scheduleDirect(runnable);
    }
}