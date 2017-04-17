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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.internal.core.javax.json.Json;
import net.maritimecloud.internal.core.javax.json.JsonNumber;
import net.maritimecloud.internal.core.javax.json.JsonObject;
import net.maritimecloud.internal.core.javax.json.JsonReader;
import net.maritimecloud.internal.core.javax.json.JsonString;
import net.maritimecloud.net.DispatchedMessage;
import net.maritimecloud.net.mms.MmsClient;

import org.junit.Test;

import testproject.HelloWorld;

/**
 *
 * @author Kasper Nielsen
 */
public class BroadcastReceiveTest extends AbstractRestTest {

    @Test
    public void fileSubscribe() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        // DispatchedMessage dm1 = c2.broadcast(new HelloWorld2().setMsg("abc"));

        HelloWorld msg = new HelloWorld().setMsg("abc");
        DispatchedMessage dm2 = c2.broadcast(msg);


        Path p = getSingleFile(broadcastIn);
        assertTrue(p.getFileName().toString().contains(dm2.getMessageId().hexString()));

        // Verify contents
        String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);

        JsonReader r = Json.createReader(new StringReader(s));
        JsonObject o = r.readObject();
        assertEquals(dm2.getMessageId().hexString(), ((JsonString) o.get("messageId")).getChars());
        assertEquals(HelloWorld.NAME, ((JsonString) o.get("broadcastType")).getChars());
        assertEquals(ID6.toString(), ((JsonString) o.get("senderId")).getChars());
        // todo sender position
        assertEquals(dm2.getTime().getTime(), ((JsonNumber) o.get("senderTimestamp")).longValue());

        assertEquals(Json.createReader(new StringReader(msg.toJSON())).readObject(), o.get("broadcast"));

        Files.delete(p);
    }

    static Path getSingleFile(Path directory) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
                ArrayList<Path> list = new ArrayList<>();
                directoryStream.iterator().forEachRemaining(e -> list.add(e));
                if (list.size() == 1) {
                    return list.get(0);
                } else if (list.size() > 1) {
                    throw new UnsupportedOperationException("More than one file");
                }
            } catch (IOException ex) {}
            Thread.sleep(1000);
        }
        throw new AssertionError("No file found");
    }

}
