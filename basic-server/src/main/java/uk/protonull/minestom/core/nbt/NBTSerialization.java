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
package uk.protonull.minestom.core.nbt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public final class NBTSerialization {

    /**
     * Function type that mirrors {@link NBTSerializable#fromNBT(NBTCompound)}.
     */
    @FunctionalInterface
    public interface NBTDeserializer<T extends NBTSerializable> {
        @NotNull
        T fromNBT(@NotNull final NBTCompound nbt);
    }

	/**
     * Reflectively retrieves a serializable's {@link NBTSerializable#fromNBT(NBTCompound) fromNBT} method.
	 *
	 * @param <T> The type of the serializable.
	 * @param clazz The serializable's class.
	 * @return Returns a deserializer function.
	 */
	@SuppressWarnings("unchecked")
    @NotNull
	public static <T extends NBTSerializable> NBTDeserializer<T> getDeserializer(@NotNull final Class<T> clazz) {
        final Method method = MethodUtils.getMatchingAccessibleMethod(clazz, "fromNBT", NBTCompound.class);
		if (!Objects.equals(clazz, method.getReturnType())) {
			throw new IllegalArgumentException("That class hasn't implemented its own fromNBT method.. please fix");
		}
		return (final NBTCompound nbt) -> {
			try {
				return (T) method.invoke(null, nbt);
			}
			catch (final IllegalAccessException | InvocationTargetException exception) {
				throw new NBTSerializationException(exception);
			}
		};
	}

}
