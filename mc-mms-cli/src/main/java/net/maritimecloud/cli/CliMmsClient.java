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
package net.maritimecloud.cli;

import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.net.mms.MmsConnectionClosingCode;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;

import java.net.URI;

/**
 * Encapsulates an MMS Client and compiles statistics for the client
 */
@SuppressWarnings("unused")
public class CliMmsClient implements MmsConnection.Listener {

    final MmsClient mmsClient;
    final MmsClientConfiguration conf;
    boolean connected;
    boolean disconnected;
    int messagesReceived;
    int messagesSent;

    /**
     * Constructor
     * @param id the MMSI id of the given MMS client
     * @param host the MMS host
     */
    public CliMmsClient(MmsiId id, String host) throws Exception {
        conf = MmsClientConfiguration.create(id);

        // Register as a listener
        conf.addListener(this);

        // Hook up a dummy position reader
        conf.setPositionReader(new PositionReader() {
            @Override
            public PositionTime getCurrentPosition() {
                return PositionTime.create(0.0, 0.0, System.currentTimeMillis());
            }
        });

        conf.setHost(host);

        mmsClient = conf.build();
        if (mmsClient == null) {
            throw new Exception("Failed creating MMS client");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void connected(URI host) {
        connected = true;
    }

    /** {@inheritDoc} */
    @Override
    public void binaryMessageReceived(byte[] message) {
        messagesReceived++;
    }

    /** {@inheritDoc} */
    @Override
    public void binaryMessageSend(byte[] message) {
        messagesSent++;
    }

    /** {@inheritDoc} */
    @Override
    public void textMessageReceived(String message) {
        messagesReceived++;
    }

    /** {@inheritDoc} */
    @Override
    public void textMessageSend(String message) {
        messagesSent++;
    }

    /** {@inheritDoc} */
    @Override
    public void disconnected(MmsConnectionClosingCode closeReason) {
        disconnected = true;
        connected = false;
    }

    // ** Getters **/

    public MmsClient getMmsClient() {
        return mmsClient;
    }

    public MmsClientConfiguration getConf() {
        return conf;
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public synchronized boolean isDisconnected() {
        return disconnected;
    }

    public int getMessagesReceived() {
        return messagesReceived;
    }

    public int getMessagesSent() {
        return messagesSent;
    }
}
