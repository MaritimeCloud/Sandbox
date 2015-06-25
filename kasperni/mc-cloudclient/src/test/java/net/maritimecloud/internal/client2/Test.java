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
package net.maritimecloud.internal.client2;

import java.util.Date;

import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.util.geometry.Position;
import net.maritimecloud.util.geometry.PositionReader;
import testproject.HelloWorld;

/**
 *
 * @author Kasper Nielsen
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        MmsClientConfiguration mcc = MmsClientConfiguration.create("mmsi:123123111");
        mcc.setPositionReader(PositionReader.fixedPosition(Position.create(1, 1)));
        mcc.setHost("mms03.maritimecloud.net");
        mcc.addListener(new MmsConnection.Listener() {

            /** {@inheritDoc} */
            @Override
            public void textMessageReceived(String message) {
                System.err.println("Recevied " + message);
            }

            /** {@inheritDoc} */
            @Override
            public void textMessageSend(String message) {
                System.err.println("Send " + message);
            }
        });
        MmsClient c = mcc.build();

        c.broadcastSubscribe(HelloWorld.class, (h, m) -> {
            System.out.println(m.getMsg());
        });

        for (;;) {
            c.broadcast(new HelloWorld().setMsg(new Date().toString()));
            Thread.sleep(1000);
            System.out.println("NEXT");
        }

    }
}
