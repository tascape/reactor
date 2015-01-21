package com.android.uiautomator.stub;

import java.io.Serializable;

/**
 * Transfer object for pointer coordinates.
 *
 * Objects of this type can be used to specify the pointer coordinates when
 * creating new {@link MotionEvent} objects and to query pointer coordinates
 * in bulk.
 *
 * Refer to {@link InputDevice} for information about how different kinds of
 * input devices and sources represent pointer coordinates.
 *
 * @author linsong wang
 */
public class PointerCoords implements Serializable {
    /**
     * Axis constant: X axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen, reports the absolute X screen position of the center of
     * the touch contact area. The units are display pixels.
     * <li>For a touch pad, reports the absolute X surface position of the center of the touch
     * contact area. The units are device-dependent; use {@link InputDevice#getMotionRange(int)}
     * to query the effective range of values.
     * <li>For a mouse, reports the absolute X screen position of the mouse pointer.
     * The units are display pixels.
     * <li>For a trackball, reports the relative horizontal displacement of the trackball.
     * The value is normalized to a range from -1.0 (left) to 1.0 (right).
     * <li>For a joystick, reports the absolute X position of the joystick.
     * The value is normalized to a range from -1.0 (left) to 1.0 (right).
     * </ul>
     * </p>
     *
     * @see #getX(int)
     * @see #getHistoricalX(int, int)
     * @see MotionEvent.PointerCoords#x
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_X = 0;

    /**
     * Axis constant: Y axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen, reports the absolute Y screen position of the center of
     * the touch contact area. The units are display pixels.
     * <li>For a touch pad, reports the absolute Y surface position of the center of the touch
     * contact area. The units are device-dependent; use {@link InputDevice#getMotionRange(int)}
     * to query the effective range of values.
     * <li>For a mouse, reports the absolute Y screen position of the mouse pointer.
     * The units are display pixels.
     * <li>For a trackball, reports the relative vertical displacement of the trackball.
     * The value is normalized to a range from -1.0 (up) to 1.0 (down).
     * <li>For a joystick, reports the absolute Y position of the joystick.
     * The value is normalized to a range from -1.0 (up or far) to 1.0 (down or near).
     * </ul>
     * </p>
     *
     * @see #getY(int)
     * @see #getHistoricalY(int, int)
     * @see MotionEvent.PointerCoords#y
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_Y = 1;

    /**
     * Axis constant: Pressure axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen or touch pad, reports the approximate pressure applied to the surface
     * by a finger or other tool. The value is normalized to a range from
     * 0 (no pressure at all) to 1 (normal pressure), although values higher than 1
     * may be generated depending on the calibration of the input device.
     * <li>For a trackball, the value is set to 1 if the trackball button is pressed
     * or 0 otherwise.
     * <li>For a mouse, the value is set to 1 if the primary mouse button is pressed
     * or 0 otherwise.
     * </ul>
     * </p>
     *
     * @see #getPressure(int)
     * @see #getHistoricalPressure(int, int)
     * @see MotionEvent.PointerCoords#pressure
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_PRESSURE = 2;

    /**
     * Axis constant: Size axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen or touch pad, reports the approximate size of the contact area in
     * relation to the maximum detectable size for the device. The value is normalized
     * to a range from 0 (smallest detectable size) to 1 (largest detectable size),
     * although it is not a linear scale. This value is of limited use.
     * To obtain calibrated size information, use
     * {@link #AXIS_TOUCH_MAJOR} or {@link #AXIS_TOOL_MAJOR}.
     * </ul>
     * </p>
     *
     * @see #getSize(int)
     * @see #getHistoricalSize(int, int)
     * @see MotionEvent.PointerCoords#size
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_SIZE = 3;

    /**
     * Axis constant: TouchMajor axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen, reports the length of the major axis of an ellipse that
     * represents the touch area at the point of contact.
     * The units are display pixels.
     * <li>For a touch pad, reports the length of the major axis of an ellipse that
     * represents the touch area at the point of contact.
     * The units are device-dependent; use {@link InputDevice#getMotionRange(int)}
     * to query the effective range of values.
     * </ul>
     * </p>
     *
     * @see #getTouchMajor(int)
     * @see #getHistoricalTouchMajor(int, int)
     * @see MotionEvent.PointerCoords#touchMajor
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_TOUCH_MAJOR = 4;

    /**
     * Axis constant: TouchMinor axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen, reports the length of the minor axis of an ellipse that
     * represents the touch area at the point of contact.
     * The units are display pixels.
     * <li>For a touch pad, reports the length of the minor axis of an ellipse that
     * represents the touch area at the point of contact.
     * The units are device-dependent; use {@link InputDevice#getMotionRange(int)}
     * to query the effective range of values.
     * </ul>
     * </p><p>
     * When the touch is circular, the major and minor axis lengths will be equal to one another.
     * </p>
     *
     * @see #getTouchMinor(int)
     * @see #getHistoricalTouchMinor(int, int)
     * @see MotionEvent.PointerCoords#touchMinor
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_TOUCH_MINOR = 5;

    /**
     * Axis constant: ToolMajor axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen, reports the length of the major axis of an ellipse that
     * represents the size of the approaching finger or tool used to make contact.
     * <li>For a touch pad, reports the length of the major axis of an ellipse that
     * represents the size of the approaching finger or tool used to make contact.
     * The units are device-dependent; use {@link InputDevice#getMotionRange(int)}
     * to query the effective range of values.
     * </ul>
     * </p><p>
     * When the touch is circular, the major and minor axis lengths will be equal to one another.
     * </p><p>
     * The tool size may be larger than the touch size since the tool may not be fully
     * in contact with the touch sensor.
     * </p>
     *
     * @see #getToolMajor(int)
     * @see #getHistoricalToolMajor(int, int)
     * @see MotionEvent.PointerCoords#toolMajor
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_TOOL_MAJOR = 6;

    /**
     * Axis constant: ToolMinor axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen, reports the length of the minor axis of an ellipse that
     * represents the size of the approaching finger or tool used to make contact.
     * <li>For a touch pad, reports the length of the minor axis of an ellipse that
     * represents the size of the approaching finger or tool used to make contact.
     * The units are device-dependent; use {@link InputDevice#getMotionRange(int)}
     * to query the effective range of values.
     * </ul>
     * </p><p>
     * When the touch is circular, the major and minor axis lengths will be equal to one another.
     * </p><p>
     * The tool size may be larger than the touch size since the tool may not be fully
     * in contact with the touch sensor.
     * </p>
     *
     * @see #getToolMinor(int)
     * @see #getHistoricalToolMinor(int, int)
     * @see MotionEvent.PointerCoords#toolMinor
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_TOOL_MINOR = 7;

    /**
     * Axis constant: Orientation axis of a motion event.
     * <p>
     * <ul>
     * <li>For a touch screen or touch pad, reports the orientation of the finger
     * or tool in radians relative to the vertical plane of the device.
     * An angle of 0 radians indicates that the major axis of contact is oriented
     * upwards, is perfectly circular or is of unknown orientation. A positive angle
     * indicates that the major axis of contact is oriented to the right. A negative angle
     * indicates that the major axis of contact is oriented to the left.
     * The full range is from -PI/2 radians (finger pointing fully left) to PI/2 radians
     * (finger pointing fully right).
     * <li>For a stylus, the orientation indicates the direction in which the stylus
     * is pointing in relation to the vertical axis of the current orientation of the screen.
     * The range is from -PI radians to PI radians, where 0 is pointing up,
     * -PI/2 radians is pointing left, -PI or PI radians is pointing down, and PI/2 radians
     * is pointing right. See also {@link #AXIS_TILT}.
     * </ul>
     * </p>
     *
     * @see #getOrientation(int)
     * @see #getHistoricalOrientation(int, int)
     * @see MotionEvent.PointerCoords#orientation
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_ORIENTATION = 8;

    /**
     * Axis constant: Vertical Scroll axis of a motion event.
     * <p>
     * <ul>
     * <li>For a mouse, reports the relative movement of the vertical scroll wheel.
     * The value is normalized to a range from -1.0 (down) to 1.0 (up).
     * </ul>
     * </p><p>
     * This axis should be used to scroll views vertically.
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_VSCROLL = 9;

    /**
     * Axis constant: Horizontal Scroll axis of a motion event.
     * <p>
     * <ul>
     * <li>For a mouse, reports the relative movement of the horizontal scroll wheel.
     * The value is normalized to a range from -1.0 (left) to 1.0 (right).
     * </ul>
     * </p><p>
     * This axis should be used to scroll views horizontally.
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_HSCROLL = 10;

    /**
     * Axis constant: Z axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute Z position of the joystick.
     * The value is normalized to a range from -1.0 (high) to 1.0 (low).
     * <em>On game pads with two analog joysticks, this axis is often reinterpreted
     * to report the absolute X position of the second joystick instead.</em>
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_Z = 11;

    /**
     * Axis constant: X Rotation axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute rotation angle about the X axis.
     * The value is normalized to a range from -1.0 (counter-clockwise) to 1.0 (clockwise).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_RX = 12;

    /**
     * Axis constant: Y Rotation axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute rotation angle about the Y axis.
     * The value is normalized to a range from -1.0 (counter-clockwise) to 1.0 (clockwise).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_RY = 13;

    /**
     * Axis constant: Z Rotation axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute rotation angle about the Z axis.
     * The value is normalized to a range from -1.0 (counter-clockwise) to 1.0 (clockwise).
     * <em>On game pads with two analog joysticks, this axis is often reinterpreted
     * to report the absolute Y position of the second joystick instead.</em>
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_RZ = 14;

    /**
     * Axis constant: Hat X axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute X position of the directional hat control.
     * The value is normalized to a range from -1.0 (left) to 1.0 (right).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_HAT_X = 15;

    /**
     * Axis constant: Hat Y axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute Y position of the directional hat control.
     * The value is normalized to a range from -1.0 (up) to 1.0 (down).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_HAT_Y = 16;

    /**
     * Axis constant: Left Trigger axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute position of the left trigger control.
     * The value is normalized to a range from 0.0 (released) to 1.0 (fully pressed).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_LTRIGGER = 17;

    /**
     * Axis constant: Right Trigger axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute position of the right trigger control.
     * The value is normalized to a range from 0.0 (released) to 1.0 (fully pressed).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_RTRIGGER = 18;

    /**
     * Axis constant: Throttle axis of a motion event.
     * <p>
     * <ul>
     * <li>For a joystick, reports the absolute position of the throttle control.
     * The value is normalized to a range from 0.0 (fully open) to 1.0 (fully closed).
     * </ul>
     * </p>
     *
     * @see #getAxisValue(int, int)
     * @see #getHistoricalAxisValue(int, int, int)
     * @see MotionEvent.PointerCoords#getAxisValue(int)
     * @see InputDevice#getMotionRange
     */
    public static final int AXIS_THROTTLE = 19;

    private static final int INITIAL_PACKED_AXIS_VALUES = 8;

    private long mPackedAxisBits;

    private float[] mPackedAxisValues;

    /**
     * Creates a pointer coords object with all axes initialized to zero.
     */
    public PointerCoords() {
    }

    /**
     * Creates a pointer coords object as a copy of the
     * contents of another pointer coords object.
     *
     * @param other The pointer coords object to copy.
     */
    public PointerCoords(PointerCoords other) {
        copyFrom(other);
    }

    /**
     * @hide
     */
    public static PointerCoords[] createArray(int size) {
        PointerCoords[] array = new PointerCoords[size];
        for (int i = 0; i < size; i++) {
            array[i] = new PointerCoords();
        }
        return array;
    }

    /**
     * The X component of the pointer movement.
     *
     * @see MotionEvent#AXIS_X
     */
    public float x;

    /**
     * The Y component of the pointer movement.
     *
     * @see MotionEvent#AXIS_Y
     */
    public float y;

    /**
     * A normalized value that describes the pressure applied to the device
     * by a finger or other tool.
     * The pressure generally ranges from 0 (no pressure at all) to 1 (normal pressure),
     * although values higher than 1 may be generated depending on the calibration of
     * the input device.
     *
     * @see MotionEvent#AXIS_PRESSURE
     */
    public float pressure;

    /**
     * A normalized value that describes the approximate size of the pointer touch area
     * in relation to the maximum detectable size of the device.
     * It represents some approximation of the area of the screen being
     * pressed; the actual value in pixels corresponding to the
     * touch is normalized with the device specific range of values
     * and scaled to a value between 0 and 1. The value of size can be used to
     * determine fat touch events.
     *
     * @see MotionEvent#AXIS_SIZE
     */
    public float size;

    /**
     * The length of the major axis of an ellipse that describes the touch area at
     * the point of contact.
     * If the device is a touch screen, the length is reported in pixels, otherwise it is
     * reported in device-specific units.
     *
     * @see MotionEvent#AXIS_TOUCH_MAJOR
     */
    public float touchMajor;

    /**
     * The length of the minor axis of an ellipse that describes the touch area at
     * the point of contact.
     * If the device is a touch screen, the length is reported in pixels, otherwise it is
     * reported in device-specific units.
     *
     * @see MotionEvent#AXIS_TOUCH_MINOR
     */
    public float touchMinor;

    /**
     * The length of the major axis of an ellipse that describes the size of
     * the approaching tool.
     * The tool area represents the estimated size of the finger or pen that is
     * touching the device independent of its actual touch area at the point of contact.
     * If the device is a touch screen, the length is reported in pixels, otherwise it is
     * reported in device-specific units.
     *
     * @see MotionEvent#AXIS_TOOL_MAJOR
     */
    public float toolMajor;

    /**
     * The length of the minor axis of an ellipse that describes the size of
     * the approaching tool.
     * The tool area represents the estimated size of the finger or pen that is
     * touching the device independent of its actual touch area at the point of contact.
     * If the device is a touch screen, the length is reported in pixels, otherwise it is
     * reported in device-specific units.
     *
     * @see MotionEvent#AXIS_TOOL_MINOR
     */
    public float toolMinor;

    /**
     * The orientation of the touch area and tool area in radians clockwise from vertical.
     * An angle of 0 radians indicates that the major axis of contact is oriented
     * upwards, is perfectly circular or is of unknown orientation. A positive angle
     * indicates that the major axis of contact is oriented to the right. A negative angle
     * indicates that the major axis of contact is oriented to the left.
     * The full range is from -PI/2 radians (finger pointing fully left) to PI/2 radians
     * (finger pointing fully right).
     *
     * @see MotionEvent#AXIS_ORIENTATION
     */
    public float orientation;

    /**
     * Clears the contents of this object.
     * Resets all axes to zero.
     */
    public void clear() {
        mPackedAxisBits = 0;

        x = 0;
        y = 0;
        pressure = 0;
        size = 0;
        touchMajor = 0;
        touchMinor = 0;
        toolMajor = 0;
        toolMinor = 0;
        orientation = 0;
    }

    /**
     * Copies the contents of another pointer coords object.
     *
     * @param other The pointer coords object to copy.
     */
    public void copyFrom(PointerCoords other) {
        final long bits = other.mPackedAxisBits;
        mPackedAxisBits = bits;
        if (bits != 0) {
            final float[] otherValues = other.mPackedAxisValues;
            final int count = Long.bitCount(bits);
            float[] values = mPackedAxisValues;
            if (values == null || count > values.length) {
                values = new float[otherValues.length];
                mPackedAxisValues = values;
            }
            System.arraycopy(otherValues, 0, values, 0, count);
        }

        x = other.x;
        y = other.y;
        pressure = other.pressure;
        size = other.size;
        touchMajor = other.touchMajor;
        touchMinor = other.touchMinor;
        toolMajor = other.toolMajor;
        toolMinor = other.toolMinor;
        orientation = other.orientation;
    }

    /**
     * Gets the value associated with the specified axis.
     *
     * @param axis The axis identifier for the axis value to retrieve.
     *
     * @return The value associated with the axis, or 0 if none.
     *
     * @see MotionEvent#AXIS_X
     * @see MotionEvent#AXIS_Y
     */
    public float getAxisValue(int axis) {
        switch (axis) {
            case AXIS_X:
                return x;
            case AXIS_Y:
                return y;
            case AXIS_PRESSURE:
                return pressure;
            case AXIS_SIZE:
                return size;
            case AXIS_TOUCH_MAJOR:
                return touchMajor;
            case AXIS_TOUCH_MINOR:
                return touchMinor;
            case AXIS_TOOL_MAJOR:
                return toolMajor;
            case AXIS_TOOL_MINOR:
                return toolMinor;
            case AXIS_ORIENTATION:
                return orientation;
            default: {
                if (axis < 0 || axis > 63) {
                    throw new IllegalArgumentException("Axis out of range.");
                }
                final long bits = mPackedAxisBits;
                final long axisBit = 1L << axis;
                if ((bits & axisBit) == 0) {
                    return 0;
                }
                final int index = Long.bitCount(bits & (axisBit - 1L));
                return mPackedAxisValues[index];
            }
        }
    }

    /**
     * Sets the value associated with the specified axis.
     *
     * @param axis  The axis identifier for the axis value to assign.
     * @param value The value to set.
     *
     * @see MotionEvent#AXIS_X
     * @see MotionEvent#AXIS_Y
     */
    public void setAxisValue(int axis, float value) {
        switch (axis) {
            case AXIS_X:
                x = value;
                break;
            case AXIS_Y:
                y = value;
                break;
            case AXIS_PRESSURE:
                pressure = value;
                break;
            case AXIS_SIZE:
                size = value;
                break;
            case AXIS_TOUCH_MAJOR:
                touchMajor = value;
                break;
            case AXIS_TOUCH_MINOR:
                touchMinor = value;
                break;
            case AXIS_TOOL_MAJOR:
                toolMajor = value;
                break;
            case AXIS_TOOL_MINOR:
                toolMinor = value;
                break;
            case AXIS_ORIENTATION:
                orientation = value;
                break;
            default: {
                if (axis < 0 || axis > 63) {
                    throw new IllegalArgumentException("Axis out of range.");
                }
                final long bits = mPackedAxisBits;
                final long axisBit = 1L << axis;
                final int index = Long.bitCount(bits & (axisBit - 1L));
                float[] values = mPackedAxisValues;
                if ((bits & axisBit) == 0) {
                    if (values == null) {
                        values = new float[INITIAL_PACKED_AXIS_VALUES];
                        mPackedAxisValues = values;
                    } else {
                        final int count = Long.bitCount(bits);
                        if (count < values.length) {
                            if (index != count) {
                                System.arraycopy(values, index, values, index + 1,
                                        count - index);
                            }
                        } else {
                            float[] newValues = new float[count * 2];
                            System.arraycopy(values, 0, newValues, 0, index);
                            System.arraycopy(values, index, newValues, index + 1,
                                    count - index);
                            values = newValues;
                            mPackedAxisValues = values;
                        }
                    }
                    mPackedAxisBits = bits | axisBit;
                }
                values[index] = value;
            }
        }
    }
}
