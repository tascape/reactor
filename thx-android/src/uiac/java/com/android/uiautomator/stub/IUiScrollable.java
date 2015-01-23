package com.android.uiautomator.stub;

/**
 *
 * @author linsong wang
 */
public interface IUiScrollable extends IUiCollection {

    void useUiScrollableSelector(UiSelector selector);

    /**
     * Scrolls forward until the UiObject is fully visible in the scrollable container (Not supported yet).
     * Use this method to make sure that the child item's edges are not offscreen.
     *
     * @param childObject {@link UiObject} representing the child element
     *
     * @return true if the child element is already fully visible, or
     *         if the method scrolled successfully until the child became fully visible;
     *         otherwise, false if the attempt to scroll failed.
     *
     * @hide
     */
    boolean ensureFullyVisible(IUiObject childObject);

    /**
     * Performs a backwards fling action with the default number of fling
     * steps (5). If the swipe direction is set to vertical,
     * then the swipe will be performed from top to bottom. If the swipe
     * direction is set to horizontal, then the swipes will be performed from
     * left to right. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @return true if scrolled, and false if can't scroll anymore
     *
     * @since API Level 16
     */
    boolean flingBackward();

    /**
     * Performs a forward fling with the default number of fling steps (5).
     * If the swipe direction is set to vertical, then the swipes will be
     * performed from bottom to top. If the swipe
     * direction is set to horizontal, then the swipes will be performed from
     * right to left. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @return true if scrolled, false if can't scroll anymore
     *
     * @since API Level 16
     */
    boolean flingForward();

    /**
     * Performs a fling gesture to reach the beginning of a scrollable layout element.
     * The beginning can be at the top-most edge in the case of vertical controls, or
     * the left-most edge for horizontal controls. Make sure to take into
     * account devices configured with right-to-left languages like Arabic and Hebrew.
     *
     * @param maxSwipes
     *
     * @return true on scrolled else false
     *
     * @since API Level 16
     */
    boolean flingToBeginning(int maxSwipes);

    /**
     * Performs a fling gesture to reach the end of a scrollable layout element.
     * The end can be at the bottom-most edge in the case of vertical controls, or
     * the right-most edge for horizontal controls. Make sure to take into
     * account devices configured with right-to-left languages like Arabic and Hebrew.
     *
     * @param maxSwipes
     *
     * @return true on scrolled, else false
     *
     * @since API Level 16
     */
    boolean flingToEnd(int maxSwipes);

    /**
     * Searches for a child element in the present scrollable container.
     * The search first looks for a child element that matches the selector
     * you provided, then looks for the content-description in its children elements.
     * If both search conditions are fulfilled, the method returns a {
     *
     * @ link UiObject}
     * representing the element matching the selector (not the child element in its
     * subhierarchy containing the content-description). By default, this method performs a
     * scroll search.
     * See {@link #getChildByDescription(UiSelector, String, boolean)}
     *
     * @param childPattern {@link UiSelector} for a child in a scollable layout element
     * @param text         Content-description to find in the children of
     *                     the <code>childPattern</code> match
     *
     * @return {@link UiObject} representing the child element that matches the search conditions
     *
     * @since API Level 16
     */
    @Override
    boolean selectChildByDescription(UiSelector childPattern, String text);

    /**
     * Searches for a child element in the present scrollable container.
     * The search first looks for a child element that matches the selector
     * you provided, then looks for the content-description in its children elements.
     * If both search conditions are fulfilled, the method returns a {
     *
     * @ link UiObject}
     * representing the element matching the selector (not the child element in its
     * subhierarchy containing the content-description).
     *
     * @param childPattern      {@link UiSelector} for a child in a scollable layout element
     * @param text              Content-description to find in the children of
     *                          the <code>childPattern</code> match (may be a partial match)
     * @param allowScrollSearch set to true if scrolling is allowed
     *
     * @return {@link UiObject} representing the child element that matches the search conditions
     *
     * @since API Level 16
     */
    boolean selectChildByDescription(UiSelector childPattern, String text, boolean allowScrollSearch);

    /**
     * Searches for a child element in the present scrollable container that
     * matches the selector you provided. The search is performed without
     * scrolling and only on visible elements.
     *
     * @param childPattern {@link UiSelector} for a child in a scollable layout element
     * @param instance     int number representing the occurance of
     *                     a <code>childPattern</code> match
     *
     * @return {@link UiObject} representing the child element that matches the search conditions
     *
     * @since API Level 16
     */
    @Override
    boolean selectChildByInstance(UiSelector childPattern, int instance);

    /**
     * Searches for a child element in the present scrollable
     * container. The search first looks for a child element that matches the
     * selector you provided, then looks for the text in its children elements.
     * If both search conditions are fulfilled, the method returns a {
     *
     * @ link UiObject}
     * representing the element matching the selector (not the child element in its
     * subhierarchy containing the text). By default, this method performs a
     * scroll search.
     * See {@link #getChildByText(UiSelector, String, boolean)}
     *
     * @param childPattern {@link UiSelector} selector for a child in a scrollable layout element
     * @param text         String to find in the children of the <code>childPattern</code> match
     *
     * @return {@link UiObject} representing the child element that matches the search conditions
     *
     * @since API Level 16
     */
    @Override
    boolean selectChildByText(UiSelector childPattern, String text);

    /**
     * Searches for a child element in the present scrollable container. The
     * search first looks for a child element that matches the
     * selector you provided, then looks for the text in its children elements.
     * If both search conditions are fulfilled, the method returns a {
     *
     * @ link UiObject}
     * representing the element matching the selector (not the child element in its
     * subhierarchy containing the text).
     *
     * @param childPattern      {@link UiSelector} selector for a child in a scrollable layout element
     * @param text              String to find in the children of the <code>childPattern</code> match
     * @param allowScrollSearch set to true if scrolling is allowed
     *
     * @return {@link UiObject} representing the child element that matches the search conditions
     *
     * @since API Level 16
     */
    boolean selectChildByText(UiSelector childPattern, String text, boolean allowScrollSearch);

    /**
     * Gets the maximum number of scrolls allowed when performing a
     * scroll action in search of a child element.
     * See {@link #getChildByDescription(UiSelector, String)} and
     * {@link #getChildByText(UiSelector, String)}.
     *
     * @return max the number of search swipes to perform until giving up
     *
     * @since API Level 16
     */
    int getMaxSearchSwipes();

    /**
     * Returns the percentage of a widget's size that's considered as a no-touch
     * zone when swiping. The no-touch zone is set as a percentage of a widget's total
     * width or height, denoting a margin around the swipable area of the widget.
     * Swipes must start and end inside this margin. This is important when the
     * widget being swiped may not respond to the swipe if started at a point
     * too near to the edge. The default is 10% from either edge.
     *
     * @return a value between 0 and 1
     *
     * @since API Level 16
     */
    double getSwipeDeadZonePercentage();

    /**
     * Performs a backward scroll with the default number of scroll steps (55).
     * If the swipe direction is set to vertical,
     * then the swipes will be performed from top to bottom. If the swipe
     * direction is set to horizontal, then the swipes will be performed from
     * left to right. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @return true if scrolled, and false if can't scroll anymore
     *
     * @since API Level 16
     */
    boolean scrollBackward();

    /**
     * Performs a backward scroll. If the swipe direction is set to vertical,
     * then the swipes will be performed from top to bottom. If the swipe
     * direction is set to horizontal, then the swipes will be performed from
     * left to right. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @param steps number of steps. Use this to control the speed of the scroll action.
     *
     * @return true if scrolled, false if can't scroll anymore
     *
     * @since API Level 16
     */
    boolean scrollBackward(int steps);

    /**
     * Performs a forward scroll action on the scrollable layout element until
     * the content-description is found, or until swipe attempts have been exhausted.
     * See {@link #setMaxSearchSwipes(int)}
     *
     * @param text content-description to find within the contents of this scrollable layout element.
     *
     * @return true if item is found; else, false
     *
     * @since API Level 16
     */
    boolean scrollDescriptionIntoView(String text);

    /**
     * Performs a forward scroll with the default number of scroll steps (55).
     * If the swipe direction is set to vertical,
     * then the swipes will be performed from bottom to top. If the swipe
     * direction is set to horizontal, then the swipes will be performed from
     * right to left. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @return true if scrolled, false if can't scroll anymore
     *
     * @since API Level 16
     */
    boolean scrollForward();

    /**
     * Performs a forward scroll. If the swipe direction is set to vertical,
     * then the swipes will be performed from bottom to top. If the swipe
     * direction is set to horizontal, then the swipes will be performed from
     * right to left. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @param steps number of steps. Use this to control the speed of the scroll action
     *
     * @return true if scrolled, false if can't scroll anymore
     *
     * @since API Level 16
     */
    boolean scrollForward(int steps);

    /**
     * Perform a forward scroll action to move through the scrollable layout element until
     * a visible item that matches the {@link UiObject} is found.
     *
     * @param obj {@link UiObject}
     *
     * @return true if the item was found and now is in view else false
     *
     * @since API Level 16
     */
    boolean scrollIntoView(IUiObject obj);

    /**
     * Perform a scroll forward action to move through the scrollable layout
     * element until a visible item that matches the selector is found.
     *
     * See {@link #scrollDescriptionIntoView(String)} and {@link #scrollTextIntoView(String)}.
     *
     * @param selector {@link UiSelector} selector
     *
     * @return true if the item was found and now is in view; else, false
     *
     * @since API Level 16
     */
    boolean scrollIntoView(UiSelector selector);

    /**
     * Performs a forward scroll action on the scrollable layout element until
     * the text you provided is visible, or until swipe attempts have been exhausted.
     * See {@link #setMaxSearchSwipes(int)}
     *
     * @param text test to look for
     *
     * @return true if item is found; else, false
     *
     * @since API Level 16
     */
    boolean scrollTextIntoView(String text);

    /**
     * Scrolls to the beginning of a scrollable layout element. The beginning
     * can be at the top-most edge in the case of vertical controls, or the
     * left-most edge for horizontal controls. Make sure to take into account
     * devices configured with right-to-left languages like Arabic and Hebrew.
     *
     * @param maxSwipes
     * @param steps     use steps to control the speed, so that it may be a scroll, or fling
     *
     * @return true on scrolled else false
     *
     * @since API Level 16
     */
    boolean scrollToBeginning(int maxSwipes, int steps);

    /**
     * Scrolls to the beginning of a scrollable layout element. The beginning
     * can be at the top-most edge in the case of vertical controls, or the
     * left-most edge for horizontal controls. Make sure to take into account
     * devices configured with right-to-left languages like Arabic and Hebrew.
     *
     * @param maxSwipes
     *
     * @return true on scrolled else false
     *
     * @since API Level 16
     */
    boolean scrollToBeginning(int maxSwipes);

    /**
     * Scrolls to the end of a scrollable layout element. The end can be at the
     * bottom-most edge in the case of vertical controls, or the right-most edge for
     * horizontal controls. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @param steps use steps to control the speed, so that it may be a scroll, or fling
     *
     * @return true on scrolled else false
     *
     * @since API Level 16
     */
    /**
     * Scrolls to the end of a scrollable layout element. The end can be at the
     * bottom-most edge in the case of vertical controls, or the right-most edge for
     * horizontal controls. Make sure to take into account devices configured with
     * right-to-left languages like Arabic and Hebrew.
     *
     * @param maxSwipes
     *
     * @return true on scrolled, else false
     *
     * @since API Level 16
     */
    boolean scrollToEnd(int maxSwipes);

    boolean scrollToEnd(int maxSwipes, int steps);

    /**
     * Set the direction of swipes to be horizontal when performing scroll actions.
     *
     *
     * @since API Level 16
     */
    void setAsHorizontalList();

    /**
     * Set the direction of swipes to be vertical when performing scroll actions.
     *
     *
     * @since API Level 16
     */
    void setAsVerticalList();

    /**
     * Sets the maximum number of scrolls allowed when performing a
     * scroll action in search of a child element.
     * See {@link #getChildByDescription(UiSelector, String)} and
     * {@link #getChildByText(UiSelector, String)}.
     *
     * @param swipes the number of search swipes to perform until giving up
     *
     * @since API Level 16
     */
    void setMaxSearchSwipes(int swipes);

    /**
     * Sets the percentage of a widget's size that's considered as no-touch
     * zone when swiping.
     * The no-touch zone is set as percentage of a widget's total width or height,
     * denoting a margin around the swipable area of the widget. Swipes must
     * always start and end inside this margin. This is important when the
     * widget being swiped may not respond to the swipe if started at a point
     * too near to the edge. The default is 10% from either edge.
     *
     * @param swipeDeadZonePercentage is a value between 0 and 1
     *
     * @since API Level 16
     */
    void setSwipeDeadZonePercentage(double swipeDeadZonePercentage);
}
