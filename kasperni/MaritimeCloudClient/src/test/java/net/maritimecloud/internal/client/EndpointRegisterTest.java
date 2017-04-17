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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.internal.core.javax.json.Json;
import net.maritimecloud.internal.core.javax.json.JsonObject;
import net.maritimecloud.internal.core.javax.json.JsonReader;
import net.maritimecloud.internal.core.javax.json.JsonString;
import net.maritimecloud.net.EndpointInvocationFuture;
import net.maritimecloud.net.mms.MmsClient;

import org.junit.Ignore;
import org.junit.Test;

import testproject.Hello;

/**
 *
 * @author Kasper Nielsen
 */
public class EndpointRegisterTest extends AbstractRestTest {

    @Test
    @Ignore
    public void fileInvokeService() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        Hello hello = c2.endpointCreate(id, Hello.class);

        EndpointInvocationFuture<Void> f = hello.hi();

        Path p = BroadcastReceiveTest.getSingleFile(endpointIn);

        String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);

        JsonReader r = Json.createReader(new StringReader(s));
        JsonObject o = r.readObject();
        assertTrue(p.getFileName().toString().contains(f.getMessageId().hexString()));
        assertEquals(f.getMessageId().hexString(), ((JsonString) o.get("messageId")).getChars());
        assertEquals(ID6.toString(), ((JsonString) o.get("senderId")).getChars());
        assertEquals(Hello.NAME + ".hi", ((JsonString) o.get("endpointType")).getChars());

        Files.delete(p);
        Path response = p.getParent().resolve(p.getFileName() + ".response");

        Files.write(response, "{}".getBytes(StandardCharsets.UTF_8));

        f.get(200, TimeUnit.SECONDS);
        assertTrue(f.isDone());

        assertFalse(Files.exists(response));
    }

    @Test
    @Ignore
    public void fileInvokeServiceParameters() throws Exception {
        MmsClient c2 = newClient(ID6, 1, 1);
        assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));

        Hello hello = c2.endpointCreate(id, Hello.class);

        EndpointInvocationFuture<Integer> f = hello.hib(123);

        Path p = BroadcastReceiveTest.getSingleFile(endpointIn);


        String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);

        JsonReader r = Json.createReader(new StringReader(s));
        JsonObject o = r.readObject();
        assertTrue(p.getFileName().toString().contains(f.getMessageId().hexString()));
        assertEquals(f.getMessageId().hexString(), ((JsonString) o.get("messageId")).getChars());
        assertEquals(ID6.toString(), ((JsonString) o.get("senderId")).getChars());
        assertEquals(Hello.NAME + ".hib", ((JsonString) o.get("endpointType")).getChars());

        o.getJsonObject("parameters");

        Files.delete(p);
        Path response = p.getParent().resolve(p.getFileName() + ".response");

        Files.write(response, "{\"result\" : 123}".getBytes(StandardCharsets.UTF_8));

        f.get(200, TimeUnit.SECONDS);
        assertTrue(f.isDone());

        assertFalse(Files.exists(response));
    }
}
