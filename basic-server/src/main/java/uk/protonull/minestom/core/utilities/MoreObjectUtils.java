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
package uk.protonull.minestom.core.utilities;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Utility class that fills in the gaps of {@link ObjectUtils}.
 */
public final class MoreObjectUtils {

	/**
	 * Determines whether two things are equal based on their respective hash codes. This is obviously less accurate
	 * than {@link Object#equals(Object)}, however this can be used to, for example, test if an object has been changed.
	 *
	 * @param <L> The left-hand object type.
	 * @param <R> The right-hand object type.
	 * @param lhs The left-hand object.
	 * @param rhs THe right-hand object.
	 * @return Returns whether the left-hand and right-hand objects have matching hashes.
	 */
	public static <L, R> boolean hashEquals(final L lhs,
                                            final R rhs) {
		return lhs == rhs || (!(lhs == null || rhs == null) && lhs.hashCode() == rhs.hashCode());
	}

}
