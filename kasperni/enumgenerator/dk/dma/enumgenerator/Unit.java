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
package dk.dma.enumgenerator;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author Kasper Nielsen
 */
public class Unit {
    final ArrayList<Conversion> conversions = new ArrayList<>();

    final String name;

    Val main;

    final TreeMap<String, Val> vals = new TreeMap<>();

    public Unit(String name) {
        this.name = requireNonNull(name);
    }

    Class<?> type() {
        return double.class;
    }

    public Val addVal(String name, String symbol) {
        Val v = new Val(name, symbol);
        vals.put(name, v);
        return v;
    }

    public void convert(Val from, Val to, String expression) {
        conversions.add(new Conversion(from, to, expression));
    }

    public void convertMultiple(Val from, Val to, long l) {
        conversions.add(new Conversion(from, to, "x / " + l));
        conversions.add(new Conversion(to, from, "x * " + l));
    }

    public void convertMultiple(Val from, Val to, double d) {
        conversions.add(new Conversion(from, to, "x / " + d + "D"));
        conversions.add(new Conversion(to, from, "x * " + d + "D"));
    }

    public Val setMain(String name, String symbol) {
        Val v = main = new Val(name, symbol);
        vals.put(name, v);
        return v;
    }

    static class Conversion {
        final String expression;

        final Val from;

        final Val to;

        Conversion(Val from, Val to, String expression) {
            this.from = requireNonNull(from);
            this.to = requireNonNull(to);
            this.expression = requireNonNull(expression);
        }
    }

    public static class Val {
        String name;

        String symbol;

        String description;

        Val(String name, String symbol) {
            this.name = requireNonNull(name);
            this.symbol = requireNonNull(symbol);
        }

        public Val setDescription(String description) {
            this.description = description;
            return this;
        }
    }
}
