/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.hangul;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.preference.PreferenceManager;

import jp.co.omronsoft.iwnnime.ml.BaseInputView;
import jp.co.omronsoft.iwnnime.ml.ControlPanelStandard;
import jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard;
import jp.co.omronsoft.iwnnime.ml.Keyboard;
import jp.co.omronsoft.iwnnime.ml.Keyboard.Key;
import jp.co.omronsoft.iwnnime.ml.KeyboardThemeActivity;
import jp.co.omronsoft.iwnnime.ml.MultiTouchKeyboardView;
import jp.co.omronsoft.iwnnime.ml.OneTouchEmojiListViewManager;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.OpenWnnEvent;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsEdit;
import jp.co.omronsoft.iwnnime.ml.WnnKeyboardFactory;
import jp.co.omronsoft.iwnnime.ml.jajp.IWnnImeJaJp;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import java.util.List;
import java.util.Locale;

import com.nttdocomo.android.voiceeditor.voiceeditor.VoiceEditorClient;


/**
 * Software Keyboard class for Chinese IME (base)
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public abstract class DefaultSoftKeyboardHangul extends DefaultSoftKeyboard {
    /** Toggle cycle table for STOP */
    private static final String[] CYCLE_TABLE_STOP = {
        "\u3002", "\u3001"
    };

    /** Toggle cycle table for COMMA */
    private static final String[] CYCLE_TABLE_COMMA = {
        ".", ","
    };

    /** Toggle cycle table for EXCLAMATION */
    private static final String[] CYCLE_TABLE_EXCLAMATION = {
        "?", "!"
    };

    /** Toggle cycle table for INVERT_EXCLAMATION */
    private static final String[] CYCLE_TABLE_INVERT_EXCLAMATION = {
        "\u00bf", "\u00a1"
    };

    /** Input mode number table */
    private static final int[] KO_MODE_NUMBER_TABLE = {KEYMODE_KO_NUMBER};

    /** Input mode phone table */
    private static final int[] KO_MODE_PHONE_TABLE = {KEYMODE_KO_PHONE};

    /* for Qwerty keyboard */
    /** Chinese Qwerty keyboard [TOGGLE STOP] */
    public static final int KEYCODE_TOGGLE_STOP         = -150;

    /** Key code for switching to pinyin mode */
    protected static final int KEYCODE_SWITCH_PINYIN = -301;

    /** Key code for switching to bopomofo mode */
    protected static final int KEYCODE_SWITCH_BOPOMOFO = -302;

    /** Key code for NOP (no-operation) */
    private static final int KEYCODE_NOP = -310;

    /** Key code for starting voiceinput */
    public static final int KEYCODE_FUNCTION_VOICEINPUT = -601;

    /** Key code for starting keyboardtype*/
    public static final int KEYCODE_FUNCTION_KEYBOARDTYPE = -602;

    /** Key code for starting handwriting */
    public static final int KEYCODE_FUNCTION_HANDWRITING = -603;

    /** Key code for starting settings */
    public static final int KEYCODE_FUNCTION_SETTINGS = -604;

    /** Key code for starting function onehanded */
    public static final int KEYCODE_FUNCTION_ONEHANDED = -605;

    /** Key code for starting function split */
    public static final int KEYCODE_FUNCTION_SPLIT = -606;

    /** Key code for starting function normalkeyboard */
    public static final int KEYCODE_FUNCTION_NORMALKEYBOARD = -607;

    /** Key code for starting function qliptray */
    public static final int KEYCODE_FUNCTION_CLIPTRAY = -608;

    /** The constant for KeyMode. It means that input mode is invalid. */
    private static final int INVALID_KEYMODE = -1;

    /** Contextual menu position for settings */
    private static final int POS_SETTINGS = 0;

    /** Contextual menu position for methods */
    private static final int POS_METHOD = 1;

    /** Contextual menu position for mushroom */
    private static final int POS_MUSHROOM = 2;

    /** Contextual menu position for keyboard theme */
    private static final int POS_KEYBOARDTHEME = 6;

    /** Docomo VoiceEditor Package Name */
    private static final String DOCOMO_VOICEEDITOR_PACKAGE_NAME =
            "com.nttdocomo.android.voiceeditor";

    /** Hand writing(IME) Package Name */
    private static final String WRITING_PACKAGE_NAME =
             "com.sevenknowledge.sevennotestabproduct.lg001.tab50";

    /** Hand writing(IME) Service Name */
    private static final String HANDWRITING_SERVICE_ID =
            "com.sevenknowledge.sevennotestabproduct.lg001.tab50/com.sevenknowledge.mazec.MazecIms";

    /** Keyboard width scale */
    private static final float KEYBOARD_SCALE_WIDTH_DEFAULT = 1.0f;

    /** Flag to call DOCOMO voice editor app,
        this value is changed to false in the build script of KDDI */
    private static final boolean ENABLE_DOCOMO = true;

    /** Function index */
    private static final int FunctionKey_FUNC_VOICEINPUT  = 0;
    private static final int FunctionKey_FUNC_HANDWRITING = 2;

    //////////////////////////////////// property
    /**
     * Input mode that is not able to be changed.
     * <br>
     * If ENABLE_CHANGE_KEYMODE is set, input mode can be changed.
     */
    private int[] mLimitedKeyMode = null;

    /**
     * Input mode that is given the first priority.
     * <br>
     * If ENABLE_CHANGE_KEYMODE is set, input mode can be changed.
     */
    private int mPreferenceKeyMode = INVALID_KEYMODE;

    /** The text of undo-key (for undo) */
    private String  mUndoKey = null;

    /** The keyboard of inputted (for undo) */
    private Keyboard  mInputKeyBoard = null;

    /** The keyboard of not inputted (for undo) */
    private Keyboard  mNoInputKeyBoard = null;

    /** keyboard type (for undo) */
    private int mUndoKeyMode = -1;

    /** The original icon preview  (for undo) */
    private Drawable mInputIconUndo = null;

    /** The original icon preview of not inputted  (for undo) */
    private Drawable mNoInputIconUndo = null;

    /** Whether this IME can undo (for undo) */
    private boolean mCanUndo = false;

    /** Whether prediction is active or not. */
    private boolean mPrediction = true;

    /** Whether the InputType is null */
    private boolean mIsInputTypeNull = false;

    /** Whether being able to use Emoji */
    private boolean mEnableEmoji = false;

    /** Whether being able to use DecoEmoji */
    private boolean mEnableDecoEmoji = false;

    /** Whether being able to use Symbol */
    private boolean mEnableSymbol = false;

    /** The menu item */
    private int[] mMenuItem;

    /** View objects (one touch emoji list) */
    protected OneTouchEmojiListViewManager mOneTouchEmojiListViewManager
            = new OneTouchEmojiListViewManager();

    /** One touch emoji list width */
    protected int mOneTouchEmojiListWidth = 0;

    /** Input mode before toggle to voice input mode. */
    private int mKeyModeBefToggleToVoice = KEYMODE_KO_HANGUL;

    //////////////////////////////////// method

    /** Default constructor */
    public DefaultSoftKeyboardHangul(IWnnLanguageSwitcher wnn) {
        super(wnn);
        mCurrentKeyMode      = KEYMODE_KO_HANGUL;
   }

    /**
     * Commit the pre-edit string for committing operation that is not explicit
     * (ex. when a candidate is selected)
     */
    private void commitText() {
        if (!mNoInput) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.COMMIT_COMPOSING_TEXT));
        }
    }

    /**
     * Change input mode
     * <br>
     * @param keyMode   The type of input mode
     */
    public void changeKeyMode(int keyMode) {
        int targetMode = keyMode;

        if (WnnKeyboardFactory.getSplitMode()
                && ONETOUCHEMOJILIST_RIGHT.equals(mOneTouchEmojiListMode)) {
            mOneTouchEmojiListMode = ONETOUCHEMOJILIST_LEFT;
        }

        if (mKeyboardView != null) {
            mKeyboardView.setShifted(false);
            ((MultiTouchKeyboardView) mKeyboardView).setCapsLock(false);
        }
        mShiftOn = KEYBOARD_SHIFT_OFF;
        mCapsLock = false;
        Keyboard kbd = getModeChangeKeyboard(targetMode);
        mCurrentKeyMode = targetMode;

        int mode = OpenWnnEvent.Mode.DIRECT;

        if(mPrediction) {
            mode = OpenWnnEvent.Mode.DEFAULT;
        } else {
            if (targetMode == getDefaultKeymode()) {
                mode = OpenWnnEvent.Mode.DEFAULT;
            } else {
                commitText();
                mode = OpenWnnEvent.Mode.DIRECT;
            }
        }

        switch (targetMode) {
        case KEYMODE_JA_VOICE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.VOICE_INPUT));
            return;

        default:
            break;
        }

        setStatusIcon();
        changeKeyboard(kbd);
        setUndoKeyMode(mCanUndo);
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, mode));
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#initView */
    @Override public View initView(OpenWnn parent, int width, int height) {
        if (OpenWnn.isDebugging()) { Log.d("OpenWnn", "DefaultSoftKeyboardHangul.initView()" + (mKeyboardView != null)); }

        View view = super.initView(parent, width, height);

        mOneTouchEmojiListViewManager.initView(parent, this);
        changeKeyboardType(KEYBOARD_QWERTY);
        mUndoKey = mWnn.getResources().getString(R.string.ti_key_12key_undo_txt);

        WnnKeyboardFactory keyboard = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][mCurrentKeyInputType][0];
        if (keyboard != null) {
            if (OpenWnn.getCurrentIme().isKeepInput()) {
                mCurrentKeyboard = keyboard.getKeyboard();
                if (mIsSymbolKeyboard) {
                    setSymbolKeyboard();
                } else {
                    setNormalKeyboard();
                    setShifted(mShiftOn);
                    if ((mKeyboardView != null) && (mKeyboardView instanceof MultiTouchKeyboardView)) {
                        MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
                        if (mCapsLock) {
                            keyboardView.setShifted(true);
                            keyboardView.setCapsLock(true);
                        }
                    }
                }
            } else {
                mCurrentKeyboard = null;
                changeKeyMode(mCurrentKeyMode);
            }
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "DefaultSoftKeyboardHangul: width="+width+", height="+height+", kbdView="+mKeyboardView);}
        return view;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setPreferences */
    @Override public void setPreferences(SharedPreferences pref, EditorInfo editor) {
        if (!OpenWnn.getCurrentIme().isKeepInput() && (mKeyboardView != null)) {
            mKeyboardView.setShifted(false);
            ((MultiTouchKeyboardView) mKeyboardView).setCapsLock(false);
        }

        //#ifdef EMOJI-DOCOMO
        mOneTouchEmojiListViewManager.setPreferences(pref);
        if (mHardKeyboardHidden) {
            String oldOneTouchEmojiListMode = mOneTouchEmojiListMode;
            mOneTouchEmojiListMode = pref.getString("opt_one_touch_emoji_list",
                    ONETOUCHEMOJILIST_HIDDEN);
            if (!mOneTouchEmojiListMode.equals(oldOneTouchEmojiListMode)) {
                createKeyboards(mWnn);
                mCurrentKeyboard = null;
                changeKeyMode(mCurrentKeyMode);
                ((IWnnLanguageSwitcher)mWnn).clearInitializedFlag();
            }
        } else
        //#endif /* EMOJI-DOCOMO */
        {
            mOneTouchEmojiListMode = ONETOUCHEMOJILIST_HIDDEN;
        }

        super.setPreferences(pref, editor);

        if (OpenWnn.getCurrentIme().isKeepInput()) {
            return;
        }

        int inputType = editor.inputType;

        mLimitedKeyMode = null;
        mPreferenceKeyMode = INVALID_KEYMODE;
        mNoInput = true;
        mDisableKeyInput = false;

        mPrediction = pref.getBoolean("opt_prediction", true);
        mIsInputTypeNull = false;

        switch (inputType & EditorInfo.TYPE_MASK_CLASS) {

        case EditorInfo.TYPE_CLASS_NUMBER:
            if (EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD == (inputType & EditorInfo.TYPE_MASK_VARIATION)) {
                disableVoiceInput();
            }
            break;

        case EditorInfo.TYPE_CLASS_DATETIME:
            break;

        case EditorInfo.TYPE_CLASS_PHONE:
            if (mHardKeyboardHidden) {
                mLimitedKeyMode = getModePhoneTable();
            } else {
                mLimitedKeyMode = getModeDefaultTable();
            }
            break;

        case EditorInfo.TYPE_CLASS_TEXT:
            switch (inputType & EditorInfo.TYPE_MASK_VARIATION) {

            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                mLimitedKeyMode = getModeCycleTable();
                disableVoiceInput();
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                disableVoiceInput();
                break;

            default:
                break;

            }
            break;

        default:
            if (inputType == EditorInfo.TYPE_NULL) {
                mIsInputTypeNull = true;
                disableVoiceInput();
            }
            break;
        }

        forceCloseVoiceInputKeyboard();

        if (mKeyboardView != null) {
            mKeyboardView.setShifted(false);
            ((MultiTouchKeyboardView) mKeyboardView).setCapsLock(false);
        }
        mCapsLock = false;

        if (inputType != mLastInputType) {
            setDefaultKeyboard();
            mLastInputType = inputType;
        }

        setStatusIcon();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#changeKeyboardType */
    @Override public void changeKeyboardType(int type) {
        Keyboard kbd = getTypeChangeKeyboard(type);
        if (kbd != null) {
            mCurrentKeyboardType = type;
            changeKeyboard(kbd);
            setKeyMode();
            setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol);
        }

        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_QWERTY));
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#onKey */
    @Override public void onKey(int primaryCode, int[] keyCodes) {
        super.onKey(primaryCode,keyCodes);
        if (mIsKeyProcessFinish) {
            return;
        }

        Context context = OpenWnn.superGetContext();
        int oldCurrentKeyMode = mCurrentKeyMode;

        switch (primaryCode) {

        case KEYCODE_QWERTY_JP_KEYBOARD:
            ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ja");
            break;

        case KEYCODE_QWERTY_TOGGLE_MODE:
            nextKeyMode();
            break;

        case KEYCODE_TOGGLE_TW_PINYIN:
            changeKeyMode(getDefaultKeymode());
            break;

        case KEYCODE_TOGGLE_TW_ALPHABET:
            this.changeKeyMode(getAlphabetKeymode());
            break;

        case KEYCODE_QWERTY_BACKSPACE:
        case KEYCODE_JP12_BACKSPACE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
            break;

        case KEYCODE_QWERTY_SHIFT:
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
                if (keyboardView != null) {
                    if (getSoftLockEnabled()) {
                        if (keyboardView.isShifted()) {
                            if (keyboardView.isCapsLock()) {
                                keyboardView.setShifted(false);
                                keyboardView.setCapsLock(false);
                                mCapsLock = false;
                                mShiftOn = KEYBOARD_SHIFT_OFF;
                            } else {
                                keyboardView.setCapsLock(true);
                                mCapsLock = true;
                            }
                        } else {
                            keyboardView.setShifted(true);
                            mShiftOn = KEYBOARD_SHIFT_ON;
                        }
                    } else {
                        if (keyboardView.isShifted()) {
                            keyboardView.setShifted(false);
                            keyboardView.setCapsLock(false);
                            mCapsLock = false;
                        } else {
                            keyboardView.setShifted(true);
                            keyboardView.setCapsLock(true);
                            mCapsLock = true;
                        }
                    }
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_INPUT_CANCEL));
                }
            }
            break;

        case KEYCODE_QWERTY_ENTER:
        case KEYCODE_JP12_ENTER:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
            break;

        case KEYCODE_QWERTY_EMOJI:
            commitText();
            if (!ENABLE_DOCOMO){
                if (oldCurrentKeyMode == KEYMODE_JA_VOICE) {
                    changeKeyMode(KEYMODE_KO_HANGUL);    
                } else {
                    changeKeyMode(oldCurrentKeyMode);
                }
            }
            if (mEnableEmoji) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeHangul.ENGINE_MODE_SYMBOL_EMOJI));
            } else {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeHangul.ENGINE_MODE_SYMBOL_SYMBOL));
            }
            break;

        case KEYCODE_QWERTY_HAN_ALPHA:
            /* Change mode to Half width alphabet */
            this.changeKeyMode(getAlphabetKeymode());
            break;

        case KEYCODE_QWERTY_HAN_NUM:
            /* Change mode to Half width numeric */
            changeKeyMode(getNumberKeymode());
            break;

        case KEYCODE_JP12_LEFT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_LEFT)));
            break;

        case KEYCODE_JP12_RIGHT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_RIGHT)));
            break;

        case KEYCODE_TOGGLE_STOP:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_STOP));
            break;

        case KEYCODE_TOGGLE_COMMA:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_COMMA));
            break;

        case KEYCODE_TOGGLE_EXCLAMATION:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_EXCLAMATION));
            break;

        case KEYCODE_TOGGLE_INVERTED_EXCLAMATION:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_INVERT_EXCLAMATION));
            break;

        case KEYCODE_NOP:
            break;

        case KEYCODE_JP12_REVERSE:
            if ( (mKeyboardView.getKeyboard().getKeys().get(0).label != null)
                    && (mKeyboardView.getKeyboard().getKeys().get(0).label.equals(mUndoKey) ) ) {
                      mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.UNDO));
                  }
            break;

        case KEYCODE_START_VOICE_INPUT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.VOICE_INPUT));
            break;

        case KEYCODE_FUNCTION_VOICEINPUT:
            if (ENABLE_DOCOMO) {
                switchToVoiceEditorIME();
            } else {
                mKeyModeBefToggleToVoice = mCurrentKeyMode;
                changeKeyMode(KEYMODE_JA_VOICE);
            }
            break;

        case KEYCODE_FUNCTION_KEY:
            int functionIndex = FunctionKey_FUNC_VOICEINPUT;
            if(!mKeyboardView.isFunctionEnable(FunctionKey_FUNC_VOICEINPUT)){
                functionIndex = FunctionKey_FUNC_HANDWRITING;
            }
            switch(functionIndex){
                case FunctionKey_FUNC_VOICEINPUT:
                    if (ENABLE_DOCOMO) {
                        switchToVoiceEditorIME();
                    } else {
                        mKeyModeBefToggleToVoice = mCurrentKeyMode;
                        changeKeyMode(KEYMODE_JA_VOICE);
                    }
                    break;
                case FunctionKey_FUNC_HANDWRITING:
                    ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ja");
                    ((IWnnLanguageSwitcher)mWnn).setHangulFlagForHw(true);
                    OpenWnn.mOriginalInputMethodSwitcher.changeInputMethod(0);
                    break;
            }
            break;

        case KEYCODE_FUNCTION_SETTINGS:
            showLongPressMenu();
            break;

        case KEYCODE_FUNCTION_HANDWRITING:
            ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ja");
            ((IWnnLanguageSwitcher)mWnn).setHangulFlagForHw(true);
            OpenWnn.mOriginalInputMethodSwitcher.changeInputMethod(0);
            break;

        case KEYCODE_FUNCTION_NORMALKEYBOARD:
            Resources res = context.getResources();
            if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setEnableFunctionOneHanded(false);
            } else {
                setEnableFunctionSplit(false);
            }
            ((IWnnLanguageSwitcher)mWnn).clearInitializedFlag();
            ((IWnnLanguageSwitcher)mWnn).onStartInputView(mWnn.getCurrentInputEditorInfo(),false);
            changeKeyMode(oldCurrentKeyMode);
            break;
        case KEYCODE_FUNCTION_ONEHANDED:
            setEnableFunctionOneHanded(true);
            ((IWnnLanguageSwitcher)mWnn).onStartInputView(mWnn.getCurrentInputEditorInfo(),false);
            changeKeyMode(oldCurrentKeyMode);
            break;

        case KEYCODE_FUNCTION_SPLIT:
            setEnableFunctionSplit(true);
            ((IWnnLanguageSwitcher)mWnn).clearInitializedFlag();
            ((IWnnLanguageSwitcher)mWnn).onStartInputView(mWnn.getCurrentInputEditorInfo(),false);
            changeKeyMode(oldCurrentKeyMode);
            break;

        case KEYCODE_FUNCTION_CLIPTRAY:
            commitText();
            showClipTray();
            break;

        default:
            if (primaryCode >= 0) {
                if (mKeyboardView.isShifted()) {
                    primaryCode = Character.toUpperCase(primaryCode);
                }
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, (char)primaryCode));
            }
            break;
        }
    }
    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#closing */
    @Override public void closing() {
        super.closing();
        mOneTouchEmojiListViewManager.close();
    }

    /**
     * Get the current Locale.
     *
     * @return The current locale.
     */
    protected abstract Locale getCurrentLocal();

    /**
     * Get the mode cycle table.
     *
     * @return The mode cycle table.
     */
    protected abstract int[] getModeCycleTable();

    /**
     * Get the mode phone table.
     *
     * @return The mode phone table.
     */
    protected int[] getModePhoneTable() {
        return KO_MODE_PHONE_TABLE;
    }

    /**
     * Get the mode default table.
     *
     * @return The mode default table.
     */
    protected abstract int[] getModeDefaultTable();

    /**
     * Get the allow toggle table.
     *
     * @return The allow toggle table.
     */
    protected abstract boolean[] getAllowToggleTable();

    /**
     * Get the default key-mode.
     *
     * @return The index of the default key-mode.
     *           (default key-mode : KEYMODE_XX_XXX in {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard})
     */
    protected abstract int getDefaultKeymode();

    /**
     * Get the alphabet key-mode.
     *
     * @return The index of the alphabet key-mode.
     *           (alphabet key-mode : KEYMODE_XX_ALPHABET in {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard})
     */
    protected abstract int getAlphabetKeymode();

    /**
     * Get the number key-mode.
     *
     * @return The index of the number key-mode.
     *           (number key-mode : KEYMODE_XX_NUMBER in {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard})
     */
    protected int getNumberKeymode() {
        return KEYMODE_KO_NUMBER;
    }

    /**
     * Get the phone key-mode.
     *
     * @return The index of the phone key-mode.
     *           (phone key-mode : KEYMODE_XX_PHONE in {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard})
     */
    protected int getPhoneKeymode() {
        return KEYMODE_KO_PHONE;
    }

    /**
     * Set the status icon that is appropriate in the current mode.
     */
    protected void setStatusIcon() {
        switch (mCurrentKeyMode) {
            case KEYMODE_KO_ALPHABET:
                mTextView.setText(R.string.ti_key_12key_switch_half_alphabet_txt);
                break;
            default:
                break;
        }
        mWnn.showStatusIcon(0);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setHardKeyboardHidden */
    @Override public void setHardKeyboardHidden(boolean hidden) {
        if (mWnn != null) {
            if (!hidden) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              IWnnImeHangul.ENGINE_MODE_OPT_TYPE_QWERTY));
            }

            if (mHardKeyboardHidden != hidden) {
                if ((mLimitedKeyMode != null)
                    || (mCurrentKeyMode != getDefaultKeymode())) {

                    mLastInputType = EditorInfo.TYPE_NULL;
                    if (mWnn.isInputViewShown()) {
                        setDefaultKeyboard();
                    }
                }

                if (hidden) {
                    mLastInputType = -1;
                }
            }
        }
        super.setHardKeyboardHidden(hidden);
    }

    /**
     * Sets the undo-key.
     *
     * @param undo {@code true}: the undo-key will be enabled; {@code false}: disable
     */
    public void setUndoKeyMode(boolean undo) {
        boolean hasChanged = false;
        mCanUndo = undo;

        if (mIsSymbolKeyboard) {
            return;
        }

        Keyboard.Key k;
        if (mInputKeyBoard != null) {
            if (mInputKeyBoard.getKeys().get(0).label != null) {
                hasChanged = true;
            }
            mInputKeyBoard.getKeys().get(0).label = null;
            mInputKeyBoard.getKeys().get(0).icon = mInputIconUndo;
            mNoInputKeyBoard.getKeys().get(0).label = null;
            mNoInputKeyBoard.getKeys().get(0).icon = mNoInputIconUndo;
        }

        if (isVoiceInputMode()) {
            if (undo) {
                hasChanged = true;
                mInputKeyBoard = getKeyboardInputted(true);
                mNoInputKeyBoard = getKeyboardInputted(false);
                if (mInputKeyBoard != null) {
                    k = mInputKeyBoard.getKeys().get(0);
                    k.label = mUndoKey;
                    mInputIconUndo = k.icon;
                    k.icon = null;

                    if (mInputKeyBoard == mNoInputKeyBoard) {
                        mNoInputIconUndo = mInputIconUndo;
                    } else {
                        k = mNoInputKeyBoard.getKeys().get(0);
                        k.label = mUndoKey;
                        mNoInputIconUndo = k.icon;
                        k.icon = null;
                    }
                }
            }
            if (mKeyboardView != null) {
                if ((mCurrentKeyboard != null) && (hasChanged)) {
                    mKeyboardView.setKeyboard(mCurrentKeyboard);
                }
            }
        }

        if (!undo) {
            mInputKeyBoard = null;
            mNoInputKeyBoard = null;
            mUndoKeyMode = mCurrentKeyMode;
        }
    }

    /**
     * Undo key-mode.
     */
    public void undoKeyMode() {
        if (mUndoKeyMode != mCurrentKeyMode) {
            changeKeyMode(mUndoKeyMode);
        }
    }

    /**
     * Change the keyboard to default.
     */
    public void setDefaultKeyboard() {
        int keymode = getDefaultKeymode();

        if (mPreferenceKeyMode != INVALID_KEYMODE) {
            keymode = mPreferenceKeyMode;
        } else if (mLimitedKeyMode != null) {
            keymode = mLimitedKeyMode[0];
        } // else {}
        changeKeyMode(keymode);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#onUpdateState */
    @Override public void onUpdateState(OpenWnn parent) {
        super.onUpdateState(parent);
        if ((mKeyboardView != null) && !OpenWnn.getCurrentIme().isKeepInput()) {
            mCapsLock = ((MultiTouchKeyboardView) mKeyboardView).isCapsLock();
            if (!mCapsLock && mKeyboardView.isShifted()) {
                mKeyboardView.setShifted(false);
            }
        }
    }

    /** @see android.inputmethodservice.KeyboardView.OnFlickKeyboardActionListener#onLongPress */
    @Override public boolean onLongPress(Keyboard.Key key) {
        if (key.codes[0] == KEYCODE_QWERTY_SHIFT
                && getSoftLockEnabled() && !OpenWnn.isTabletMode()) {
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;

                if (!keyboardView.isCapsLock()) {
                    mCapsLock = true;
                    mShiftOn = KEYBOARD_SHIFT_ON;
                    keyboardView.setShifted(true);
                    keyboardView.setCapsLock(true);
                } else {
                    mCapsLock = false;
                    mShiftOn = KEYBOARD_SHIFT_OFF;
                    keyboardView.setShifted(false);
                    keyboardView.setCapsLock(false);
                }
            }
            return true;
        }
        return super.onLongPress(key);
    }

    /**
     * Get SoftLock Enabled.
     *
     * @return {@code true} if Soft lock enabled.
     */
    protected boolean getSoftLockEnabled() {
        return (mCurrentKeyMode == KEYMODE_KO_HANGUL
                || mCurrentKeyMode == KEYMODE_KO_ALPHABET);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#inputByFlickDirection */
    @Override protected void inputByFlickDirection(int direction, boolean isCommit) {
        if (isEnableFlickMode(mFlickPressKey)) {
            switch (direction) {
            case FLICK_DIRECTION_SLIDE1:
                inputByFlick(FLICK_DIRECTION_SLIDE1_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE2:
                inputByFlick(FLICK_DIRECTION_SLIDE2_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE3:
                inputByFlick(FLICK_DIRECTION_SLIDE3_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE4:
                inputByFlick(FLICK_DIRECTION_SLIDE4_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE_NEUTRAL:
                inputByFlick(FLICK_DIRECTION_SLIDE_NEUTRAL_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE_NORMAL:
                inputByFlick(FLICK_DIRECTION_SLIDE_NORMAL_INDEX, isCommit);
                break;

            default:
                break;

            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#inputByFlick */
    @Override protected void inputByFlick(int directionIndex, boolean isCommit) {
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#isEnableFlickMode */
    @Override protected boolean isEnableFlickMode(int key){
        return isFlickKey(key);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#getModeCycleCount */
    @Override protected int getModeCycleCount() {
        boolean[] toggle = getAllowToggleTable();
        int cnt = 0;
        int size = toggle.length;

        for (int i = 0; i < size; i++) {
            if (toggle[i]) {
                cnt++;
            }
        }
        return cnt;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#isFlickKey */
    @Override protected boolean isFlickKey(int key) {
        boolean ret;

        switch(key) {
        case KEYCODE_QWERTY_TOGGLE_MODE:
            ret = true;
            break;

        default:
            ret = false;
            break;

        }
        return ret;
    }

    /**
     * Get toggle keymode.
     *
     * @param count    The toggle count
     * @return         The keymode
     */
    protected int getToggleKeyMode(int count) {
        boolean[] table = getAllowToggleTable();
        int length = table.length;
        int enablecnt  = 0;
        int defaultMode = getDefaultKeymode();
        int mode = 0;
        for (int i = 0; i < length; i++) {
            mode = (defaultMode + i) % length;
            if (table[mode]) {
                enablecnt++;
                if (enablecnt >= count) {
                    break;
                }
            }
        }
        return mode;
    }

    /**
     * Change the key-mode to the next input mode.
     */
    public void nextKeyMode() {
        if(getSoftLockEnabled() && mShiftOn == KEYBOARD_SHIFT_ON && !mCapsLock) {
            mShiftOn = KEYBOARD_SHIFT_OFF;
        }

        boolean[] table = getAllowToggleTable();
        int length = table.length;
        int current = mCurrentKeyMode;
        int next = 0;
        for (int i = 1; i < length; i++) {
            next = (current + i) % length;
            if (mIsInputTypeNull) {
                if ((next == getAlphabetKeymode())
                        || (next == getNumberKeymode())) {
                    break;
                }
            } else if (table[next]) {
                break;
            } // else {}
        }
        changeKeyMode(next);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#isVoiceInputMode */
    @Override protected boolean isVoiceInputMode() {
        return (mCurrentKeyMode == KEYMODE_KO_VOICE);
    }

    /**
     * Set emoji enabled.
     *
     * @param enableEmoji     {@code true}  - Emoji is enabled.
     *                        {@code false} - Emoji is disabled.
     * @param enableDecoEmoji {@code true}  - DecoEmoji is enabled.
     *                        {@code false} - DecoEmoji is disabled.
     * @param enableSymbol    {@code true}  - Symbol is enabled.
     *                        {@code false} - Symbol is disabled.
     */
    public void setEnableEmojiSymbol(boolean enableEmoji, boolean enableDecoEmoji, boolean enableSymbol) {
        mMainLayout.removeView(mOneTouchEmojiListViewManager.getView());

        float keyboardWidthScale = KEYBOARD_SCALE_WIDTH_DEFAULT;
        int columnNum = mOneTouchEmojiListViewManager.get1stViewColumnNum(mDisplayMode);
        int oneTouchEmojiListPaddingLeft = 0;
        int oneTouchEmojiListPaddingRight = 0;
        int mOneHandedViewWidth = 0;

        DisplayMetrics dm = mWnn.getResources().getDisplayMetrics();

        boolean isSplitCurrent = isSplitMode();
        boolean isOneHandedCurrent =  isOneHandedMode();

        if (mKeyboardView != null) {
            int leftPadding = 0;
            if (isSplitCurrent) {
                if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                    leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key_split;
                } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                    leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_qwerty_split;
                } // else {}
            } else if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key_default;
            } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_qwerty_default;
            } // else {}
            mKeyboardView.setPopupOffset(mWnn.getResources().getDimensionPixelSize(leftPadding), 0);
            mKeyboardView.setPadding(mWnn.getResources().getDimensionPixelSize(leftPadding),
                    mKeyboardView.getPaddingTop(), 0, mKeyboardView.getPaddingBottom());
        }

        if (!isOneHandedCurrent) {
            if (isSplitCurrent) {
                oneTouchEmojiListPaddingLeft = (mWnn.getResources().getDimensionPixelSize(
                        R.dimen.one_touch_emoji_list_padding_keyboard_left_split));
                oneTouchEmojiListPaddingRight = (mWnn.getResources().getDimensionPixelSize(
                        R.dimen.one_touch_emoji_list_padding_keyboard_right_split));
            } else {
                oneTouchEmojiListPaddingLeft = (mWnn.getResources().getDimensionPixelSize(
                        R.dimen.one_touch_emoji_list_padding_keyboard_left));
                oneTouchEmojiListPaddingRight = (mWnn.getResources().getDimensionPixelSize(
                        R.dimen.one_touch_emoji_list_padding_keyboard_right));
            }

            hideOneHandedView();
        } else {

            if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_RIGHT)
                    || mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_RIGHT)) {
                if (ONETOUCHEMOJILIST_LEFT.equals(mOneTouchEmojiListMode)) {
                    oneTouchEmojiListPaddingLeft = (mWnn.getResources().getDimensionPixelSize(
                            R.dimen.one_touch_emoji_list_padding_keyboard_left_right_one_handed));
                    oneTouchEmojiListPaddingRight = 0;
                } else {
                    oneTouchEmojiListPaddingLeft = 0;
                    oneTouchEmojiListPaddingRight = (mWnn.getResources().getDimensionPixelSize(
                            R.dimen.one_touch_emoji_list_padding_keyboard_left_right));
                }
            } else if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_LEFT)
                    || mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_LEFT)) {
                if (ONETOUCHEMOJILIST_LEFT.equals(mOneTouchEmojiListMode)) {
                    oneTouchEmojiListPaddingLeft = (mWnn.getResources().getDimensionPixelSize(
                            R.dimen.one_touch_emoji_list_padding_keyboard_left_right));
                    oneTouchEmojiListPaddingRight = 0;
                } else {
                    oneTouchEmojiListPaddingLeft = 0;
                    oneTouchEmojiListPaddingRight = (mWnn.getResources().getDimensionPixelSize(
                            R.dimen.one_touch_emoji_list_padding_keyboard_left_right_one_handed));
                }
            }
            showOneHandedView();

            mOneHandedViewWidth = mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_handed_keybord_view_width);
        }

        boolean isSplitPrev = WnnKeyboardFactory.getSplitMode();
        if (isSplitPrev != isSplitCurrent) {
            WnnKeyboardFactory.setSplitMode(isSplitCurrent);
        }

        if (isSplitCurrent) {
            mOneTouchEmojiListWidth = mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width_split_1stview) * columnNum;
        } else if (isOneHandedCurrent) {
            mOneTouchEmojiListWidth = mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width_one_handed) * columnNum;
        } else {
            mOneTouchEmojiListWidth = mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width) * columnNum;
        }

        boolean isShowOneTouchEmojiList = isNormalInputMode()
                && (enableEmoji || enableDecoEmoji || enableSymbol);

        if (isShowOneTouchEmojiList) {
            View oneTouchEmojiList1stView = mOneTouchEmojiListViewManager.getView()
                                            .findViewById(R.id.one_touch_emoji_list_1st_base);

            if (ONETOUCHEMOJILIST_LEFT.equals(mOneTouchEmojiListMode)) {
                mOneTouchEmojiListWidth += oneTouchEmojiListPaddingLeft;

                oneTouchEmojiList1stView.setPadding(oneTouchEmojiListPaddingLeft,
                                                oneTouchEmojiList1stView.getPaddingTop(),
                                                0,
                                                oneTouchEmojiList1stView.getPaddingBottom());

                if (mKeyboardView != null) {
                    int oneTouchEmojiListPaddingKeyboardLeft = 0;

                    if (isSplitCurrent) {
                        if (mCurrentKeyboardType == KEYBOARD_12KEY || isNaviKeyboard()) {
                            oneTouchEmojiListPaddingKeyboardLeft
                                    = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key_split;
                        } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                            oneTouchEmojiListPaddingKeyboardLeft
                                    = R.dimen.one_touch_emoji_list_padding_keyboard_left_qwerty_split;
                        }
                    } else if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                        oneTouchEmojiListPaddingKeyboardLeft
                        = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key;
                    } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                        oneTouchEmojiListPaddingKeyboardLeft
                        = R.dimen.one_touch_emoji_list_padding_keyboard_left_qwerty;
                    } // else

                    mKeyboardView.setPopupOffset(mWnn.getResources().getDimensionPixelSize(
                            oneTouchEmojiListPaddingKeyboardLeft), 0);
                    mKeyboardView.setPadding(mWnn.getResources().getDimensionPixelSize(
                            oneTouchEmojiListPaddingKeyboardLeft),
                            mKeyboardView.getPaddingTop(),
                            mKeyboardView.getPaddingRight(),
                            mKeyboardView.getPaddingBottom());

                    if (isSplitCurrent) {
                        int emojiViewX = (dm.widthPixels - mOneTouchEmojiListWidth) / 2
                                + mWnn.getResources().getDimensionPixelSize(
                                R.dimen.one_touch_emoji_list_padding_left_split);
                        mOneTouchEmojiListViewManager.getView().setX(emojiViewX);
                        mMainLayout.addView(mOneTouchEmojiListViewManager.getView(),
                                new ViewGroup.LayoutParams(mOneTouchEmojiListWidth,
                                        mOneTouchEmojiListViewManager.getView().getLayoutParams().height));
                    } else {
                        mMainLayout.addView(mOneTouchEmojiListViewManager.getView(), 1,
                                new ViewGroup.LayoutParams(mOneTouchEmojiListWidth,
                                        ViewGroup.LayoutParams.MATCH_PARENT));
                        keyboardWidthScale
                                = (dm.widthPixels - mOneTouchEmojiListWidth) / (float)dm.widthPixels;
                    }
                }
            } else if (ONETOUCHEMOJILIST_RIGHT.equals(mOneTouchEmojiListMode)) {
                mOneTouchEmojiListWidth += oneTouchEmojiListPaddingRight;

                oneTouchEmojiList1stView.setPadding(0,
                                                oneTouchEmojiList1stView.getPaddingTop(),
                                                oneTouchEmojiListPaddingRight,
                                                oneTouchEmojiList1stView.getPaddingBottom());

                if (mKeyboardView != null) {
                    int oneTouchEmojiListPaddingKeyboardLeft = 0;

                    if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                        oneTouchEmojiListPaddingKeyboardLeft = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key;
                    } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                        oneTouchEmojiListPaddingKeyboardLeft = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key;
                    } // else

                    mKeyboardView.setPopupOffset(mWnn.getResources().getDimensionPixelSize(
                            oneTouchEmojiListPaddingKeyboardLeft), 0);
                    mKeyboardView.setPadding(mWnn.getResources().getDimensionPixelSize(
                            oneTouchEmojiListPaddingKeyboardLeft),
                            mKeyboardView.getPaddingTop(),
                            0,
                            mKeyboardView.getPaddingBottom());
                }

                if (isSplitCurrent) {
                    int emojiViewX = (dm.widthPixels - mOneTouchEmojiListWidth) / 2
                            + mWnn.getResources().getDimensionPixelSize(
                            R.dimen.one_touch_emoji_list_padding_left_split);
                    mOneTouchEmojiListViewManager.getView().setX(emojiViewX);
                    mMainLayout.addView(mOneTouchEmojiListViewManager.getView(),
                            new ViewGroup.LayoutParams(mOneTouchEmojiListWidth,
                                    mOneTouchEmojiListViewManager.getView().getLayoutParams().height));
                } else {
                    mMainLayout.addView(mOneTouchEmojiListViewManager.getView(),
                        mMainLayout.indexOfChild(mKeyboardView) + 1,
                        new ViewGroup.LayoutParams(mOneTouchEmojiListWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    keyboardWidthScale
                    = (dm.widthPixels - mOneTouchEmojiListWidth) / (float)dm.widthPixels;
                }
            }
        }

        if ( (!isShowOneTouchEmojiList)
                || (isShowOneTouchEmojiList
                && !ONETOUCHEMOJILIST_LEFT.equals(mOneTouchEmojiListMode)
                && !ONETOUCHEMOJILIST_RIGHT.equals(mOneTouchEmojiListMode)) ) {
            if (isOneHandedCurrent && !mIsSymbolKeyboard) {
                int paddingKeyboardTop = 0;
                int paddingKeyboardBottom = 0;
                int paddingKeyboardLeft = 0;
                int paddingKeyboardRight = 0;

                if (mCurrentKeyboardType == KEYBOARD_12KEY || isNaviKeyboard()) {
                    paddingKeyboardTop = R.dimen.one_handed_keybord_padding_keyboard_top;
                    paddingKeyboardBottom = R.dimen.one_handed_keybord_padding_keyboard_bottom;
                    if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_RIGHT)
                            || mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_RIGHT)) {
                        paddingKeyboardLeft = R.dimen.one_handed_keybord_padding_keyboard_left_right_one_handed;
                        paddingKeyboardRight = R.dimen.one_handed_keybord_padding_keyboard_left_right;
                    } else if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_LEFT)
                            || mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_LEFT)) {
                        paddingKeyboardLeft = R.dimen.one_handed_keybord_padding_keyboard_left_right;
                        paddingKeyboardRight = R.dimen.one_handed_keybord_padding_keyboard_left_right_one_handed;
                    } // else
                } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                    paddingKeyboardTop = R.dimen.one_handed_keybord_padding_qwerty_keyboard_top;
                    paddingKeyboardBottom = R.dimen.one_handed_keybord_padding_qwerty_keyboard_bottom;
                    if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_RIGHT)
                            || mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_RIGHT)) {
                        paddingKeyboardLeft = R.dimen.one_handed_keybord_padding_qwerty_keyboard_left_right_one_handed;
                        paddingKeyboardRight = R.dimen.one_handed_keybord_padding_qwerty_keyboard_left_right;
                    } else if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_LEFT)
                            || mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_LEFT)) {
                        paddingKeyboardLeft = R.dimen.one_handed_keybord_padding_qwerty_keyboard_left_right;
                        paddingKeyboardRight = R.dimen.one_handed_keybord_padding_qwerty_keyboard_left_right_one_handed;
                    }
                }

                mKeyboardView.setPopupOffset(
                        mWnn.getResources().getDimensionPixelSize(paddingKeyboardLeft), 0);
                mKeyboardView.setPadding(
                        mWnn.getResources().getDimensionPixelSize(paddingKeyboardLeft),
                        mWnn.getResources().getDimensionPixelSize(paddingKeyboardTop),
                        mWnn.getResources().getDimensionPixelSize(paddingKeyboardRight),
                        mWnn.getResources().getDimensionPixelSize(paddingKeyboardBottom));
            }
        }

        mEnableEmoji = enableEmoji;
        mEnableDecoEmoji = enableDecoEmoji;
        mEnableSymbol = enableSymbol;

        if (isOneHandedCurrent && !mIsSymbolKeyboard) {
            mOneHandedRightView.setLayoutParams(
                    new LinearLayout.LayoutParams(mOneHandedViewWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            mOneHandedLeftView.setLayoutParams(
                    new LinearLayout.LayoutParams(mOneHandedViewWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            keyboardWidthScale
                -= mOneHandedViewWidth / (float)dm.widthPixels;
        }

        if ((mKeyboardView != null) && (mMainView != null) && (mCurrentKeyboard != null)) {
            if (((Float.compare(WnnKeyboardFactory.getWidthScale(), keyboardWidthScale) != 0)
                    && !mIsSymbolKeyboard)
                    || ((isSplitPrev != isSplitCurrent) && !mIsSymbolKeyboard)) {
                WnnKeyboardFactory.setWidthScale(keyboardWidthScale);
                WnnKeyboardFactory.setKeyboardMode(isShowOneTouchEmojiList);
                mKeyboard = new WnnKeyboardFactory[3][2][4][2][12][4][2];
                createKeyboards(mWnn);
                mCurrentKeyboard = null;
                Keyboard keyboard = getModeChangeKeyboard(mCurrentKeyMode);
                changeKeyboard(keyboard);
                setShifted(mShiftOn);
                setUndoKeyMode(mCanUndo);
                mKeyboardView.clearWindowInfo();
            }
            mKeyboardView.invalidateAllKeys();
        }

        mOneTouchEmojiListViewManager.set1stBaseWidth(mOneTouchEmojiListWidth);
        mOneTouchEmojiListViewManager.set2ndBaseWidth(dm.widthPixels - mOneHandedViewWidth);
        mOneTouchEmojiListViewManager.updateView(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol,
                mOneTouchEmojiListMode);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setNormalKeyboard */
    @Override public void setNormalKeyboard() {
        boolean isInitFlg = false;
        if (mCurrentKeyboard != null) {
            if (mIsSymbolKeyboard) {
                isInitFlg = true;
            }
        }
        super.setNormalKeyboard();

        if (isInitFlg) {
            setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol);
        }

        mOneTouchEmojiListViewManager.showView();
        if (!mCapsLock) {
            setShiftByEditorInfo(true);
        }
    }

    /**
     * show long press menu.
     */
    public void showLongPressMenu() {
        // Create the build for the dialog.
        BaseInputView baseInputView = (BaseInputView)getCurrentView();
        AlertDialog.Builder builder = new AlertDialog.Builder(baseInputView.getContext());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.ti_dialog_button_cancel_txt, null);

        // Set the items in the menu.
        Resources r = baseInputView.getResources();
        CharSequence itemSettingMenu = r.getString(R.string.ti_long_press_dialog_setting_menu_txt);
        CharSequence itemInputMethod = r.getString(R.string.ti_long_press_dialog_input_method_txt);
        CharSequence itemInputTheme = r.getString(R.string.ti_long_press_dialog_keyboard_theme_txt);
        CharSequence itemMushroom = r.getString(R.string.ti_dialog_option_menu_item_mushroom_txt);
        CharSequence[] dialogItem;

        if (mEnableMushroom) {
            mMenuItem = new int[] {POS_KEYBOARDTHEME, POS_SETTINGS, POS_MUSHROOM, POS_METHOD};
            dialogItem = new CharSequence[] {itemInputTheme, itemSettingMenu, itemMushroom, itemInputMethod};
        } else {
            mMenuItem = new int[] {POS_KEYBOARDTHEME, POS_SETTINGS, POS_METHOD};
            dialogItem = new CharSequence[] {itemInputTheme, itemSettingMenu, itemInputMethod};
        }

        // Set the process for items of menu.
        builder.setItems(dialogItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface longPressDialog, int position) {
                Context c = getCurrentView().getContext();
                OpenWnn wnn = OpenWnn.getCurrentIme();
                Intent intent = new Intent();
                longPressDialog.dismiss();
                switch (mMenuItem[position]) {
                case POS_SETTINGS:
                    // Setting Menu link
                    wnn.requestHideSelf(0);
                    intent.setClass(c, ControlPanelStandard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(intent);
                    break;

                case POS_METHOD:
                    // Input method choose
                    InputMethodManager manager =
                        (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.showInputMethodPicker();
                    break;

                case POS_MUSHROOM:
                    // Mushroom
                    wnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CALL_MUSHROOM));
                    break;

                case POS_KEYBOARDTHEME:
                    ((IWnnLanguageSwitcher)mWnn).clearInitializedFlag();
                    wnn.requestHideSelf(0);
                    intent.setClass(c, KeyboardThemeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(intent);
                    break;

                default:
                    break;

                }
            }
        });
        builder.setTitle(r.getString(R.string.ti_long_press_dialog_title_txt));

        baseInputView.showDialog(builder);

        initializeFlick();
    }

    /**
     * Set the symbol keyboard.
     */
    @Override public void setSymbolKeyboard() {
        super.setSymbolKeyboard();
        mOneTouchEmojiListViewManager.hideView();
        hideOneHandedView();
    }
    /**
     * Handle a back key event.
     *
     * @return {@code true}:True when event is digested; {@code false}:if not
     */
    public boolean processBackKeyEvent() {
        boolean ret = false;
        if ((mCurrentKeyboardType != KEYBOARD_NAVIGATION) && (mCurrentKeyMode != KEYMODE_JA_VOICE)
                && (mCurrentKeyMode != KEYMODE_JA_NUMERIC)
                && (mCurrentKeyMode != KEYMODE_JA_NUMERIC_DECIMAL)
                && (mCurrentKeyMode != KEYMODE_JA_NUMERIC_SIGNED) && !mIsSymbolKeyboard
                && (mEnableEmoji || mEnableDecoEmoji || mEnableSymbol)
                && ((ONETOUCHEMOJILIST_LEFT.equals(mOneTouchEmojiListMode))
                        || (ONETOUCHEMOJILIST_RIGHT.equals(mOneTouchEmojiListMode)))) {
            ret = mOneTouchEmojiListViewManager.processBackKeyEvent();
        }
        return ret;
    }

    /**
     * Returns the visibility of One Touch Emoji List.
     *
     * @return {@code true}:True if One Touch Emoji List is visible; {@code false}:if not
     */
    public boolean isShownOneTouchEmojiList() {
        return !ONETOUCHEMOJILIST_HIDDEN.equals(mOneTouchEmojiListMode) && (mEnableEmoji
                || mEnableDecoEmoji || mEnableSymbol);
    }

    /**
     * Switch to VoiceEditor IME.
     *
     * @param select  {@code true} if selected.
     */
    private void switchToVoiceEditorIME() {
        commitText();
        try {
            PackageManager pm = mWnn.getPackageManager();
            ApplicationInfo applicationInfo
                    = pm.getApplicationInfo(DOCOMO_VOICEEDITOR_PACKAGE_NAME, 0);
            if (!applicationInfo.enabled) {
                Toast.makeText(getCurrentView().getContext(),
                        R.string.ti_dcm_voiceeditor_Invalid_txt, Toast.LENGTH_LONG)
                        .show();
                return;
            }
        } catch (NameNotFoundException nameFoundException) {
            Toast.makeText(getCurrentView().getContext(),
                    R.string.ti_dcm_voiceeditor_Invalid_txt, Toast.LENGTH_LONG)
                    .show();
            Log.e("iWnn",
                "DefaultSoftKeyboardHangul::switchToVoiceEditorIME() NameNotFoundException");
            return;
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getCurrentView().getContext(),
                    R.string.ti_dcm_voiceeditor_Invalid_txt, Toast.LENGTH_LONG)
                    .show();
            Log.e("iWnn",
                "DefaultSoftKeyboardHangul::switchToVoiceEditorIME() IllegalArgumentException");
            return;
        }

        if (IWnnImeJaJp.isLatticeInputMode()) {
            IWnnImeJaJp.setLatticeInputMode(false);
        }

        VoiceEditorClient viClient = new VoiceEditorClient();
        if (viClient.switchToVoiceEditorIME((InputMethodService)OpenWnn.superGetContext())) {
            return;
        } else {
            Toast.makeText(getCurrentView().getContext(),
                    R.string.ti_dcm_voiceeditor_Invalid_txt, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Called when the instance is destroyed.
     */
    @Override public void onDestroy() {
        mOneTouchEmojiListViewManager.onDestroy();
    }
}
