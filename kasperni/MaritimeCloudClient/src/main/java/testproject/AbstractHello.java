package testproject;

import java.io.IOException;

import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.ValueWriter;
import net.maritimecloud.net.EndpointImplementation;
import net.maritimecloud.net.MessageHeader;

public abstract class AbstractHello implements EndpointImplementation {

    protected abstract void hi(MessageHeader header);

    protected abstract Integer hia(MessageHeader header);

    protected abstract Integer hib(MessageHeader header, Integer f1);

    protected abstract B1 hic(MessageHeader header);

    protected abstract B1 hid(MessageHeader header, B1 b2);

    /** {@inheritDoc} */
    @Override
    public final void invoke(String name, MessageHeader header, MessageReader reader, ValueWriter writer) throws IOException {
        if (name.equals("hi")) {
            hi(header);
            return;
        }
        if (name.equals("hia")) {
            Integer result = hia(header);
            writer.writeInt(result);
            return;
        }
        if (name.equals("hib")) {
            Integer f1_ = reader.readInt(1, "f1", null);;
            Integer result = hib(header, f1_);
            writer.writeInt(result);
            return;
        }
        if (name.equals("hic")) {
            B1 result = hic(header);
            writer.writeMessage(result, B1.SERIALIZER);
            return;
        }
        if (name.equals("hid")) {
            B1 b2_ = reader.readMessage(1, "b2", B1.SERIALIZER);;
            B1 result = hid(header, b2_);
            writer.writeMessage(result, B1.SERIALIZER);
            return;
        }
        throw new UnsupportedOperationException("Unknown method '" + name + "'");
    }

    public final String getEndpointName() {
        return "testproject.Hello";
    }
}
