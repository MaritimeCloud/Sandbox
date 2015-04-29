/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maritimecloud.sandbox.tls.client.tomcat.util;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;


/**
 *
 * @author Kasper Nielsen
 */
public class JavaXEndpoint extends Endpoint {

    public final CountDownLatch done = new CountDownLatch(1);

    public void assertDone() {
        try {
            if (!done.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("No PingPong");
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Client: Socket Closed: " + closeReason);
    }

    /** {@inheritDoc} */
    @Override
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace(System.err);
    }

    /** {@inheritDoc} */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("Client: Socket Connected: ");
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println("Client: Received TEXT message: " + message);
                if (message.equals("2")) {
                    done.countDown();
                }
            }
        });

        try {
            session.getBasicRemote().sendText("1");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
