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
package uk.protonull.minestom.core;

import java.util.Objects;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.DiscoveredExtension;
import net.minestom.server.extensions.Extension;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class BetterExtension extends Extension {

    /**
     * @return Returns this extension's description.
     */
    @NotNull
    @Override
    public DiscoveredExtension getOrigin() {
        return Objects.requireNonNull(super.getOrigin(),
                "Origin hasn't been set. Did you call this in your extension's constructor or static block?");
    }

    /**
     * @return Returns the logger of this extension.
     */
    @NotNull
    @Override
    public Logger getLogger() {
        return Objects.requireNonNull(super.getLogger(),
                "Logger hasn't been set. Did you call this in your extension's constructor or static block?");
    }

    /**
     * @return Returns the name of this extension.
     */
    public String getName() {
        return getOrigin().getName();
    }

    /**
     * Disables this extension.
     */
    public void disable() {
        // TODO: Have to do this because Minestom made the method private for some reason
        try {
            MethodUtils.invokeExactMethod(
                    MinecraftServer.getExtensionManager(),
                    "unloadExtension",
                    new Object[]{
                            getName()
                    },
                    new Object[] {
                            String.class
                    });
        }
        catch (final Throwable thrown) {
            throw new RuntimeException("Could not disable extension: " + getName(), thrown);
        }
    }

}
