/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gregwhitaker.fireforget;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.nio.NioEventLoopGroup;
import io.reactivesocket.ConnectionSetupPayload;
import io.reactivesocket.DefaultReactiveSocket;
import io.reactivesocket.Frame;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.netty.tcp.client.ClientTcpDuplexConnection;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import rx.Observable;
import rx.RxReactiveStreams;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Client that sends fire-and-forget messages to the server at a set interval.
 */
public class Client {
    private final InetSocketAddress remoteAddress;

    /**
     * Main entry-point of the Client application.
     *
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        Client client = new Client(new InetSocketAddress("localhost", 8080));
        client.start();
    }

    /**
     * Initializes this instance of {@link Client}.
     *
     * @param remoteAddress ip address and port to connect to using reactive socket
     */
    public Client(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Starts the client.
     *
     * @throws Exception
     */
    private void start() throws Exception {
        Publisher<ClientTcpDuplexConnection> publisher = ClientTcpDuplexConnection
                .create(remoteAddress, new NioEventLoopGroup(1));

        ClientTcpDuplexConnection duplexConnection = RxReactiveStreams.toObservable(publisher).toBlocking().last();
        ReactiveSocket reactiveSocket = DefaultReactiveSocket.fromClientConnection(duplexConnection,
                ConnectionSetupPayload.create("UTF-8", "UTF-8"), t -> t.printStackTrace());

        reactiveSocket.startAndWait();

        // Create an observable that emits messages at a specific interval.
        Publisher<Payload> requestStream = RxReactiveStreams
                .toPublisher(Observable
                        .interval(1_000, TimeUnit.MILLISECONDS)
                        .onBackpressureDrop()
                        .map(i ->
                                new Payload() {
                                    @Override
                                    public ByteBuffer getData() {
                                        return ByteBuffer.wrap(("YO " + i).getBytes());
                                    }

                                    @Override
                                    public ByteBuffer getMetadata() {
                                        return Frame.NULL_BYTEBUFFER;
                                    }
                                }
                        )
                );

        CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);

        // Subscribe to the observable that is emitting messages at a specific interval.
        requestStream.subscribe(new Subscriber<Payload>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Payload payload) {
                ByteBuf buffer = Unpooled.buffer(payload.getData().capacity());
                buffer.writeBytes(payload.getData());

                byte[] bytes = new byte[buffer.capacity()];
                buffer.readBytes(bytes);

                System.out.println("Sent: " + new String(bytes));

                // Send the messages emitted by the observable to the server via reactive socket.  The messages are
                // sent using fire-and-forget mode so there is no response expected by the client.
                reactiveSocket
                        .fireAndForget(payload)
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onSubscribe(Subscription s) {
                                s.request(Long.MAX_VALUE);
                            }

                            @Override
                            public void onNext(Void aVoid) {

                            }

                            @Override
                            public void onError(Throwable t) {
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {
                                latch.countDown();
                            }
                        });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        latch.await();
        System.exit(0);
    }
}
