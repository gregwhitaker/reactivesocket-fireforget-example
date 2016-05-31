package com.github.gregwhitaker.fireforget;

import java.net.InetSocketAddress;

/**
 *
 */
public class ExampleRunner {

    /**
     * Main entry-point for this example.
     *
     * @param args command line arguments
     */
    public static void main(String... args) throws Exception {
        Server.start(new InetSocketAddress("localhost", 8080));
        Client.start(new InetSocketAddress("localhost", 8081));

        Thread.currentThread().join();
    }
}
