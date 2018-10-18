package de.eudaemon.sving.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Matchers {

    private Matchers() {

    }

    public static Matcher<Hint> hasComponentWithName(Matcher<String> name) {
        return new ComponentWithName(name);
    }

    private static class ComponentWithName
            extends TypeSafeDiagnosingMatcher<Hint> {

        private final Matcher<String> name;

        private ComponentWithName(Matcher<String> name_) {
            name = name_;
        }

        @Override
        protected boolean matchesSafely(Hint item, Description mismatchDescription) {
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

    public static Matcher<Stream<?>> empty() {
        return new EmptyStream();
    }

    private static class EmptyStream
            extends TypeSafeDiagnosingMatcher<Stream<?>> {

        @Override
        protected boolean matchesSafely(Stream<?> item, Description mismatchDescription) {
            List<?> list = item.collect(Collectors.toList());
            if (list.isEmpty()) {
                return true;
            } else {
                mismatchDescription.appendText("Contained " + list.size() + " elements");
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("An empty Stream");
        }
    }
}
