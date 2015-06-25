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

import org.junit.Test;


/**
 *
 * @author Kasper Nielsen
 */
public class MFInvokeTest extends AbstractRestTest {

    @Test
    public void ignore() {

    }
    // @Test
    // public void fileInvokeService() throws Exception {
    // MmsClient c2 = newClient(ID6, 1, 1);
    // assertTrue(c2.connection().awaitConnected(1, TimeUnit.SECONDS));
    //
    // MaritimeTextingService mts = c2.endpointCreate(id, MaritimeTextingService.class);
    //
    // EndpointInvocationFuture<Void> f = mts.sendMessage(new
    // MaritimeText().setMsg("sdfsdfsd").setSeverity(MaritimeTextingNotificationSeverity.));
    //
    //
    // Thread.sleep(1000000);
    // Path p = BroadcastReceiveTest.getSingleFile(endpointIn);
    //
    // String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    //
    // JsonReader r = Json.createReader(new StringReader(s));
    // JsonObject o = r.readObject();
    // assertTrue(p.getFileName().toString().contains(f.getMessageId().hexString()));
    // assertEquals(f.getMessageId().hexString(), ((JsonString) o.get("messageId")).getChars());
    // assertEquals(ID6.toString(), ((JsonString) o.get("senderId")).getChars());
    // assertEquals(MaritimeTextingService.NAME + ".sendMessage", ((JsonString) o.get("endpointType")).getChars());
    //
    // // Files.delete(p);
    // Path response = p.getParent().resolve(p.getFileName() + ".response");
    //
    // Files.write(response, "{}".getBytes(StandardCharsets.UTF_8));
    //
    // f.get(200, TimeUnit.SECONDS);
    // assertTrue(f.isDone());
    //
    // assertFalse(Files.exists(response));
    // }
}
