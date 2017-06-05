/*
 * Copyright 2017 TomeOkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.decay.app;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RunWith(JUnit4.class)
public class SortTest {

    @Test
    public void sortTest() {
        Bundle.create().first(1, 2, 3, 4, 5).second(3, 4, 5, 6, 7).expire(1, 2, 3, 4, 5, 6, 7).mergeTest();
        Bundle.create().first(1, 2, 3, 5).second(3, 4, 6, 7).expire(1, 2, 3, 4, 5, 6, 7).mergeTest();
        Bundle.create().first(1, 2, 3, 5).second(4, 6, 7, 8, 9).expire(1, 2, 3, 4, 5, 6, 7, 8, 9).mergeTest();
        Bundle.create().first(1, 2, 3, 5).second(3, 4).expire(1, 2, 3, 4, 5).mergeTest();
        Bundle.create().first(1, 2, 3, 5).second(4).expire(1, 2, 3, 4, 5).mergeTest();
        Bundle.create().first(1, 2, 3, 5).second().expire(1, 2, 3, 5).mergeTest();
        Bundle.create().first().second(4).expire(4).mergeTest();
        Bundle.create().first(1, 2).second(4, 5).expire(1, 2, 4, 5).mergeTest();
        Bundle.create().first(4, 5).second(1, 2).expire(1, 2, 4, 5).mergeTest();
        Bundle.create().first(1, 2).second(1, 2).expire(1, 2).mergeTest();
        Bundle.create().first(1).second(1).expire(1).mergeTest();
        Bundle.create().first(1, 2, 3).second(1, 2).expire(1, 2, 3).mergeTest();
        Bundle.create().first(1, 2, 3).second(1, 2, 3).expire(1, 2, 3).mergeTest();
        Bundle.create().first(1, 2, 3).second(1, 2, 4).expire(1, 2, 3, 4).mergeTest();
    }

    static class Bundle {
        List<Integer> first, second;
        Integer[] expire;

        public static Bundle create() {
            return new Bundle();
        }

        public Bundle first(Integer... first) {
            this.first = Arrays.asList(first);
            return this;
        }

        public Bundle second(Integer... second) {
            this.second = Arrays.asList(second);
            return this;
        }

        public Bundle expire(Integer... expire) {
            this.expire = expire;
            return this;
        }

        public void mergeTest() {
            List<Integer> third = mergeLists(first, second, Integer::compareTo);
            Integer[] result = new Integer[third.size()];
            third.toArray(result);
            Assert.assertArrayEquals(expire, result);
        }
    }

    @Test
    public void descSortTest() {
        List<Integer> first = Arrays.asList(3, 2, 1);
        List<Integer> second = Arrays.asList(3, 2, 1);

        List<Integer> third = combineSortList(first, second, Comparator.reverseOrder());
        Integer[] result = new Integer[third.size()];
        third.toArray(result);
        System.out.println(Arrays.toString(result));
    }

    private static <T> List<T> combineSortList(List<T> first, List<T> second, Comparator<T> comparator) {
        if (first == null) return second;
        if (second == null) return first;

        if (first.isEmpty()) return new ArrayList<>(second);
        if (second.isEmpty()) return new ArrayList<>(first);

        int fcount = first.size();
        int scount = second.size();

        int pos = fcount;
        boolean found = false;
        T right = second.get(0);
        for (int i = fcount - 1; i >= 0; i--) {
            T left = first.get(i);
            int result = comparator.compare(left, right);
            if (result == 0) {
                pos = i;
                found = true;
                break;
            } else if (result < 0) {
                pos = i;
                break;
            } else {
                pos = i - 1;
            }
        }

        // first 总小于 second
        if (pos >= fcount) {
            List<T> third = new ArrayList<>(fcount + scount);
            third.addAll(first);
            third.addAll(second);
            return third;
        }

        // 两个 list 存在相同的部分
        List<T> third = new ArrayList<>(pos + scount);
        third.addAll(first.subList(0, found ? pos : pos + 1));
        third.add(second.get(0));

        int pi = pos + 1, pj = 1;
        for (; pi < fcount; pi++) {
            T l = first.get(pi);

            boolean inc = false;
            for (int j = pj; j < scount; j++) {
                T r = second.get(j);

                int result = comparator.compare(l, r);
                if (result == 0) {
                    pj = j + 1;
                    third.add(r);
                    inc = true;
                    break;
                } else if (result > 0) {
                    pj = j + 1;
                    third.add(r);
                } else {
                    pj = j;
                    third.add(l);
                    break;
                }
            }

            if (pj >= scount) {
                if (inc) pi++;
                break;
            }
        }

        if (pi < fcount) {
            third.addAll(first.subList(pi, fcount));
        }

        if (pj < scount) {
            third.addAll(second.subList(pj, scount));
        }
        return third;
    }

    public static <T> List<T> mergeLists(List<T> first, List<T> second, Comparator<T> comparator) {
        if (first == null) return second;
        if (second == null) return first;

        if (first.isEmpty()) return new ArrayList<>(second);
        if (second.isEmpty()) return new ArrayList<>(first);

        final int m = first.size(), n = second.size();

        int pos = m - 1;
        boolean found = false;
        T right = second.get(0);
        for (; pos >= 0; --pos) {
            T left = first.get(pos);
            int result = comparator.compare(left, right);
            if (result == 0) {
                found = true;
                break;
            } else if (result < 0) {
                break;
            }
        }

        // if found: first[pos] == second[0]
        // else: first[pos] < second[0] || (pos == -1, so subList is empty, second[0] is the minimum)
        List<T> third = new ArrayList<>(pos + n);
        third.addAll(first.subList(0, found ? pos : pos + 1));
        third.add(second.get(0));

        int pi = pos + 1, pj = 1;
        while(pi < m && pj < n) {
            T l = first.get(pi);
            T r = second.get(pj);
            int result = comparator.compare(l, r);
            if (result == 0) {
                ++pi;
                ++pj;
                third.add(r);
            } else if (result > 0) {
                third.add(r);
                ++pj;
            } else {
                third.add(l);
                ++pi;
            }
        }

        if (pi < m) third.addAll(first.subList(pi, m));
        if (pj < n) third.addAll(second.subList(pj, n));

        // int pi = pos + 1, pj = 1;
        // for (; pi < m; ++pi) {
        //     T l = first.get(pi);
        //
        //     boolean skip = false;
        //     for (; pj < n; ++pj) {
        //         T r = second.get(pj);
        //
        //         int result = comparator.compare(l, r);
        //         if (result == 0) {
        //             ++pj;
        //             third.add(r);
        //             skip = true;
        //             break;
        //         } else if (result > 0) {
        //             third.add(r);
        //         } else {
        //             third.add(l);
        //             break;
        //         }
        //     }
        //
        //     if (pj >= n) {
        //         if (skip) pi++;
        //         break;
        //     }
        // }
        //
        // if (pi < m) third.addAll(first.subList(pi, m));
        // if (pj < n) third.addAll(second.subList(pj, n));

        return third;
    }
}
