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

import io.reactivesocket.Frame;
import io.reactivesocket.Payload;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Client {

    public void start() {
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
    }
}
