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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cakeframework.internal.codegen.CodegenClass;
import org.cakeframework.internal.codegen.CodegenEnum;
import org.cakeframework.internal.codegen.CodegenEnum.CodegenEnumConstant;
import org.cakeframework.internal.codegen.CodegenMethod;

import com.google.common.base.CaseFormat;

import dk.dma.enumgenerator.Unit.Conversion;
import dk.dma.enumgenerator.Unit.Val;

/**
 *
 * @author Kasper Nielsen
 */
public class Generator {

    final String u;

    final Unit unit;

    final String v;

    Generator(Unit unit) {
        u = unit.name + "Unit";
        this.unit = unit;
        v = unit.name.toLowerCase();
    }

    public static void generate(Unit unit) throws IOException {
        Generator g = new Generator(unit);
        CodegenClass c = g.generateClass();
        CodegenClass e = g.generateEnum();

        Path rootpath = Paths.get("enumgenerator");
        c.writeSource(rootpath);
        e.writeSource(rootpath);

        System.out.println(c);
    }

    public CodegenClass generateClass() {
        CodegenClass c = new CodegenClass();
        c.setPackage("dk.dma.enumgenerator.test");
        c.setDefinition("public final class " + unit.name);

        c.addField("private final ", unit.type(), " ", v, ";");
        c.addField("private final ", unit.name, "Unit unit;");

        CodegenMethod con = c.addMethod("(", unit.type(), " ", v, ", ", unit.name, "Unit unit)");
        con.add("this.", v, " = ", v, ";");
        con.add("this.unit = unit;");

        // c.newMethod(")
        CodegenMethod getUnit = c.addMethod("public ", u, " getUnit()");
        getUnit.add("return unit;");


        for (Val val : unit.vals.values()) {
            CodegenMethod m = c.addMethod("public ", unit.type(), " to", val.name, "()");
            m.add("return unit.to" + val.name, "(", v, ");");
            m.javadoc().set(
                    "Returns the " + v + " in "
                            + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, val.name).replace("_", " ") + ".");
            m.javadoc().addParameter(v, "the " + v + " to convert");
            m.javadoc().addReturn("the converted " + v);
            // m.throwNewUnsupportedOperationException("fuckoff");
        }


        // Generate Serializer

        // c.addInnerClass("static final class Serializer implements ", ValueSerializer.class, "<", unit.name, ">");

        return c;
    }


    /**
     * Converts the specified speed from this speed unit to kilometers per hour. For example, to convert 100 meters per
     * second to kilometers per hour: <code>SpeedUnit.METERS_PER_SECOND.toKilometersPerHour(100)</code>.
     *
     * @param speed
     *            the speed to convert
     * @return the converted speed
     */
    public CodegenClass generateEnum() {
        CodegenEnum c = new CodegenEnum();
        c.setPackage("dk.dma.enumgenerator.test");
        c.setDefinition("public enum " + unit.name + "Unit");

        for (Val v : unit.vals.values()) {
            String enumName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, v.name);

            CodegenEnumConstant e = c.newConstant(enumName);
            if (v.description != null) {
                e.javadoc().set(v.description);
            }
            for (Val va : unit.vals.values()) {
                CodegenMethod m = e.newMethod("public double to", va.name, "(", unit.type(), " ", this.v, ")");
                if (v == va) {
                    m.add("return ", this.v, ";");
                } else {
                    boolean found = false;
                    for (Conversion co : unit.conversions) {
                        if (co.from == v && co.to == va) {
                            String ex = co.expression.replace("x", this.v);
                            String val = "return " + ex + ";";
                            m.add(val);
                            found = true;
                        }
                    }
                    if (!found) {
                        m.throwNewUnsupportedOperationException("NotImplementedYet");
                    }
                }
            }

            // 32 c toFahrenheit
            // Celcius
        }

        for (Val v : unit.vals.values()) {
            CodegenMethod m = c.addMethod("public abstract double to", v.name, "(", unit.type(), " ", this.v, ")");
            // 32 c toFahrenheit
            // Celcius
        }


        return c;
    }
}
