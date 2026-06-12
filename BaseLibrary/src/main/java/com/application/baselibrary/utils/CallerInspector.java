package com.application.baselibrary.utils;

public class CallerInspector {

    /**
     * Returns the class name and method name of the external caller
     * (the method that invoked your utility).
     */
    public static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Index 0 = getStackTrace
        // Index 1 = getCallerInfo
        // Index 2 = logCaller (if used)
        // Index 3 = the external caller of logCaller
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return caller.getClassName() + "." + caller.getMethodName() +
                    " (line " + caller.getLineNumber() + ")";
        }
        return "Unknown caller";
    }

    /**
     * Logs the external caller info to console.
     */
    public static void logCaller() {
        // Skip this method itself by asking for index 3 instead of 2
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            System.out.println("Called by: " + caller.getClassName() + "." +
                    caller.getMethodName() + " (line " + caller.getLineNumber() + ")");
        } else {
            System.out.println("Called by: Unknown");
        }
    }
}
