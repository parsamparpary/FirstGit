/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.hangul;
///////////////// iwnn_trial_add_0

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.KeyCharacterMap;
import android.view.View;
import android.view.WindowManager;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.BaseInputView;
import jp.co.omronsoft.iwnnime.ml.CandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard;
import jp.co.omronsoft.iwnnime.ml.IWnnImeBase;
import jp.co.omronsoft.iwnnime.ml.InputViewManager;
import jp.co.omronsoft.iwnnime.ml.KeyboardView;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.OpenWnnEvent;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.StrSegment;
import jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.WnnEngine;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnSymbolEngine;
import jp.co.omronsoft.iwnnime.ml.jajp.IWnnImeJaJp;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;
import jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
///////////////// iwnn_trial_add_1

/**
 * iWnn IME (Chinese)
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public abstract class IWnnImeHangul extends IWnnImeBase {
///////////////// iwnn_trial_add_2

    //////////////////////////////////////////////////////////////// public static final
    /**
     * Mode of the convert engine (Symbol list).
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL = 104;

    /**
     * Mode of the convert engine (Keyboard type is QWERTY).
     * Use with {@code OpenWnn.CHANGE_MODE} event to change ambiguous searching pattern.
     *  (This setting becomes OR. It treats in a word as an additive attribute without
     *  nullifying other ENGINE_MODE_xxx. )
     */
    public static final int ENGINE_MODE_OPT_TYPE_QWERTY = 105;

    //////////////////////////////////////////////////////////////// private static final
    /** [For debugging] True when debugging processing is effectively done */
    private static final boolean DEBUG = false;

    /** Highlight color style for the converted clause */
    private static final CharacterStyle SPAN_CONVERT_BGCOLOR_HL_HANGUL  = new BackgroundColorSpan(0xFF8888FF);

    /** private area character code got by <code>KeyEvent.getUnicodeChar()</code> */
    private static final int PRIVATE_AREA_CODE = 61184;

    /** Maximum length of input string */
    private static final int LIMIT_INPUT_NUMBER = 50;

    /** Message for {@code mHandler} (close) */
    private static final int MSG_CLOSE = 2;

    /** Message for {@code mHandler} (toggle time limit) */
    private static final int MSG_TOGGLE_TIME_LIMIT = 3;

    /** Message for {@code mHandler} (updating the learning dictionary) */
    private static final int MSG_UPDATE_LEARNING_DICTIONARY = 5;

    /** Message for {@code mHandler} (restart) */
    private static final int MSG_RESTART = 7;

    /** Delay time(msec.) updating the learning dictionary. */
    private static final int DELAY_MS_UPDATE_LEARNING_DICTIONARY = 0;

    /** Delay time(msec.) to disable toggling character. */
    private static final int DELAY_MS_DISABLE_TOGGLE = 500;

    /** Transmitted definition for APP. */
    public static final String COMMIT_TEXT_THROUGH_ACTION = "jp.co.omronsoft.iwnnime.ml";

    /** Action key for Through YOMI of committed text. */
    public static final String COMMIT_TEXT_THROUGH_KEY_YOMI = "yomi";

    ////////////////////////////////////////////////////////////////// classes

    /** Convert engine's state */
    protected class EngineState {
        /** Definition for {@code EngineState.*} (invalid) */
        public static final int INVALID = -1;

        /** Definition for {@code EngineState.language} (Hangul) */
        public static final int LANGUAGE_HANGUL = 0;

        /** Definition for {@code EngineState.convertType} (prediction/no conversion) */
        public static final int CONVERT_TYPE_NONE = 0;

        /** Definition for {@code EngineState.convertType} (consecutive clause conversion) */
        public static final int CONVERT_TYPE_RENBUN = 1;

        /** Definition for {@code EngineState.temporaryMode} (change back to the normal dictionary) */
        public static final int TEMPORARY_DICTIONARY_MODE_NONE = 0;

        /** Definition for {@code EngineState.temporaryMode} (change to the symbol dictionary) */
        public static final int TEMPORARY_DICTIONARY_MODE_SYMBOL = 1;

        /** Definition for {@code EngineState.temporaryMode} (change to the user dictionary) */
        public static final int TEMPORARY_DICTIONARY_MODE_USER = 2;

        /** Definition for {@code EngineState.preferenceDictionary} (no preference dictionary) */
        public static final int PREFERENCE_DICTIONARY_NONE = 0;

        /** Definition for {@code EngineState.preferenceDictionary} (person's name) */
        public static final int PREFERENCE_DICTIONARY_PERSON_NAME = 1;

        /** Definition for {@code EngineState.preferenceDictionary} (place name) */
        public static final int PREFERENCE_DICTIONARY_POSTAL_ADDRESS = 2;

        /** Definition for {@code EngineState.preferenceDictionary} (email/URI) */
        public static final int PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI = 3;

        /** Definition for <code>EngineState.keyboard</code> (undefined) */
        public static final int KEYBOARD_UNDEF = 0;

        /** Definition for <code>EngineState.keyboard</code> (undefined) */
        public static final int KEYBOARD_QWERTY = 1;

        /** Definition for <code>EngineState.keyboard</code> (undefined) */
        public static final int KEYBOARD_12KEY  = 2;

        /** Language */
        public int language = INVALID;

        /** Type of conversion */
        public int convertType = INVALID;

        /** Temporary mode */
        public int temporaryMode = INVALID;

        /** Preference dictionary setting */
        public int preferenceDictionary = INVALID;

        /** keyboard */
        public int keyboard = INVALID;

        /**
         * Default constructor
         */
        public EngineState() {
            super();
        }

        /**
         * Returns whether current type of conversion is consecutive clause(RENBUNSETSU) conversion.
         *
         * @return {@code true} if current type of conversion is consecutive clause conversion.
         */
        public boolean isRenbun() {
            return convertType == CONVERT_TYPE_RENBUN;
        }

        /**
         * Returns whether current type of conversion is no conversion.
         *
         * @return {@code true} if no conversion is executed currently.
         */
        public boolean isConvertState() {
            return convertType != CONVERT_TYPE_NONE;
        }

        /**
         * Check whether or not the mode is "symbol list".
         *
         * @return {@code true} if the mode is "symbol list".
         */
        public boolean isSymbolList() {
            return temporaryMode == TEMPORARY_DICTIONARY_MODE_SYMBOL;
        }

    }

    /** Whether exact match searching or not */
    protected boolean mExactMatchMode = false;

    /** Spannable string builder for displaying the composing text */
    protected SpannableStringBuilder mDisplayText;

    //////////////////////////////////////////////////////////////// private


    /** Backup for switching the converter */
    private WnnEngine mConverterBack;

    /** iWnn conversion engine for Japanese */
    protected iWnnEngine mConverterIWnn;

    /** Pseudo engine (GIJI) for sign (Backup variable for switch) */
    private IWnnSymbolEngine mConverterSymbolEngineBack;

    /** True when the wildcard prediction is enable */
    private boolean mEnableFunfun = true;

    /** Conversion Engine's state */
    protected EngineState mEngineState = new EngineState();

    /** Whether learning function is active or not. */
    private boolean mEnableLearning = true;

    /** Whether prediction is active or not. */
    private boolean mEnablePrediction = true;

    /** Whether using the converter */
    private boolean mEnableConverter = true;

    /** Whether displaying the symbol list */
    private boolean mEnableSymbolList = true;

    /** Whether non ASCII code is enabled */
    private boolean mEnableSymbolListNonHalf = true;

    /** Whether being able to use Emoji */
    private boolean mEnableEmoji = false;

    /** Whether being able to use DecoEmoji */
    private boolean mEnableDecoEmoji = false;

    /** Enable mistyping correction or not */
    private boolean mEnableSpellCorrection = true;

    /** Whether the H/W keyboard is hidden. */
    private boolean mHardKeyboardHidden = true;

    /**
     * Number of committed clauses on consecutive clause conversion
     * <br>
     * [Purpose to use]
     * <br>
     * * Fix the candidate position on a consecutive clause conversion
     * <br>
     * * Convert consecutive clause when call updateViewStatus() with mCommitCount == 0
     * <br>
     *   (when mCommitCount not equal 0, it gets only the candidate)

     * <br>
     *
     * [Timing to update]
     * <br>
     * * Clear 0, when Start converting or reconverting
     * <br>
     * * Increment, when it's committed
     */
    private int mCommitCount = 0;

    /** Current orientation of the display */
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;

    /** Regular expression pattern for English separators */
    private  Pattern mEnglishAutoCommitDelimiter = null;

    /** Cursor position in the composing text */
    private int mComposingStartCursor = 0;

    /** Cursor position before committing text */
    private int mCommitStartCursor = 0;

    /** Previous committed text */
    private StringBuffer mPrevCommitText = null;

    /** Set if the IME want to disable the initialization on onUpdateSelection() */
    private boolean mIgnoreCursorMove = false;

    /** Predict character string when fixed (for Undo) */
    private String  mCommitPredictKey = null;

    /** Wildcard prediction when fixed (for Undo) */
    private int mCommitFunfun = 0;

    /** Cursor position when fixed (for Undo) */
    private int mCommitCursorPosition = 0;

    /** State of character number specification predict when fixed (for Undo) */
    private boolean mCommitExactMatch = false;

    /** True in case of possible Undo (for Undo) */
    private boolean mCanUndo = false;

    /** Layer1 information of the composing text when committed */
    private ArrayList<StrSegment> mCommitLayer1StrSegment = null;

    /** Whether there is a continued predicted candidate */
    private boolean mHasContinuedPrediction;

    /** True to make "o" of a Wildcard prediction space */
    private boolean mHasRequiredFunfunSpace = false;

    /** Whether text selection has started */
    private boolean mHasStartedTextSelection = true;

    /** Whether candidate is fullscreen or not */
    private boolean mFullCandidate = false;

    /** Use Mushroom */
    protected boolean mEnableMushroom = false;

    /** Stroke of committed text */
    private String mStrokeOfCommitText = null;

    /** Size of the stroke to delete */
    private int mSizeOfDeleteStroke = 0;

    /** Enable head conversion */
    private boolean mEnableHeadConv = true;

    /** TextCandidatesViewManager */
    protected TextCandidatesViewManager mTextCandidatesViewManager = null;

//    /** TextCandidates1LineViewManager */
//    protected TextCandidates1LineViewManager mTextCandidates1LineViewManager = null;

    /** EditorInfo */
    protected EditorInfo mAttribute = null;
    /** {@code Handler} for drawing candidates/displaying */
    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLOSE:
                    if (mConverterIWnn != null) mConverterIWnn.close();
                    if (mConverterSymbolEngineBack != null) mConverterSymbolEngineBack.close();
                    break;
                case MSG_TOGGLE_TIME_LIMIT:
                    if (isDirectInputMode()) {
                        sendKeyChar(mLastToggleCharTypeNull);
                        mLastToggleCharTypeNull = 0;
                    } else if (mStatus == STATUS_INPUT) {
                        mStatus = STATUS_INPUT_EDIT;
                    } // else {}
                    break;
                case MSG_UPDATE_LEARNING_DICTIONARY:
                    if (DEBUG) {Log.d(TAG, "Learning dictionary update");}
                    if (mConverterIWnn != null) {
                        mConverterIWnn.writeoutDictionary(getLanguage(), iWnnEngine.SetType.LEARNDIC);
                    }
                    break;
                case MSG_RESTART:
                    onStartInput(mAttribute, false);
                    onStartInputView(mAttribute, false);
                    break;
                default:
                    ; // nothing to do.
                    break;
            }
        }
    };

    ////////////////////////////////////////////////////////////////////// methods
    /**
     * Constructor
     *
     * @param wnn  IWnnLanguageSwitcher instance
     */
    public IWnnImeHangul(IWnnLanguageSwitcher wnn) {
        super(wnn);
        setComposingText(new ComposingText());
        mTextCandidatesViewManager = new TextCandidatesViewManager(iWnnEngine.CANDIDATE_MAX);
        setCandidatesViewManager(mTextCandidatesViewManager);
        setInputViewManager(createInputViewManager(wnn));
        mConverterIWnn = iWnnEngine.getEngine();
        setConverter(mConverterIWnn);
        String localeString = mConverterIWnn.getLocaleString(getLanguage());
        mConverterSymbolEngineBack = new IWnnSymbolEngine(OpenWnn.superGetContext(), localeString);

        mDisplayText = new SpannableStringBuilder();
        setAutoHideMode(false);

        mPrevCommitText = new StringBuffer();
        mCommitLayer1StrSegment = new ArrayList<StrSegment>();

        ((DefaultSoftKeyboard) getInputViewManager()).resetCurrentKeyboard();
    }

    /**
     * Constructor
     *
     * @param context The context that sets engine.
     * @param wnn  IWnnLanguageSwitcher instance
     */
    public IWnnImeHangul(Context context, IWnnLanguageSwitcher wnn) {
        this(wnn);
//        attachBaseContext(context);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreate */
    @Override public void onCreate() {
        onCreateOpenWnn();

        String delimiter = Pattern.quote(getResources().getString(R.string.ti_en_word_separators_txt));
        mEnglishAutoCommitDelimiter = Pattern.compile(".*[" + delimiter + "]$", Pattern.DOTALL);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreateCandidatesView */
    @Override public View onCreateCandidatesView() {
        return null;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreateInputView */
    @Override public View onCreateInputView() {
        int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
        boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
        ((DefaultSoftKeyboard) getInputViewManager()).setHardKeyboardHidden(hidden);
        mTextCandidatesViewManager.setHardKeyboardHidden(hidden);
        mHardKeyboardHidden = hidden;
        View v = super.onCreateInputView();
        return v;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onStartInput */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        if (attribute.initialSelStart != attribute.initialSelEnd) {
            mHasStartedTextSelection = true;
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onStartInputView */
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        if (DEBUG) {Log.d(TAG, "onStartInputView(" + attribute + "," + restarting + ")");}

        boolean keep = restarting && (mAttribute != null)
            && TextUtils.equals(mAttribute.fieldName, attribute.fieldName);

        mAttribute = attribute;

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();

        boolean ignore = false;
        if (keep) {
            InputConnection ic = getInputConnection();
            if (ic != null) {
                CharSequence chars = ic.getTextBeforeCursor(1, 0);
                if ((chars != null) && !chars.equals("")) {
                    ignore = true;
                }
            }
        }

        // load preferences
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());

        if (ignore || OpenWnn.getCurrentIme().isKeepInput()) {
            onStartInputViewOpenWnn(attribute, ignore);
        } else {
            // Recreate a symbol engine. There is a possibility that
            // histories of symbol had been deleted from a user dictionary
            // tools.
            if (mConverterSymbolEngineBack != null) {
                mConverterSymbolEngineBack.close();
            }
            String localeString = mConverterIWnn.getLocaleString(getLanguage());
            mConverterSymbolEngineBack = new IWnnSymbolEngine(
                        OpenWnn.superGetContext(), localeString);

            mIgnoreCursorMove = false;
            clearCommitInfo();

            // initialize screen
            initializeScreen();

            ((DefaultSoftKeyboard) getInputViewManager()).resetCurrentKeyboard();

            onStartInputViewOpenWnn(attribute, ignore);
            if (OpenWnn.isTabletMode()) {
                mTextCandidatesViewManager.setPreferences(pref);
            }

            // initialize views
            setCandidateIsViewTypeFull(false);
            candidatesViewManager.clearCandidates();

            getInputConnection().finishComposingText();
        }

        fitInputType(pref, attribute);
        updateFullscreenMode();

        mTextCandidatesViewManager.setAutoHide(true);

        // The connection of study is cut by IME beginning.
        if (isEnableL2Converter()) {
            breakSequence();
        }

        AsyncCommitMushRoomString();

        OpenWnn ime = OpenWnn.getCurrentIme();
        if (ime != null) {
            if (ime.mOriginalInputMethodSwitcher.mSymbolMode) {
                onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, 
                                IWnnImeJaJp.ENGINE_MODE_SYMBOL_SYMBOL));
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#hideWindow */
    @Override public void hideWindow() {
        getComposingText().clear();
        InputViewManager inputViewManager = getInputViewManager();

        BaseInputView baseInputView = ((BaseInputView)((DefaultSoftKeyboard) inputViewManager).getCurrentView());
        if (baseInputView != null) {
            baseInputView.closeDialog();
        }

        inputViewManager.onUpdateState(mWnnSwitcher);
        clearCommitInfo();
        inputViewManager.closing();

        hideWindowOpenWnn();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_LEARNING_DICTIONARY),
                DELAY_MS_UPDATE_LEARNING_DICTIONARY);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onUpdateSelection */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        if (DEBUG) {Log.d(TAG, "onUpdateSelection()" + oldSelStart + ":" + oldSelEnd + ":"
                          + newSelStart + ":" + newSelEnd + ":" + candidatesStart + ":" + candidatesEnd);}
        onUpdateSelectionOpenWnn(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

        mComposingStartCursor = (candidatesStart < 0) ? newSelEnd : candidatesStart;

        // It makes it to improper undo by the selection beginning.
        boolean prevSelection = mHasStartedTextSelection;
        if (newSelStart != newSelEnd) {
            clearCommitInfo();
            mHasStartedTextSelection = true;
        } else {
            mHasStartedTextSelection = false;
        }

        if (mHasContinuedPrediction) {
            mHasContinuedPrediction = false;
            mIgnoreCursorMove = false;
            return;
        }

        if (mEngineState.isSymbolList()) {
            return;
        }

        boolean isNotComposing = ((candidatesStart < 0) && (candidatesEnd < 0));
        ComposingText composingText = getComposingText();
        if (((composingText.size(ComposingText.LAYER1) + mFunfun) != 0)
              && !isNotComposing) {

            if (mHasStartedTextSelection) {
                InputConnection inputConnection = getInputConnection();
                if (inputConnection != null) {
                    if (0 < mFunfun) {
                        inputConnection.setComposingText(composingText.toString(mTargetLayer), 1);
                        int funfunStart = Math.max(candidatesEnd - mFunfun, 0);
                        int selStart = newSelStart;
                        int selEnd = newSelEnd;
                        if (funfunStart < newSelStart) {
                            selStart = Math.max(newSelStart - mFunfun, funfunStart);
                        }
                        if (funfunStart < newSelEnd) {
                            selEnd = Math.max(newSelEnd - mFunfun, funfunStart);
                        }
                        inputConnection.setSelection(selStart, selEnd);
                        mFunfun = 0;
                        setFunFun(mFunfun);
                    }
                    inputConnection.finishComposingText();
                }
                composingText.clear();
                initializeScreen();
            } else {
                updateViewStatus(mTargetLayer, false, true);
            }
            mIgnoreCursorMove = false;
        } else {
            if (mIgnoreCursorMove) {
                mIgnoreCursorMove = false;
            } else {
                int commitEnd = mCommitStartCursor + mPrevCommitText.length();
                if ((((newSelEnd < oldSelEnd) || (commitEnd < newSelEnd)) && clearCommitInfo())
                    || isNotComposing) {
                    // The connection of learn is cut by the cursor movement.
                    if (isEnableL2Converter()) {
                        breakSequence();
                    }

                    InputConnection inputConnection = getInputConnection();
                    if (inputConnection != null) {
                        // Delete "o", when committed automatically while wildcard prediction.
                        if (1 < mFunfun) {
                            inputConnection.deleteSurroundingText(mFunfun - 1, 0);
                        }
                        if (isNotComposing && (composingText.size(ComposingText.LAYER1) != 0)) {
                            inputConnection.finishComposingText();
                        }
                    }

                    // Initialize screen if text selection does not keep.
                    // (Stop initialization when the symbol list open on text selection.)
                    if ((prevSelection != mHasStartedTextSelection) || !mHasStartedTextSelection) {
                        initializeScreen();
                    }
                }
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onConfigurationChanged */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        if (DEBUG) {Log.d(TAG, "onConfigurationChanged()");}
        View getCurrentView = getCandidatesViewManager().getCurrentView();
        boolean candidateShown = false;
        if (getCurrentView != null) {
            candidateShown = getCurrentView.isShown();
        }
        try {
            InputConnection inputConnection = getInputConnection();
            if (inputConnection != null) {
                boolean onRotation = false;
                if (mOrientation != newConfig.orientation) {
                    mOrientation = newConfig.orientation;
                    onRotation = true;
                    OpenWnn.getCurrentIme().setStateOfKeepInput(true);
                }

                onConfigurationChangedOpenWnn(newConfig);

                if (super.isInputViewShown()) {
                    boolean requestCandidate = (!onRotation || candidateShown);
                    updateViewStatus(mTargetLayer, requestCandidate, true);
                }

                int hiddenState = newConfig.hardKeyboardHidden;
                boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
                if (mOrientation != newConfig.orientation || mHardKeyboardHidden != hidden) {
                    if (mRecognizing) {
                        mRecognizing = false;
                    }
                    mOrientation = newConfig.orientation;
                    mHardKeyboardHidden = hidden;
                    commitConvertingText();
                    if (mFunfun > 0) {
                        inputConnection.deleteSurroundingText(mFunfun, 0);
                        mFunfun = 0;
                        setFunFun(mFunfun);
                    }
                    //So as not to display wildcard prediction, delete the composing text.
                    initializeScreen();
                    updateViewStatus(mTargetLayer, false, true);
                }
                ((DefaultSoftKeyboard) getInputViewManager()).setHardKeyboardHidden(hidden);
                mTextCandidatesViewManager.setHardKeyboardHidden(hidden);
            } else {
                onConfigurationChangedOpenWnn(newConfig);
            }
        } catch (Exception ex) {
            Log.e(TAG, "IWnnImeHangul::onConfigurationChanged " + ex.toString());
        }
        OpenWnn.getCurrentIme().setStateOfKeepInput(false);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvent */
    @Override synchronized public boolean onEvent(OpenWnnEvent ev) {
        InputConnection inputConnection = getInputConnection();
        boolean ret = false;
        if ((mFullCandidate && ((mStatus & STATUS_CANDIDATE_FULL) != 0))
                || isRightOrLeftKeyEvents(ev)) {
            ret = handleEvent(ev);
        } else {
            inputConnection.beginBatchEdit();
            ret = handleEvent(ev);
            inputConnection.endBatchEdit();
        }
        return ret;
    }

    /**
     * Process an event.
     *
     * @param  ev  An event
     * @return  {@code true} if the event is processed in this method; {@code false} if not.
     */
    private boolean handleEvent(OpenWnnEvent ev) {
        if (DEBUG) {Log.d(TAG, "onEvent()");}
///////////////// iwnn_trial_add_3

        EngineState state;
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        ComposingText composingText = getComposingText();
        InputConnection inputConnection = getInputConnection();

        // Event handling
        // The keyboard is processed when it is invalid executable.
        switch (ev.code) {

        case OpenWnnEvent.KEYUP:
            return true;

        case OpenWnnEvent.KEYLONGPRESS:
            return onKeyLongPressEvent(ev.keyEvent);

        // Switch a dictionary engine
        case OpenWnnEvent.CHANGE_MODE:
            changeEngineMode(ev.mode);
            //when input string is full, initialize the screen
            if (!((ev.mode == ENGINE_MODE_SYMBOL)
                  || (ev.mode == ENGINE_MODE_SYMBOL_EMOJI)
                  || (ev.mode == ENGINE_MODE_SYMBOL_EMOJI_UNI6)
                  || (ev.mode == ENGINE_MODE_SYMBOL_SYMBOL)
                  || (ev.mode == ENGINE_MODE_SYMBOL_DECOEMOJI)
                  || (ev.mode == ENGINE_MODE_SYMBOL_KAO_MOJI)
                  || (ev.mode == ENGINE_MODE_SYMBOL_ADD_SYMBOL))) {
                if (!OpenWnn.getCurrentIme().isKeepInput()) {
                    initializeScreen();
                }
            }
            return true;

        // Update a candidates
        case OpenWnnEvent.UPDATE_CANDIDATE:
            if (mEngineState.isRenbun()) {
                // Back to prediction instead of reconversion
                // (there's a possibility that a committing text could be changed by deleting candidate)
                composingText.setCursor(ComposingText.LAYER1,
                                         composingText.toString(ComposingText.LAYER1).length());
                mExactMatchMode = false;
                mFunfun = 0;
                setFunFun(mFunfun);
                updateViewStatusForPrediction(true, true); // prediction only
            } else {
                updateViewStatus(mTargetLayer, true, true);
            }
            return true;

        // Switch a view
        case OpenWnnEvent.CHANGE_INPUT_VIEW:
            View inputView = onCreateInputViewOpenWnn();
            if (inputView != null) {
                setInputView(inputView);
            }
            WindowManager wm = (WindowManager)mWnnSwitcher.getSystemService(Context.WINDOW_SERVICE);
            mTextCandidatesViewManager.initView(mWnnSwitcher,
                                              wm.getDefaultDisplay().getWidth(),
                                              wm.getDefaultDisplay().getHeight());
            View view = null;
            view = mTextCandidatesViewManager.getCurrentView();
            if (view != null) {
                mWnnSwitcher.setCandidatesView(view);
            }
            return true;

        case OpenWnnEvent.TOUCH_OTHER_KEY:
            mStatus |= STATUS_INPUT_EDIT;
            return true;

        case OpenWnnEvent.UNDO:
            return undo();

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollUp();
            }
            return true;

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollDown();
            }
            return true;

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_FULL_UP:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollFullUp();
            }
            return true;

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_FULL_DOWN:
             if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollFullDown();
            }
            return true;

        case OpenWnnEvent.FOCUS_CANDIDATE_START:
            ((DefaultSoftKeyboard) getInputViewManager()).setOkToEnterKey();
            return true;

        case OpenWnnEvent.FOCUS_CANDIDATE_END:
            getInputViewManager().onUpdateState(getSwitcher());
            return true;

        case OpenWnnEvent.VOICE_INPUT:
            startShortcutIME();
            return true;

        default:
            // No processing
            break;
        }

        // Get the Keycode
        KeyEvent keyEvent = ev.keyEvent;
        int keyCode = 0;
        if (keyEvent != null) {
            keyCode = keyEvent.getKeyCode();
        }

        if (isDirectInputMode()) {
            if (inputConnection != null) {
                if (mHandler.hasMessages(MSG_TOGGLE_TIME_LIMIT)
                        && (ev.code != OpenWnnEvent.TOGGLE_CHAR)) {
                    mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
                    sendKeyChar(mLastToggleCharTypeNull);
                    mLastToggleCharTypeNull = 0;
                }

                switch (ev.code) {
                case OpenWnnEvent.INPUT_SOFT_KEY:
                    sendKeyEventDirect(keyCode);
                    return true;

                case OpenWnnEvent.INPUT_CHAR:
                    if(ev.chars[0] >= '0' && ev.chars[0] <= '9') {
                        inputConnection.commitText(String.valueOf((char) ev.chars[0]), 1);
                    } else {
                        sendKeyChar(ev.chars[0]);
                    }
                    return true;

                case OpenWnnEvent.TOGGLE_CHAR:
                    String[] table = ev.toggleTable;
                    if (mLastToggleCharTypeNull == 0) {
                        mLastToggleCharTypeNull = table[0].charAt(0);
                    } else {
                        mLastToggleCharTypeNull = ((mLastToggleCharTypeNull == table[0].charAt(0))
                                                   ? table[1].charAt(0) : table[0].charAt(0));
                    }
                    mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TOGGLE_TIME_LIMIT),
                                                DELAY_MS_DISABLE_TOGGLE);
                    break;
                default:
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (isInputViewShown()) {
                            InputViewManager inputViewManager = getInputViewManager();
                            inputViewManager.closing();
                            requestHideSelf(0);
                            return true;
                            }
                    default:
                        break;
                    }
                    break;
                }
            }

            // The processing from now on is not executed in case of input invalid.
            return false;
        }

        if (mEngineState.isSymbolList()) {
            if (keyEvent != null && keyEvent.isPrintingKey() && isTenKeyCode(keyCode) && !keyEvent.isNumLockOn()) {
                // If the numeric keypad is pressed when NumLock is OFF,
                // Key events since the original notification will come, this is not done.
                return false;
            }
            switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL:
                return false;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                OpenWnn ime = OpenWnn.getCurrentIme();
                if ((ime != null) && ime.mOriginalInputMethodSwitcher.mSymbolMode) {
                    getSwitcher().changeMode(ime.mOriginalInputMethodSwitcher.IME_TYPE_HANDWRITING);
                } else {
                    initializeScreen();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                //select Candidate
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.selectFocusCandidate();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (candidatesViewManager.isFocusCandidate()) {
                    processLeftKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (candidatesViewManager.isFocusCandidate()) {
                    processRightKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                processDownKeyEvent();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (candidatesViewManager.isFocusCandidate()) {
                    processUpKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_SPACE:
                if (keyEvent != null) {
                    if (keyEvent.isShiftPressed()) {
                        onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP));
                    } else if (keyEvent.isAltPressed()) {
                        if (keyEvent.getRepeatCount() == 0) {
                            switchSymbolList();
                        }
                    } else {
                        onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN));
                    }
                }
                return true;

            case KeyEvent.KEYCODE_SYM:
                switchSymbolList();
                return true;

            case KeyEvent.KEYCODE_PAGE_UP:
                candidatesViewManager.scrollPageAndUpdateFocus(false);
                return true;

            case KeyEvent.KEYCODE_PAGE_DOWN:
                candidatesViewManager.scrollPageAndUpdateFocus(true);
                return true;

            case KeyEvent.KEYCODE_MOVE_END:
                if (candidatesViewManager.isFocusCandidate()) {
                    processEndKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_MOVE_HOME:
                if (candidatesViewManager.isFocusCandidate()) {
                    processHomeKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_PICTSYMBOLS:
                if (keyEvent != null) {
                    if (keyEvent.getRepeatCount() == 0) {
                        switchSymbolList();
                    }
                }
                return true;

            default:
                // Do nothing.
            }

            if ((ev.code == OpenWnnEvent.INPUT_KEY) &&
                (keyCode != KeyEvent.KEYCODE_SEARCH) &&
                (keyCode != KeyEvent.KEYCODE_ALT_LEFT) &&
                (keyCode != KeyEvent.KEYCODE_ALT_RIGHT) &&
                (keyCode != KeyEvent.KEYCODE_SHIFT_LEFT) &&
                (keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT)) {
                state = new EngineState();
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
                updateEngineState(state);
            }
        }

        if (ev.code == OpenWnnEvent.LIST_CANDIDATES_FULL) {
            setCandidateIsViewTypeFull(true);
            return true;
        } else if (ev.code == OpenWnnEvent.LIST_CANDIDATES_NORMAL) {
            setCandidateIsViewTypeFull(false);
            return true;
        } // else {}

        // In case of events other than the undo command
        if (!((ev.code == OpenWnnEvent.UNDO)
              || (ev.code == OpenWnnEvent.COMMIT_COMPOSING_TEXT)
              || ((keyEvent != null)
                  && ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT)
                      ||(keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
                      ||(keyCode == KeyEvent.KEYCODE_ALT_LEFT)
                      ||(keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
                      ||(keyEvent.isShiftPressed()
                         && (((keyCode == KeyEvent.KEYCODE_DEL) || (keyCode == KeyEvent.KEYCODE_FORWARD_DEL))
                             || (keyCode == KeyEvent.KEYCODE_SPACE)))
                      ||(keyEvent.isAltPressed() && (keyCode == KeyEvent.KEYCODE_SPACE)))))) {

            clearCommitInfo();
        }

        boolean ret = false;
        switch (ev.code) {
        case OpenWnnEvent.INPUT_CHAR:
            mFunfun = 0;
            setFunFun(mFunfun);
            if (!isEnableL2Converter()) {
                /* instant input */
                commitText(false);
                commitText(new String(ev.chars));
                candidatesViewManager.clearCandidates();
            } else {
                processSoftKeyboardCode(ev.chars);
            }
            ret = true;
            break;

        case OpenWnnEvent.TOGGLE_CHAR:
            if (!isEnableL2Converter()) {
                commitText(false);
            }
            processSoftKeyboardToggleChar(ev.toggleTable);
            ret = true;
            break;

        case OpenWnnEvent.TOGGLE_REVERSE_CHAR:
            if (((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT)
                && !(mEngineState.isConvertState())) {

                int cursor = composingText.getCursor(ComposingText.LAYER1);
                if (cursor > 0) {
                    StrSegment strSegment = composingText.getStrSegment(ComposingText.LAYER1, cursor - 1);
                    if (strSegment != null) {
                        String prevChar = strSegment.string;
                        if (prevChar != null) {
                            String c = searchToggleCharacter(prevChar, ev.toggleTable, true);
                            if (c != null) {
                                composingText.delete(ComposingText.LAYER1, false);
                                appendStrSegment(new StrSegment(c));
                                mFunfun = 0;
                                setFunFun(mFunfun);
                                updateViewStatusForPrediction(true, true);
                                ret = true;
                                break;
                            }
                        }
                    }
                }
            }
            break;

        case OpenWnnEvent.REPLACE_CHAR:
            int cursor = composingText.getCursor(ComposingText.LAYER1);
            if ((cursor > 0)
                && !(mEngineState.isConvertState())) {

                StrSegment strSegment = composingText.getStrSegment(ComposingText.LAYER1, cursor - 1);
                if (strSegment != null) {
                    String search = strSegment.string;
                    if (search != null) {
                        String c = (String)ev.replaceTable.get(search);
                        if (c != null) {
                            composingText.delete(1, false);
                            appendStrSegment(new StrSegment(c));
                            mFunfun = 0;
                            setFunFun(mFunfun);
                            updateViewStatusForPrediction(true, true);
                            ret = true;
                            mStatus = STATUS_INPUT_EDIT;
                            break;
                        }
                    }
                }
            }
            break;

        case OpenWnnEvent.INPUT_KEY:
            if (keyEvent == null) {
                break;
            }
            /* update shift/alt state */
            switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                return false;

            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                composingText = getComposingText();
                if ((composingText.size(ComposingText.LAYER1) + mFunfun) < 1) {
                    return false;
                } else {
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!candidatesViewManager.isFocusCandidate()) {
                    getInputViewManager().onUpdateState(mWnnSwitcher);
                }
                break;

            case KeyEvent.KEYCODE_PAGE_UP:
                if (OpenWnn.isTabletMode()) {
                    if (candidatesViewManager.getCurrentView().isShown()) {
                        candidatesViewManager.scrollPageAndUpdateFocus(false);
                        return true;
                    } else {
                        return false;
                    }
                }
                if (candidatesViewManager.getCurrentView().isShown()) {
                    if (mFullCandidate) {
                        candidatesViewManager.scrollPageAndUpdateFocus(false);
                    } else if (mTextCandidatesViewManager.getCanReadMore()) {
                        mTextCandidatesViewManager.setFullMode();
                    }
                    return true;
                } else {
                    return false;
                }

            case KeyEvent.KEYCODE_PAGE_DOWN:
                if (OpenWnn.isTabletMode()) {
                    if (candidatesViewManager.getCurrentView().isShown()) {
                        candidatesViewManager.scrollPageAndUpdateFocus(true);
                        return true;
                    } else {
                        return false;
                    }
                }
                if (candidatesViewManager.getCurrentView().isShown()) {
                    if (mFullCandidate) {
                        candidatesViewManager.scrollPageAndUpdateFocus(true);
                    } else if (mTextCandidatesViewManager.getCanReadMore()) {
                        mTextCandidatesViewManager.setFullMode();
                    }
                    return true;
                } else {
                    return false;
                }

            case KeyEvent.KEYCODE_PICTSYMBOLS:
                commitAllText();
                if (mEnableEmoji) {
                    changeEngineMode(ENGINE_MODE_SYMBOL_EMOJI);
                } else {
                    if (mEnableSymbolListNonHalf) {
                        changeEngineMode(ENGINE_MODE_SYMBOL_KAO_MOJI);
                    } else {
                        changeEngineMode(ENGINE_MODE_SYMBOL_SYMBOL);
                    }
                }
                return true;

            default:
                ; // nothing to do.
                break;

            }

            /* handle other key event */
            ret = processKeyEvent(keyEvent);
            break;

        case OpenWnnEvent.INPUT_SOFT_KEY:
            ret = processKeyEvent(keyEvent);
            if (!ret) {
                sendKeyEventDirect(keyCode);
                ret = true;
            }
            break;

        case OpenWnnEvent.SELECT_CANDIDATE:
            initCommitInfoForWatchCursor();
            mStatus = commitText(ev.word);
            checkCommitInfo();
            break;

        case OpenWnnEvent.CONVERT:
            if (mEngineState.isRenbun()) {
                if (!candidatesViewManager.isFocusCandidate()) {
                    processDownKeyEvent();
                }
                processRightKeyEvent();
                break;
            }
            startConvert(EngineState.CONVERT_TYPE_RENBUN);
            break;

        case OpenWnnEvent.COMMIT_COMPOSING_TEXT:
            commitAllText();
            break;

        case OpenWnnEvent.CALL_MUSHROOM:
            callMushRoom(ev.word);
            break;

        default:
            ; // nothing to do.
            break;
        }

        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvaluateFullscreenMode */
    @Override public boolean onEvaluateFullscreenMode() {
        if (DEBUG) {Log.d(TAG, "onEvaluateFullscreenMode()");}
        boolean ret = false;
        if (!OpenWnn.isTabletMode()) {
            if (!mEnableFullscreen ||
                (getResources().getConfiguration().hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES)) {
                ret = false;
            } else {
                ret = onEvaluateFullscreenModeOpenWnn();
            }
        }
        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvaluateInputViewShown */
    @Override public boolean onEvaluateInputViewShown() {
        // Always show the keyboard
        return true;
    }

    /**
     * Create a <code>StrSegment</code> from a character code.
     * <br>
     * @param charCode character code
     * @return <code>StrSegment</code> created; null if an error occurs.
     */
    private StrSegment createStrSegment(int charCode) {
        if (charCode == 0 || (charCode & KeyCharacterMap.COMBINING_ACCENT) != 0 || charCode == PRIVATE_AREA_CODE) {
            return null;
        }
        return new StrSegment(Character.toChars(charCode));
    }

    /**
     * Key event handler.
     *
     * @param ev        A key event
     * @return  {@code true} if the event is handled in this method.
     *          {@code false} if not.
     */
    private boolean processKeyEvent(KeyEvent ev) {
        if (ev == null) {
            return false;
        }
        int key = ev.getKeyCode();
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        ComposingText composingText = getComposingText();
        InputViewManager inputViewManager = getInputViewManager();
        WnnEngine converter = getConverter();

        // keys which produce a glyph
        if (ev.isPrintingKey()) {
            if (isTenKeyCode(key) && !ev.isNumLockOn()) {
                // If the numeric keypad is pressed when NumLock is OFF,
                // in order to get the key events of the original notification, return false.
                return false;
            }
            if (ev.isCtrlPressed()){
                if (key == KeyEvent.KEYCODE_A || key == KeyEvent.KEYCODE_F || key == KeyEvent.KEYCODE_C ||
                    key == KeyEvent.KEYCODE_V || key == KeyEvent.KEYCODE_X || key == KeyEvent.KEYCODE_Z) {
                    if( composingText.size(ComposingText.LAYER1) + mFunfun < 1 ) {
                      return false;
                    } else  {
                      return true;
                    }
                }
            }
            /* do nothing if the character is not able to display or the character is dead key */
            if ((ev.isAltPressed() == true && ev.isShiftPressed() == true)) {
                int charCode = ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_ALT_ON);

                if (charCode == 0 || (charCode & KeyCharacterMap.COMBINING_ACCENT) != 0 || charCode == PRIVATE_AREA_CODE) {
                    return true;
                }
            }

            commitConvertingText();

            StrSegment str;

            /* get the key character */
            if (ev.isAltPressed() == false && ev.isShiftPressed() == false) {
                str = createStrSegment(ev.getUnicodeChar());
            } else if (ev.isShiftPressed() == true && ev.isAltPressed() == false) {
                str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
            } else if (ev.isShiftPressed() == false && ev.isAltPressed() == true) {
                str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_ALT_ON));
            } else {
                str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON
                                         | MetaKeyKeyListener.META_ALT_ON));
            }

            if (str == null) {
                return true;
            }

            /* append the character to the composing text if the character is not TAB */
            if (str.string.charAt(0) != '\u0009') { // not TAB
                processHardwareKeyboardInputChar(str);
                return true;
            }else{
                commitText(true);
                commitText(str.string);
                initializeScreen();
                return true;
            }

        } else if (key == KeyEvent.KEYCODE_SPACE) {
            processHardwareKeyboardSpaceKey(ev);
            return true;

        } else if (key == KeyEvent.KEYCODE_MENU && mEnableMushroom && ev.isAltPressed()) {
            callMushRoom(null);
            return true;

        } else if (key == KeyEvent.KEYCODE_SYM) {
            /* display the symbol list */
            initCommitInfoForWatchCursor();
            mStatus = commitText(true);
            checkCommitInfo();
            if (mEnableEmoji) {
                changeEngineMode(ENGINE_MODE_SYMBOL_EMOJI);
            } else {
                changeEngineMode(ENGINE_MODE_SYMBOL_SYMBOL);
            }
            return true;
        } // else {}

        if (key == KeyEvent.KEYCODE_BACK) {
            KeyboardView keyboardView = ((DefaultSoftKeyboard) inputViewManager).getKeyboardView();
            if (keyboardView.handleBack()) {
                return true;
            }
        }

        if (DEBUG) {Log.d(TAG, "mComposingText.size="+composingText.size(1));}
        // Functional key
        if ((composingText.size(ComposingText.LAYER1) + mFunfun) > 0) {
            switch (key) {
            case KeyEvent.KEYCODE_MOVE_HOME:
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_HOME);
                } else {
                    int len = composingText.getCursor(ComposingText.LAYER1);
                    int funfunlen = mFunfun;
                    for (int i = 0; i <= len + funfunlen; i++) {
                        processLeftKeyEvent();
                    }
                }
                return true;

            case KeyEvent.KEYCODE_MOVE_END:
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_END);
                } else {
                    int pos = composingText.getCursor(ComposingText.LAYER1);
                    int maxlen = composingText.toString(ComposingText.LAYER1).length();
                    if (pos != maxlen) {
                        if (mFunfun == 0) {
                            for (;;) {
                                if (mEngineState.isRenbun()) {
                                    if (pos == maxlen) {
                                        break;
                                    }
                                } else {
                                    if (pos == maxlen + 1) {
                                        break;
                                    }
                                }
                                processRightKeyEvent();
                                pos++;
                            }
                        }
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL:
                if (processDelKeyEventForUndo(ev)) {
                    return true;
                }

                mStatus = STATUS_INPUT_EDIT;
                if (mEngineState.isConvertState()) {
                    composingText.setCursor(ComposingText.LAYER1,
                                             composingText.toString(ComposingText.LAYER1).length());
                    mExactMatchMode = false;
                    mFunfun = 0;
                    setFunFun(mFunfun);
                } else {
                    if (mFunfun > 0) {
                        mFunfun = 0;
                        setFunFun(mFunfun);
                    } else {
                        if ((OpenWnn.isTabletMode())
                                && (ev.isAltPressed() == true ||
                                    ev.isShiftPressed() == true ||
                                    ev.isCtrlPressed() == true)) {
                            return true;
                        } else {
                            if (key == KeyEvent.KEYCODE_FORWARD_DEL) {
                                composingText.deleteForward(ComposingText.LAYER1);
                             } else {
                                 if ((composingText.size(ComposingText.LAYER1) == 1)
                                        && composingText.getCursor(ComposingText.LAYER1) != 0) {
                                    initializeScreen();
                                    return true;
                                } else {
                                    composingText.delete(ComposingText.LAYER1, false);
                                }
                            }
                        }
                    }
                }
                updateViewStatusForPrediction(true, true); // prediction only
                return true;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                if (DEBUG) {Log.d(TAG, "mCandidatesViewManager.getViewType()="+candidatesViewManager.getViewType());}
                if (candidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                    setCandidateIsViewTypeFull(false);
                } else {
                    if (((DefaultSoftKeyboardHangul)inputViewManager).processBackKeyEvent()) {
                        return true;
                    } else if (!mEngineState.isConvertState()) {
                        initializeScreen();
                        if (converter != null) {
                            converter.init(mWnnSwitcher.getFilesDirPath());
                        }
                    } else {
                        candidatesViewManager.clearCandidates();
                        mStatus = STATUS_INPUT_EDIT;
                        mFunfun = 0;
                        setFunFun(mFunfun);
                        mExactMatchMode = false;
                        composingText.setCursor(ComposingText.LAYER1,
                                                composingText.toString(ComposingText.LAYER1).length());
                        updateViewStatusForPrediction(true, true); // clear
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!isEnableL2Converter()) {
                    commitText(false);
                    return false;
                } else {
                    processLeftKeyEvent();
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!isEnableL2Converter()) {
                    if (mEngineState.keyboard == EngineState.KEYBOARD_12KEY) {
                        commitText(false);
                    }
                } else {
                    processRightKeyEvent();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                processDownKeyEvent();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (candidatesViewManager.isFocusCandidate()) {
                    processUpKeyEvent();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                //select Candidate
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.selectFocusCandidate();
                    return true;
                }

                int cursor = composingText.getCursor(ComposingText.LAYER1) + mFunfun;
                if (cursor < 1) {
                    return true;
                }

                int funfun = mFunfun;
                mHasRequiredFunfunSpace = true;

                initCommitInfoForWatchCursor();
                mStatus = commitText(true);
                checkCommitInfo();

                if (0 < funfun) {
                    initializeScreen();
                    breakSequence();
                }
                return true;

            case KeyEvent.KEYCODE_TAB:
                processTabKeyEvent(ev);
                return true;

            default:
                return !isThroughKeyCode(key);
            }
        } else {
            // if there is no composing string.
            if (candidatesViewManager.getCurrentView().isShown()) {
                // display relational prediction candidates
                switch (key) {
                case KeyEvent.KEYCODE_MOVE_HOME:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_HOME);
                    }
                    return true;

                case KeyEvent.KEYCODE_MOVE_END:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_END);
                    }
                    return true;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
                        return true;
                    }
                    if (isEnableL2Converter()) {
                        // initialize the converter
                        converter.init(mWnnSwitcher.getFilesDirPath());
                    }
                    mStatus = STATUS_INPUT_EDIT;
                    updateViewStatusForPrediction(true, true);
                    return false;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
                        return true;
                    }
                    if (mEnableFunfun) {
                        mFunfun++;
                        setFunFun(mFunfun);
                    } else if (isEnableL2Converter()) {
                        /* initialize the converter */
                        converter.init(mWnnSwitcher.getFilesDirPath());
                    } // else {}
                    mStatus = STATUS_INPUT_EDIT;
                    updateViewStatusForPrediction(true, true);

                    if (mEnableFunfun) {
                        return true;
                    } else {
                        return false;
                    }

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    processDownKeyEvent();
                    return true;

                case KeyEvent.KEYCODE_DPAD_UP:
                    if (candidatesViewManager.isFocusCandidate()) {
                        processUpKeyEvent();
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.selectFocusCandidate();
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_TAB:
                    processTabKeyEvent(ev);
                    return true;

                default:
                    break;

                }
                return processKeyEventNoInputCandidateShown(ev);
            } else {
                switch (key) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    OpenWnn ime = OpenWnn.getCurrentIme();
                    if ((ime != null) && ime.mOriginalInputMethodSwitcher.mSymbolMode) {
                        getSwitcher().changeMode(ime.mOriginalInputMethodSwitcher.IME_TYPE_HANDWRITING);
                        return true;
                    }

                    if (((DefaultSoftKeyboardHangul)inputViewManager).processBackKeyEvent()) {
                        return true;
                    }
                    /*
                     * If 'BACK' key is pressed when the SW-keyboard is shown
                     * and the candidates view is not shown, dissmiss the SW-keyboard.
                     */
                    if (isInputViewShown()) {
                        inputViewManager.closing();
                        requestHideSelf(0);
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DEL:
                case KeyEvent.KEYCODE_FORWARD_DEL:
                    if (processDelKeyEventForUndo(ev)) {
                        return true;
                    }
                    break;
                default:
                    break;
                }
            }
        }

        return false;
    }

    /**
     * Handle the space key event from the Hardware keyboard.
     *
     * @param ev  The space key event
     */
    private void processHardwareKeyboardSpaceKey(KeyEvent ev) {
        /* H/W space key */
        if (ev.isAltPressed()){
            commitAllText();
            if (mEnableEmoji) {
                changeEngineMode(ENGINE_MODE_SYMBOL_EMOJI);
            } else {
                changeEngineMode(ENGINE_MODE_SYMBOL_SYMBOL);
            }
        } else {
            processKeyboardSpaceKey();
        }
    }

    /**
     * Handle the character code from the hardware keyboard except the space key.
     *
     * @param str  The input character
     */
    private void processHardwareKeyboardInputChar(StrSegment str) {
        if (isEnableL2Converter()) {
            boolean commit = false;
            Matcher m = mEnglishAutoCommitDelimiter.matcher(str.string);
            if (m.matches()) {
                commitText(true);
                commit = true;
            }
            appendStrSegment(str);

            if (commit) {
                commitText(true);
            } else {
                mStatus = STATUS_INPUT;
                mFunfun = 0;
                setFunFun(mFunfun);
                updateViewStatusForPrediction(true, true);
            }
        } else {
            appendStrSegment(str);
            boolean completed = true;

            if (completed) {
                commitText(false);
            } else {
                updateViewStatus(ComposingText.LAYER1, false, true);
            }
        }
    }

    /**
     * Update candidates.
     */
    private void updatePrediction() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        ComposingText composingText = getComposingText();
        WnnEngine converter = getConverter();
        int candidates = 0;
        int cursor = composingText.getCursor(ComposingText.LAYER1);
        if (isEnableL2Converter() || mEngineState.isSymbolList()) {
            if (mExactMatchMode) {
                // exact matching
                mConverterIWnn.setConvertedCandidateEnabled(true);
                candidates = converter.predict(composingText, 0, cursor);
            } else {
                if (mEnableFunfun && mFunfun > 0) {
                    // funfun prediction
                    if (mFunfun < 4) {
                        // try exact matching
                        candidates = converter.predict(composingText, cursor + mFunfun,
                                                        cursor + mFunfun);
                    }
                    if (candidates <= 0) {
                        // try prefix matching
                        while ((candidates = converter.predict(composingText,
                                                                cursor + mFunfun, -1)) <= 0) {
                            // if there is no candidates, decrement funfun length.
                            if (--mFunfun == 0) {
                                setFunFun(mFunfun);
                                candidates = converter.predict(composingText, 0, -1);
                                break;
                            }
                        }
                    }
                } else {
                    // normal prediction
                    candidates = converter.predict(composingText, 0, -1);
                }
            }
        } else {
            mFunfun = 0;
            setFunFun(mFunfun);
        }

        // update the candidates view
        if (candidates > 0) {
            mHasContinuedPrediction = (((composingText.size(ComposingText.LAYER1) + mFunfun) == 0)
                                       && !mEngineState.isSymbolList());
            candidatesViewManager.setEnableCandidateLongClick(true);
            candidatesViewManager.displayCandidates(converter);
        } else {
            candidatesViewManager.clearCandidates();
        }
    }

    /**
     * Handle a left key event.
     */
    private void processLeftKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        if (candidatesViewManager.isFocusCandidate()) {
            candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
            return;
        }

        ComposingText composingText = getComposingText();
        if (mEngineState.isConvertState()) {
            if (1 < composingText.getCursor(ComposingText.LAYER1)) {
                composingText.moveCursor(ComposingText.LAYER1, -1);
            }
        } else if (mExactMatchMode) {
            composingText.moveCursor(ComposingText.LAYER1, -1);
        } else {
            if (mEnableFunfun && mFunfun > 0) {
                mFunfun--;
                setFunFun(mFunfun);
            } else {
                mExactMatchMode = true;
            }
        }

        mCommitCount = 0; // Reconversion when converting it
        mStatus = STATUS_INPUT_EDIT;
        updateViewStatus(mTargetLayer, true, true);
    }

    /**
     * Handle a right key event.
     */
    private void processRightKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        if (candidatesViewManager.isFocusCandidate()) {
            candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
            return;
        }

        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();
        if (mExactMatchMode || (mEngineState.isConvertState())) {
            int textSize = composingText.size(ComposingText.LAYER1);
            if (composingText.getCursor(ComposingText.LAYER1) == textSize) {
                mExactMatchMode = false;
                mFunfun = 0;
                setFunFun(mFunfun);
                layer = ComposingText.LAYER1; // convert -> prediction

                EngineState state = new EngineState();
                state.convertType = EngineState.CONVERT_TYPE_NONE;
                updateEngineState(state);
            } else {
                composingText.moveCursor(ComposingText.LAYER1, 1);
            }
        } else {
            if (composingText.getCursor(ComposingText.LAYER1)
                    < composingText.size(ComposingText.LAYER1)) {
                composingText.moveCursor(ComposingText.LAYER1, 1);
            } else if (mEnableFunfun) {
                mFunfun++;
                setFunFun(mFunfun);
            } // else {}
        }

        mCommitCount = 0; // Reconversion when converting it
        mStatus = STATUS_INPUT_EDIT;

        updateViewStatus(layer, true, true);
    }

    /**
     * Handle a down key event.
     */
    private void processDownKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
    }

    /**
     * Handle a up key event.
     */
    private void processUpKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
    }

    /**
     * Handle a home key event.
     */
    private void processHomeKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_HOME);
    }

    /**
     * Handle a end key event.
     */
    private void processEndKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_END);
    }

    /**
     * Processing the input keycode. [DEL] [Undo Only processing]
     *
     * @param ev        A key event
     * @return {@code true} when event is digested. {@code false} if not.
     */
    private boolean processDelKeyEventForUndo(KeyEvent ev){
        //Undo, if the Key event is "Shift+Del"
        if (ev.isShiftPressed()) {
            if (undo()) {
                return true;
            }
        }
        clearCommitInfo();
        return false;
    }

    /**
     * Handle a key event which is not right or left key when the
     * composing text is empty and some candidates are shown.
     *
     * @param ev        A key event
     * @return          {@code true} if this consumes the event; {@code false} if not.
     */
    boolean processKeyEventNoInputCandidateShown(KeyEvent ev) {
        boolean ret = true;

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        int key = ev.getKeyCode();

        switch (key) {
        case KeyEvent.KEYCODE_DEL:
        case KeyEvent.KEYCODE_FORWARD_DEL:
            if (processDelKeyEventForUndo(ev)) {
                return true;
            }

            // Only erasing the candidate window
            ret = true;
            break;
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_NUMPAD_ENTER:
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_MENU:
            ret = false;
            break;

        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ESCAPE:
            ret = true;
            break;

        case KeyEvent.KEYCODE_BACK:
            candidatesViewManager = getCandidatesViewManager();
            if (candidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                setCandidateIsViewTypeFull(false);
                return true;
            } else if (((DefaultSoftKeyboardHangul)getInputViewManager()).processBackKeyEvent()) {
                return true;
            } else {
                ret = true;
            }
            break;

        default:
            return !isThroughKeyCode(key);
        }

        WnnEngine converter = getConverter();
        if (converter != null) {
            // initialize the converter
            converter.init(mWnnSwitcher.getFilesDirPath());
        }
        updateViewStatusForPrediction(true, true);
        return ret;
    }

    /**
     * Send key events (DOWN and UP) to the text field directly.
     *
     * @param keyCode  Key event code
     */
    private void sendKeyEventDirect(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            sendKeyChar('\n');
        } else {
            sendDownUpKeyEvents(keyCode);
        }
    }

    /**
     * Update views and the display of the composing text for predict mode.
     *
     * @param updateCandidates  {@code true} to update the candidates view.
     *                          {@code false} the candidates view is not updated.
     * @param updateEmptyText   {@code true} to update always.
     *                          {@code false} to update the composing text if it is not empty.
     */
    private void updateViewStatusForPrediction(boolean updateCandidates, boolean updateEmptyText) {
        EngineState state = new EngineState();
        state.convertType = EngineState.CONVERT_TYPE_NONE;
        updateEngineState(state);

        updateViewStatus(ComposingText.LAYER1, updateCandidates, updateEmptyText);
    }

    /**
     * Update views and the display of the composing text.
     *
     * @param layer             Display layer of the composing text
     * @param updateCandidates  {@code true} to update the candidates view.
     *                          {@code false} the candidates view is not updated.
     * @param updateEmptyText   {@code true} to update always.
     *                          {@code false} to update the composing text if it is not empty.
     */
    @Override protected void updateViewStatus(int layer, boolean updateCandidates, boolean updateEmptyText) {
        mTargetLayer = layer;
        ComposingText composingText = getComposingText();

        if (updateCandidates) {
            updateCandidateView();
            if (mFullCandidate) {
                mFullCandidate = false;
                updateFullscreenMode();
            }
        }
        // notice to the input view
        getInputViewManager().onUpdateState(mWnnSwitcher);

        // set the text for displaying as the composing text
        mDisplayText.clear();

        int keymode = ((DefaultSoftKeyboardHangul)getInputViewManager()).getKeyMode();
        if (keymode == DefaultSoftKeyboardHangul.KEYMODE_KO_HANGUL) {
            mDisplayText.insert(0, composingText.toString(ComposingText.LAYER2));
        } else {
            mDisplayText.insert(0, composingText.toString(layer));
        }

        for (int i = 0; i < mFunfun; i++) {
            mDisplayText.append("\u25cb");
        }

        // add decoration to the text
        int cursor = composingText.getCursor(layer);
        InputConnection inputConnection = getInputConnection();

        if ((inputConnection != null) && (mDisplayText.length() != 0 || updateEmptyText)) {
            if (cursor != 0) {
                int highlightEnd = 0;

                if (mExactMatchMode) {

                    mDisplayText.setSpan(SPAN_EXACT_BGCOLOR_HL, 0, cursor,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    highlightEnd = cursor;

                } else if (layer == ComposingText.LAYER2) {
                    highlightEnd = composingText.toString(layer, 0, 0).length();

                    // highlights the first segment
                    mDisplayText.setSpan(SPAN_CONVERT_BGCOLOR_HL_HANGUL, 0,
                                         highlightEnd,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } // else {}

                if (highlightEnd != 0) {
                    // highlights remaining text
                    mDisplayText.setSpan(SPAN_REMAIN_BGCOLOR_HL, highlightEnd,
                                         composingText.toString(layer).length(),
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // text color in the highlight
                    mDisplayText.setSpan(SPAN_TEXTCOLOR, 0,
                                         composingText.toString(layer).length(),
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            mDisplayText.setSpan(SPAN_UNDERLINE, 0, mDisplayText.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int displayCursor = ((cursor + mFunfun) == 0) ?  0 : 1;

            // update the composing text on the EditView
            if ((mDisplayText.length() != 0) || !mHasStartedTextSelection) {
                inputConnection.setComposingText(mDisplayText, displayCursor);
            }
        }
    }

    /**
     * Update the candidates view.
     */
    private void updateCandidateView() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        ComposingText composingText = getComposingText();
        WnnEngine converter = getConverter();
        switch (mTargetLayer) {
        case ComposingText.LAYER0:
        case ComposingText.LAYER1: // prediction
            int keymode = ((DefaultSoftKeyboardHangul)getInputViewManager()).getKeyMode();
            if (keymode == DefaultSoftKeyboardHangul.KEYMODE_KO_HANGUL) {
                composingText.setCursor(ComposingText.LAYER1, 0);
                mFunfun = 0;
                setFunFun(mFunfun);
                mExactMatchMode = false;
                mCommitCount = 0;
                converter.convert(composingText);
            }
            if (mEnablePrediction || mEngineState.isSymbolList()) {
                // update the candidates view
                updatePrediction();
            } else {
                candidatesViewManager.clearCandidates();
            }
            break;
        case ComposingText.LAYER2: // convert
            if (mCommitCount == 0) {
                mConverterIWnn.setConvertedCandidateEnabled(false);
                converter.convert(composingText);
            }

            int candidates = converter.makeCandidateListOf(mCommitCount);

            if (candidates != 0) {
                composingText.setCursor(ComposingText.LAYER2, 1);
                boolean isExistOtherSegment = (composingText.size(ComposingText.LAYER2) > 1);
                candidatesViewManager.setEnableCandidateLongClick(!isExistOtherSegment);
                candidatesViewManager.displayCandidates(converter);
            } else {
                composingText.setCursor(ComposingText.LAYER1,
                                        composingText.toString(ComposingText.LAYER1).length());
                candidatesViewManager.clearCandidates();
            }
            break;
        default:
            break;
        }
    }

    /**
     * Commit the displaying composing text.
     *
     * @param learn  {@code true} to register the committed string to the learning dictionary.
     * @return          IME's status after commit
     */
    private int commitText(boolean learn) {
        ComposingText composingText = getComposingText();
        int layer = mTargetLayer;

        int keymode = ((DefaultSoftKeyboardHangul)getInputViewManager()).getKeyMode();
        if (keymode == DefaultSoftKeyboardHangul.KEYMODE_KO_HANGUL) {
            layer = ComposingText.LAYER2;
        }

        int cursor = composingText.getCursor(layer);
        String tmp = composingText.toString(layer, 0, cursor - 1);

        if (getConverter() != null) {
            if (learn) {
                if (mEngineState.isRenbun()) {
                    learnWord(null);
                } else {
                    if (composingText.size(ComposingText.LAYER1) != 0) {
                        WnnWord word = new WnnWord(0, tmp, tmp,
                                                   iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN);
                        learnWord(word);
                    }
                }
            } else {
                breakSequence();
            }
        }
        return commitTextThroughInputConnection(tmp);
    }

    /**
     * Commit all uncommitted words.
     */
    private void commitAllText() {
        initCommitInfoForWatchCursor();
        if (mEngineState.isConvertState()) {
            commitConvertingText();
        } else {
            ComposingText composingText = getComposingText();
            composingText.setCursor(ComposingText.LAYER1,
                                    composingText.size(ComposingText.LAYER1));
            mStatus = commitText(true);
        }
        checkCommitInfo();
    }

    /**
     * Commit a word.
     *
     * @param word              A word to commit
     * @return                  IME's status after commit
     */
    private int commitText(WnnWord word) {
        if (getConverter() != null) {
            learnWord(word);
        }

        int mode = word.getSymbolMode();
        if ((mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_SYMBOL)
                || (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_EMOJI)
                || (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_DECOEMOJI)
                || (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_MIXED_EMOJI)) {

            mConverterIWnn.init(mWnnSwitcher.getFilesDirPath());
            return commitComposingText(DecoEmojiUtil.getSpannedCandidate(word));
        } else {
            return commitTextThroughInputConnection(word.candidate);
        }
    }

    /**
     * Commit a string.
     *
     * @param str  A string to commit
     */
    private void commitText(String str) {
        mFunfun = 0;
        setFunFun(mFunfun);
        getInputConnection().commitText(str, 1);
        mPrevCommitText.append(str);
        mIgnoreCursorMove = true;

        updateViewStatusForPrediction(false, false);
        // for debug
        //mComposingText.debugout();
    }

    /**
     * Commit a string through {@link android.view.inputmethod.InputConnection}.
     *
     * @param string  A string to commit
     * @return                  IME's status after committing
     */
    private int commitComposingText(CharSequence string) {
        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();

        if (0 < string.length()) {
            calculateSizeOfDeleteStroke();
            // Through YOMI of committed text for APP
            commitTextToInputConnection(string);
            mPrevCommitText.append(string);
            mIgnoreCursorMove = true;

            int cursor = composingText.getCursor(layer);
            if (cursor > 0) {
                int check = cursor - mSizeOfDeleteStroke;
                if ((mSizeOfDeleteStroke > 0) && (check > 0) && mEnableHeadConv) {
                    composingText.deleteStrSegment(layer, 0, mSizeOfDeleteStroke - 1);
                } else {
                    composingText.deleteStrSegment(layer, 0, composingText.getCursor(layer) - 1);
                }
                composingText.setCursor(layer, composingText.size(layer));
            }
            mExactMatchMode = false;
            mFunfun = 0;
            setFunFun(mFunfun);
            mCommitCount++;

            if ((layer == ComposingText.LAYER2) && (composingText.size(layer) == 0)) {
                layer = 1; // for connected prediction
            }

            if (layer == ComposingText.LAYER2) {
                EngineState state = new EngineState();
                state.convertType = EngineState.CONVERT_TYPE_RENBUN;
                updateEngineState(state);

                updateViewStatus(layer, true, false);
            } else {
                updateViewStatusForPrediction(true, false);
            }
        }

        if (composingText.size(ComposingText.LAYER0) == 0) {
            return STATUS_INIT;
        } else {
            return STATUS_INPUT_EDIT;
        }
    }

    /**
     * Calculate size of the stroke to delete
     *
     */
    private void calculateSizeOfDeleteStroke() {
        mSizeOfDeleteStroke = 0;

        if (mStrokeOfCommitText == null){
            return;
        } // else {}

        mSizeOfDeleteStroke = mStrokeOfCommitText.length();
        return ;
    }


    /**
     * Commit text and input connection.
     *
     * @param commitText  committed text
     */
    public void commitTextToInputConnection(CharSequence commitText) {
        InputConnection inputConnection = getInputConnection();

        inputConnection.commitText(commitText, 1);
        Bundle data = new Bundle();
        if ((mStrokeOfCommitText != null) && (mStrokeOfCommitText.length() > 0)) {
            data.putString(COMMIT_TEXT_THROUGH_KEY_YOMI, mStrokeOfCommitText);
        } else {
            data.putString(COMMIT_TEXT_THROUGH_KEY_YOMI, commitText.toString());
        }
        inputConnection.performPrivateCommand(COMMIT_TEXT_THROUGH_ACTION, data);
        mStrokeOfCommitText = null;
    }

    /**
     * Commit a string through {@link android.view.inputmethod.InputConnection}.
     *
     * @param string  A string to commit
     * @return  IME's status after commit
     */
    private int commitTextThroughInputConnection(String string) {
        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();

        if (mHasRequiredFunfunSpace) {
            int funfun = mFunfun;
            if (0 < funfun) {
                StringBuilder builder = new StringBuilder(string);
                for (int i = 0; i < funfun; i++) {
                    builder.append(' ');
                }
                string = builder.toString();
            }
            mHasRequiredFunfunSpace = false;
        }

        if (0 < string.length()) {
            getInputConnection().commitText(string, 1);
            mPrevCommitText.append(string);
            mIgnoreCursorMove = true;

            int cursor = composingText.getCursor(layer);
            if (cursor > 0) {
                composingText.deleteStrSegment(layer, 0, composingText.getCursor(layer) - 1);
                composingText.setCursor(layer, composingText.size(layer));
            }
            mExactMatchMode = false;
            mFunfun = 0;
            setFunFun(mFunfun);
            mCommitCount++;

            if ((layer == ComposingText.LAYER2) && (composingText.size(layer) == 0)) {
                layer = 1; // for connected prediction
            }
            boolean committed = false;
            if ((EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI == mEngineState.preferenceDictionary) && (!mEngineState.isSymbolList())) {
                committed = true;
                getCandidatesViewManager().clearCandidates();
             }

            if (layer == ComposingText.LAYER2) {
                EngineState state = new EngineState();
                state.convertType = EngineState.CONVERT_TYPE_RENBUN;
                updateEngineState(state);

                updateViewStatus(layer, !committed, false);
            } else {
                updateViewStatusForPrediction(!committed, false);
            }
        }

        if (composingText.size(ComposingText.LAYER0) == 0) {
            return STATUS_INIT;
        } else {
            return STATUS_INPUT_EDIT;
        }
    }

    /**
     * Change the conversion engine and the letter converter
     *
     * @param mode  Engine's mode to be changed
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnnEvent.Mode
     * @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboardHangul
     */
    private void changeEngineMode(int mode) {
        EngineState state = new EngineState();

        switch (mode) {
        case ENGINE_MODE_OPT_TYPE_QWERTY:
            state.keyboard = EngineState.KEYBOARD_QWERTY;
            updateEngineState(state);
            clearCommitInfo();
            return;

        case ENGINE_MODE_SYMBOL:
            if (mEnableSymbolList && !isDirectInputMode()) {
                mFunfun = 0;
                setFunFun(mFunfun);
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL;
                updateEngineState(state);
                updateViewStatusForPrediction(true, true);
            }
            return;

        case ENGINE_MODE_SYMBOL_EMOJI:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_EMOJI);
            return;

        case ENGINE_MODE_SYMBOL_DECOEMOJI:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_DECOEMOJI);
            return;

        case ENGINE_MODE_SYMBOL_SYMBOL:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_OTHERS_SYMBOL);
            return;

        case ENGINE_MODE_SYMBOL_KAO_MOJI:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_OTHERS_KAO_MOJI);
            return;

        case ENGINE_MODE_SYMBOL_ADD_SYMBOL:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_ADD_SYMBOL);
            return;

        case OpenWnnEvent.Mode.DIRECT:
            setConverter(null);
            break;

        case OpenWnnEvent.Mode.DEFAULT:
        default:
            if(mAttribute != null){
                switch (mAttribute.inputType & EditorInfo.TYPE_MASK_FLAGS) {
                case EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS:
                    int keymode = ((DefaultSoftKeyboardHangul)getInputViewManager()).getKeyMode();
                    if (keymode == DefaultSoftKeyboardHangul.KEYMODE_KO_ALPHABET || keymode == DefaultSoftKeyboardHangul.KEYMODE_KO_NUMBER) {
                        mEnablePrediction = false;
                        mEnableLearning = false;
                        mEnableConverter = false;
                        commitText(true);
                        initializeScreen();
                    } else {
                        SharedPreferences pref = PreferenceManager
                                .getDefaultSharedPreferences(OpenWnn.superGetContext());
                        mEnablePrediction = pref.getBoolean("opt_prediction", true);
                        mEnableLearning   = pref.getBoolean("opt_enable_learning", true);
                        mEnableConverter = true;
                    }
                    break;
                default:
                    break;
                }
            }
            setConverter(mConverterIWnn);
            break;
        }

        mConverterBack = getConverter();
    }

    /**
     * Update the conversion engine's state.
     *
     * @param state  Engine's state to be updated
     */
    private void updateEngineState(EngineState state) {
        if (DEBUG) {Log.d(TAG, "updateEngineState()");}

        EngineState myState = mEngineState;

        // Language setting
        if (state.language != EngineState.INVALID) {
            myState.language = state.language;
            setDictionary();
            breakSequence();

            /* update keyboard setting */
            if (state.keyboard == EngineState.INVALID) {
                state.keyboard = myState.keyboard;
            }
        }

        // Conversion type setting
        if ((state.convertType != EngineState.INVALID)
            && (myState.convertType != state.convertType)) {

            myState.convertType = state.convertType;
            setDictionary();
        }

        // Switch setting Temporary dictionary.
        if (state.temporaryMode != EngineState.INVALID) {

            switch (state.temporaryMode) {
            case EngineState.TEMPORARY_DICTIONARY_MODE_NONE:
                if (DEBUG) {Log.d(TAG, "EngineState.TEMPORARY_DICTIONARY_MODE_NONE");}

                if (myState.temporaryMode != EngineState.TEMPORARY_DICTIONARY_MODE_NONE) {
                    setDictionary();
                    mConverterSymbolEngineBack.initializeMode();
                    setConverter(mConverterBack);
                    mTextCandidatesViewManager.setSymbolMode(false, 0);
                    ((DefaultSoftKeyboard)getInputViewManager()).setNormalKeyboard();
                }
                break;

            case EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL:
                if (DEBUG) {Log.d(TAG, "EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL");}

                setConverter(mConverterSymbolEngineBack);

                if (OpenWnn.isTabletMode()) {
                    setCandidatesViewManager(mTextCandidatesViewManager);
                    View view = mTextCandidatesViewManager.getCurrentView();
                    if (view != null) {
                        getSwitcher().setCandidatesView(view);
                    }
                }

                mTextCandidatesViewManager.setSymbolMode(true, mConverterSymbolEngineBack.getMode());
                mConverterSymbolEngineBack.updateAdditionalSymbolInfo(false, false);
                ((DefaultSoftKeyboard)getInputViewManager()).setSymbolKeyboard();
                break;

            default:
                break;
            }

            myState.temporaryMode = state.temporaryMode;
        }

        // Set the preference dictionary
        if ((state.preferenceDictionary != EngineState.INVALID)
            && (myState.preferenceDictionary != state.preferenceDictionary)) {

            if (DEBUG) {Log.d(TAG, "EngineState.preferenceDictionary");}
            myState.preferenceDictionary = state.preferenceDictionary;

            setDictionary();
        }

        /* keyboard type */
        if (state.keyboard != EngineState.INVALID) {
            int flexibleType;
            int keyboardType;

            switch (state.keyboard) {
            case EngineState.KEYBOARD_12KEY:
                flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_ON;
                keyboardType = iWnnEngine.KeyboardType.KEY_TYPE_KEYPAD12;
                break;

            case EngineState.KEYBOARD_QWERTY:
            default:
                keyboardType = iWnnEngine.KeyboardType.KEY_TYPE_QWERTY;
                if (!mEnableSpellCorrection) {
                    flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_OFF;
                } else {
                    flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_ON;
                }
                break;
            }

            // Doesn't process while setting change.
            if (!OpenWnn.getCurrentIme().isKeepInput()) {
                mConverterIWnn.setFlexibleCharset(flexibleType, keyboardType);
            }
            myState.keyboard = state.keyboard;
        }
    }

    /**
     * Set dictionaries to be used.
     */
    protected abstract void setDictionary();

    /**
     * Create the input view manager.
     *
     * @param wnn  instance of IWnnLanguageSwitcher
     * @return The input view manager for a specific language
     */
    protected abstract InputViewManager createInputViewManager(IWnnLanguageSwitcher wnn);

    /**
     * Return the specific language.
     *
     * @return The specific language type of {@link jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine.LanguageType} class
     */
    protected abstract int getLanguage();

    /**
     * Handle a toggle key input event.
     *
     * @param table  Table of toggle characters
     */
    private void processSoftKeyboardToggleChar(String[] table) {
        String c = null;
        ComposingText composingText = getComposingText();
        InputConnection inputConnection = getInputConnection();

        // Search previous character, and then search next character if found.
        if ((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT) {
            String prevChar = null;
            if (0 < composingText.size(ComposingText.LAYER1)) {
                int cursor = composingText.getCursor(ComposingText.LAYER1);
                if (0 < cursor) {
                    StrSegment strSegment = composingText.getStrSegment(ComposingText.LAYER1, cursor - 1);
                    if (strSegment != null) {
                        prevChar = strSegment.string;
                    }
                }
            } else {
                prevChar = inputConnection.getTextBeforeCursor(1, 0).toString();
            }

            if (prevChar != null) {
                c = searchToggleCharacter(prevChar, table, true);
            }
        }

        if (c != null) {
            /* Delete previous character. */
            if (0 < composingText.size(ComposingText.LAYER1)) {
                composingText.delete(ComposingText.LAYER1, false);
            } else {
                inputConnection.deleteSurroundingText(1, 0);
                mIgnoreCursorMove = true;
            }
        } else {
            /* Append first character in table to the composing text. */
            c = table[0];
        }

        commitConvertingText();

        if (!isEnableL2Converter() && !mEnablePrediction) {
            commitText(true);
            inputConnection.setComposingText("", 0); // Clears funfun text.
            CharSequence seq = inputConnection.getTextBeforeCursor(1, 0);
            if ((seq != null) && (0 < seq.length()) && (seq.charAt(0) == ' ')) {
                inputConnection.deleteSurroundingText(1, 0);
                mIgnoreCursorMove = true;
            }
            appendStrSegment(new StrSegment(c));
            commitText(false);

            // If can't input a character, try a next character from toggle cycle table.
            //   ex) The "," can't be inputted when the inputType is "numberDecimal"
            int size = table.length;
            for (int i = 0; (i < size); i++) {
                seq = inputConnection.getTextBeforeCursor(1, 0);
                if ((seq != null) && seq.toString().equals(c)) {
                    break; // input ok.
                }
                c = searchToggleCharacter(c, table, true);
                if (c == null) {
                    break; // end of table.
                }
                appendStrSegment(new StrSegment(c));
                commitText(false);
            }

            getCandidatesViewManager().clearCandidates();
        } else {
            appendStrSegment(new StrSegment(c));
        }

        mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TOGGLE_TIME_LIMIT),
                                    DELAY_MS_DISABLE_TOGGLE);

        mFunfun = 0;
        setFunFun(mFunfun);
        mStatus = STATUS_INPUT;
        updateViewStatusForPrediction(true, true);
    }

    /**
     * Handle character input from the software keyboard.
     *
     * @param chars   The input character(s)
     */
    private void processSoftKeyboardCode(char[] chars) {
        if (chars == null) {
            return;
        }

        if (chars[0] == ' ') {
            processKeyboardSpaceKey();
        } else {
            commitConvertingText();

            // And, an English predict is fixed for QWERTY by '.'etc.
            appendStrSegment(new StrSegment(chars));
            updateViewStatusForPrediction(true, true);
        }
    }

    /**
     * Handle character input from the software keyboard(space).
     */
    private void processKeyboardSpaceKey() {
        ComposingText composingText = getComposingText();
        if (composingText.size(0) == 0) {
            /* Insert space  */
            commitText(" ");
            getCandidatesViewManager().clearCandidates();
            breakSequence();
        } else {
            initCommitInfoForWatchCursor();
            commitText(true);
            commitSpaceJustOne();
            checkCommitInfo();
            if (mEngineState.isRenbun()) {
                CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
                if (!candidatesViewManager.isFocusCandidate()) {
                    processDownKeyEvent();
                }
                processRightKeyEvent();
                return;
            }

            /* Commit conversion string */
            int cursor = composingText.getCursor(ComposingText.LAYER1) + mFunfun;
            if (cursor < 1) {
                return;
            }

            int funfun = mFunfun;

            initCommitInfoForWatchCursor();
            mStatus = commitText(true);
            checkCommitInfo();

            if (0 < funfun) {
                initializeScreen();
                breakSequence();
            }
        }
     }

    /**
     * Start consecutive clause conversion or EISU-KANA conversion mode.
     *
     * @param convertType   The conversion type({@code EngineState.CONVERT_TYPE_*})
     */
    private void startConvert(int convertType) {
        if (!isEnableL2Converter()) {
            return;
        }

        ComposingText composingText = getComposingText();
        if (mEngineState.convertType != convertType) {
            // The cursor is specified for an appropriate position if there is no range specification.
            if (!mExactMatchMode) {
                if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
                    // The clause position is not specified when consecutive clause conversion.
                    composingText.setCursor(ComposingText.LAYER1, 0);
                } else {
                    if (mEngineState.isRenbun()) {
                        /* EISU-KANA conversion specifying the position of the segment if previous mode is conversion mode */
                        mExactMatchMode = true;
                    } else {
                        // It is a range specification as for all input character. (EISU-KANA)
                        composingText.setCursor(ComposingText.LAYER1,
                                                composingText.size(ComposingText.LAYER1));
                    }
                }
            }

            if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
                // clears variables for the prediction
                mFunfun = 0;
                setFunFun(mFunfun);
                mExactMatchMode = false;
            }
            // clears variables for the convert
            mCommitCount = 0;

            EngineState state = new EngineState();
            state.convertType = convertType;
            updateEngineState(state);

            updateViewStatus(ComposingText.LAYER2, true, true);
        }
    }

    /**
     * Get the shift key state from the editor.
     *
     * @param editor  editor
     * @return state id of the shift key (0:off, 1:on)
     */
    protected int getShiftKeyState(EditorInfo editor) {
        return (getCurrentInputConnection().getCursorCapsMode(editor.inputType) == 0) ? 0 : 1;
    }

    /**
     * Memorize a selected word.
     *
     * @param word  a selected word
     */
    private void learnWord(WnnWord word) {
        if (word == null) {
            /* learning ConvertMultiple */
            mConverterIWnn.learn(mEnableLearning);
        } else {
            /* learning Nomal */
            if (!mEnableLearning) {
                word.attribute = word.attribute | iWnnEngine.WNNWORD_ATTRIBUTE_NO_DICTIONARY;
            }

            getConverter().learn(word);
        }
    }

    /**
     * Fit an editor info.
     *
     * @param preference  an preference data.
     * @param info  an editor info.
     */
    private void fitInputType(SharedPreferences preference, EditorInfo info) {
        if (info.inputType == EditorInfo.TYPE_NULL) {
            setDirectInputMode(true);
            return;
        }

        DefaultSoftKeyboardHangul inputViewManager = (DefaultSoftKeyboardHangul)getInputViewManager();
        int keymode = ((DefaultSoftKeyboardHangul)getInputViewManager()).getKeyMode();

        /* voice input initialize */
        mHasUsedVoiceInput = preference.getBoolean(PREF_HAS_USED_VOICE_INPUT, false);

        /////////// Initial value
        // funfun mode
        mEnableFunfun = preference.getBoolean("opt_funfun", false);
        mEnableLearning   = preference.getBoolean("opt_enable_learning", true);
        mEnablePrediction = preference.getBoolean("opt_prediction", true);
        if (OpenWnn.isTabletMode()) {
            mEnableSpellCorrection = preference.getBoolean("opt_spell_correction", false);
        } else {
            mEnableSpellCorrection = preference.getBoolean("opt_spell_correction", true);
        }
        mEnableFullscreen = preference.getBoolean("fullscreen_mode", true);
        mEnableMushroom = preference.getString("opt_mushroom", "notuse").equals("notuse") ? false : true;
        mEnableHeadConv = preference.getBoolean("opt_head_conversion", getResources().getBoolean(R.bool.opt_head_conversion_default_value));
        mConverterIWnn.setEnableHeadConversion(mEnableHeadConv);

        int preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_NONE;
        mEnableConverter = true;
        mEnableSymbolList = true;
        mEnableSymbolListNonHalf = true;
        mEnableEmoji = false;
        mEnableDecoEmoji = false;
        setEnabledTabs(false, true);
        mTextCandidatesViewManager.setEnableEmoticon(true);
        mConverterIWnn.setEmailAddressFilter(false);
        if ((info.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0) {
            mEnableFullscreen = false;
        }

        /////////// The state is changed according to the input type.
        if (keymode == DefaultSoftKeyboardHangul.KEYMODE_KO_ALPHABET) {
            switch (info.inputType & EditorInfo.TYPE_MASK_FLAGS) {
            case EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS:
                mEnablePrediction = false;
                mEnableLearning = false;
                mEnableConverter = false;
                break;
            default:
                break;
            }
        }

        switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
            mEnableConverter = false;
            if (EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD == (info.inputType & EditorInfo.TYPE_MASK_VARIATION)) {
                mEnableSymbolListNonHalf = false;
                mTextCandidatesViewManager.setEnableEmoticon(false);
            }
            break;

        case EditorInfo.TYPE_CLASS_DATETIME:
            mEnableConverter = false;
            break;

        case EditorInfo.TYPE_CLASS_PHONE:
            mEnableSymbolList = false;
            mEnableConverter = false;
            mEnableSymbolListNonHalf = false;
            mConverterIWnn.setEmailAddressFilter(true);
            mTextCandidatesViewManager.setEnableEmoticon(false);
            break;

        case EditorInfo.TYPE_CLASS_TEXT:

            switch (info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
            case EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME:
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_PERSON_NAME;
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                mEnablePrediction = false;
                mEnableLearning = false;
                mEnableConverter = false;
                mEnableSymbolListNonHalf = false;
                mConverterIWnn.setEmailAddressFilter(true);
                mTextCandidatesViewManager.setEnableEmoticon(false);
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS;
                break;

            case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                mEnablePrediction = false;
                mEnableLearning = false;
                mEnableSymbolList = false;
                break;

            default:
                // Uncorrespondence
                break;
            }
            break;

        default:
            break;
        }

        //#ifdef EMOJI-DOCOMO
        if (!OpenWnn.isTabletMode()) {
            Bundle bundle = info.extras;
            if (bundle != null) {
                mEnableEmoji = bundle.getBoolean("allowEmoji");
                mTextCandidatesViewManager.setEnableEmoji(mEnableEmoji);
                EmojiAssist assist = EmojiAssist.getInstance();
                if (assist != null) {
                    int functype = assist.getEmojiFunctionType();
                    if (functype != 0) {
                        mEnableDecoEmoji = bundle.getBoolean("allowDecoEmoji");
                        mTextCandidatesViewManager.setEnableDecoEmoji(mEnableDecoEmoji);
                        int emojitype = bundle.getInt("emojiType");
                        OpenWnn.setEmojiType(emojitype);
                    }
                }
            }
        }
        setEmojiFlag();
        //#endif /* EMOJI-DOCOMO */

        mConverterIWnn.setEmojiFilter(!mEnableEmoji);
        mConverterIWnn.setPreferences(preference);
        mConverterIWnn.setDecoEmojiFilter(!mEnableDecoEmoji);
        DecoEmojiUtil.setConvertFunctionEnabled(mEnableDecoEmoji);
        inputViewManager.setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbolList);

        if (!mEnablePrediction) {
            mEnableFunfun = false;
        }

        // Doesn't process while setting change.
        if (!OpenWnn.getCurrentIme().isKeepInput()) {
            EngineState state = new EngineState();
            state.language = EngineState.LANGUAGE_HANGUL;
            state.preferenceDictionary = preferenceDictionary;
            state.convertType = EngineState.CONVERT_TYPE_NONE;

            updateEngineState(state);
        }
    }


    /**
     * Append a {@link StrSegment} to the composing text.
     * <br>
     * If the length of the composing text exceeds
     * {@code LIMIT_INPUT_NUMBER}, the appending operation is ignored.
     *
     * @param  str  Input segment
     */
    private void appendStrSegment(StrSegment str) {
        ComposingText composingText = getComposingText();

        if (composingText.size(ComposingText.LAYER1) >= LIMIT_INPUT_NUMBER) {
            return;//When the number of maximum input characters is exceeded, nothing is processed.
        }
        composingText.insertStrSegment(ComposingText.LAYER0, ComposingText.LAYER1, str);
        return;
    }

    /**
     * Commit the consecutive clause conversion.
     */
    private void commitConvertingText() {
        if (mEngineState.isRenbun()) {
            ComposingText composingText = getComposingText();
            int size = composingText.size(ComposingText.LAYER2);
            for (int i = mCommitCount; i < size; i++) {
                getConverter().makeCandidateListOf(i);
                learnWord(null);
            }

            String text = composingText.toString(ComposingText.LAYER2);
            getInputConnection().commitText(text, 1);
            mPrevCommitText.append(text);
            mIgnoreCursorMove = true;
            initializeScreen();
        }

    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#initializeScreen */
    @Override protected void initializeScreen() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        ComposingText composingText = getComposingText();

        if ((composingText.size(ComposingText.LAYER0) != 0) || (mFunfun != 0)) {
            getInputConnection().setComposingText("", 0);
        }
        composingText.clear();
        mExactMatchMode = false;
        mFunfun = 0;
        setFunFun(mFunfun);
        mStatus = STATUS_INIT;
        View candidateView = candidatesViewManager.getCurrentView();
        if ((candidateView != null) && candidateView.isShown()) {
            candidatesViewManager.clearCandidates();
        }
        getInputViewManager().onUpdateState(mWnnSwitcher);

        EngineState state = new EngineState();
        state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
        updateEngineState(state);
        if (mFullCandidate) {
            mFullCandidate = false;
            updateFullscreenMode();
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onFinishInput */
    @Override public void onFinishInput() {
        InputConnection inputConnection = getInputConnection();
        if (inputConnection != null) {
            if (mFunfun > 0) {
                inputConnection.deleteSurroundingText(mFunfun, 0);
                mFunfun = 0;
                setFunFun(mFunfun);
            }
            //So as not to display Wildcard prediction, delete the composing text.
            initializeScreen();
        }
        onFinishInputOpenWnn();
    }

    /**
     * Check whether the layer 2 converter is active or not.
     *
     * @return {@code true} if the converter is active. {@code false} if not.
     */
    private boolean isEnableL2Converter() {
        if (getConverter() == null || !mEnableConverter) {
            return false;
        }

        return true;
    }

    /**
     * Handling KeyEvent(KEYLONGPRESS)
     * <br>
     * This method is called from {@link #handleEvent}.
     *
     * @param ev   An long press key event
     * @return    {@code true} if the event is processed in this method; {@code false} if not.
     */
    private boolean onKeyLongPressEvent(KeyEvent ev) {
        // Do nothing. If you need this event, Call KeyEvent#startTracking().
        return false;
    }

    /**
     * Processing the undo.
     *
     * @return {@code true}:Processing undo , {@code false}:if not.
     */
    private boolean undo() {
        if (mCanUndo) {
            boolean hasCommitedByVoiceInput= mHasCommitedByVoiceInput;

            EngineState state = new EngineState();
            state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
            updateEngineState(state);

            //The character string fixed to be displayed is deleted.
            getInputConnection().deleteSurroundingText(mPrevCommitText.length(), 0);
            DefaultSoftKeyboardHangul inputManager = ((DefaultSoftKeyboardHangul)getInputViewManager());
            inputManager.undoKeyMode();
            clearCommitInfo();

            // The cursor moves by deleteSurroundingText().
            // To avoid initializing, the increment does a final frequency.
            mIgnoreCursorMove = true;

            if (mEnableLearning && !hasCommitedByVoiceInput) {
                mConverterIWnn.undo(1);
            }
            ComposingText composingText = getComposingText();
            composingText.clear();

            //The predict character string is returned while inputting it.
            String[] strSpilit = mCommitPredictKey.split("");
            int length = mCommitPredictKey.split("").length;
            length--;
            for (int i = 0; i < length; i++) {
                appendStrSegment(new StrSegment(strSpilit[i + 1]));
            }

            if (length != 0) {
                int size = mCommitLayer1StrSegment.size();
                StrSegment[] layer1StrSegment = new StrSegment[size];
                mCommitLayer1StrSegment.toArray(layer1StrSegment);
                composingText.setCursor(ComposingText.LAYER1, length);
                composingText.replaceStrSegment(ComposingText.LAYER1, layer1StrSegment, length);

                mFunfun = mCommitFunfun;
                setFunFun(mFunfun);
            }
            mExactMatchMode = mCommitExactMatch;
            composingText.setCursor(ComposingText.LAYER1, mCommitCursorPosition);

            mStatus = STATUS_INPUT;
            if (isEnableL2Converter()) {
                getConverter().init(mWnnSwitcher.getFilesDirPath());
            }

            updateViewStatusForPrediction(true, true);

            breakSequence();
            return true;
        }
        return false;
    }

    /**
     * Initialize the committed text's information.
     */
    private void initCommitInfoForWatchCursor() {
        if (!isEnableL2Converter()) {
            return;
        }

        mCommitStartCursor = mComposingStartCursor;
        mPrevCommitText.delete(0, mPrevCommitText.length());

        // for undo
        if ((0 <= mCommitStartCursor) && !mEngineState.isSymbolList()) {
            ComposingText composingText = getComposingText();
            mCommitFunfun = mFunfun;
            mCommitPredictKey = composingText.toString(ComposingText.LAYER0);
            if (mEngineState.isRenbun()) {
                mCommitCursorPosition = composingText.size(ComposingText.LAYER1);
            } else {
                mCommitCursorPosition = composingText.getCursor(ComposingText.LAYER1);
            }
            mCommitExactMatch = mExactMatchMode;
            DefaultSoftKeyboardHangul inputManager = ((DefaultSoftKeyboardHangul)getInputViewManager());
            inputManager.setUndoKeyMode(true);
            mCanUndo = true;

            mCommitLayer1StrSegment.clear();
            int size = composingText.size(ComposingText.LAYER1);
            for (int i = 0; i < size; i++) {
                StrSegment c = composingText.getStrSegment(ComposingText.LAYER1, i);
                if (c != null) {
                    mCommitLayer1StrSegment.add(new StrSegment(c.string, c.from, c.to));
                }
            }
        }
    }

    /**
     * Clear the commit text's info.
     * @return {@code true}:cleared, {@code false}:has already cleared.
     */
    private boolean clearCommitInfo() {
        if (mCommitStartCursor < 0) {
            return false;
        }

        mCommitStartCursor = -1;

        // for undo
        DefaultSoftKeyboardHangul inputManager = ((DefaultSoftKeyboardHangul)getInputViewManager());
        inputManager.setUndoKeyMode(false);
        mCanUndo = false;
        mHasCommitedByVoiceInput = false;

        return true;
    }

    /**
     * Verify the commit text.
     */
    private void checkCommitInfo() {
        if (mCommitStartCursor < 0) {
            return;
        }

        int composingLength = getComposingText().toString(mTargetLayer).length();
        CharSequence seq = getInputConnection().getTextBeforeCursor(mPrevCommitText.length() + composingLength, 0);
        if (seq != null && seq.length() >= composingLength) {
            seq = seq.subSequence(0, seq.length() - composingLength);
            if (!seq.equals(mPrevCommitText.toString())) {
                mIgnoreCursorMove = false;
                clearCommitInfo();
            }
        } else {
            mIgnoreCursorMove = false;
            clearCommitInfo();
        }
    }

    /** @see IWnnImeBase#close */
    @Override public void close(boolean now) {
        Message message = mHandler.obtainMessage(MSG_CLOSE);
        if (now) {
            mHandler.handleMessage(message);
        } else {
            mHandler.sendMessageDelayed(message, 0);
        }
    }

    /**
     * Break the sequence of words.
     */
    private void breakSequence() {
        mConverterIWnn.breakSequence();
    }

    /**
     * Return whether the key event is left/right or not.
     *
     * @param  ev  An event
     * @return {@code true} if key event is right or left; {@code false} if otherwise.
     */
    private boolean isRightOrLeftKeyEvents(OpenWnnEvent ev) {
        boolean ret = false;
        if (ev.code == OpenWnnEvent.INPUT_SOFT_KEY) {
            KeyEvent keyEvent = ev.keyEvent;
            int keyCode = 0;
            if (keyEvent != null) {
                keyCode = keyEvent.getKeyCode();
            }

            if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                ret = true;
            }
        }
        return ret;
    }

    /** @see IWnnImeBase#setFunFun */
    @Override public void setFunFun (int funfun) {
        if (OpenWnn.getCurrentIme() != null) {
            OpenWnn.getCurrentIme().setFunfun(funfun) ;
        }
    }

    /**
     * Change symbol engine state.
     *
     * @param  state  Engine state
     * @param  mode   Engine mode
     */
    private void changeSymbolEngineState(EngineState state, int mode) {
        if (mEnableSymbolList && !isDirectInputMode()) {
            if (mode == IWnnSymbolEngine.MODE_NONE) {
                if (mEnableSymbolListNonHalf) {
                    mConverterSymbolEngineBack.setSymToggle();
                } else {
                    mConverterSymbolEngineBack.setMode(IWnnSymbolEngine.MODE_OTHERS_SYMBOL);
                }
            } else {
                mConverterSymbolEngineBack.setMode(mode);
            }
            mFunfun = 0;
            setFunFun(mFunfun);
            state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL;
            updateEngineState(state);
            updateViewStatusForPrediction(true, true);
        }
    }

    /**
     * Set candidate view type.
     *
     * @param isFullCandidate {@code true} - to full view.
     *                        {@code false} - to normal view.
     */
    private void setCandidateIsViewTypeFull(boolean isFullCandidate) {
        mFullCandidate = isFullCandidate;
        if (mFullCandidate) {
            mStatus |= STATUS_CANDIDATE_FULL;
            getCandidatesViewManager().setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
            updateFullscreenMode();
            if (!mEngineState.isSymbolList()) {
                getInputViewManager().hideInputView();
            }
        } else {
            mStatus &= ~STATUS_CANDIDATE_FULL;
            getCandidatesViewManager().setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
            updateFullscreenMode();
            getInputViewManager().showInputView();
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#commitVoiceResult */
    @Override protected void commitVoiceResult(String result) {
        clearCommitInfo();
        initCommitInfoForWatchCursor();
        commitText(result);
        checkCommitInfo();
    }

    /**
     * Switch symbol list.
     */
    private void switchSymbolList() {
        changeSymbolEngineState(new EngineState(), IWnnSymbolEngine.MODE_NONE);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#restartSelf */
    public void restartSelf(EditorInfo attribute) {
        mAttribute = attribute;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RESTART), 0);
    }

    /**
     * Handle a Tab key event.
     *
     * @param  ev  KeyEvent
     */
    private void processTabKeyEvent(KeyEvent ev) {
        if (ev == null) {
            return;
        }

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        boolean isShowCand = candidatesViewManager.getCurrentView().isShown();
        if (!isShowCand) {
            return;
        }
        boolean isFocusCand = candidatesViewManager.isFocusCandidate();

        if (ev.isShiftPressed()) {
            if (isFocusCand) {
                processLeftKeyEvent();
            } else {
                // Focus on the final candidate.
                processEndKeyEvent();
            }
        } else {
            if (isFocusCand) {
                processRightKeyEvent();
            } else {
                // Focus on the first candidate.
                processDownKeyEvent();
            }
        }
    }

    /**
     * Insert a space if the previous character is not a space.
     */
    private void commitSpaceJustOne() {
        CharSequence seq = getInputConnection().getTextBeforeCursor(1, 0);
        if ((seq != null) && (seq.length() > 0)) {
            if (seq.charAt(0) != ' ') {
                commitText(" ");
            }
        }
    }

    /**
     * Emoji or DecoEmoji flag setter
     *
     */
    private void setEmojiFlag() {
        IWnnLanguageSwitcher switcher = getSwitcher();
        switcher.setEmojiFlag(mEnableEmoji || mEnableDecoEmoji);
        return ;
    }

    /**
     * Set enable tabs.
     *
     * @param enableEmoji    {@code true}  - Emoji is enabled.
     *                       {@code false} - Emoji is disabled.
     * @param enableEmoticon {@code true}  - Emoticon is enabled.
     *                       {@code false} - Emoticon is disabled.
     */
    private void setEnabledTabs(boolean enableEmoji, boolean enableEmoticon) {
        mTextCandidatesViewManager.setEnableEmoji(enableEmoji);
        mTextCandidatesViewManager.setEnableEmoticon(enableEmoticon);

        mTextCandidatesViewManager.setEnableDecoEmoji(false);
    }
}
