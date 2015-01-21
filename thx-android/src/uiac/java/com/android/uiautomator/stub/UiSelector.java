/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.uiautomator.stub;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Specifies the elements in the layout hierarchy for tests to target, filtered
 * by properties such as text value, content-description, class name, and state
 * information. You can also target an element by its location in a layout
 * hierarchy.
 *
 * @since API Level 16
 * @author linsong wang
 */
public class UiSelector implements Serializable {
    private static final long serialVersionUID = -973659764329185L;

    static final int SELECTOR_NIL = 0;

    static final int SELECTOR_TEXT = 1;

    static final int SELECTOR_START_TEXT = 2;

    static final int SELECTOR_CONTAINS_TEXT = 3;

    static final int SELECTOR_CLASS = 4;

    static final int SELECTOR_DESCRIPTION = 5;

    static final int SELECTOR_START_DESCRIPTION = 6;

    static final int SELECTOR_CONTAINS_DESCRIPTION = 7;

    static final int SELECTOR_INDEX = 8;

    static final int SELECTOR_INSTANCE = 9;

    static final int SELECTOR_ENABLED = 10;

    static final int SELECTOR_FOCUSED = 11;

    static final int SELECTOR_FOCUSABLE = 12;

    static final int SELECTOR_SCROLLABLE = 13;

    static final int SELECTOR_CLICKABLE = 14;

    static final int SELECTOR_CHECKED = 15;

    static final int SELECTOR_SELECTED = 16;

    static final int SELECTOR_ID = 17;

    static final int SELECTOR_PACKAGE_NAME = 18;

    static final int SELECTOR_CHILD = 19;

    static final int SELECTOR_CONTAINER = 20;

    static final int SELECTOR_PATTERN = 21;

    static final int SELECTOR_PARENT = 22;

    static final int SELECTOR_COUNT = 23;

    static final int SELECTOR_LONG_CLICKABLE = 24;

    static final int SELECTOR_TEXT_REGEX = 25;

    static final int SELECTOR_CLASS_REGEX = 26;

    static final int SELECTOR_DESCRIPTION_REGEX = 27;

    static final int SELECTOR_PACKAGE_NAME_REGEX = 28;

    static final int SELECTOR_RESOURCE_ID = 29;

    static final int SELECTOR_CHECKABLE = 30;

    static final int SELECTOR_RESOURCE_ID_REGEX = 31;

    private final Map<Integer, Object> mSelectorAttributes = new HashMap<>();

    public Object get(int i) {
        return this.mSelectorAttributes.get(i);
    }

    /**
     * Set the search criteria to match the visible text displayed
     * in a widget (for example, the text label to launch an app).
     *
     * The text for the element must match exactly with the string in your input
     * argument. Matching is case-sensitive.
     *
     * @param text Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector text(String text) {
        return buildSelector(SELECTOR_TEXT, text);
    }

    /**
     * Set the search criteria to match the visible text displayed in a layout
     * element, using a regular expression.
     *
     * The text in the widget must match exactly with the string in your
     * input argument.
     *
     * @param regex a regular expression
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 17
     */
    public UiSelector textMatches(String regex) {
        return buildSelector(SELECTOR_TEXT_REGEX, Pattern.compile(regex));
    }

    /**
     * Set the search criteria to match visible text in a widget that is
     * prefixed by the text parameter.
     *
     * The matching is case-insensitive.
     *
     * @param text Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector textStartsWith(String text) {
        return buildSelector(SELECTOR_START_TEXT, text);
    }

    /**
     * Set the search criteria to match the visible text in a widget
     * where the visible text must contain the string in your input argument.
     *
     * The matching is case-sensitive.
     *
     * @param text Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector textContains(String text) {
        return buildSelector(SELECTOR_CONTAINS_TEXT, text);
    }

    /**
     * Set the search criteria to match the class property
     * for a widget (for example, "android.widget.Button").
     *
     * @param className Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector className(String className) {
        return buildSelector(SELECTOR_CLASS, className);
    }

    /**
     * Set the search criteria to match the class property
     * for a widget, using a regular expression.
     *
     * @param regex a regular expression
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 17
     */
    public UiSelector classNameMatches(String regex) {
        return buildSelector(SELECTOR_CLASS_REGEX, Pattern.compile(regex));
    }

    /**
     * Set the search criteria to match the class property
     * for a widget (for example, "android.widget.Button").
     *
     * @param type type
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 17
     */
    public <T> UiSelector className(Class<T> type) {
        return buildSelector(SELECTOR_CLASS, type.getName());
    }

    /**
     * Set the search criteria to match the content-description
     * property for a widget.
     *
     * The content-description is typically used
     * by the Android Accessibility framework to
     * provide an audio prompt for the widget when
     * the widget is selected. The content-description
     * for the widget must match exactly
     * with the string in your input argument.
     *
     * Matching is case-sensitive.
     *
     * @param desc Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector description(String desc) {
        return buildSelector(SELECTOR_DESCRIPTION, desc);
    }

    /**
     * Set the search criteria to match the content-description
     * property for a widget.
     *
     * The content-description is typically used
     * by the Android Accessibility framework to
     * provide an audio prompt for the widget when
     * the widget is selected. The content-description
     * for the widget must match exactly
     * with the string in your input argument.
     *
     * @param regex a regular expression
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 17
     */
    public UiSelector descriptionMatches(String regex) {
        return buildSelector(SELECTOR_DESCRIPTION_REGEX, Pattern.compile(regex));
    }

    /**
     * Set the search criteria to match the content-description
     * property for a widget.
     *
     * The content-description is typically used
     * by the Android Accessibility framework to
     * provide an audio prompt for the widget when
     * the widget is selected. The content-description
     * for the widget must start
     * with the string in your input argument.
     *
     * Matching is case-insensitive.
     *
     * @param desc Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector descriptionStartsWith(String desc) {
        return buildSelector(SELECTOR_START_DESCRIPTION, desc);
    }

    /**
     * Set the search criteria to match the content-description
     * property for a widget.
     *
     * The content-description is typically used
     * by the Android Accessibility framework to
     * provide an audio prompt for the widget when
     * the widget is selected. The content-description
     * for the widget must contain
     * the string in your input argument.
     *
     * Matching is case-insensitive.
     *
     * @param desc Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector descriptionContains(String desc) {
        return buildSelector(SELECTOR_CONTAINS_DESCRIPTION, desc);
    }

    /**
     * Set the search criteria to match the given resource ID.
     *
     * @param id Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 18
     */
    public UiSelector resourceId(String id) {
        return buildSelector(SELECTOR_RESOURCE_ID, id);
    }

    /**
     * Set the search criteria to match the resource ID
     * of the widget, using a regular expression.
     *
     * @param regex a regular expression
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 18
     */
    public UiSelector resourceIdMatches(String regex) {
        return buildSelector(SELECTOR_RESOURCE_ID_REGEX, Pattern.compile(regex));
    }

    /**
     * Set the search criteria to match the widget by its node
     * index in the layout hierarchy.
     *
     * The index value must be 0 or greater.
     *
     * Using the index can be unreliable and should only
     * be used as a last resort for matching. Instead,
     * consider using the {@link #instance(int)} method.
     *
     * @param index Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector index(final int index) {
        return buildSelector(SELECTOR_INDEX, index);
    }

    /**
     * Set the search criteria to match the
     * widget by its instance number.
     *
     * The instance value must be 0 or greater, where
     * the first instance is 0.
     *
     * For example, to simulate a user click on
     * the third image that is enabled in a UI screen, you
     * could specify a a search criteria where the instance is
     * 2, the {@link #className(String)} matches the image
     * widget class, and {@link #enabled(boolean)} is true.
     * The code would look like this:
     * <code>
     * new UiSelector().className("android.widget.ImageView")
     *    .enabled(true).instance(2);
     * </code>
     *
     * @param instance Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector instance(final int instance) {
        return buildSelector(SELECTOR_INSTANCE, instance);
    }

    /**
     * Set the search criteria to match widgets that are enabled.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector enabled(boolean val) {
        return buildSelector(SELECTOR_ENABLED, val);
    }

    /**
     * Set the search criteria to match widgets that have focus.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector focused(boolean val) {
        return buildSelector(SELECTOR_FOCUSED, val);
    }

    /**
     * Set the search criteria to match widgets that are focusable.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector focusable(boolean val) {
        return buildSelector(SELECTOR_FOCUSABLE, val);
    }

    /**
     * Set the search criteria to match widgets that are scrollable.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector scrollable(boolean val) {
        return buildSelector(SELECTOR_SCROLLABLE, val);
    }

    /**
     * Set the search criteria to match widgets that
     * are currently selected.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector selected(boolean val) {
        return buildSelector(SELECTOR_SELECTED, val);
    }

    /**
     * Set the search criteria to match widgets that
     * are currently checked (usually for checkboxes).
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector checked(boolean val) {
        return buildSelector(SELECTOR_CHECKED, val);
    }

    /**
     * Set the search criteria to match widgets that are clickable.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector clickable(boolean val) {
        return buildSelector(SELECTOR_CLICKABLE, val);
    }

    /**
     * Set the search criteria to match widgets that are checkable.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 18
     */
    public UiSelector checkable(boolean val) {
        return buildSelector(SELECTOR_CHECKABLE, val);
    }

    /**
     * Set the search criteria to match widgets that are long-clickable.
     *
     * Typically, using this search criteria alone is not useful.
     * You should also include additional criteria, such as text,
     * content-description, or the class name for a widget.
     *
     * If no other search criteria is specified, and there is more
     * than one matching widget, the first widget in the tree
     * is selected.
     *
     * @param val Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 17
     */
    public UiSelector longClickable(boolean val) {
        return buildSelector(SELECTOR_LONG_CLICKABLE, val);
    }

    /**
     * Adds a child UiSelector criteria to this selector.
     *
     * Use this selector to narrow the search scope to
     * child widgets under a specific parent widget.
     *
     * @param selector
     *
     * @return UiSelector with this added search criterion
     *
     * @since API Level 16
     */
    public UiSelector childSelector(UiSelector selector) {
        return buildSelector(SELECTOR_CHILD, selector);
    }

    private UiSelector patternSelector(UiSelector selector) {
        return buildSelector(SELECTOR_PATTERN, selector);
    }

    private UiSelector containerSelector(UiSelector selector) {
        return buildSelector(SELECTOR_CONTAINER, selector);
    }

    /**
     * Adds a child UiSelector criteria to this selector which is used to
     * start search from the parent widget.
     *
     * Use this selector to narrow the search scope to
     * sibling widgets as well all child widgets under a parent.
     *
     * @param selector
     *
     * @return UiSelector with this added search criterion
     *
     * @since API Level 16
     */
    public UiSelector fromParent(UiSelector selector) {
        return buildSelector(SELECTOR_PARENT, selector);
    }

    /**
     * Set the search criteria to match the package name
     * of the application that contains the widget.
     *
     * @param name Value to match
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 16
     */
    public UiSelector packageName(String name) {
        return buildSelector(SELECTOR_PACKAGE_NAME, name);
    }

    /**
     * Set the search criteria to match the package name
     * of the application that contains the widget.
     *
     * @param regex a regular expression
     *
     * @return UiSelector with the specified search criteria
     *
     * @since API Level 17
     */
    public UiSelector packageNameMatches(String regex) {
        return buildSelector(SELECTOR_PACKAGE_NAME_REGEX, Pattern.compile(regex));
    }

    @Override
    public String toString() {
        return this.getClass() + "[" + this.mSelectorAttributes + "]";
    }

    /**
     * Building a UiSelector always returns a new UiSelector and never modifies the
     * existing UiSelector being used.
     */
    private UiSelector buildSelector(int selectorId, Object selectorValue) {
        if (selectorId == SELECTOR_CHILD || selectorId == SELECTOR_PARENT) {
            throw new UnsupportedOperationException();
//            selector.getLastSubSelector().mSelectorAttributes.put(selectorId, selectorValue);
        } else {
            this.mSelectorAttributes.put(selectorId, selectorValue);
        }
        return this;
    }
}
