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

import dk.dma.enumgenerator.Generator;

/**
 *
 * @author Kasper Nielsen
 */
public class Test2 {

    public static void main(String[] args) throws IOException {
        Generator.generate(SIBaseUnits.addTemperature());
        Generator.generate(SIBaseUnits.addLength());
        Generator.generate(SIBaseUnits.addSpeed());
        // Units.newSI("second", "second", "time");
        // Units.newSI("ampare", "m", "length");
        // Units.newSI("kelvin", "m", "length");
        // Units.newSI("mole", "m", "length");
        // Units.newSI("candala", "m", "LuminousIntensity");
    }


}
