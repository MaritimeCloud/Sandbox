package testproject;

import java.io.IOException;
import java.util.Objects;

import net.maritimecloud.internal.message.Hashing;
import net.maritimecloud.message.Message;
import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.MessageSerializer;
import net.maritimecloud.message.MessageWriter;

public class B1 implements Message {

    /** The full name of this message. */
    public static final String NAME = "testproject.B1";

    /** A message serializer that can read and write instances of this class. */
    public static final MessageSerializer<B1> SERIALIZER = new Serializer();

    /** Field definition. */
    private Integer f1;

    /** Field definition. */
    private Integer f2;

    /** Creates a new B1. */
    public B1() {}

    /**
     * Creates a new B1 by reading from a message reader.
     *
     * @param reader
     *            the message reader
     */
    B1(MessageReader reader) throws IOException {
        this.f1 = reader.readInt(1, "f1", null);
        this.f2 = reader.readInt(3, "f2", null);
    }

    /**
     * Creates a new B1 by copying an existing.
     *
     * @param instance
     *            the instance to copy all fields from
     */
    B1(B1 instance) {
        this.f1 = instance.f1;
        this.f2 = instance.f2;
    }

    void writeTo(MessageWriter w) throws IOException {
        w.writeInt(1, "f1", f1);
        w.writeInt(3, "f2", f2);
    }

    public Integer getF1() {
        return f1;
    }

    public boolean hasF1() {
        return f1 != null;
    }

    public B1 setF1(Integer f1) {
        this.f1 = f1;
        return this;
    }

    public Integer getF2() {
        return f2;
    }

    public boolean hasF2() {
        return f2 != null;
    }

    public B1 setF2(Integer f2) {
        this.f2 = f2;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public B1 immutable() {
        return new Immutable(this);
    }

    /** Returns a JSON representation of this message */
    public String toJSON() {
        return MessageSerializer.writeToJSON(this, SERIALIZER);
    }

    /**
     * Creates a message of this type from a JSON throwing a runtime exception if the format of the message does not match
     */
    public static B1 fromJSON(CharSequence c) {
        return MessageSerializer.readFromJSON(SERIALIZER, c);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 31 + Hashing.hashcode(this.f1);
        return 31 * result + Hashing.hashcode(this.f2);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof B1) {
            B1 o = (B1) other;
            return Objects.equals(f1, o.f1) &&
                   Objects.equals(f2, o.f2);
        }
        return false;
    }

    /** A serializer for reading and writing instances of B1. */
    static class Serializer extends MessageSerializer<B1> {

        /** {@inheritDoc} */
        @Override
        public B1 read(MessageReader reader) throws IOException {
            return new B1(reader);
        }

        /** {@inheritDoc} */
        @Override
        public void write(B1 message, MessageWriter writer) throws IOException {
            message.writeTo(writer);
        }
    }

    /** An immutable version of B1. */
    static class Immutable extends B1 {

        /**
         * Creates a new Immutable instance.
         *
         * @param instance
         *            the instance to make an immutable copy of
         */
        Immutable(B1 instance) {
            super(instance);
        }

        /** {@inheritDoc} */
        @Override
        public B1 immutable() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public B1 setF1(Integer f1) {
            throw new UnsupportedOperationException("Instance is immutable");
        }

        /** {@inheritDoc} */
        @Override
        public B1 setF2(Integer f2) {
            throw new UnsupportedOperationException("Instance is immutable");
        }
    }
}
