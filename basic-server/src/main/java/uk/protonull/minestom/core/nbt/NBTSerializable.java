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

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface NBTSerializable {

	/**
	 * Serializes this class onto a given NBTCompound.
	 *
	 * @param nbt The NBTCompound to serialize into, which <i>should</i> NEVER be null, so feel free to throw an
	 *            {@link NBTSerializationException} if it is. You can generally assume that the NBTCompound is new and
	 *            therefore empty, but you <i>may</i> wish to check that.
	 */
	void toNBT(@NotNull final NBTCompound nbt);

	/**
	 * <p>Deserializes a given NBTCompound into a new class instance.</p>
	 *
	 * <p><b>NOTE:</b> When copying this to your extension class, change the return type to that class.</p>
	 *
	 * @param nbt The NBTCompound to deserialize from, which <i>should</i> NEVER be null, so feel free to throw an
	 *            {@link NBTSerializationException} if it is.
	 * @return Returns a new instance of this class.
	 */
	@NotNull
	public static NBTSerializable fromNBT(@NotNull final NBTCompound nbt) {
		throw new NotImplementedException("Please implement me on your class!");
	}

}
