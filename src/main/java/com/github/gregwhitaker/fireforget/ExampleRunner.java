package com.github.gregwhitaker.fireforget;

/**
 * Runs the fire-and-forget example.
 */
public class ExampleRunner {

    /**
     * Main entry-point for this example.  This starts the server and the client application.
     *
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        Server.main();
        Client.main();

        Thread.currentThread().join();
    }
}
