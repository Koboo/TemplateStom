/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2021 Protonull <protonull@protonmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.protonull.minestom;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.minestom.server.Bootstrap;

public final class Program {

    private static final String SERVER_NAME = "BasicMinestomServer";
    private static final String SERVER_DEFAULT_PATH = "uk.protonull.minestom.server.Server";
    private static final String SERVER_PATH_PROPERTY = "server.path";

    public static void main(final String[] arguments) {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        final Logger logger = Logger.getLogger(SERVER_NAME);
        try {
            Bootstrap.bootstrap(System.getProperty(SERVER_PATH_PROPERTY, SERVER_DEFAULT_PATH), arguments);
        }
        catch (final Throwable thrown) {
            logger.log(Level.SEVERE, "An error occurred while bootstrapping!", thrown);
            System.exit(1); // Sometimes Minestom just doesn't exit ¯\_(ツ)_/¯
        }
    }

}
