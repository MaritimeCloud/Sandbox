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

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.internal.core.javax.json.Json;
import net.maritimecloud.internal.core.javax.json.JsonNumber;
import net.maritimecloud.internal.core.javax.json.JsonObject;
import net.maritimecloud.internal.core.javax.json.JsonReader;
import net.maritimecloud.internal.core.javax.json.JsonString;
import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsClient;

import org.junit.Test;

import testproject.B1;
import testproject.Hello;

/**
 *
 * @author Kasper Nielsen
 */
public class EndpointInvokeTest extends AbstractRestTest {

    @Test
    public void fileInvokeService() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        ArrayBlockingQueue<MessageHeader> q = new ArrayBlockingQueue<>(1);
        c2.endpointRegister(new AbstractTestService() {
            @Override
            protected void hi(MessageHeader header) {
                q.add(header);
            }
        }).awaitRegistered(1, TimeUnit.SECONDS);

        String msg = invokeService("hi", null);

        Path out = endpointOut.resolve("tmp" + System.nanoTime());
        Files.write(out, msg.getBytes(StandardCharsets.UTF_8));

        MessageHeader mh = q.poll(10, TimeUnit.SECONDS);
        assertEquals(id, mh.getSender());

        Path response = out.getParent().resolve(out.getFileName().toString() + ".response");
        MoreAsserts.assertTrueWithin(() -> Files.exists(response), 2, TimeUnit.SECONDS);

        String s = new String(Files.readAllBytes(response), StandardCharsets.UTF_8);
        JsonReader r = Json.createReader(new StringReader(s));
        JsonObject o = r.readObject();

        assertEquals(Hello.NAME + ".hi", ((JsonString) o.get("endpointType")).getChars());

        Files.delete(response);
    }

    @Test
    public void fileInvokeServiceParameters() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        ArrayBlockingQueue<MessageHeader> hib = new ArrayBlockingQueue<>(1);
        c2.endpointRegister(new AbstractTestService() {

            /** {@inheritDoc} */
            @Override
            protected Integer hib(MessageHeader header, Integer f1) {
                hib.add(header);
                return f1 + 123;
            }

            /** {@inheritDoc} */
            @Override
            protected B1 hid(MessageHeader header, B1 b2) {
                B1 result = new B1();
                System.out.println("Got f1=" + b2.getF1() + " f2=" + b2.getF2());
                result.setF1(b2.getF1() + 123);
                result.setF2(b2.getF2() + 123);
                hib.add(header);
                return result;
            }
        }).awaitRegistered(1, TimeUnit.SECONDS);

        // hib()
        String msg = invokeService("hib", "\"f1\": 321");

        Path out = endpointOut.resolve("tmp" + System.nanoTime());
        Files.write(out, msg.getBytes(StandardCharsets.UTF_8));

        MessageHeader mh = hib.poll(10, TimeUnit.SECONDS);
        assertEquals(id, mh.getSender());

        Path response1 = out.getParent().resolve(out.getFileName().toString() + ".response");
        MoreAsserts.assertTrueWithin(() -> Files.exists(response1), 2, TimeUnit.SECONDS);

        String s = new String(Files.readAllBytes(response1), StandardCharsets.UTF_8);
        JsonReader r = Json.createReader(new StringReader(s));
        JsonObject o = r.readObject();

        assertEquals(Hello.NAME + ".hib", ((JsonString) o.get("endpointType")).getChars());
        assertEquals(444, ((JsonNumber) o.get("result")).intValue());
        Files.delete(response1);


        // hid()
        msg = invokeService("hid", "\"b2\": { \"f1\": 100, \"f2\":101 }");

        out = endpointOut.resolve("tmp" + System.nanoTime());
        Files.write(out, msg.getBytes(StandardCharsets.UTF_8));

        mh = hib.poll(310, TimeUnit.SECONDS);
        assertEquals(id, mh.getSender());

        Path response2 = out.getParent().resolve(out.getFileName().toString() + ".response");
        MoreAsserts.assertTrueWithin(() -> Files.exists(response2), 2, TimeUnit.SECONDS);

        s = new String(Files.readAllBytes(response2), StandardCharsets.UTF_8);
        r = Json.createReader(new StringReader(s));
        o = r.readObject();

        assertEquals(Hello.NAME + ".hid", ((JsonString) o.get("endpointType")).getChars());

        JsonObject result = (JsonObject) o.get("result");
        assertEquals(223, ((JsonNumber) result.get("f1")).intValue());
        assertEquals(224, ((JsonNumber) result.get("f2")).intValue());

        // Files.delete(response);
    }

    public void fileInvokeServiceWithReturn() {

    }


    private String invokeService(String endpointName, String parameters) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"receiverId\" : \"mmsi:6\",");
        sb.append("\"endpointType\" : \"" + Hello.NAME + "." + endpointName + "\"");
        if (parameters != null) {
            sb.append(",\"parameters\" : {" + parameters + "}");
        }
        sb.append("}");
        return sb.toString();
    }
}
