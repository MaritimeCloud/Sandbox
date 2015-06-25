package testproject;

import java.io.IOException;

import net.maritimecloud.message.Message;
import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.MessageSerializer;
import net.maritimecloud.message.MessageWriter;
import net.maritimecloud.message.ValueSerializer;
import net.maritimecloud.net.EndpointInvocationFuture;
import net.maritimecloud.net.LocalEndpoint;

public final class Hello extends LocalEndpoint {

    /** The name of the endpoint. */
    public static final String NAME = "testproject.Hello";

    public Hello(Invocator ei) {
        super(ei);
    }

    public EndpointInvocationFuture<Void> hi() {
        Hi arguments = new Hi();
        return invokeRemote("testproject.Hello.hi", arguments, Hi.SERIALIZER, null);
    }

    public EndpointInvocationFuture<Integer> hia() {
        Hia arguments = new Hia();
        return invokeRemote("testproject.Hello.hia", arguments, Hia.SERIALIZER, ValueSerializer.INT);
    }

    public EndpointInvocationFuture<Integer> hib(Integer f1) {
        Hib arguments = new Hib();
        arguments.setF1(f1);
        return invokeRemote("testproject.Hello.hib", arguments, Hib.SERIALIZER, ValueSerializer.INT);
    }

    public EndpointInvocationFuture<B1> hic() {
        Hic arguments = new Hic();
        return invokeRemote("testproject.Hello.hic", arguments, Hic.SERIALIZER, B1.SERIALIZER);
    }

    public EndpointInvocationFuture<B1> hid(B1 b2) {
        Hid arguments = new Hid();
        arguments.setB2(b2);
        return invokeRemote("testproject.Hello.hid", arguments, Hid.SERIALIZER, B1.SERIALIZER);
    }

    static class Hi implements Message {

        /** The full name of this message. */
        public static final String NAME = "testproject.Hello.hi";

        /** A message serializer that can read and write instances of this class. */
        public static final MessageSerializer<Hi> SERIALIZER = new HiSerializer();

        /** {@inheritDoc} */
        @Override
        public Hi immutable() {
            throw new UnsupportedOperationException("method not supported");
        }

        /** Returns a JSON representation of this message */
        public String toJSON() {
            throw new UnsupportedOperationException("method not supported");
        }
    }

    /** A serializer for reading and writing instances of Hi. */
    static class HiSerializer extends MessageSerializer<Hi> {

        /** {@inheritDoc} */
        @Override
        public Hi read(MessageReader reader) throws IOException {
            return new Hi();
        }

        /** {@inheritDoc} */
        @Override
        public void write(Hi message, MessageWriter writer) throws IOException {}
    }

    static class Hia implements Message {

        /** The full name of this message. */
        public static final String NAME = "testproject.Hello.hia";

        /** A message serializer that can read and write instances of this class. */
        public static final MessageSerializer<Hia> SERIALIZER = new HiaSerializer();

        /** {@inheritDoc} */
        @Override
        public Hia immutable() {
            throw new UnsupportedOperationException("method not supported");
        }

        /** Returns a JSON representation of this message */
        public String toJSON() {
            throw new UnsupportedOperationException("method not supported");
        }
    }

    /** A serializer for reading and writing instances of Hia. */
    static class HiaSerializer extends MessageSerializer<Hia> {

        /** {@inheritDoc} */
        @Override
        public Hia read(MessageReader reader) throws IOException {
            return new Hia();
        }

        /** {@inheritDoc} */
        @Override
        public void write(Hia message, MessageWriter writer) throws IOException {}
    }

    static class Hib implements Message {

        /** The full name of this message. */
        public static final String NAME = "testproject.Hello.hib";

        /** A message serializer that can read and write instances of this class. */
        public static final MessageSerializer<Hib> SERIALIZER = new HibSerializer();

        /** Field definition. */
        private Integer f1;

        /** Creates a new Hib. */
        public Hib() {}

        /**
         * Creates a new Hib by reading from a message reader.
         *
         * @param reader
         *            the message reader
         */
        Hib(MessageReader reader) throws IOException {
            this.f1 = reader.readInt(1, "f1", null);
        }

        void writeTo(MessageWriter w) throws IOException {
            w.writeInt(1, "f1", f1);
        }

        public Integer getF1() {
            return f1;
        }

        public boolean hasF1() {
            return f1 != null;
        }

        public Hib setF1(Integer f1) {
            this.f1 = f1;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Hib immutable() {
            throw new UnsupportedOperationException("method not supported");
        }

        /** Returns a JSON representation of this message */
        public String toJSON() {
            throw new UnsupportedOperationException("method not supported");
        }
    }

    /** A serializer for reading and writing instances of Hib. */
    static class HibSerializer extends MessageSerializer<Hib> {

        /** {@inheritDoc} */
        @Override
        public Hib read(MessageReader reader) throws IOException {
            return new Hib(reader);
        }

        /** {@inheritDoc} */
        @Override
        public void write(Hib message, MessageWriter writer) throws IOException {
            message.writeTo(writer);
        }
    }

    static class Hic implements Message {

        /** The full name of this message. */
        public static final String NAME = "testproject.Hello.hic";

        /** A message serializer that can read and write instances of this class. */
        public static final MessageSerializer<Hic> SERIALIZER = new HicSerializer();

        /** {@inheritDoc} */
        @Override
        public Hic immutable() {
            throw new UnsupportedOperationException("method not supported");
        }

        /** Returns a JSON representation of this message */
        public String toJSON() {
            throw new UnsupportedOperationException("method not supported");
        }
    }

    /** A serializer for reading and writing instances of Hic. */
    static class HicSerializer extends MessageSerializer<Hic> {

        /** {@inheritDoc} */
        @Override
        public Hic read(MessageReader reader) throws IOException {
            return new Hic();
        }

        /** {@inheritDoc} */
        @Override
        public void write(Hic message, MessageWriter writer) throws IOException {}
    }

    static class Hid implements Message {

        /** The full name of this message. */
        public static final String NAME = "testproject.Hello.hid";

        /** A message serializer that can read and write instances of this class. */
        public static final MessageSerializer<Hid> SERIALIZER = new HidSerializer();

        /** Field definition. */
        private B1 b2;

        /** Creates a new Hid. */
        public Hid() {}

        /**
         * Creates a new Hid by reading from a message reader.
         *
         * @param reader
         *            the message reader
         */
        Hid(MessageReader reader) throws IOException {
            this.b2 = reader.readMessage(1, "b2", B1.SERIALIZER);
        }

        void writeTo(MessageWriter w) throws IOException {
            w.writeMessage(1, "b2", b2, B1.SERIALIZER);
        }

        public B1 getB2() {
            return b2;
        }

        public boolean hasB2() {
            return b2 != null;
        }

        public Hid setB2(B1 b2) {
            this.b2 = b2;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Hid immutable() {
            throw new UnsupportedOperationException("method not supported");
        }

        /** Returns a JSON representation of this message */
        public String toJSON() {
            throw new UnsupportedOperationException("method not supported");
        }
    }

    /** A serializer for reading and writing instances of Hid. */
    static class HidSerializer extends MessageSerializer<Hid> {

        /** {@inheritDoc} */
        @Override
        public Hid read(MessageReader reader) throws IOException {
            return new Hid(reader);
        }

        /** {@inheritDoc} */
        @Override
        public void write(Hid message, MessageWriter writer) throws IOException {
            message.writeTo(writer);
        }
    }
}
