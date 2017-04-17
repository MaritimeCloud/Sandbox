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
package net.maritimecloud.internal.client.destination;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.net.mms.MmsConnectionClosingCode;

import com.google.common.collect.EvictingQueue;

/**
 *
 * @author Kasper Nielsen
 */
public class ConnectionLogger implements MmsConnection.Listener {

    static final Clock clock = Clock.systemDefaultZone();

    final EvictingQueue<String> connectionStatus = EvictingQueue.create(1000);

    final EvictingQueue<Map.Entry<LocalDateTime, String>> received = EvictingQueue.create(1000);

    final EvictingQueue<Map.Entry<LocalDateTime, String>> send = EvictingQueue.create(1000);

    private synchronized void addMessage(String msg) {
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
        connectionStatus.add(timestamp + " " + msg + "\n");
    }

    /** {@inheritDoc} */
    @Override
    public void connected(URI host) {
        addMessage("Succesfully connected to " + host);
    }

    /** {@inheritDoc} */
    @Override
    public void connecting(URI host) {
        addMessage("Trying to connect to " + host);
    }

    /** {@inheritDoc} */
    @Override
    public void disconnected(MmsConnectionClosingCode closeReason) {
        addMessage("Disconnected " + closeReason.getMessage() + " (The correct disconnect reason might not be listed)");
    }

    public synchronized String getFullConnectionLog() {
        if (connectionStatus.isEmpty()) {
            return "No log entries";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ArrayList<String> l = new ArrayList<>(connectionStatus);
        for (String s : l) {
            pw.print(s);
            pw.print("<br>");
        }
        return sw.toString();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void textMessageReceived(String message) {
        received.add(new AbstractMap.SimpleImmutableEntry<>(LocalDateTime.now(), message));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void textMessageSend(String message) {
        send.add(new AbstractMap.SimpleImmutableEntry<>(LocalDateTime.now(), message));
    }


    public synchronized String getInfo(boolean isSend) {
        EvictingQueue<Map.Entry<LocalDateTime, String>> s = isSend ? send : received;
        if (s.isEmpty()) {
            return "No log entries";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // print table header


        pw.println("<html>");
        pw.println("<table style=\"width:100%\">");

        for (Entry<LocalDateTime, String> e : s) {
            pw.println("<tr>");
            pw.println("<td>");
            pw.println(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(e.getKey()));
            pw.println("</td>");
            pw.println("<td>");
            pw.println(e.getValue());
            pw.println("</td>");
            pw.println("</tr>");
        }
        pw.println("</table>");
        pw.println("</html>");
        return sw.toString();
    }
}
