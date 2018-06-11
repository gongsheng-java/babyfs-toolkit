/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.babyfs.tk.commons.base;

import java.io.Serializable;

/**
 * A simple object that holds onto a pair of object references, first and second.
 *
 * @param <FIRST>
 * @param <SECOND>
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class Pair<FIRST, SECOND> implements Serializable {
    public final FIRST first;
    public final SECOND second;

    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    public FIRST getFirst() {
        return first;
    }

    public SECOND getSecond() {
        return second;
    }

    /**
     * @param first
     * @param second
     * @param <FIRST>
     * @param <SECOND>
     * @return
     */
    public static <FIRST, SECOND> Pair<FIRST, SECOND> of(FIRST first, SECOND second) {
        return new Pair<FIRST, SECOND>(first, second);
    }

    @Override
    public int hashCode() {
        return 17 * ((first != null) ? first.hashCode() : 0)
                + 17 * ((second != null) ? second.hashCode() : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair<?, ?>)) {
            return false;
        }

        Pair<?, ?> that = (Pair<?, ?>) o;
        return eq(this.first, that.first) && eq(this.second, that.second);
    }

    private static boolean eq(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", first, second);
    }
}
