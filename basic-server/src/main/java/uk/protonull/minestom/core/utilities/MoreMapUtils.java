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

import com.google.common.collect.BiMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class MoreMapUtils {

    /**
     * Removes all the elements of the given map that satisfy the given predicate. Errors or runtime exceptions thrown
     * during iteration or by the predicate are relayed to the caller.
     *
     * @param <K> The type of the map's keys.
     * @param <V> The type of the map's values.
     * @param map The map instance to filter.
     * @param filter The filter predicate which returns {@code true} on elements that should be removed.
     * @return Returns {@code true} if any elements were removed.
     *
     * @throws NullPointerException if either the given map or filter is null.
     * @throws UnsupportedOperationException if elements cannot be removed from this collection. Implementations may
     *         throw this exception if a matching element cannot be removed or if, in general, removal is not supported.
     */
    public static <K, V> boolean removeIf(@NotNull final Map<K, V> map,
                                          @NotNull final BiPredicate<K, V> filter) {
        return map.entrySet().removeIf((entry) -> filter.test(entry.getKey(), entry.getValue()));
    }

    /**
     * Retrieves a key from a map based on a given value. If two or more keys share a value,
     * the key that's returned is the first that matches during standard iteration.
     *
     * @param <K> The type of the map's keys.
     * @param <V> The type of the map's values.
     * @param map The map to retrieve the key from.
     * @param value The value to base the search on.
     * @return Returns the key, or null.
     */
    public static <K, V> K getKeyFromValue(@NotNull final Map<K, V> map,
                                           final V value) {
        if (map instanceof BiMap<K, V> biMap) {
            return biMap.inverse().get(value);
        }
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Attempts to retrieve a value from a given map from a range of keys.
     *
     * @param <K> The type of the map's keys.
     * @param <V> The type of the map's values.
     * @param <R> The desired return type.
     * @param map The map to retrieve the value from.
     * @param keys The keys to retrieve the value from in order of appearance.
     * @param parser The function to process the value from the map.
     * @param fallback The value that should be returned if none of the keys return a [valid] value.
     * @return Returns a value, either from the keys or the fallback, both of which may be null.
     */
    public static <K, V, R> R attemptGet(@NotNull final Map<K, V> map,
                                         @NotNull final Set<K> keys,
                                         @NotNull final Function<V, R> parser,
                                         final R fallback) {
        if (map.isEmpty() || keys.isEmpty()) {
            return fallback;
        }
        for (final K key : keys) {
            if (map.containsKey(key)) {
                try {
                    return parser.apply(map.get(key));
                }
                catch (final Throwable thrown) {
                    return fallback;
                }
            }
        }
        return fallback;
    }

    /**
     * Sets all the given keys a particular value.
     *
     * @param <K> The type of the map's keys.
     * @param <V> The type of the map's values.
     * @param map The map to set to.
     * @param keys The keys to set the value for.
     * @param value The value to set.
     */
    public static <K, V> void setMultipleKeys(@NotNull final Map<K, V> map,
                                              @NotNull final Set<K> keys,
                                              final V value) {
        for (final K key : keys) {
            map.put(key, value);
        }
    }

    /**
     * Computes all elements of a given map.
     *
     * @param <K> The type of the map's keys.
     * @param <V> The type of the map's values.
     * @param map The map to compute.
     * @param computer The computing function, which should return the new value for the given key.
     */
    public static <K, V> void computeEntries(@NotNull final Map<K, V> map,
                                             @NotNull final BiFunction<K, V, V> computer) {
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            final V beforeValue = entry.getValue();
            final V afterValue = computer.apply(entry.getKey(), beforeValue);
            if (beforeValue != afterValue) { // Use reference equals for quicker comparison
                entry.setValue(afterValue);
            }
        }
    }

    /**
     * Generates a new map where the String keys are case-insensitive.
     *
     * @param <T> The type of the map's values.
     * @return Returns a new TreeMap with String keys that are <b>NOT</b> case-sensitive.
     */
    @NotNull
    public static <T> TreeMap<String, T> newStringKeyMap() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

}
