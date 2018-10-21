package de.eudaemon.sving.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Matchers {

    private static Collector<CharSequence, ?, String> streamJoiner = Collectors.joining("[", ", ", "]");;

    private Matchers() {
    }

    public static Matcher<Hint<? extends Component>> hasComponentWithName(Matcher<String> name) {
        return new ComponentWithName(name);
    }

    private static class ComponentWithName
            extends TypeSafeDiagnosingMatcher<Hint<? extends Component>> {

        private final Matcher<String> name;

        private ComponentWithName(Matcher<String> name_) {
            name = name_;
        }

        @Override
        protected boolean matchesSafely(Hint<? extends Component> item, Description mismatchDescription) {
            if (name.matches(item.component.getName())) {
                return true;
            } else {
                name.describeMismatch(item.component.getName(), mismatchDescription);
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a hint for a component with name ");
            name.describeTo(description);
        }

    }

    public static <T> Matcher<Stream<T>> anyMatch(Matcher<T> elementMatcher) {
        return new AnyInStream<>(elementMatcher);
    }

    private static class AnyInStream<T>
            extends TypeSafeDiagnosingMatcher<Stream<T>> {

        private final Matcher<T> element;

        private AnyInStream(Matcher<T> element_) {
            element = element_;
        }

        @Override
        protected boolean matchesSafely(Stream<T> item, Description mismatchDescription) {
            List<T> list = item.collect(Collectors.toList());
            if (list.stream().noneMatch(element::matches)) {
                mismatchDescription.appendText(" but was ")
                        .appendText(list.stream().map(Object::toString).collect(Collectors.joining("[", ", ", "]")));
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("A stream containing any element that ");
            element.describeTo(description);
        }
    }

    public static <T> Matcher<T> matches(String description, Predicate<T> matches, Function<T, String> describeMismatch) {
        return new GenericMatcher<>(description, matches, describeMismatch);
    }

    private static class GenericMatcher<T>
            extends TypeSafeMatcher<T> {

        private final String descriptionText;
        private final Predicate<T> matches;
        private final Function<T, String> describeMismatch;

        private GenericMatcher(
                String descriptionText_,
                Predicate<T> matches_,
                Function<T, String> describeMismatch_
                ) {
            descriptionText = descriptionText_;
            matches = matches_;
            describeMismatch = describeMismatch_;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(descriptionText);
        }

        @Override
        protected boolean matchesSafely(T item) {
            return matches.test(item);
        }

        @Override
        protected void describeMismatchSafely(T item, Description mismatchDescription) {
            mismatchDescription.appendText(describeMismatch.apply(item));
        }
    }

}
