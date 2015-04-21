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
package net.maritimecloud.cli.benchmark;

import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.ValueWriter;
import net.maritimecloud.net.EndpointImplementation;
import net.maritimecloud.net.MessageHeader;

import java.io.IOException;

/**
 * A test endpoint implementation
 */
public class TestEndpointImpl implements EndpointImplementation {

    protected String test(MessageHeader context) {
        return "pong";
    }

    /** {@inheritDoc} */
    @Override
    public final void invoke(String name, MessageHeader context, MessageReader reader, ValueWriter writer)
            throws IOException {
        if (name.equals("test")) {
            String result = test(context);
            writer.writeText(result);
            return;
        }
        throw new UnsupportedOperationException("Unknown method '" + name + "'");
    }

    /** {@inheritDoc} */
    @Override
    public final String getEndpointName() {
        return "TestEndpoint";
    }
}
