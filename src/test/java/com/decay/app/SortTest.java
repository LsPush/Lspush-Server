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
        // List<Integer> first = Arrays.asList(1, 2, 3, 4, 5);
        // List<Integer> second = Arrays.asList(3, 4, 5, 6, 7);

        // List<Integer> first = Arrays.asList(1, 2, 3, 5);
        // List<Integer> second = Arrays.asList(3, 4, 6, 7);

        // List<Integer> first = Arrays.asList(1, 2, 3, 5);
        // List<Integer> second = Arrays.asList(4, 6, 7, 8, 9);

        // List<Integer> first = Arrays.asList(1, 2, 3, 5);
        // List<Integer> second = Arrays.asList(3, 4);

        // List<Integer> first = Arrays.asList(1, 2, 3, 5);
        // List<Integer> second = Arrays.asList(4);

        // List<Integer> first = Arrays.asList(1, 2, 3, 5);
        // List<Integer> second = new ArrayList<>();

        // List<Integer> first = new ArrayList<>();
        // List<Integer> second = Arrays.asList(4);

        // List<Integer> first = Arrays.asList(1, 2);
        // List<Integer> second = Arrays.asList(4, 5);

        // List<Integer> first = Arrays.asList(4, 5);
        // List<Integer> second = Arrays.asList(1, 2);

        // List<Integer> first = Arrays.asList(1, 2);
        // List<Integer> second = Arrays.asList(1, 2);

        // List<Integer> first = Arrays.asList(1);
        // List<Integer> second = Arrays.asList(1);

        // List<Integer> first = Arrays.asList(1, 2, 3);
        // List<Integer> second = Arrays.asList(1, 2);

        // List<Integer> first = Arrays.asList(1, 2, 3);
        // List<Integer> second = Arrays.asList(1, 2, 3);

        List<Integer> first = Arrays.asList(1, 2, 3);
        List<Integer> second = Arrays.asList(1, 2, 4);

        List<Integer> third = combineSortList(first, second, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        Integer[] result = new Integer[third.size()];
        third.toArray(result);
        System.out.println(Arrays.toString(result));
    }

    @Test
    public void descSortTest() {
        List<Integer> first = Arrays.asList(3, 2, 1);
        List<Integer> second = Arrays.asList(3, 2, 1);

        List<Integer> third = combineSortList(first, second, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        Integer[] result = new Integer[third.size()];
        third.toArray(result);
        System.out.println(Arrays.toString(result));
    }

    private <T> List<T> combineSortList(List<T> first, List<T> second, Comparator<T> comparator) {
        if (second.size() == 0) return new ArrayList<>(first);

        int fcount = first.size();
        int scount = second.size();

        int pos = fcount;
        boolean noFound = true;
        for (int i = fcount - 1; i >= 0; i--) {
            T left = first.get(i);
            T right = second.get(0);
            int result = comparator.compare(left, right);
            if (result == 0) {
                pos = i;
                noFound = false;
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
        third.addAll(first.subList(0, noFound ? pos + 1 : pos));
        third.add(second.get(0));

        int pi = pos + 1, pj = 1;
        for (; pi < fcount; pi++) {
            T left = first.get(pi);

            boolean inc = false;
            for (int j = pj; j < scount; j++) {
                T right = second.get(j);

                int result = comparator.compare(left, right);
                if (result == 0) {
                    pj = j + 1;
                    third.add(right);
                    inc = true;
                    break;
                } else if (result > 0) {
                    pj = j + 1;
                    third.add(right);
                } else {
                    pj = j;
                    third.add(left);
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
}
