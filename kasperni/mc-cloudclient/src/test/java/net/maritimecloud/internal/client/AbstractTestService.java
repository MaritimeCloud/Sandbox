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
package net.maritimecloud.internal.client;

import net.maritimecloud.net.MessageHeader;
import testproject.AbstractHello;
import testproject.B1;

/**
 *
 * @author Kasper Nielsen
 */
public class AbstractTestService extends AbstractHello {

    /** {@inheritDoc} */
    @Override
    protected void hi(MessageHeader header) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected Integer hia(MessageHeader header) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected Integer hib(MessageHeader header, Integer f1) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected B1 hic(MessageHeader header) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected B1 hid(MessageHeader header, B1 b2) {
        throw new UnsupportedOperationException();
    }
}
