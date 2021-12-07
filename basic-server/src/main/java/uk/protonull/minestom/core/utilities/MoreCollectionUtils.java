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

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class that fills in the gaps of {@link CollectionUtils}.
 *
 * @author Protonull
 */
public final class MoreCollectionUtils {

    /**
	 * @param <T> The type of the collection's elements.
	 * @param collection The collection to ensure the size of.
	 * @param size The size to ensure.
	 * @param defaultElement The element to place into the collection if expanded.
	 */
	public static <T> void ensureMinimumSize(@Nonnull final Collection<T> collection,
											 final int size,
											 final T defaultElement) {
		if (size < 0 || size < collection.size()) {
			return;
		}
		for (int i = 0, l = size - collection.size(); i < l; i++) {
			collection.add(defaultElement);
		}
	}

    /**
     * Retrieves a random element from a collection of elements.
     *
     * @param <T> The type of element.
     * @param collection The collection to retrieve a value from.
     * @return Returns a random element, or null.
     */
    public static <T> T randomElement(@NotNull final Collection<T> collection) {
        final int size = collection.size();
        if (size < 1) {
            return null;
        }
        if (size == 1) {
            return IterableUtils.get(collection, 0);
        }
        return IterableUtils.get(collection, ThreadLocalRandom.current().nextInt(size));
    }

	/**
	 * Calculates the number of elements that fulfill a given condition.
	 *
	 * @param <T> The type of element.
	 * @param collection The collection to match the elements of.
	 * @param matcher The matcher function itself.
	 * @return Returns the number of elements that match.
	 */
	public static <T> int numberOfMatches(@NotNull final Collection<T> collection,
                                          @NotNull final Predicate<T> matcher) {
		int counter = 0;
		for (final T element : collection) {
			if (matcher.test(element)) {
				counter++;
			}
		}
		return counter;
	}

}
