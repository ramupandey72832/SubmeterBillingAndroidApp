package com.application.baselibrary.threading.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility to schedule a one-time task after a delay.
 */
public class OneTimeTaskRunner {

//    private static final ScheduledExecutorService scheduler =
//            Executors.newSingleThreadScheduledExecutor();

    /**
     * Runs the given task once after the specified delay.
     *
     * @param task  The code to run (Runnable).
     * @param delay Delay amount.
     * @param unit  Time unit for the delay.
     */
    public static void runOnce(Runnable task, long delay, TimeUnit unit) {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            try {
                task.run();
            } finally {
                // Shut down scheduler after task completes
                scheduler.shutdown();
            }
        }, delay, unit);
    }
}
