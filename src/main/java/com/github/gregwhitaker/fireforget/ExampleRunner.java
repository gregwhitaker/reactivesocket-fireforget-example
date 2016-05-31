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
        Server server = new Server(new InetSocketAddress("localhost", 8080));
        server.start();

        Client client = new Client();
        client.start();

        Thread.currentThread().join();
    }
}
