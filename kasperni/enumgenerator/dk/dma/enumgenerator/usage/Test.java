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
package dk.dma.enumgenerator.usage;

import java.io.IOException;

import net.maritimecloud.message.MessageWriter;
import net.maritimecloud.message.ValueReader;
import net.maritimecloud.message.ValueSerializer;
import net.maritimecloud.message.ValueWriter;


/**
 *
 * @author Kasper Nielsen
 */
public class Test {

    public static void main(String[] args) throws IOException {
        // Units u = Units.create("Length");
        // u.add("Meters", 1);
        // u.add("Kilometers", 1000);
        // u.add("Knots", 1862);
        // u.add("Miles", 1609.344);
        // Generator.generate(u);

        // Units u = Units.create("Length", "Meter");
        // u.add("Kilometer", 1000);
        // u.add("Knot", 1862);
        // u.add("Mile", 1609.344);
        //
        //
        // u = Units.create("Temperature", "Kelvin");
        // u.add("Celcius", 273.15);
        //
        // Units.newSI("metre", "m", "length");
        // Units.newSI("kilogram", "kg", "weight");

    }

    enum Length {
        Meter, Kilometer, Knot, Mile;
    }

    static class Temperatur extends Unit<Temperatur> {
        public static final Temperatur Kelvin = new Temperatur("K") {
            public String convertTo(Temperatur other) {
                if (other == Celcius) {
                    return "x + 273.15d";
                } else {
                    return "(x + 459.67d) * 5d⁄9d";
                }
            }
        };

        public static final Temperatur Celcius = new Temperatur("\u00f8C");

        public static final Temperatur Fahrenheit = new Temperatur("\u00f8F");

        Temperatur(String symbol) {
            super(symbol);
            conversion(Kelvin, Celcius, "x + 273.15d");
            conversion(Kelvin, Fahrenheit, "(x + 459.67d) * 5d⁄9d");
        }
    }

    static class Unit<T extends Unit<T>> {
        final String symbol;

        Unit(String symbol) {
            this.symbol = symbol;
        }

        public final void conversion(T from, T to, String expression) {

        }

        public String convertTo(T other) {
            return null;
        }
    }


    // Vi skal have et value object type agtigt foobar.

    // det her er for aandsvagt
    // speed : {
    // value = 123.23
    // }

    static class Distance implements ValueObject {
        static final ValueSerializer<Distance> p = new ValueSerializer<Distance>() {
            public Distance read(ValueReader s) throws IOException {
                s.readText();
                return new Distance();
            }

            @Override
            public void write(Distance t, ValueWriter writer) throws IOException {
                writer.writeInt(1);
            }

            @Override
            public void write(int tag, String name, Distance t, MessageWriter writer) throws IOException {}
        };

        public void write(SerWriter s) {
            s.writeString("foo");
        }
    }
}
