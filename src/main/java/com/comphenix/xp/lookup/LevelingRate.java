/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.comphenix.xp.lookup;

import com.comphenix.xp.extra.IntervalTree;
import com.tealcube.minecraft.bukkit.kern.objecthunter.exp4j.Expression;

/**
 * Allows users to modify the amount of experience that is needed to level up a level.
 *
 * @author Kristian
 */
public class LevelingRate extends IntervalTree<Integer, Integer> {

    // Store the expressions too
    protected
    IntervalTree<Integer, Expression>
        expressions =
        new IntervalTree<Integer, Expression>() {
            protected Integer decrementKey(Integer key) {
                return key - 1;
            }

            protected Integer incrementKey(Integer key) {
                return key + 1;
            }
        };

    /**
     * Associates a given interval of levels with a certain amount of experience. Any previous association will be
     * overwritten in the given range. <p> Overlapping intervals are not permitted. A key can only be associated with a
     * single value.
     *
     * @param lowerBound - the minimum level (inclusive).
     * @param upperBound - the maximum level (inclusive).
     * @param experience - the amount of experience.
     */
    @Override
    public void put(Integer lowerBound, Integer upperBound, Integer experience) {
        super.put(lowerBound, upperBound, experience);
    }

    /**
     * Determines if the given level has a specified amount of experience.
     *
     * @param level - level to check.
     * @return TRUE if the given level has a custom amount of experience, FALSE otherwise.
     */
    @Override
    public boolean containsKey(Integer level) {
        return expressions.containsKey(level) || super.containsKey(level);
    }

    /**
     * Inserts every level range from the given tree into the current tree.
     *
     * @param other - the levels to read from.
     */
    public void putAll(LevelingRate other) {

        // Copy the expressions first - be sure to use the overridden method
        for (IntervalTree<Integer, Expression>.Entry entry : other.expressions.entrySet()) {
            put(entry.getKey().lowerEndpoint(), entry.getKey().upperEndpoint(), entry.getValue());
        }

        // Then the cache
        super.putAll(other);
    }

    /**
     * Retrieves the value of the integer or expression range that contains this level.
     *
     * @param level - the level to find.
     * @return The value of that range.
     */
    @Override
    public Integer get(Integer level) {

        // The integer cache first
        Integer cached = super.get(level);

        if (cached == null) {

            Expression computed = expressions.get(level);

            if (computed != null) {

                int value = (int) computed.setVariable("LEVEL", level).evaluate();

                // Cache this result and return
                super.put(level, level, value);
                return value;

            } else {
                return null;
            }

        } else {
            return cached;
        }
    }

    /**
     * Associates a given interval of levels with a certain amount of experience using an expression. Any previous
     * association will be overwritten in the given range. <p> Overlapping intervals are not permitted. A key can only
     * be associated with a single value.
     *
     * @param lowerBound - the minimum level (inclusive).
     * @param upperBound - the maximum level (inclusive).
     * @param experience - the amount of experience.
     */
    public void put(Integer lowerBound, Integer upperBound, Expression experience) {
        // Clear the "integer" cache
        super.put(lowerBound, upperBound, null);

        // Insert it into the expression tree
        expressions.put(lowerBound, upperBound, experience);
    }


    @Override
    protected Integer decrementKey(Integer key) {
        return key - 1;
    }

    @Override
    protected Integer incrementKey(Integer key) {
        return key + 1;
    }
}
