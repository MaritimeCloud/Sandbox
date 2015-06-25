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
package net.maritimecloud.internal.client.configuration;


/**
 *
 * @author Kasper Nielsen
 */
public class TestIt {

    // public static void maind(String[] args) throws InterruptedException, ExecutionException {
    // MmsClientConfiguration conf = MmsClientConfiguration.create("mmsi:123321323");
    // conf.setHost("mms03.maritimecloud.net");
    //
    // MmsClient mc = conf.build();
    //
    //
    // for (MaritimeTextingService s : mc.endpointLocate(MaritimeTextingService.class).findAll().get()) {
    // System.out.println(s.getRemoteId());
    // }
    //
    //
    // MaritimeTextingService s = mc.endpointCreate(MaritimeId.create("mmsi:111222333"), MaritimeTextingService.class);
    //
    // EndpointInvocationFuture<Void> f = s.sendMessage(new MaritimeText().setMsg("hejhej").setSeverity(
    // MaritimeTextingNotificationSeverity.MESSAGE));
    //
    // System.out.println("Waiting");
    // f.get();
    // System.out.println("GOT IT");
    // }
    //
    //
    // public static void main(String[] args) throws InterruptedException, ExecutionException {
    // MmsClientConfiguration conf = MmsClientConfiguration.create("mmsi:123321323");
    // conf.setHost("mms03.maritimecloud.net");
    //
    // MmsClient mc = conf.build();
    //
    // mc.endpointRegister(new AbstractMaritimeTextingService() {
    //
    // @Override
    // protected void sendMessage(MessageHeader header, MaritimeText msg) {
    // System.out.println("Got " + msg.toJSON());
    //
    // }
    // });
    //
    // Thread.sleep(100000000);
    // }
}
