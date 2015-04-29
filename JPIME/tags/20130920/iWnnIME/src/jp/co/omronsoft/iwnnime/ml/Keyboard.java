/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (C) 2008-2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * This file is porting from Android framework.
 *   frameworks/base/core/java/android/inputmethodservice/Keyboard.java
 *
 * package android.inputmethodservice;
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xmlpull.v1.XmlPullParserException;

/**
 * Loads an XML description of a keyboard and stores the attributes of the keys. A keyboard
 * consists of rows of keys.
 * <p>The layout file for a keyboard contains XML that looks like the following snippet:</p>
 * <pre>
 * &lt;Keyboard
 *         android:keyWidth="%10p"
 *         android:keyHeight="50px"
 *         android:horizontalGap="2px"
 *         android:verticalGap="2px" &gt;
 *     &lt;Row android:keyWidth="32px" &gt;
 *         &lt;Key android:keyLabel="A" /&gt;
 *         ...
 *     &lt;/Row&gt;
 *     ...
 * &lt;/Keyboard&gt;
 * </pre>
 */
public class Keyboard {

    static final String TAG = "Keyboard";

    // Keyboard XML Tags
    private static final String TAG_KEYBOARD = "Keyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";
    private static final String TAG_INCLUDE = "include";
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_CASE = "case";
    private static final String TAG_ROW_KEY = "Row-Key";

    /** Edge of left */
    public static final int EDGE_LEFT = 0x01;

    /** Edge of right */
    public static final int EDGE_RIGHT = 0x02;

    /** Edge of top */
    public static final int EDGE_TOP = 0x04;

    /** Edge of bottom */
    public static final int EDGE_BOTTOM = 0x08;

    /** Keycode of SHIFT */
    public static final int KEYCODE_SHIFT = -1;

    /** Keycode of MODE_CHANGE */
    public static final int KEYCODE_MODE_CHANGE = -2;

    /** Keycode of CANCEL */
    public static final int KEYCODE_CANCEL = -3;

    /** Keycode of DONE */
    public static final int KEYCODE_DONE = -4;

    /** Keycode of DELETE */
    public static final int KEYCODE_DELETE = -5;

    /** Keycode of ALT */
    public static final int KEYCODE_ALT = -6;

    /** Keycode of  voice input */
    public static final int KEYCODE_VOICE_INPUT = -312;

    /** Voice input button key line */
    private static final int VOICE_INPUT_KEYLINE = 2;

    /** Key of "key_height_portrait" setting. */
    private static final String KEY_HEIGHT_PORTRAIT_KEY = "key_height_portrait";
    /** Key of "key_height_landscape" setting. */
    private static final String KEY_HEIGHT_LANDSCAPE_KEY = "key_height_landscape";
    /** Key of "key_height_portrait" setting. */
    private static final String KEY_HEIGHT_PORTRAIT_SYMBOL_KEY = "key_height_portrait_symbol";
    /** Key of "key_height_landscape" setting. */
    private static final String KEY_HEIGHT_LANDSCAPE_SYMBOL_KEY = "key_height_landscape_symbol";

    /** Keyboard line (Line 4 keyboard). */
    private static final int KEY_LINE_DEFAULT_KEYBOARD = 4;
    /** Keyboard line (Line 5 keyboard). */
    private static final int KEY_LINE_5_KEYBOARD = 5;

    /** Context **/
    private static Context mContext;

    /** Orientation **/
    private int mOrientation;


    /** Horizontal gap default for all rows */
    private int mDefaultHorizontalGap;

    /** Default key width */
    private int mDefaultWidth;

    /** Default key height */
    private int mDefaultHeight;

    /** Default key height correction. */
    private int mKeyHeightCorrection = 0;

    /** Default gap between rows */
    private int mDefaultVerticalGap;

    /** Is the keyboard in the shifted state */
    private boolean mShifted;

    /** Key instance for the shift key, if present */
    private Key mShiftKey;

    /** Key index for the shift key, if present */
    private int mShiftKeyIndex = -1;

    /** Total height of the keyboard, including the padding and keys */
    private int mTotalHeight;

    /**
     * Total width of the keyboard, including left side gaps and keys, but not any gaps on the
     * right side.
     */
    private int mTotalWidth;

    /** List of keys in this keyboard */
    private List<Key> mKeys;

    /** List of modifier keys such as Shift & Alt, if any */
    private List<Key> mModifierKeys;

    /** Width of the screen available to fit the keyboard */
    private int mDisplayWidth;

    /** Height of the screen */
    private int mDisplayHeight;

    /** Keyboard mode, or zero, if none.  */
    private int mKeyboardMode;

    /** Keyboard mode, or zero, if none.  */
    private int mXmlLayoutResId;

    /** Scale of keyboard width*/
    private float mScaleWidth;

    /** Whether this keyboard is Phone */
    public boolean isPhone;

    // Variables for pre-computing nearest keys.

    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 5;
    private static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;
    private int mCellWidth;
    private int mCellHeight;
    private int[][] mGridNeighbors;
    private int mProximityThreshold;
    /** Number of key widths from current touch point to search for nearest keys. */
    private static float SEARCH_DISTANCE = 1.8f;
    /** Provisional key icon Id for drawing QWERTY keyboard numeric/symbol key preview. */
    private static final int KEY_ID_QWERTY_NUM_SYMBOL_PREVIEW  = 143;

    /** Width of the mini-keyboard */
    private int mMiniKeyboardWidth = 0;

    private int mKeyX = 0;
    private int mKeyY = 0;

    /**
     * Container for keys in the keyboard. All keys in a row are at the same Y-coordinate.
     * Some of the key size defaults can be overridden per row from what the {@link Keyboard}
     * defines.
     */
    public static class Row {
        /** Default width of a key in this row. */
        public int defaultWidth;
        /** Default height of a key in this row. */
        public int defaultHeight;
        /** Default horizontal gap between keys in this row. */
        public int defaultHorizontalGap;
        /** Vertical gap following this row. */
        public int verticalGap;
        /**
         * Edge flags for this row of keys. Possible values that can be assigned are
         * {@link Keyboard#EDGE_TOP EDGE_TOP} and {@link Keyboard#EDGE_BOTTOM EDGE_BOTTOM}
         */
        public int rowEdgeFlags;

        /** The keyboard mode for this row */
        public int mode;

        private Keyboard parent;

        /** Constructor */
        public Row(Keyboard parent) {
            this.parent = parent;
        }

        /** Constructor */
        public Row(Resources res, Keyboard parent, XmlResourceParser parser) {
            this.parent = parent;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.WnnKeyboardView);
            defaultWidth = getDimensionOrFractionForHorizontal(a,
                    R.styleable.WnnKeyboardView_keyWidth,
                    parent.mDisplayWidth, parent.mDefaultWidth, parent.mScaleWidth);

            //Set Key defaultHeight
            defaultHeight = parent.mDefaultHeight;

            defaultHorizontalGap = getDimensionOrFractionForHorizontal(a,
                    R.styleable.WnnKeyboardView_horizontalGap,
                    parent.mDisplayWidth, parent.mDefaultHorizontalGap, parent.mScaleWidth);
            verticalGap = getDimensionOrFraction(a,
                    R.styleable.WnnKeyboardView_verticalGap,
                    parent.mDisplayHeight, parent.mDefaultVerticalGap);
            rowEdgeFlags = a.getInt(R.styleable.WnnKeyboardView_rowEdgeFlags, 0);
            mode = a.getResourceId(R.styleable.WnnKeyboardView_keyboardMode,
                    0);
            a.recycle();
        }
    }

    /**
     * Class for describing the position and characteristics of a single key in the keyboard.
     */
    public static class Key {
        /**
         * All the key codes (unicode or custom code) that this key could generate, zero'th
         * being the most important.
         */
        public int[] codes;

        /** Label to display */
        public CharSequence label;

        /** Icon to display instead of a label. Icon takes precedence over a label */
        public Drawable icon;
        /** Whether this icon from keyboardskin */
        public boolean mIsIconSkin = false;
        /** Preview version of the icon, for the preview popup */
        public Drawable iconPreview;
        /** Preview version of the icon, for the preview popup */
        public CharSequence keyPreviewLabel;
        /** Width of the key, not including the gap */
        public int width;
        /** Height of the key, not including the gap */
        public int height;
        /** The horizontal gap before this key */
        public int gap;
        /** The verticalGap gap before this key */
        public int vgap;
        /** Whether this key is sticky, i.e., a toggle key */
        public boolean sticky;
        /** X coordinate of the key in the keyboard layout */
        public int x;
        /** Y coordinate of the key in the keyboard layout */
        public int y;
        /** The current pressed state of this key */
        public boolean pressed;
        /** If this is a sticky key, is it on? */
        public boolean on;
        /** Text to output when pressed. This can be multiple characters, like ".com" */
        public CharSequence text;
        /** Popup characters */
        public CharSequence popupCharacters;
        /** Key Hint characters */
        public CharSequence hintLabel;

        /**
         * Flags that specify the anchoring to edges of the keyboard for detecting touch events
         * that are just out of the boundary of the key. This is a bit mask of
         * {@link Keyboard#EDGE_LEFT}, {@link Keyboard#EDGE_RIGHT}, {@link Keyboard#EDGE_TOP} and
         * {@link Keyboard#EDGE_BOTTOM}.
         */
        public int edgeFlags;
        /** Whether this is a modifier key, such as Shift or Alt */
        public boolean modifier;
        /** The keyboard that this key belongs to */
        private Keyboard keyboard;
        /**
         * If this key pops up a mini keyboard, this is the resource id for the XML layout for that
         * keyboard.
         */
        public int popupResId;
        /** Whether this key repeats itself when held down */
        public boolean repeatable;
        /** Whether this key is 2nd key */
        public boolean isSecondKey;
        /** Whether this key is press & hold key */
        public boolean isPressHoldKey;
        /** Whether this key is show preview */
        public boolean showPreview;
        /** VoiceIconPreview to display instead of an Icon. */
        public Drawable voiceIconPreview;
        /** Whether this key is split key */
        private boolean isSplitKey;

        /** Temp flag from keyboardskin */
        private boolean mIsSkin = false;

        public int keyIconId;
        public int keyIconPreviewId;

        private final static int[] KEY_STATE_NORMAL_ON = {
            android.R.attr.state_checkable,
            android.R.attr.state_checked
        };

        private final static int[] KEY_STATE_PRESSED_ON = {
            android.R.attr.state_pressed,
            android.R.attr.state_checkable,
            android.R.attr.state_checked
        };

        private final static int[] KEY_STATE_NORMAL_OFF = {
            android.R.attr.state_checkable
        };

        private final static int[] KEY_STATE_PRESSED_OFF = {
            android.R.attr.state_pressed,
            android.R.attr.state_checkable
        };

        private final static int[] KEY_STATE_NORMAL = {
        };

        private final static int[] KEY_STATE_PRESSED = {
            android.R.attr.state_pressed
        };

        /** Create an empty key with no attributes. */
        public Key(Row parent) {
            keyboard = parent.parent;
        }

        /** Create a key with the given top-left coordinate and extract its attributes from
         * the XML parser.
         * @param res resources associated with the caller's context
         * @param parent the row that this key belongs to. The row must already be attached to
         * a {@link Keyboard}.
         * @param x the x coordinate of the top-left
         * @param y the y coordinate of the top-left
         * @param parser the XML parser containing the attributes for this key
         */
        public Key(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
            this(parent);

            this.x = x;
            this.y = y;

            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.WnnKeyboardView);

            width = getDimensionOrFractionForHorizontal(a,
                    R.styleable.WnnKeyboardView_keyWidth,
                    keyboard.mDisplayWidth, parent.defaultWidth, keyboard.mScaleWidth);

            gap = getDimensionOrFractionForHorizontal(a,
                    R.styleable.WnnKeyboardView_horizontalGap,
                    keyboard.mDisplayWidth, parent.defaultHorizontalGap, keyboard.mScaleWidth);
            vgap = getDimensionOrFraction(a,
                    R.styleable.WnnKeyboardView_verticalGap,
                    keyboard.mDisplayHeight, parent.verticalGap);
            this.x += gap;
            int vgap = getDimensionOrFraction(a,
                    R.styleable.WnnKeyboardView_verticalGap,
                    keyboard.mDisplayHeight, 0);
            this.y += vgap;
            TypedValue codesValue = new TypedValue();
            a.getValue(R.styleable.WnnKeyboardView_codes,
                    codesValue);
            if (codesValue.type == TypedValue.TYPE_INT_DEC
                    || codesValue.type == TypedValue.TYPE_INT_HEX) {
                codes = new int[] { codesValue.data };
            } else if (codesValue.type == TypedValue.TYPE_STRING) {
                codes = parseCSV(codesValue.string.toString());
            }

            height = getDimensionOrFraction(a,
                     R.styleable.WnnKeyboardView_keyHeight,
                     keyboard.mDisplayHeight, parent.defaultHeight);

            iconPreview = getDrawable(a, R.styleable.WnnKeyboardView_iconPreview);
            if (iconPreview != null) {
                iconPreview.setBounds(0, 0, iconPreview.getIntrinsicWidth(),
                        iconPreview.getIntrinsicHeight());
            }
            keyPreviewLabel = getText(a,
                    R.styleable.WnnKeyboardView_keyPreviewLabel);
            popupCharacters = getText(a,
                    R.styleable.WnnKeyboardView_popupCharacters);
            popupResId = a.getResourceId(
                    R.styleable.WnnKeyboardView_popupKeyboard, 0);
            KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
            if ((popupResId != 0) && (langPack.isValid())) {
                // When setting up a multi-language packs,
                // to keep that are defined in KeyboardLanguagePackData, the resource ID of the pop-up keyboard
                popupResId = KeyboardLanguagePackData.READ_RESOURCE_TAG_POPUP;
            }
            repeatable = a.getBoolean(
                    R.styleable.WnnKeyboardView_isRepeatable, false);
            modifier = a.getBoolean(
                    R.styleable.WnnKeyboardView_isModifier, false);
            sticky = a.getBoolean(
                    R.styleable.WnnKeyboardView_isSticky, false);
            edgeFlags = a.getInt(R.styleable.WnnKeyboardView_keyEdgeFlags, 0);
            edgeFlags |= parent.rowEdgeFlags;

            icon = getDrawable(a, R.styleable.WnnKeyboardView_keyIcon);
            mIsIconSkin = mIsSkin;
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            label = getText(a, R.styleable.WnnKeyboardView_keyLabel);
            text = getText(a, R.styleable.WnnKeyboardView_keyOutputText);

            if (codes == null && !TextUtils.isEmpty(label)) {
                codes = new int[] { label.charAt(0) };
            }
            isSecondKey = a.getBoolean(R.styleable.WnnKeyboardView_isSecondKey, false);
            isPressHoldKey = a.getBoolean(R.styleable.WnnKeyboardView_isPressHoldKey, false);
            showPreview = a.getBoolean(R.styleable.WnnKeyboardView_showPreview, true);
            isSplitKey = a.getBoolean(R.styleable.WnnKeyboardView_isSplitKey, false);

            hintLabel = a.getText(
                    R.styleable.WnnKeyboardView_keyHintLabel);

            keyIconId = a.getInt(R.styleable.WnnKeyboardView_keyIconId, 0);
            keyIconPreviewId = a.getInt(R.styleable.WnnKeyboardView_keyIconPreviewId,0);

            if (0 < keyIconId) {
                icon = new KeyDrawable(res ,keyIconId, width, height);
            }

            if (0 < keyIconPreviewId) {
                iconPreview = new KeyDrawable(res ,keyIconPreviewId-1, width, height);
            } else {
                if (!TextUtils.isEmpty(label)) {
                    int id = (0 < keyIconId)? keyIconId : KEY_ID_QWERTY_NUM_SYMBOL_PREVIEW;
                    if (keyIconId >= KeyboardView.KEY_ID_PHONE_NUM
                            && keyIconId < KeyboardView.KEY_ID_PHONE_SYM) {
                        id = KEY_ID_QWERTY_NUM_SYMBOL_PREVIEW;
                    }
                    iconPreview = new KeyDrawable(res ,id, width, height, label);
                }
            }

            a.recycle();
        }

        /** Get drawable */
        private Drawable getDrawable(TypedArray array, int id) {
            KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
            KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
            boolean langValid = langPack.isValid();
            Drawable result = null;
            mIsSkin = false;

            if (langValid) {
                // Get drawable to language pack xml file.
                result = langPack.getDrawable(array.getResourceId(id, 0));
            } else {
                // Get drawable to iWnn IME xml file.
                result = array.getDrawable(id);
            }

            if (result != null && keyskin.isValid()) {
                Drawable tmpicon = null;
                if (langValid) {
                    // The resource ID convert to keyboard-skin key.
                    String reskey = langPack.conversionSkinKey(mContext,
                                                                langPack.getEnableKeyboardLanguagePackDataName(),
                                                                array.getResourceId(id, 0));
                    tmpicon = keyskin.getDrawable(reskey);
                } else {
                    int resid = array.getResourceId(id, 0);
                    tmpicon = keyskin.getDrawable(resid);
                }
                if (tmpicon != null) {
                    result = tmpicon;
                    mIsSkin = true;
                }
            }
            return result;
        }

        /** Get text */
        private CharSequence getText(TypedArray array, int id) {
            KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
            int resId = array.getResourceId(id, 0);
            CharSequence result = null;

            // Even when multi-language settings,
            // if it is written directly to the text data to xml is obtained from the TypedArray.
            if ((langPack.isValid()) && (resId != 0)) {
                result = langPack.getText(resId);
            } else {
                result = array.getText(id);
            }
            return result;
        }

        /** Get Split Key */
        public boolean isSplitKey() {
            return isSplitKey;
        }

        /**
         * Informs the key that it has been pressed, in case it needs to change its appearance or
         * state.
         * @see #onReleased(boolean)
         */
        public void onPressed() {
            pressed = true;
        }

        /**
         * Changes the pressed state of the key. If it is a sticky key, it will also change the
         * toggled state of the key if the finger was release inside.
         * @param inside whether the finger was released inside the key
         * @see #onPressed()
         */
        public void onReleased(boolean inside) {
            pressed = false;
        }

        int[] parseCSV(String value) {
            int count = 0;
            int lastIndex = 0;
            if (value.length() > 0) {
                count++;
                while ((lastIndex = value.indexOf(",", lastIndex + 1)) > 0) {
                    count++;
                }
            }
            int[] values = new int[count];
            count = 0;
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens()) {
                try {
                    values[count++] = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Error parsing keycodes " + value);
                }
            }
            return values;
        }

        /**
         * Detects if a point falls inside this key.
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key. If the key is attached to an edge,
         * it will assume that all points between the key and the edge are considered to be inside
         * the key.
         */
        public boolean isInside(int x, int y) {
            boolean leftEdge = (edgeFlags & EDGE_LEFT) > 0;
            boolean rightEdge = (edgeFlags & EDGE_RIGHT) > 0;
            boolean topEdge = (edgeFlags & EDGE_TOP) > 0;
            boolean bottomEdge = (edgeFlags & EDGE_BOTTOM) > 0;
            if ((x >= this.x - this.gap || (leftEdge && x <= this.x + this.width))
                    && (x < this.x + this.width  || (rightEdge && x >= this.x))
                    && (y >= this.y || (topEdge && y <= this.y + this.height))
                    && (y < this.y + this.height + this.vgap || (bottomEdge && y >= this.y))) {
                return true;
            } else {
                return false;
            }
        }

         /**
         * Detects if a area falls inside this key.
         * @param x the x-coordinate of the area
         * @param y the y-coordinate of the area
         * @param w the width of the area
         * @param h the height of the area
         * @return whether or not the area falls inside the key.
         */
        public boolean isInside(int x, int y, int w, int h) {
            if ((this.x <= (x + w)) && (x <= (this.x + this.width))
                    && (this.y <= (y + h)) && (y <= (this.y + this.height))) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Detects if a point falls inside the split key.
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key. If the key is attached to an edge,
         * it will assume that all points between the key and the edge are considered to be inside
         * the key.
         */
        public boolean isInsideSplitKey(int x, int y) {
            boolean leftEdge = (edgeFlags & EDGE_LEFT) > 0;
            boolean rightEdge = (edgeFlags & EDGE_RIGHT) > 0;
            boolean topEdge = (edgeFlags & EDGE_TOP) > 0;
            boolean bottomEdge = (edgeFlags & EDGE_BOTTOM) > 0;
            if ((x >= this.x || (leftEdge && x <= this.x + this.width))
                    && (x < this.x + this.width  || (rightEdge && x >= this.x))
                    && (y >= this.y || (topEdge && y <= this.y + this.height))
                    && (y < this.y + this.height + this.vgap || (bottomEdge && y >= this.y))) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the square of the distance between the center of the key and the given point.
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return the square of the distance of the point from the center of the key
         */
        public int squaredDistanceFrom(int x, int y) {
            int xDist = this.x + width / 2 - x;
            int yDist = this.y + height / 2 - y;
            return xDist * xDist + yDist * yDist;
        }

        /**
         * Returns the drawable state for the key, based on the current state and type of the key.
         * @return the drawable state of the key.
         * @see android.graphics.drawable.StateListDrawable#setState(int[])
         */
        public int[] getCurrentDrawableState() {
            int[] states = KEY_STATE_NORMAL;

            if (sticky) {
                if (on) {
                    if (pressed) {
                        states = KEY_STATE_PRESSED_ON;
                    } else {
                        states = KEY_STATE_NORMAL_ON;
                    }
                } else {
                    if (pressed) {
                        states = KEY_STATE_PRESSED_OFF;
                    } else {
                        states = KEY_STATE_NORMAL_OFF;
                    }
                }
            } else {
                if (pressed) {
                    states = KEY_STATE_PRESSED;
                }
            }
            return states;
        }

        /** resize iconPreview. */
        public void resizeIconPreview() {
            if (this.iconPreview != null) {
                iconPreview.setBounds(0, 0, iconPreview.getIntrinsicWidth(), iconPreview.getIntrinsicHeight());
            }
        }

        /**
         * Set secondKey flag value.
         *
         * @param seconKey This key is second key or not
         */
        public void setSecondKey(boolean secondKey) {
            isSecondKey = secondKey;
        }
    }

    /**
     * Creates a keyboard from the given xml key layout file.
     * @param context the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     */
    public Keyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, 0);
    }

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     * @param context the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param modeId keyboard mode identifier
     */
    public Keyboard(Context context, int xmlLayoutResId, int modeId) {
        mContext = context;
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        mScaleWidth = 1.0f;
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;
        boolean isFloatingLandscape = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isFloatingLandscape();
        if (isFloatingLandscape) {
            mDisplayWidth = dm.heightPixels;
            mDisplayHeight = dm.widthPixels;
        }

        //Set Orientation
        mOrientation = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isNotExpandedFloatingMode();
        if (isFloatingShown) {
            mDisplayWidth = res.getDimensionPixelSize(R.dimen.floating_keyboard_width);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mOrientation = Configuration.ORIENTATION_PORTRAIT;
            }
        }

        //Log.v(TAG, "keyboard's display metrics:" + dm);

        mDefaultHorizontalGap = 0;
        mDefaultWidth = mDisplayWidth / 10;
        mDefaultVerticalGap = 0;
        mDefaultHeight = 75;
        mKeys = new ArrayList<Key>();
        mModifierKeys = new ArrayList<Key>();
        mKeyboardMode = modeId;
        mXmlLayoutResId = xmlLayoutResId;
        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        if (langPack.isValid()) {
            loadKeyboard(context, langPack.getXmlParser(xmlLayoutResId));
        } else {
            loadKeyboard(context, res.getXml(xmlLayoutResId));
        }
    }

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     * @param context the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param scaleWidth scale of the keyboard width.
     * @param modeId keyboard mode identifier.
     */
    public Keyboard(Context context, int xmlLayoutResId, float scaleWidth, int modeId) {
        mContext = context;
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        mScaleWidth = scaleWidth;
        mDisplayWidth = (int)(dm.widthPixels * mScaleWidth);
        mDisplayHeight = dm.heightPixels;
        boolean isFloatingLandscape = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isFloatingLandscape();
        if (isFloatingLandscape) {
            mDisplayWidth = (int)(dm.heightPixels * mScaleWidth);
            mDisplayHeight = dm.widthPixels;
        }

        //Set Orientation
        mOrientation = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isNotExpandedFloatingMode();
        if (isFloatingShown) {
            mDisplayWidth = res.getDimensionPixelSize(R.dimen.floating_keyboard_width);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mOrientation = Configuration.ORIENTATION_PORTRAIT;
            }
        }

        mDefaultHorizontalGap = 0;
        mDefaultWidth = mDisplayWidth / 10;
        mDefaultVerticalGap = 0;
        mDefaultHeight = 75;
        mKeys = new ArrayList<Key>();
        mModifierKeys = new ArrayList<Key>();
        mKeyboardMode = modeId;
        mXmlLayoutResId = xmlLayoutResId;
        loadKeyboard(context, res.getXml(xmlLayoutResId));
    }

    /**
     * <p>Creates a blank keyboard from the given resource file and populates it with the specified
     * characters in left-to-right, top-to-bottom fashion, using the specified number of columns.
     * </p>
     * <p>If the specified number of columns is -1, then the keyboard will fit as many keys as
     * possible in each row.</p>
     * @param context the application or service context
     * @param layoutTemplateResId the layout template file, containing no keys.
     * @param characters the list of characters to display on the keyboard. One key will be created
     * for each character.
     * @param columns the number of columns of keys to display. If this number is greater than the
     * number of keys that can fit in a row, it will be ignored. If this number is -1, the
     * keyboard will fit as many keys as possible in each row.
     */
    public Keyboard(Context context, int layoutTemplateResId,
            CharSequence characters, int columns, int horizontalPadding) {
        this(context, layoutTemplateResId);
        int x = 0;
        int y = 0;
        int column = 0;
        mTotalWidth = 0;
        mScaleWidth = 1.0f;

        Row row = new Row(this);
        row.defaultHeight = mDefaultHeight;
        row.defaultWidth = mDefaultWidth;
        row.defaultHorizontalGap = mDefaultHorizontalGap;
        row.verticalGap = mDefaultVerticalGap;
        row.rowEdgeFlags = EDGE_TOP | EDGE_BOTTOM;
        final int maxColumns = columns == -1 ? Integer.MAX_VALUE : columns;
        for (int i = 0; i < characters.length(); i++) {
            char c = characters.charAt(i);
            if (column >= maxColumns
                    || x + mDefaultWidth + horizontalPadding >= mDisplayWidth /2) {
                x = 0;
                y += mDefaultVerticalGap + mDefaultHeight;
                column = 0;
            }
            final Key key = new Key(row);
            key.x = x;
            key.y = y;
            key.width = mDefaultWidth;
            key.height = mDefaultHeight;
            key.gap = mDefaultHorizontalGap;
            key.label = String.valueOf(c);
            key.codes = new int[] { c };
            column++;
            x += key.width + key.gap;
            mKeys.add(key);
            if (x > mTotalWidth) {
                mTotalWidth = x;
            }
        }
        mTotalHeight = y + mDefaultHeight;
        mMiniKeyboardWidth = mTotalWidth;
    }

    /**
     * Get the list of keys in this keyboard.
     *
     * @return The list of keys.
     */
    public List<Key> getKeys() {
        return mKeys;
    }

    /**
     * Get the list of modifier keys such as Shift & Alt, if any.
     *
     * @return The list of modifier keys.
     */
    public List<Key> getModifierKeys() {
        return mModifierKeys;
    }

    protected int getHorizontalGap() {
        return mDefaultHorizontalGap;
    }

    protected void setHorizontalGap(int gap) {
        mDefaultHorizontalGap = gap;
    }

    protected int getVerticalGap() {
        return mDefaultVerticalGap;
    }

    protected void setVerticalGap(int gap) {
        mDefaultVerticalGap = gap;
    }

    protected int getKeyHeight() {
        return mDefaultHeight;
    }

    protected void setKeyHeight(int height) {
        mDefaultHeight = height;
    }

    protected int getKeyWidth() {
        return mDefaultWidth;
    }

    protected void setKeyWidth(int width) {
        mDefaultWidth = width;
    }

    /**
     * Returns the total height of the keyboard
     * @return the total height of the keyboard
     */
    public int getHeight() {
        return mTotalHeight;
    }

    /**
     * Returns the total minimum width of the keyboard
     * @return the total minimum width of the keyboard
     */
    public int getMinWidth() {
        return mDisplayWidth;
    }
    /**
     * Returns the display width
     * @return the display width
     */
    public int getDisplayWidth() {
        return mDisplayWidth;
    }

    /**
     * Returns the width of the mini-keyboard
     * @return the width of the mini-keyboard
     */
    public int getMiniKeyboardWidth() {
        return mMiniKeyboardWidth;
    }

    /**
     * Sets the keyboard to be shifted.
     *
     * @param shiftState  the keyboard shift state.
     * @return {@code true} if shift state changed.
     */
    public boolean setShifted(boolean shiftState) {
        if (mShiftKey != null) {
            mShiftKey.on = shiftState;
        }
        if (mShifted != shiftState) {
            mShifted = shiftState;
            return true;
        }
        return false;
    }

    /**
     * Returns whether keyboard is shift state or not.
     *
     * @return  {@code true} if keyboard is shift state; otherwise, {@code false}.
     */
    public boolean isShifted() {
        return mShifted;
    }

    /**
     * Returns the xml resource ID.
     *
     * @return  the xml resource ID.
     */
    public int getXmlLayoutResId() {
        return mXmlLayoutResId;
    }

    private void computeNearestNeighbors() {
        // Round-up so we don't have any pixels outside the grid
        mCellWidth = (getMinWidth() + GRID_WIDTH - 1) / GRID_WIDTH;
        mCellHeight = (getHeight() + GRID_HEIGHT - 1) / GRID_HEIGHT;
        mGridNeighbors = new int[GRID_SIZE][];
        int[] indices = new int[mKeys.size()];
        final int gridWidth = GRID_WIDTH * mCellWidth;
        final int gridHeight = GRID_HEIGHT * mCellHeight;
        for (int x = 0; x < gridWidth; x += mCellWidth) {
            for (int y = 0; y < gridHeight; y += mCellHeight) {
                int count = 0;
                for (int i = 0; i < mKeys.size(); i++) {
                    final Key key = mKeys.get(i);
                    if (key.squaredDistanceFrom(x, y) < mProximityThreshold ||
                            key.squaredDistanceFrom(x + mCellWidth - 1, y) < mProximityThreshold ||
                            key.squaredDistanceFrom(x + mCellWidth - 1, y + mCellHeight - 1)
                                < mProximityThreshold ||
                            key.squaredDistanceFrom(x, y + mCellHeight - 1) < mProximityThreshold ||
                            (((key.codes[0] == '\u3000') || (key.codes[0] == ' '))
                                    && key.isInside(x, y, mCellWidth, mCellHeight))) {

                        indices[count++] = i;
                    }
                }
                int [] cell = new int[count];
                System.arraycopy(indices, 0, cell, 0, count);
                mGridNeighbors[(y / mCellHeight) * GRID_WIDTH + (x / mCellWidth)] = cell;
            }
        }
    }

    /**
     * Returns the indices of the keys that are closest to the given point.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the array of integer indices for the nearest keys to the given point. If the given
     * point is out of range, then an array of size zero is returned.
     */
    public int[] getNearestKeys(int x, int y) {
        if (mGridNeighbors == null) computeNearestNeighbors();
        if (x >= 0 && x < getMinWidth() && y >= 0 && y < getHeight()) {
            int index = (y / mCellHeight) * GRID_WIDTH + (x / mCellWidth);
            if (index < GRID_SIZE) {
                return mGridNeighbors[index];
            }
        }
        return new int[0];
    }

    protected Row createRowFromXml(Resources res, XmlResourceParser parser) {
        return new Row(res, this, parser);
    }

    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
            XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }

    private void loadKeyboard(Context context, XmlResourceParser parser) {
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "Keyboard::loadKeyboard()  Start");}
        boolean inKey = false;
        boolean inRow = false;
        boolean inInclude = false;
        Key key = null;
        Row currentRow = null;
        Resources res = context.getResources();
        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        boolean isLangPack = langPack.isValid();
        if (isLangPack) {
            res = langPack.getResources();
        }
        boolean skipRow = false;

        mKeyX = 0;
        mKeyY = 0;
        int rowCount = 0;

        try {
            int event;
            if (parser != null) {
                while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                    if (event == XmlResourceParser.START_TAG) {
                        String tag = parser.getName();
                        if (TAG_ROW.equals(tag)) {
                            inRow = true;
                            mKeyX = 0;
                            currentRow = createRowFromXml(res, parser);
                            if (isLangPack) {
                                if (mDefaultVerticalGap != currentRow.verticalGap) {
                                    currentRow.verticalGap = Math.round(context.getResources().getFraction(R.fraction.keyboard_12key_row4_verticalGap, mDisplayHeight, mDisplayHeight));
                                }
                                rowCount++;
                                if (rowCount == KEY_LINE_5_KEYBOARD) {
                                    currentRow.defaultHeight += mKeyHeightCorrection;
                                }
                            }
                            skipRow = currentRow.mode != 0 && currentRow.mode != mKeyboardMode;
                            if (skipRow) {
                                skipToEndOfRow(parser);
                                inRow = false;
                            }
                        } else if (TAG_KEY.equals(tag)) {
                            if (currentRow == null) {
                                continue;
                            }
                            inKey = true;
                            key = createKeyFromXml(res, currentRow, mKeyX, mKeyY, parser);
                            mKeys.add(key);
                            if (key.codes[0] == KEYCODE_SHIFT) {
                                mShiftKey = key;
                                mShiftKeyIndex = mKeys.size()-1;
                                mModifierKeys.add(key);
                            } else if (key.codes[0] == KEYCODE_ALT) {
                                mModifierKeys.add(key);
                            }
                        } else if (TAG_INCLUDE.equals(tag)) {
                            loadTagInclude(parser, res, currentRow);
                            inInclude = true;
                        } else if (TAG_KEYBOARD.equals(tag)) {
                            parseKeyboardAttributes(res, parser);
                        }
                    } else if (event == XmlResourceParser.END_TAG) {
                        if (inKey) {
                            inKey = false;
                            mKeyX += key.gap + key.width;
                            if (mKeyX > mTotalWidth) {
                                mTotalWidth = mKeyX;
                            }
                        } else if (inInclude) {
                            inInclude = false;
                        } else if (inRow) {
                            inRow = false;
                            mKeyY += currentRow.verticalGap;
                            mKeyY += currentRow.defaultHeight;
                        } else {
                            // TODO: error or extend?
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
        mTotalHeight = mKeyY - mDefaultVerticalGap;
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "Keyboard::loadKeyboard()  End");}
    }

    /**
     * Get parser for include keyboard xml.
     */
    private XmlResourceParser getParserOfLayout(Resources res, XmlResourceParser parser) {
        AttributeSet attr = Xml.asAttributeSet(parser);
        TypedArray keyboardAttr = res.obtainAttributes(attr, R.styleable.WnnKeyboardView);
        int resId = keyboardAttr.getResourceId(R.styleable.WnnKeyboardView_keyboardLayout, 0);
        keyboardAttr.recycle();
        XmlResourceParser parserForInclude = null;
        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        if (!langPack.isValid()) {
            parserForInclude = res.getXml(resId);
        } else {
            parserForInclude = langPack.getSimpleXmlParser(resId);
        }

        return parserForInclude;
    }

    /**
     * Load tag of include.
     */
    private void loadTagInclude(XmlResourceParser parser, Resources res, Row currentRow){
        boolean selected = false;
        XmlResourceParser parserForInclude = getParserOfLayout(res, parser);

        if (parserForInclude == null) {
            return;
        }

        try {
            int event = 0;
            while ((event = parserForInclude.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parserForInclude.getName();
                    if (!selected) {
                        if (TAG_ROW_KEY.equals(tag)) {
                            selected = loadTagKeyFromXml(parserForInclude, res, currentRow);
                        } else if (TAG_SWITCH.equals(tag)) {
                            selected = loadTagSwitchFromXml(parserForInclude, res, currentRow);
                        } // else {}
                    } // else {}
                } // else {}
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
    }

    /**
     * Load tag of SWITCH from Xml.
     */
    private boolean loadTagSwitchFromXml(XmlResourceParser parser, Resources res, Row currentRow){
        boolean selected = false;
        boolean inCase = false;
        boolean inRow = false;
        boolean inKey = false;
        boolean inInclude = false;
        boolean skipRow = false;
        Key key = null;

        try {
            int event = 0;
            boolean setIsKey = false;
            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (!setIsKey) {
                        if (TAG_CASE.equals(tag)) {
                            inCase = true;
                            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                                               R.styleable.WnnKeyboardView);
                            int rowCondition = a.getInt(R.styleable.WnnKeyboardView_keyboardCondition, 0);
                            setIsKey = DefaultSoftKeyboard.getKeyboardCondition(rowCondition);
                            a.recycle();
                        } // else {}
                    } else {
                        if (TAG_ROW.equals(tag)) {
                            inRow = true;
                            mKeyX = 0;
                            currentRow = createRowFromXml(res, parser);
                            skipRow = currentRow.mode != 0 && currentRow.mode != mKeyboardMode;
                            if (skipRow) {
                                skipToEndOfRow(parser);
                                inRow = false;
                            } // else {}
                        } else if (TAG_KEY.equals(tag)) {
                            if (currentRow == null) {
                                continue;
                            } // else {}
                            inKey = true;
                            key = createKeyFromXml(res, currentRow, mKeyX, mKeyY, parser);
                            mKeys.add(key);
                            if (key.codes[0] == KEYCODE_SHIFT) {
                                mShiftKey = key;
                                mShiftKeyIndex = mKeys.size()-1;
                                mModifierKeys.add(key);
                            } else if (key.codes[0] == KEYCODE_ALT) {
                                mModifierKeys.add(key);
                            } // else {}
                        } else if (TAG_INCLUDE.equals(tag)) {
                            loadTagInclude(parser, res, currentRow);
                            inInclude = true;
                        }  // else {}
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false;
                        mKeyX += key.gap + key.width;
                        if (mKeyX > mTotalWidth) {
                            mTotalWidth = mKeyX;
                        } // else {}
                    } else if (inInclude) {
                        inInclude = false;
                    } else if (inRow) {
                        inRow = false;
                        mKeyY += currentRow.verticalGap;
                        mKeyY += currentRow.defaultHeight;
                    } else if (inCase) {
                        inCase = false;
                        if (setIsKey) {
                           selected = true;
                           break;
                        } // else {}
                    } // else {}
                } // else {}
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException error:" + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException error:" + e);
            e.printStackTrace();
        }
        return selected;
    }

    /**
     * Load tag of KEY from Xml.
     */
    private boolean loadTagKeyFromXml(XmlResourceParser parser, Resources res, Row currentRow){
        boolean inRow = false;
        boolean inKey = false;
        boolean skipRow = false;
        Key key = null;

        try {
            int event = 0;
            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (TAG_ROW.equals(tag)) {
                        inRow = true;
                        mKeyX = 0;
                        currentRow = createRowFromXml(res, parser);
                        skipRow = currentRow.mode != 0 && currentRow.mode != mKeyboardMode;
                        if (skipRow) {
                            skipToEndOfRow(parser);
                            inRow = false;
                        } // else {}
                    } else if (TAG_KEY.equals(tag)) {
                        if (currentRow == null) {
                            continue;
                        } // else {}
                        inKey = true;
                        key = createKeyFromXml(res, currentRow, mKeyX, mKeyY, parser);
                        mKeys.add(key);
                        if (key.codes[0] == KEYCODE_SHIFT) {
                            mShiftKey = key;
                            mShiftKeyIndex = mKeys.size()-1;
                            mModifierKeys.add(key);
                        } else if (key.codes[0] == KEYCODE_ALT) {
                            mModifierKeys.add(key);
                        } // else {}
                    } // else {}
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false;
                        mKeyX += key.gap + key.width;
                        if (mKeyX > mTotalWidth) {
                            mTotalWidth = mKeyX;
                        } // else {}
                    } else if (inRow) {
                        inRow = false;
                        mKeyY += currentRow.verticalGap;
                        mKeyY += currentRow.defaultHeight;
                    } // else {}
                } // else {}
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException error:" + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException error:" + e);
            e.printStackTrace();
        }
        return true;
    }

    private void skipToEndOfRow(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int event;
        while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
            if (event == XmlResourceParser.END_TAG
                    && parser.getName().equals(TAG_ROW)) {
                break;
            }
        }
    }

    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                R.styleable.WnnKeyboardView);
        isPhone = a.getBoolean(R.styleable.WnnKeyboardView_isPhone, false);
        mDefaultWidth = getDimensionOrFractionForHorizontal(a,
                R.styleable.WnnKeyboardView_keyWidth,
                mDisplayWidth, mDisplayWidth / 10, mScaleWidth);
        mDefaultHeight = getDimensionOrFraction(a,
                R.styleable.WnnKeyboardView_keyHeight,
                mDisplayHeight, 75);

        //Key height get SharedPreference
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        mDefaultHorizontalGap = getDimensionOrFractionForHorizontal(a,
                R.styleable.WnnKeyboardView_horizontalGap,
                mDisplayWidth, 0, mScaleWidth);
        mDefaultVerticalGap = getDimensionOrFraction(a,
                R.styleable.WnnKeyboardView_verticalGap,
                mDisplayHeight, 0);

        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        boolean isLangPack = langPack.isValid();
        int Keyline = a.getInt(R.styleable.WnnKeyboardView_keyline, KEY_LINE_DEFAULT_KEYBOARD);
        if (isLangPack) {
            Resources mainRes = mContext.getResources();
            mDefaultHeight = mainRes.getDimensionPixelSize(R.dimen.key_height);
            switch (mXmlLayoutResId) {
            case KeyboardLanguagePackData.READ_RESOURCE_TAG_PHONE:
                mDefaultVerticalGap = mainRes.getDimensionPixelSize(R.dimen.keyboard_12key_common_vertical_gap);
                break;
            case KeyboardLanguagePackData.READ_RESOURCE_TAG_4KEY:
            case KeyboardLanguagePackData.READ_RESOURCE_TAG_POPUP:
                mDefaultVerticalGap = 0;
                break;
            default:
                mDefaultHeight = mainRes.getDimensionPixelSize(R.dimen.key_height_qwerty);
                mDefaultVerticalGap = mainRes.getDimensionPixelSize(R.dimen.keyboard_qwerty_common_vertical_gap);
                if (Keyline == KEY_LINE_5_KEYBOARD) {
                    mDefaultVerticalGap = mainRes.getDimensionPixelSize(R.dimen.keyboard_qwerty_common_5line_vertical_gap);
                }
                break;
            }
        }

        boolean mOneHandedShown  = false;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isNotExpandedFloatingMode();
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;

        if (keyboard != null) {
            mOneHandedShown = ((DefaultSoftKeyboard)keyboard).isOneHandedMode();
        }
        if ((mOneHandedShown || isFloatingShown) && (mXmlLayoutResId != R.xml.popup_mail_kddi)
                            && (mXmlLayoutResId != R.xml.popup_url)
                            && (mXmlLayoutResId != R.xml.popup_period_en)
                            && (mXmlLayoutResId != R.xml.popup_period_en_full)
                            && (mXmlLayoutResId != R.xml.popup_period_ja)
                            && (mXmlLayoutResId != R.xml.popup_period_ja_full)
                            && (mXmlLayoutResId != R.xml.keyboard_4key)
                            && (mXmlLayoutResId != R.xml.keyboard_popup_num_symbol_template)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_ja)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_en)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_ja_en)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_ko)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_ja_ko)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_en_ko)
                            && (mXmlLayoutResId != R.xml.popup_changelanguage_ja_en_ko)) {
            Resources mainRes = mContext.getResources();
            mDefaultHeight = mainRes.getDimensionPixelSize(R.dimen.key_height_one_handed);
            mDefaultVerticalGap = mainRes.getDimensionPixelSize(R.dimen.keyboard_one_handed_vertical_gap);
        }

        if ((mXmlLayoutResId != R.xml.popup_mail_kddi)
                && (mXmlLayoutResId != R.xml.popup_url)
                && (mXmlLayoutResId != R.xml.popup_period_en)
                && (mXmlLayoutResId != R.xml.popup_period_en_full)
                && (mXmlLayoutResId != R.xml.popup_period_ja)
                && (mXmlLayoutResId != R.xml.popup_period_ja_full)) {
            if (mXmlLayoutResId == R.xml.keyboard_4key) {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT
                        && pref.contains(KEY_HEIGHT_PORTRAIT_SYMBOL_KEY)) {
                    mDefaultHeight = pref.getInt(KEY_HEIGHT_PORTRAIT_SYMBOL_KEY, mDefaultHeight);
                } else if (mOrientation == Configuration.ORIENTATION_LANDSCAPE
                        && pref.contains(KEY_HEIGHT_LANDSCAPE_SYMBOL_KEY)) {
                    mDefaultHeight = pref.getInt(KEY_HEIGHT_LANDSCAPE_SYMBOL_KEY, mDefaultHeight);
                }
            } else {
                if( mOrientation == Configuration.ORIENTATION_PORTRAIT
                        && pref.contains(KEY_HEIGHT_PORTRAIT_KEY)){
                    mDefaultHeight = pref.getInt(KEY_HEIGHT_PORTRAIT_KEY, mDefaultHeight);
                }else if( mOrientation == Configuration.ORIENTATION_LANDSCAPE
                                && pref.contains(KEY_HEIGHT_LANDSCAPE_KEY)){
                    mDefaultHeight = pref.getInt(KEY_HEIGHT_LANDSCAPE_KEY, mDefaultHeight);
                }
            }
        }

        float floatDefaultHeight = 0.0f;
        if (Keyline == KEY_LINE_5_KEYBOARD) {
            floatDefaultHeight = (float)(mDefaultHeight * (Keyline - 1) ) / (float)Keyline;
            mDefaultHeight = (int)floatDefaultHeight;
            floatDefaultHeight = floatDefaultHeight - (float)(mDefaultVerticalGap * (Keyline - 1)) / (float)Keyline;
        }
        if (Keyline == KEY_LINE_5_KEYBOARD) {
            float heightCorrection = (floatDefaultHeight - (float)mDefaultHeight) * (float)KEY_LINE_5_KEYBOARD;
            BigDecimal bi = new BigDecimal(heightCorrection);
            mKeyHeightCorrection = bi.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        mProximityThreshold = (int) (mDefaultWidth * SEARCH_DISTANCE);
        mProximityThreshold = mProximityThreshold * mProximityThreshold; // Square it for comparison
        a.recycle();
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        TypedValue value = a.peekValue(index);
        if (value == null) return defValue;
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return a.getDimensionPixelSize(index, defValue);
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // Round it to avoid values like 47.9999 from getting truncated
            return Math.round(a.getFraction(index, base, base, defValue));
        }
        return defValue;
    }

    static int getDimensionOrFractionForHorizontal(TypedArray a, int index, int base, int defValue,
            float scale) {
        TypedValue value = a.peekValue(index);
        if (value == null) return defValue;
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return (int)(a.getDimensionPixelOffset(index, defValue) * scale);
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // Round it to avoid values like 47.9999 from getting truncated
            return Math.round(a.getFraction(index, base, base, defValue));
        }
        return defValue;
    }
}
