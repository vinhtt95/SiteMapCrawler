package com.vinhtt.sitemapcrawler;

/**
 * Main entry point for the application to bypass JavaFX 11+ module checks.
 *
 * @author vinhtt
 * @version 1.0
 */
public class Launcher {
    /**
     * Delegates execution to the actual JavaFX Application class.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        App.main(args);
    }
}