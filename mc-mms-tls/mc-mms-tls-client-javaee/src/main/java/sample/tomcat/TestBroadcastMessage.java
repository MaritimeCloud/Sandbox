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
package sample.tomcat;

import net.maritimecloud.internal.message.Hashing;
import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.MessageSerializer;
import net.maritimecloud.message.MessageWriter;
import net.maritimecloud.net.BroadcastMessage;

import java.io.IOException;
import java.util.Objects;

/**
 * A test broadcast message
 */
public class TestBroadcastMessage implements BroadcastMessage {

    /** The full name of this message */
    public static final String NAME ="net.maritimecloud.cli.benchmark.TestBroadcastMessage";

    /** A message serializer that can read and write instances of this class. */
    public static final MessageSerializer<TestBroadcastMessage> SERIALIZER = new Serializer();

    /** Hey */
    private String msg;

    /** Hey */
    private Integer id;

    /** Creates a new BroadcastTestMessage. */
    public TestBroadcastMessage() {}

    /** Creates a new BroadcastTestMessage. */
    public TestBroadcastMessage(String msg, Integer id) {
        this.msg = msg;
        this.id = id;
    }

    /**
     * Creates a new BroadcastTestMessage by reading from a message reader.
     *
     * @param reader
     *            the message reader
     */
    TestBroadcastMessage(MessageReader reader) throws IOException {
        this.msg = reader.readText(1, "msg", null);
        this.id = reader.readInt(2, "id", null);
    }

    void writeTo(MessageWriter w) throws IOException {
        w.writeText(1, "msg", msg);
        w.writeInt(2, "id", id);
    }

    public String getMsg() {
        return msg;
    }

    public boolean hasMsg() {
        return msg != null;
    }

    public TestBroadcastMessage setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public boolean hasId() {
        return id != null;
    }

    public TestBroadcastMessage setId(Integer id) {
        this.id = id;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public TestBroadcastMessage immutable() {
        return this;
    }

    /** Returns a JSON representation of this message */
    public String toJSON() {
        return MessageSerializer.writeToJSON(this, SERIALIZER);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 31 + Hashing.hashcode(this.msg);
        return 31 * result + Hashing.hashcode(this.id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof TestBroadcastMessage) {
            TestBroadcastMessage o = (TestBroadcastMessage) other;
            return Objects.equals(msg, o.msg) &&
                   Objects.equals(id, o.id);
        }
        return false;
    }

    /** A serializer for reading and writing instances of BroadcastTestMessage. */
    static class Serializer extends MessageSerializer<TestBroadcastMessage> {

        /** {@inheritDoc} */
        @Override
        public TestBroadcastMessage read(MessageReader reader) throws IOException {
            return new TestBroadcastMessage(reader);
        }

        /** {@inheritDoc} */
        @Override
        public void write(TestBroadcastMessage message, MessageWriter writer) throws IOException {
            message.writeTo(writer);
        }
    }
}
