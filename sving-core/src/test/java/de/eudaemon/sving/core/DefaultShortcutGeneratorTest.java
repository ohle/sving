package de.eudaemon.sving.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultShortcutGeneratorTest {

    private static DefaultShortcutGenerator generator = new DefaultShortcutGenerator("abc");

    @ParameterizedTest
    @MethodSource("lotsOfHints")
    void usesOnlyAllowedCharacters(String h) {
        List<Character> allowed = List.of('a', 'b', 'c');
        assertThat(
                toCharList(h),
                everyItem(isIn(allowed))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 30, 543})
    void doesNotProduceUnnecessarilyLongShortcuts(int total) {
        Stream<List<Character>> shortcuts =
                generator.generate(total).stream()
                        .map(this::toCharList);
        int maxLength = (int) Math.ceil(Math.log(total) / Math.log(3));
        assertThat(
                shortcuts.collect(Collectors.toList()),
                everyItem(iterableWithSize(lessThanOrEqualTo(maxLength))
                ));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 30, 543})
    void noShortcutIsThePrefixOfAnother(int total) {
        List<String> shortcuts =
                generator.generate(total).stream()
                        .sorted(Comparator.comparing(String::length))
                        .collect(Collectors.toList());

        Map<String, List<String>> pairsToCheck = new HashMap<>();
        shortcuts.forEach(sc -> pairsToCheck.put(
                sc,
                shortcuts.stream()
                        .dropWhile(s -> s.length() <= sc.length())
                        .collect(Collectors.toList()))
        );
        pairsToCheck.forEach( (prefix, longer) -> assertThat(longer, not(hasItem(startsWith(prefix)))));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 30, 543})
    void producesDistinctHints(int total) {
        Set<String> uniqueShortcuts = new HashSet<>(generator.generate(total));
        assertThat(uniqueShortcuts.size(), equalTo(total));
    }

    private List<Character> toCharList(String h) {
        return h.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
    }

    private static List<String> lotsOfHints() {
        return generator.generate(20);
    }
}