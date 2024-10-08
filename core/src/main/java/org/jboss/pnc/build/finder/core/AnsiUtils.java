/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.build.finder.core;

import static org.apache.commons.lang3.BooleanUtils.FALSE;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public final class AnsiUtils {
    static {
        String isNative = System.getProperty("native", FALSE);

        if (!FALSE.equals(isNative)) {
            install();
        }
    }

    private AnsiUtils() {

    }

    private static void install() {
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
    }

    public static Object cyan(Object o) {
        return new Object() {
            @Override
            public String toString() {
                return Ansi.ansi().fgCyan().a(o).reset().toString();
            }
        };
    }

    public static Object green(Object o) {
        return new Object() {
            @Override
            public String toString() {
                return Ansi.ansi().fgGreen().a(o).reset().toString();
            }
        };
    }

    public static Object red(Object o) {
        return new Object() {
            @Override
            public String toString() {
                return Ansi.ansi().fgRed().a(o).reset().toString();
            }
        };
    }

    public static Object boldRed(Object o) {
        return new Object() {
            @Override
            public String toString() {
                return Ansi.ansi().fgRed().bold().a(o).reset().toString();
            }
        };
    }

    public static Object boldYellow(Object o) {
        return new Object() {
            @Override
            public String toString() {
                return Ansi.ansi().fgYellow().bold().a(o).reset().toString();
            }
        };
    }
}
