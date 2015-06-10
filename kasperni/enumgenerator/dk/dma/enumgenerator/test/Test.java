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
package dk.dma.enumgenerator.test;


/**
 *
 * @author Kasper Nielsen
 */
public class Test {

    public static void main(String[] args) {
        Temperature t = new Temperature(123, TemperatureUnit.CELCIUS);

        System.out.println(t.toCelcius());
        System.out.println(t.toKelvin());
        System.out.println(t.toFahrenheit());
    }
}
