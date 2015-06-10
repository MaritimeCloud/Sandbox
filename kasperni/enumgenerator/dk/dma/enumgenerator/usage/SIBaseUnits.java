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

import dk.dma.enumgenerator.Unit;
import dk.dma.enumgenerator.Unit.Val;

/**
 *
 * @author Kasper Nielsen
 */
public class SIBaseUnits {


    static Unit addTemperature() {
        Unit u = new Unit("Temperature");
        Val k = u.setMain("Kelvin", "K");
        Val c = u.addVal("Celcius", "\u00f8C");
        Val f = u.addVal("Fahrenheit", "\u00f8F");

        u.convert(c, k, "x + 273.15d");
        u.convert(k, f, "x * 9D / 5D - 459.67d");
        u.convert(k, c, "x - 273.15d");
        u.convert(c, f, "x * 9D / 5D + 32");
        u.convert(f, k, "(x + 459.67d) * 5D / 9D");
        u.convert(f, c, "(x - 32) * 5D / 9D");
        return u;
    }

    static Unit addLength() {
        Unit l = new Unit("Length");
        Val m = l.setMain("Meters", "m").setDescription(
                "Meter as defined by the International Bureau of Weights and Measures.");
        Val km = l.addVal("Kilometers", "km").setDescription(
                "Kilometer as defined by the International Bureau of Weights and Measures.");
        Val mile = l.addVal("Miles", "mile").setDescription("A mile is defined as 1609.344 meters.");
        Val nm = l.addVal("NauticalMiles", "nm").setDescription("A nautical mile is defined as 1852 meters.");


        l.convertMultiple(m, km, 1000);
        l.convertMultiple(m, mile, 1609.344d);
        l.convertMultiple(m, nm, 1852);

        l.convertMultiple(km, mile, 1.609344d);
        l.convertMultiple(km, nm, 1.852d);

        l.convertMultiple(nm, mile, 1.150779d);
        return l;
    }

    static Unit addElectricCurrent() {
        Unit l = new Unit("ElectricCurrent");
        Val m = l.setMain("Ampere", "A").setDescription(
                "Ampere as defined by the International Bureau of Weights and Measures.");
        return l;
    }

    // mass kilogram kg

    // time second s
    // electric current ampere A
    // amount of substance mole mol
    // luminous intensity candela cd
    //


    static Unit addSpeed() {
        Unit l = new Unit("Speed");
        Val ms = l.setMain("MetresPerSecond", "m/s").setDescription("Metres per Second");
        Val kph = l.addVal("KilometersPerHour", "km/h").setDescription("Kilometers per Hour");
        Val mph = l.addVal("MilesPerHour", "mph").setDescription("Miles Per Hour");
        Val knots = l.addVal("Knots", "knots").setDescription("Knots");

        l.convertMultiple(kph, ms, 3.6d);
        l.convertMultiple(knots, ms, 1.9438444924406d); // not exact
        l.convertMultiple(ms, mph, 0.44704d);

        l.convertMultiple(kph, mph, 1.609344d);
        l.convertMultiple(kph, knots, 1.852d);
        l.convertMultiple(mph, knots, 1.1507794480235425d); // not exact

        return l;
    }
}
