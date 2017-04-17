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
package testproject.test;

import javax.ws.rs.Path;

import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.util.geometry.Position;
import net.maritimecloud.util.geometry.PositionReader;
import testproject.AbstractHello;
import testproject.B1;
import testproject.HelloWorld;

/**
 *
 * @author Kasper Nielsen
 */
public class Test extends AbstractHello {

    public static void main(String[] args) throws Exception {
        MmsClient mc = MmsClientConfiguration.create("mmsi:123").setHost("mms03.maritimecloud.net")
                .addListener(new MmsConnection.Listener() {
                    @Override
                    public void textMessageReceived(String message) {
                        System.out.println("----Received----");
                        System.out.println(message);
                    }

                    @Override
                    public void textMessageSend(String message) {
                        System.out.println("----Send----");
                        System.out.println(message);
                    }
                }).setPositionReader(PositionReader.fixedPosition(Position.create(1, 1))).build();


        mc.endpointRegister(new Test());


        mc.broadcastSubscribe(HelloWorld.class, (h, m) -> {
            System.out.println(m.getMsg());
        });

        for (int i = 0; i < 10000; i++) {
            mc.broadcast(new HelloWorld().setMsg("geh " + i));
            Thread.sleep(10000);
        }

        Thread.sleep(10000000);
        // Thread.sleep(0);
    }

    /** {@inheritDoc} */
    @Override
    protected void hi(MessageHeader header) {
        System.out.println("HejHEJ");
    }

    /** {@inheritDoc} */
    @Override
    @Path("/fdgfh")
    protected Integer hia(MessageHeader header) {
        return 123;
    }

    /** {@inheritDoc} */
    @Override
    protected Integer hib(MessageHeader header, Integer f1) {
        System.out.println("Got " + f1);
        return 1234;
    }

    /** {@inheritDoc} */
    @Override
    protected B1 hic(MessageHeader header) {
        return new B1().setF1(1);
    }

    /** {@inheritDoc} */
    @Override
    protected B1 hid(MessageHeader header, B1 b2) {
        System.out.println("Got " + b2.getF1() + ", " + b2.getF2());
        return new B1().setF1(2);
    }
}
