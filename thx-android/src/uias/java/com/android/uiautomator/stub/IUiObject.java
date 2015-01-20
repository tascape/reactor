package com.android.uiautomator.stub;

import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import java.io.Serializable;

/**
 *
 * @author wlinsong
 */
public interface IUiObject extends Serializable {

    /**
     * Clears the existing text contents in an editable field.
     *
     * The {@link UiSelector} of this object must reference a UI element that is editable.
     *
     * When you call this method, the method first sets focus at the start edge of the field.
     * The method then simulates a long-press to select the existing text, and deletes the
     * selected text.
     *
     * If a "Select-All" option is displayed, the method will automatically attempt to use it
     * to ensure full text selection.
     *
     * Note that it is possible that not all the text in the field is selected; for example,
     * if the text contains separators such as spaces, slashes, at symbol etc.
     * Also, not all editable fields support the long-press functionality.
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    void clearTextField() throws UiObjectNotFoundException;

    /**
     * Performs a click at the center of the visible bounds of the UI element represented
     * by this UiObject.
     *
     * @return true id successful else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean click() throws UiObjectNotFoundException;

    /**
     * Waits for window transitions that would typically take longer than the
     * usual default timeouts.
     * See {@link #clickAndWaitForNewWindow(long)}
     *
     * @return true if the event was triggered, else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean clickAndWaitForNewWindow() throws UiObjectNotFoundException;

    /**
     * Performs a click at the center of the visible bounds of the UI element represented
     * by this UiObject and waits for window transitions.
     *
     * This method differ from {@link UiObject#click()} only in that this method waits for a
     * a new window transition as a result of the click. Some examples of a window transition:
     * <li>launching a new activity</li>
     * <li>bringing up a pop-up menu</li>
     * <li>bringing up a dialog</li>
     *
     * @param timeout timeout before giving up on waiting for a new window
     *
     * @return true if the event was triggered, else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean clickAndWaitForNewWindow(long timeout) throws UiObjectNotFoundException;

    /**
     * Clicks the bottom and right corner of the UI element
     *
     * @return true on success
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean clickBottomRight() throws UiObjectNotFoundException;

    /**
     * Clicks the top and left corner of the UI element
     *
     * @return true on success
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean clickTopLeft() throws UiObjectNotFoundException;

    /**
     * Drags this object to a destination UiObject.
     * The number of steps specified in your input parameter can influence the
     * drag speed, and varying speeds may impact the results. Consider
     * evaluating different speeds when using this method in your tests.
     *
     * @param destObj the destination UiObject.
     * @param steps   usually 40 steps. You can increase or decrease the steps to change the speed.
     *
     * @return true if successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 18
     */
    boolean dragTo(UiObject destObj, int steps) throws UiObjectNotFoundException;

    /**
     * Drags this object to arbitrary coordinates.
     * The number of steps specified in your input parameter can influence the
     * drag speed, and varying speeds may impact the results. Consider
     * evaluating different speeds when using this method in your tests.
     *
     * @param destX the X-axis coordinate.
     * @param destY the Y-axis coordinate.
     * @param steps usually 40 steps. You can increase or decrease the steps to change the speed.
     *
     * @return true if successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 18
     */
    boolean dragTo(int destX, int destY, int steps) throws UiObjectNotFoundException;

    /**
     * Check if view exists.
     *
     * This methods performs a {@link #waitForExists(long)} with zero timeout. This
     * basically returns immediately whether the view represented by this UiObject
     * exists or not. If you need to wait longer for this view, then see
     * {@link #waitForExists(long)}.
     *
     * @return true if the view represented by this UiObject does exist
     *
     * @since API Level 16
     */
    boolean exists();

    /**
     * Returns the view's <code>bounds</code> property. See {@link #getVisibleBounds()}
     *
     * @return Rect
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    Rect getBounds() throws UiObjectNotFoundException;

    /**
     * Creates a new UiObject for a child view that is under the present UiObject.
     *
     * @param selector for child view to match
     *
     * @return a new UiObject representing the child view
     *
     * @since API Level 16
     */
    UiObject getChild(UiSelector selector) throws UiObjectNotFoundException;

    /**
     * Counts the child views immediately under the present UiObject.
     *
     * @return the count of child views.
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    int getChildCount() throws UiObjectNotFoundException;

    /**
     * Retrieves the <code>className</code> property of the UI element.
     *
     * @return class name of the current node represented by this UiObject
     *
     * @throws UiObjectNotFoundException if no match was found
     * @since API Level 18
     */
    String getClassName() throws UiObjectNotFoundException;

    /**
     * Reads the <code>content_desc</code> property of the UI element
     *
     * @return value of node attribute "content_desc"
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    String getContentDescription() throws UiObjectNotFoundException;

    /**
     * Creates a new UiObject for a sibling view or a child of the sibling view,
     * relative to the present UiObject.
     *
     * @param selector for a sibling view or children of the sibling view
     *
     * @return a new UiObject representing the matched view
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    UiObject getFromParent(UiSelector selector) throws UiObjectNotFoundException;

    /**
     * Reads the view's <code>package</code> property
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    String getPackageName() throws UiObjectNotFoundException;

    /**
     * Debugging helper. A test can dump the properties of a selector as a string
     * to its logs if needed. <code>getSelector().toString();</code>
     *
     * @return {@link UiSelector}
     *
     * @since API Level 16
     */
    UiSelector getSelector();

    /**
     * Reads the <code>text</code> property of the UI element
     *
     * @return text value of the current node represented by this UiObject
     *
     * @throws UiObjectNotFoundException if no match could be found
     * @since API Level 16
     */
    String getText() throws UiObjectNotFoundException;

    /**
     * Returns the visible bounds of the view.
     *
     * If a portion of the view is visible, only the bounds of the visible portion are
     * reported.
     *
     * @return Rect
     *
     * @throws UiObjectNotFoundException
     * @see {@link #getBounds()}
     * @since API Level 17
     */
    Rect getVisibleBounds() throws UiObjectNotFoundException;

    /**
     * Checks if the UI element's <code>checkable</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isCheckable() throws UiObjectNotFoundException;

    /**
     * Check if the UI element's <code>checked</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws com.android.uiautomator.core.UiObjectNotFoundException
     *
     * @since API Level 16
     */
    boolean isChecked() throws UiObjectNotFoundException;

    /**
     * Checks if the UI element's <code>clickable</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isClickable() throws UiObjectNotFoundException;

    /**
     * Checks if the UI element's <code>enabled</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isEnabled() throws UiObjectNotFoundException;

    /**
     * Check if the UI element's <code>focusable</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isFocusable() throws UiObjectNotFoundException;

    /**
     * Check if the UI element's <code>focused</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isFocused() throws UiObjectNotFoundException;

    /**
     * Check if the view's <code>long-clickable</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isLongClickable() throws UiObjectNotFoundException;

    /**
     * Check if the view's <code>scrollable</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isScrollable() throws UiObjectNotFoundException;

    /**
     * Checks if the UI element's <code>selected</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean isSelected() throws UiObjectNotFoundException;

    /**
     * Long clicks the center of the visible bounds of the UI element
     *
     * @return true if operation was successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean longClick() throws UiObjectNotFoundException;

    /**
     * Long clicks bottom and right corner of the UI element
     *
     * @return true if operation was successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean longClickBottomRight() throws UiObjectNotFoundException;

    /**
     * Long clicks on the top and left corner of the UI element
     *
     * @return true if operation was successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean longClickTopLeft() throws UiObjectNotFoundException;

    /**
     * Performs a multi-touch gesture. You must specify touch coordinates for
     * at least 2 pointers. Each pointer must have all of its touch steps
     * defined in an array of {@link PointerCoords}. You can use this method to
     * specify complex gestures, like circles and irregular shapes, where each
     * pointer may take a different path.
     *
     * To create a single point on a pointer's touch path:
     * <code>
     *       PointerCoords p = new PointerCoords();
     *       p.x = stepX;
     *       p.y = stepY;
     *       p.pressure = 1;
     *       p.size = 1;
     * </code>
     *
     * @param touches represents the pointers' paths. Each {@link PointerCoords}
     *                array represents a different pointer. Each {@link PointerCoords} in an
     *                array element represents a touch point on a pointer's path.
     *
     * @return <code>true</code> if all touch events for this gesture are injected successfully,
     *         <code>false</code> otherwise
     *
     * @since API Level 18
     */
    boolean performMultiPointerGesture(MotionEvent.PointerCoords[]... touches);

    /**
     * Generates a two-pointer gesture with arbitrary starting and ending points.
     *
     * @param startPoint1 start point of pointer 1
     * @param startPoint2 start point of pointer 2
     * @param endPoint1   end point of pointer 1
     * @param endPoint2   end point of pointer 2
     * @param steps       the number of steps for the gesture. Steps are injected
     *                    about 5 milliseconds apart, so 100 steps may take around 0.5 seconds to complete.
     *
     * @return <code>true</code> if all touch events for this gesture are injected successfully,
     *         <code>false</code> otherwise
     *
     * @since API Level 18
     */
    boolean performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1, Point endPoint2, int steps);

    /**
     * Performs a two-pointer gesture, where each pointer moves diagonally
     * toward the other, from the edges to the center of this UiObject .
     *
     * @param percent percentage of the object's diagonal length for the pinch gesture
     * @param steps   the number of steps for the gesture. Steps are injected
     *                about 5 milliseconds apart, so 100 steps may take around 0.5 seconds to complete.
     *
     * @return <code>true</code> if all touch events for this gesture are injected successfully,
     *         <code>false</code> otherwise
     *
     * @throws UiObjectNotFoundException
     * @since API Level 18
     */
    boolean pinchIn(int percent, int steps) throws UiObjectNotFoundException;

    /**
     * Performs a two-pointer gesture, where each pointer moves diagonally
     * opposite across the other, from the center out towards the edges of the
     * this UiObject.
     *
     * @param percent percentage of the object's diagonal length for the pinch gesture
     * @param steps   the number of steps for the gesture. Steps are injected
     *                about 5 milliseconds apart, so 100 steps may take around 0.5 seconds to complete.
     *
     * @return <code>true</code> if all touch events for this gesture are injected successfully,
     *         <code>false</code> otherwise
     *
     * @throws UiObjectNotFoundException
     * @since API Level 18
     */
    boolean pinchOut(int percent, int steps) throws UiObjectNotFoundException;

    /**
     * Sets the text in an editable field, after clearing the field's content.
     *
     * The {@link UiSelector} selector of this object must reference a UI element that is editable.
     *
     * When you call this method, the method first simulates a {@link #click()} on
     * editable field to set focus. The method then clears the field's contents
     * and injects your specified text into the field.
     *
     * If you want to capture the original contents of the field, call {@link #getText()} first.
     * You can then modify the text and use this method to update the field.
     *
     * @param text string to set
     *
     * @return true if operation is successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean setText(String text) throws UiObjectNotFoundException;

    /**
     * Performs the swipe down action on the UiObject.
     * The swipe gesture can be performed over any surface. The targeted
     * UI element does not need to be scrollable.
     * See also:
     * <ul>
     * <li>{@link UiScrollable#scrollToBeginning(int)}</li>
     * <li>{@link UiScrollable#scrollToEnd(int)}</li>
     * <li>{@link UiScrollable#scrollBackward()}</li>
     * <li>{@link UiScrollable#scrollForward()}</li>
     * </ul>
     *
     * @param steps indicates the number of injected move steps into the system. Steps are
     *              injected about 5ms apart. So a 100 steps may take about 1/2 second to complete.
     *
     * @return true if successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean swipeDown(int steps) throws UiObjectNotFoundException;

    /**
     * Performs the swipe left action on the UiObject.
     * The swipe gesture can be performed over any surface. The targeted
     * UI element does not need to be scrollable.
     * See also:
     * <ul>
     * <li>{@link UiScrollable#scrollToBeginning(int)}</li>
     * <li>{@link UiScrollable#scrollToEnd(int)}</li>
     * <li>{@link UiScrollable#scrollBackward()}</li>
     * <li>{@link UiScrollable#scrollForward()}</li>
     * </ul>
     *
     * @param steps indicates the number of injected move steps into the system. Steps are
     *              injected about 5ms apart. So a 100 steps may take about 1/2 second to complete.
     *
     * @return true if successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean swipeLeft(int steps) throws UiObjectNotFoundException;

    /**
     * Performs the swipe right action on the UiObject.
     * The swipe gesture can be performed over any surface. The targeted
     * UI element does not need to be scrollable.
     * See also:
     * <ul>
     * <li>{@link UiScrollable#scrollToBeginning(int)}</li>
     * <li>{@link UiScrollable#scrollToEnd(int)}</li>
     * <li>{@link UiScrollable#scrollBackward()}</li>
     * <li>{@link UiScrollable#scrollForward()}</li>
     * </ul>
     *
     * @param steps indicates the number of injected move steps into the system. Steps are
     *              injected about 5ms apart. So a 100 steps may take about 1/2 second to complete.
     *
     * @return true if successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean swipeRight(int steps) throws UiObjectNotFoundException;

    /**
     * Performs the swipe up action on the UiObject.
     * See also:
     * <ul>
     * <li>{@link UiScrollable#scrollToBeginning(int)}</li>
     * <li>{@link UiScrollable#scrollToEnd(int)}</li>
     * <li>{@link UiScrollable#scrollBackward()}</li>
     * <li>{@link UiScrollable#scrollForward()}</li>
     * </ul>
     *
     * @param steps indicates the number of injected move steps into the system. Steps are
     *              injected about 5ms apart. So a 100 steps may take about 1/2 second to complete.
     *
     * @return true of successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    boolean swipeUp(int steps) throws UiObjectNotFoundException;

    /**
     * Waits a specified length of time for a view to become visible.
     *
     * This method waits until the view becomes visible on the display, or
     * until the timeout has elapsed. You can use this method in situations where
     * the content that you want to select is not immediately displayed.
     *
     * @param timeout the amount of time to wait (in milliseconds)
     *
     * @return true if the view is displayed, else false if timeout elapsed while waiting
     *
     * @since API Level 16
     */
    boolean waitForExists(long timeout);

    /**
     * Waits a specified length of time for a view to become undetectable.
     *
     * This method waits until a view is no longer matchable, or until the
     * timeout has elapsed.
     *
     * A view becomes undetectable when the {@link UiSelector} of the object is
     * unable to find a match because the element has either changed its state or is no
     * longer displayed.
     *
     * You can use this method when attempting to wait for some long operation
     * to compete, such as downloading a large file or connecting to a remote server.
     *
     * @param timeout time to wait (in milliseconds)
     *
     * @return true if the element is gone before timeout elapsed, else false if timeout elapsed
     *         but a matching element is still found.
     *
     * @since API Level 16
     */
    boolean waitUntilGone(long timeout);

}
