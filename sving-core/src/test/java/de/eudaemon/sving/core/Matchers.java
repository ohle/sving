package de.eudaemon.sving.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

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
}
