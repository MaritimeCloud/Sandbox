package testproject;

import java.io.IOException;
import java.util.Objects;

import net.maritimecloud.internal.message.Hashing;
import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.MessageSerializer;
import net.maritimecloud.message.MessageWriter;
import net.maritimecloud.net.BroadcastMessage;

public class HelloWorld implements BroadcastMessage {

    /** The full name of this message. */
    public static final String NAME = "testproject.HelloWorld";

    /** A message serializer that can read and write instances of this class. */
    public static final MessageSerializer<HelloWorld> SERIALIZER = new Serializer();

    /** Field definition. */
    private String msg;

    /** Creates a new HelloWorld. */
    public HelloWorld() {}

    /**
     * Creates a new HelloWorld by reading from a message reader.
     *
     * @param reader
     *            the message reader
     */
    HelloWorld(MessageReader reader) throws IOException {
        this.msg = reader.readText(1, "msg", null);
    }

    /**
     * Creates a new HelloWorld by copying an existing.
     *
     * @param instance
     *            the instance to copy all fields from
     */
    HelloWorld(HelloWorld instance) {
        this.msg = instance.msg;
    }

    void writeTo(MessageWriter w) throws IOException {
        w.writeText(1, "msg", msg);
    }

    public String getMsg() {
        return msg;
    }

    public boolean hasMsg() {
        return msg != null;
    }

    public HelloWorld setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public HelloWorld immutable() {
        return new Immutable(this);
    }

    /** Returns a JSON representation of this message */
    public String toJSON() {
        return MessageSerializer.writeToJSON(this, SERIALIZER);
    }

    /**
     * Creates a message of this type from a JSON throwing a runtime exception if the format of the message does not match
     */
    public static HelloWorld fromJSON(CharSequence c) {
        return MessageSerializer.readFromJSON(SERIALIZER, c);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Hashing.hashcode(this.msg);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof HelloWorld) {
            HelloWorld o = (HelloWorld) other;
            return Objects.equals(msg, o.msg);
        }
        return false;
    }

    /** A serializer for reading and writing instances of HelloWorld. */
    static class Serializer extends MessageSerializer<HelloWorld> {

        /** {@inheritDoc} */
        @Override
        public HelloWorld read(MessageReader reader) throws IOException {
            return new HelloWorld(reader);
        }

        /** {@inheritDoc} */
        @Override
        public void write(HelloWorld message, MessageWriter writer) throws IOException {
            message.writeTo(writer);
        }
    }

    /** An immutable version of HelloWorld. */
    static class Immutable extends HelloWorld {

        /**
         * Creates a new Immutable instance.
         *
         * @param instance
         *            the instance to make an immutable copy of
         */
        Immutable(HelloWorld instance) {
            super(instance);
        }

        /** {@inheritDoc} */
        @Override
        public HelloWorld immutable() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public HelloWorld setMsg(String msg) {
            throw new UnsupportedOperationException("Instance is immutable");
        }
    }
}
