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
package net.maritimecloud.internal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import net.maritimecloud.internal.message.MessageHelper;
import net.maritimecloud.message.Message;
import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsClient;

import org.junit.Test;

import testproject.HelloWorld;

/**
 *
 * @author Kasper Nielsen
 */
public class BroadcastSendTest extends AbstractRestTest {

    @Test
    public void restIllegalBroadcasts() throws Exception {
        post("broadcast/send", Response.Status.BAD_REQUEST.getStatusCode(), "qsad");
        post("broadcast/send", Response.Status.BAD_REQUEST.getStatusCode(), "{}");
        post("broadcast/send", Response.Status.NOT_FOUND.getStatusCode(), "{\"broadcastType\" : \"ss.Ssss\"}");
    }

    @Test
    public void restSendBroadcast() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        BlockingQueue<MessageHeader> header = new ArrayBlockingQueue<>(1);
        c2.broadcastSubscribe(HelloWorld.class, (h, m) -> {
            assertEquals("fooo", m.getMsg());
            header.add(h);
        });

        String msgId = post("broadcast/send", 200, createBroadcast(new HelloWorld().setMsg("fooo")));
        assertEquals(msgId, header.poll(2, TimeUnit.SECONDS).getMessageId().hexString());
    }

    @Test
    public void fileSendBroadcast() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        BlockingQueue<MessageHeader> header = new ArrayBlockingQueue<>(1);
        c2.broadcastSubscribe(HelloWorld.class, (h, m) -> {
            assertEquals("fooo", m.getMsg());
            header.add(h);
        });

        String msg = createBroadcast(new HelloWorld().setMsg("fooo"));
        Path out = broadcastOut.resolve("tmp" + System.nanoTime());
        Files.write(out, msg.getBytes(StandardCharsets.UTF_8));

        MessageHeader mh = header.poll(120, TimeUnit.SECONDS);
        assertEquals(id, mh.getSender());

        // File should deleted after it has been send
        MoreAsserts.assertTrueWithin(() -> !Files.exists(out), 2, TimeUnit.SECONDS);
    }

    @Test
    public void fileSendBroadcastFail() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        BlockingQueue<MessageHeader> header = new ArrayBlockingQueue<>(1);
        c2.broadcastSubscribe(HelloWorld.class, (h, m) -> {
            assertEquals("fooo", m.getMsg());
            header.add(h);
        });

        String relative = "tmp" + System.nanoTime();
        Path out = broadcastOut.resolve(broadcastOut.resolve(relative));

        Files.write(out, "[]23".getBytes(StandardCharsets.UTF_8));


        // File should move to fail folder
        MoreAsserts.assertTrueWithin(() -> !Files.exists(out), 200, TimeUnit.SECONDS);
        MoreAsserts.assertTrueWithin(() -> Files.exists(fail.resolve(relative)), 2, TimeUnit.SECONDS);
        MoreAsserts.assertTrueWithin(() -> Files.exists(fail.resolve(relative + ".error")), 2, TimeUnit.SECONDS);

        Files.delete(fail.resolve(relative));
        Files.delete(fail.resolve(relative + ".error"));
    }

    private String createBroadcast(Message m) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"broadcastType\" : \"" + MessageHelper.getName(m) + "\",");
        sb.append("\"broadcast\" : " + m.toJSON());
        sb.append("}");
        return sb.toString();
    }
}
