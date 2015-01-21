package com.android.uiautomator.stub;

import android.view.MotionEvent;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiScrollable;
import java.util.ArrayList;
import java.util.List;

/*
 * @author linsong wang
 */
public class UiObjectStub implements IUiObject {

    private static final long serialVersionUID = -87236495L;

    private UiObject uiObject;

    @Override
    public void useSelector(UiSelector selector) {
        com.android.uiautomator.core.UiSelector uiSelector = UiObjectStub.convert(selector);
        this.uiObject = new UiObject(uiSelector);
    }

    /**
     * Creates a new UiObject for a child view that is under the present UiObject.
     *
     * @param selector for child view to match
     *
     * @return a new UiObject representing the child view
     *
     * @throws com.android.uiautomator.stub.UiObjectNotFoundException
     *
     * @since API Level 16
     */
    public UiObject getChild(com.android.uiautomator.core.UiSelector selector) throws UiObjectNotFoundException {
        try {
            return this.uiObject.getChild(selector);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    public UiObject getFromParent(com.android.uiautomator.core.UiSelector selector) throws UiObjectNotFoundException {
        try {
            return this.uiObject.getFromParent(selector);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Counts the child views immediately under the present UiObject.
     *
     * @return the count of child views.
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public int getChildCount() throws UiObjectNotFoundException {
        try {
            return this.uiObject.getChildCount();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    public boolean dragTo(UiObject destObj, int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.dragTo(destObj, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean dragTo(int destX, int destY, int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.dragTo(destX, destY, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean swipeUp(int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.swipeUp(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean swipeDown(int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.swipeDown(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean swipeLeft(int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.swipeLeft(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean swipeRight(int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.swipeRight(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Performs a click at the center of the visible bounds of the UI element represented
     * by this UiObject.
     *
     * @return true id successful else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean click() throws UiObjectNotFoundException {
        try {
            return this.uiObject.click();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean clickAndWaitForNewWindow() throws UiObjectNotFoundException {
        try {
            return this.uiObject.clickAndWaitForNewWindow();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean clickAndWaitForNewWindow(long timeout) throws UiObjectNotFoundException {
        try {
            return this.uiObject.clickAndWaitForNewWindow(timeout);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Clicks the top and left corner of the UI element
     *
     * @return true on success
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean clickTopLeft() throws UiObjectNotFoundException {
        try {
            return this.uiObject.clickTopLeft();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Long clicks bottom and right corner of the UI element
     *
     * @return true if operation was successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean longClickBottomRight() throws UiObjectNotFoundException {
        try {
            return this.uiObject.longClickBottomRight();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Clicks the bottom and right corner of the UI element
     *
     * @return true on success
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean clickBottomRight() throws UiObjectNotFoundException {
        try {
            return this.uiObject.clickBottomRight();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Long clicks the center of the visible bounds of the UI element
     *
     * @return true if operation was successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean longClick() throws UiObjectNotFoundException {
        try {
            return this.uiObject.longClick();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Long clicks on the top and left corner of the UI element
     *
     * @return true if operation was successful
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean longClickTopLeft() throws UiObjectNotFoundException {
        try {
            return this.uiObject.longClickTopLeft();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Reads the <code>text</code> property of the UI element
     *
     * @return text value of the current node represented by this UiObject
     *
     * @throws UiObjectNotFoundException if no match could be found
     * @since API Level 16
     */
    @Override
    public String getText() throws UiObjectNotFoundException {
        try {
            return this.uiObject.getText();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Retrieves the <code>className</code> property of the UI element.
     *
     * @return class name of the current node represented by this UiObject
     *
     * @throws UiObjectNotFoundException if no match was found
     * @since API Level 18
     */
    @Override
    public String getClassName() throws UiObjectNotFoundException {
        try {
            return this.uiObject.getClassName();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Reads the <code>content_desc</code> property of the UI element
     *
     * @return value of node attribute "content_desc"
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public String getContentDescription() throws UiObjectNotFoundException {
        try {
            return this.uiObject.getContentDescription();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean setText(String text) throws UiObjectNotFoundException {
        try {
            return this.uiObject.setText(text);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public void clearTextField() throws UiObjectNotFoundException {
        try {
            this.uiObject.clearTextField();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Check if the UI element's <code>checked</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws com.android.uiautomator.stub.UiObjectNotFoundException
     *
     * @since API Level 16
     */
    @Override
    public boolean isChecked() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isChecked();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Checks if the UI element's <code>selected</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isSelected() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isSelected();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Checks if the UI element's <code>checkable</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isCheckable() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isCheckable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Checks if the UI element's <code>enabled</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isEnabled() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isEnabled();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Checks if the UI element's <code>clickable</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isClickable() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isClickable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Check if the UI element's <code>focused</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isFocused() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isFocused();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Check if the UI element's <code>focusable</code> property is currently true.
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isFocusable() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isFocusable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Check if the view's <code>scrollable</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isScrollable() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isScrollable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Check if the view's <code>long-clickable</code> property is currently true
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public boolean isLongClickable() throws UiObjectNotFoundException {
        try {
            return this.uiObject.isLongClickable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Reads the view's <code>package</code> property
     *
     * @return true if it is else false
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public String getPackageName() throws UiObjectNotFoundException {
        try {
            return this.uiObject.getPackageName();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public Rect getVisibleBounds() throws UiObjectNotFoundException {
        try {
            android.graphics.Rect rect = this.uiObject.getVisibleBounds();
            return new Rect(rect.left, rect.top, rect.right, rect.bottom);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

    /**
     * Returns the view's <code>bounds</code> property. See {@link #getVisibleBounds()}
     *
     * @return Rect
     *
     * @throws UiObjectNotFoundException
     * @since API Level 16
     */
    @Override
    public Rect getBounds() throws UiObjectNotFoundException {
        try {
            android.graphics.Rect rect = this.uiObject.getBounds();
            return new Rect(rect.left, rect.top, rect.right, rect.bottom);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean waitForExists(long timeout) {
        return this.uiObject.waitForExists(timeout);
    }

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
    @Override
    public boolean waitUntilGone(long timeout) {
        return this.uiObject.waitUntilGone(timeout);
    }

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
    @Override
    public boolean exists() {
        return this.uiObject.exists();
    }

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
    @Override
    public boolean pinchOut(int percent, int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.pinchOut(percent, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean pinchIn(int percent, int steps) throws UiObjectNotFoundException {
        try {
            return this.uiObject.pinchIn(percent, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            throw new UiObjectNotFoundException(ex);
        }
    }

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
    @Override
    public boolean performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1,
            Point endPoint2, int steps) {
        android.graphics.Point sp1 = new android.graphics.Point(startPoint1.x, startPoint1.y);
        android.graphics.Point sp2 = new android.graphics.Point(startPoint2.x, startPoint2.y);

        android.graphics.Point ep1 = new android.graphics.Point(endPoint1.x, endPoint1.y);
        android.graphics.Point ep2 = new android.graphics.Point(endPoint2.x, endPoint2.y);

        return this.uiObject.performTwoPointerGesture(sp1, sp2, ep1, ep2, steps);
    }

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
    @Override
    public boolean performMultiPointerGesture(PointerCoords[]... touches) {
        List<MotionEvent.PointerCoords[]> ts = new ArrayList<>();
        for (int i = 0; i < touches.length; i++) {
            PointerCoords[] pcss = touches[i];
            MotionEvent.PointerCoords[] mepcss = new MotionEvent.PointerCoords[pcss.length];
            for (PointerCoords pcs : pcss) {
                mepcss[i] = UiObjectStub.convert(pcs);
            }
            ts.add(mepcss);
        }
        throw new UnsupportedOperationException();
//        return this.uiObject.performMultiPointerGesture();
    }

    private static MotionEvent.PointerCoords convert(PointerCoords pcs) {
        MotionEvent.PointerCoords mepcs = new MotionEvent.PointerCoords();
        mepcs.x = pcs.x;
        mepcs.y = pcs.y;
        mepcs.pressure = pcs.pressure;
        mepcs.size = pcs.size;
        mepcs.orientation = pcs.orientation;
        mepcs.toolMajor = pcs.toolMajor;
        mepcs.toolMinor = pcs.toolMinor;
        mepcs.touchMajor = pcs.touchMajor;
        mepcs.touchMinor = pcs.touchMinor;
        return mepcs;
    }

    private static com.android.uiautomator.core.UiSelector convert(UiSelector selector) {
        com.android.uiautomator.core.UiSelector s = new com.android.uiautomator.core.UiSelector();
        if (selector.get(UiSelector.SELECTOR_CHECKABLE) != null) {
            s = s.checkable((boolean) selector.get(UiSelector.SELECTOR_CHECKABLE));
        }
        if (selector.get(UiSelector.SELECTOR_CHECKED) != null) {
            s = s.checked((boolean) selector.get(UiSelector.SELECTOR_CHECKED));
        }
        if (selector.get(UiSelector.SELECTOR_CLASS) != null) {
            s = s.className((String) selector.get(UiSelector.SELECTOR_CLASS));
        }
        if (selector.get(UiSelector.SELECTOR_CLASS_REGEX) != null) {
            s = s.classNameMatches((String) selector.get(UiSelector.SELECTOR_CLASS_REGEX));
        }

        if (selector.get(UiSelector.SELECTOR_CLICKABLE) != null) {
            s = s.clickable((boolean) selector.get(UiSelector.SELECTOR_CLICKABLE));
        }
        if (selector.get(UiSelector.SELECTOR_DESCRIPTION) != null) {
            s = s.description((String) selector.get(UiSelector.SELECTOR_DESCRIPTION));
        }
        if (selector.get(UiSelector.SELECTOR_CONTAINS_TEXT) != null) {
            s = s.descriptionContains((String) selector.get(UiSelector.SELECTOR_CONTAINS_TEXT));
        }
        if (selector.get(UiSelector.SELECTOR_DESCRIPTION_REGEX) != null) {
            s = s.descriptionMatches((String) selector.get(UiSelector.SELECTOR_DESCRIPTION_REGEX));
        }
        if (selector.get(UiSelector.SELECTOR_START_TEXT) != null) {
            s = s.descriptionStartsWith((String) selector.get(UiSelector.SELECTOR_START_TEXT));
        }
        if (selector.get(UiSelector.SELECTOR_ENABLED) != null) {
            s = s.enabled((boolean) selector.get(UiSelector.SELECTOR_ENABLED));
        }
        if (selector.get(UiSelector.SELECTOR_FOCUSABLE) != null) {
            s = s.focusable((boolean) selector.get(UiSelector.SELECTOR_FOCUSABLE));
        }
        if (selector.get(UiSelector.SELECTOR_FOCUSED) != null) {
            s = s.focused((boolean) selector.get(UiSelector.SELECTOR_FOCUSED));
        }
        if (selector.get(UiSelector.SELECTOR_INDEX) != null) {
            s = s.index((int) selector.get(UiSelector.SELECTOR_INDEX));
        }
        if (selector.get(UiSelector.SELECTOR_INSTANCE) != null) {
            s = s.instance((int) selector.get(UiSelector.SELECTOR_INSTANCE));
        }
        if (selector.get(UiSelector.SELECTOR_LONG_CLICKABLE) != null) {
            s = s.longClickable((boolean) selector.get(UiSelector.SELECTOR_LONG_CLICKABLE));
        }
        if (selector.get(UiSelector.SELECTOR_PACKAGE_NAME) != null) {
            s = s.packageName((String) selector.get(UiSelector.SELECTOR_PACKAGE_NAME));
        }
        if (selector.get(UiSelector.SELECTOR_PACKAGE_NAME_REGEX) != null) {
            s = s.packageNameMatches((String) selector.get(UiSelector.SELECTOR_PACKAGE_NAME_REGEX));
        }
        if (selector.get(UiSelector.SELECTOR_RESOURCE_ID) != null) {
            s = s.resourceId((String) selector.get(UiSelector.SELECTOR_RESOURCE_ID));
        }
        if (selector.get(UiSelector.SELECTOR_RESOURCE_ID_REGEX) != null) {
            s = s.resourceIdMatches((String) selector.get(UiSelector.SELECTOR_RESOURCE_ID_REGEX));
        }
        if (selector.get(UiSelector.SELECTOR_SCROLLABLE) != null) {
            s = s.scrollable((boolean) selector.get(UiSelector.SELECTOR_SCROLLABLE));
        }
        if (selector.get(UiSelector.SELECTOR_SELECTED) != null) {
            s = s.selected((boolean) selector.get(UiSelector.SELECTOR_SELECTED));
        }
        if (selector.get(UiSelector.SELECTOR_TEXT) != null) {
            s = s.text((String) selector.get(UiSelector.SELECTOR_TEXT));
        }
        if (selector.get(UiSelector.SELECTOR_CONTAINS_TEXT) != null) {
            s = s.textContains((String) selector.get(UiSelector.SELECTOR_CONTAINS_TEXT));
        }
        if (selector.get(UiSelector.SELECTOR_TEXT_REGEX) != null) {
            s = s.textMatches((String) selector.get(UiSelector.SELECTOR_TEXT_REGEX));
        }
        if (selector.get(UiSelector.SELECTOR_START_TEXT) != null) {
            s = s.textStartsWith((String) selector.get(UiSelector.SELECTOR_START_TEXT));
        }
//        System.out.println(s);
        return s;
    }
}
