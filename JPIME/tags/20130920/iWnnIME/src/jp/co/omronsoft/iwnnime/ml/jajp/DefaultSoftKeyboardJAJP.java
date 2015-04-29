/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.jajp;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodInfo;
import android.widget.LinearLayout;
import android.widget.Toast;

import jp.co.omronsoft.iwnnime.ml.BaseInputView;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.ControlPanelStandard;
import jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard;
import jp.co.omronsoft.iwnnime.ml.FlickKeyboardView;
import jp.co.omronsoft.iwnnime.ml.InputMethodBase;
import jp.co.omronsoft.iwnnime.ml.InputMethodSwitcher;
import jp.co.omronsoft.iwnnime.ml.KeyDrawable;
import jp.co.omronsoft.iwnnime.ml.Keyboard;
import jp.co.omronsoft.iwnnime.ml.Keyboard.Key;
import jp.co.omronsoft.iwnnime.ml.KeyboardSkinData;
import jp.co.omronsoft.iwnnime.ml.KeyboardThemeActivity;
import jp.co.omronsoft.iwnnime.ml.MultiTouchKeyboardView;
import jp.co.omronsoft.iwnnime.ml.OneTouchEmojiListViewManager;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.OpenWnnEvent;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.WnnKeyboardFactory;
import jp.co.omronsoft.iwnnime.ml.StrSegment;
import jp.co.omronsoft.iwnnime.ml.jajp.IWnnImeJaJp;
import com.nttdocomo.android.voiceeditor.voiceeditor.VoiceEditorClient;
import java.lang.Float;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * The default Software Keyboard class for Japanese IME.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
@SuppressWarnings("serial")
public class DefaultSoftKeyboardJAJP extends DefaultSoftKeyboard {

    /** Enable English word prediction on half-width alphabet mode */
    private static final boolean USE_ENGLISH_PREDICT = true;

    /** Key code for switching to full-width HIRAGANA mode */
    private static final int KEYCODE_SWITCH_FULL_HIRAGANA = -301;

    /** Key code for switching to full-width KATAKANA mode */
    private static final int KEYCODE_SWITCH_FULL_KATAKANA = -302;

    /** Key code for switching to full-width alphabet mode */
    private static final int KEYCODE_SWITCH_FULL_ALPHABET = -303;

    /** Key code for switching to full-width number mode */
    private static final int KEYCODE_SWITCH_FULL_NUMBER = -304;

    /** Key code for switching to half-width KATAKANA mode */
    private static final int KEYCODE_SWITCH_HALF_KATAKANA = -306;

    /** Key code for switching to half-width alphabet mode */
    private static final int KEYCODE_SWITCH_HALF_ALPHABET = -307;

    /** Key code for switching to half-width number mode */
    private static final int KEYCODE_SWITCH_HALF_NUMBER = -308;

    /** Key code for case toggle key */
    private static final int KEYCODE_SELECT_CASE = -309;

    /** Key code for EISU-KANA conversion */
    public static final int KEYCODE_EISU_KANA = -305;

    /** Key code for NOP (no-operation) */
    private static final int KEYCODE_NOP = -310;

    /** Key code for switching to full-width HIRAGANA mode with long press menu */
    private static final int KEYCODE_SWITCH_FULL_HIRAGANA_MENU = -313;

    /** Key code for switching to full-width number mode with long press menu */
    private static final int KEYCODE_SWITCH_FULL_NUMBER_MENU = -314;

    /** Key code for switching to half-width alphabet mode with long press menu */
    private static final int KEYCODE_SWITCH_HALF_ALPHABET_MENU = -315;

    /** Key code for switching to half-width number mode with long press menu */
    private static final int KEYCODE_SWITCH_HALF_NUMBER_MENU = -316;

    /** Key code for displaying the keyboard type switch menu */
    public static final int KEYCODE_INPUT_MODE = -412;

    /** Key code for displaying the keyboard setting menu switch */
    public static final int KEYCODE_KEYBOARD_SETTING = -413;

    /** Key code for starting voiceinput */
    public static final int KEYCODE_FUNCTION_VOICEINPUT = -601;

    /** Key code for starting keyboardtype */
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

    /** Key code for switching numeric mode to other mode */
    private static final int KEYCODE_NUM_MOJI=-609;

    /** Default key-mode */
    private static final int KEYMODE_DEFAULTMODE = -1;

    /** Key code for navigation keypad */
    private static final int KEYID_KEYSELECT = 58;
    private static final int KEYID_KEYDESELECT = 59;
    private static final int KEYID_COPY = 60;
    private static final int KEYID_COPYON = 61;
    private static final int KEYID_CUT = 62;
    private static final int KEYID_CUTON = 63;

    /** index for functionkey */
    private static final int FunctionKey_FUNC_NONE       = -1;
    private static final int FunctionKey_FUNC_VOICEINPUT  = 0;
    private static final int FunctionKey_FUNC_KBD         = 1;
    private static final int FunctionKey_FUNC_HANDWRITING = 2;
    private static final int FunctionKey_FUNC_IMESETTINGS = 3;

    /** Input mode toggle cycle table */
    private static final int[] JP_MODE_CYCLE_TABLE = {
        KEYMODE_JA_FULL_HIRAGANA, KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER
    };

    /** Definition for {@code mInputType} (toggle) */
    private static final int INPUT_TYPE_TOGGLE = 1;

    /** Definition for {@code mInputType} (commit instantly) */
    private static final int INPUT_TYPE_INSTANT = 2;

    /** Max key number of the 12 key keyboard (depends on the definition of keyboards) */
    private static final int KEY_NUMBER_12KEY = 20;

    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_HIRA_B         = 621;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_B          = 622;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_B          = 623;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_NON_B          = 624;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_OFF_HIRA_B = 625;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_OFF_HIRA_B = 626;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_HIRA_B           = 627;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_ENG_B            = 628;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_NUM_B            = 629;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_NON_B            = 630;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_ENG_OFF_HIRA_B   = 631;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_NUM_OFF_HIRA_B   = 632;

    /** Key Mode for Flick Input Symbol/Numeric */
    private static final int KEY_MODE_CHANGE_SYMNUM_NUM_B=633;
    private static final int KEY_MODE_CHANGE_SYMNUM_SYM_B=634;

    /** Slide Modekey Popup drawable id table */
    private static final int[][][] SLIDE_DRAWABLE_ID_TABLE = {
        {
         {KEY_MODE_CHANGE_EFFECTIVE_OFF_HIRA_B,
          KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_B,
          KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_B,
          KEY_MODE_CHANGE_EFFECTIVE_OFF_NON_B},

         {KEY_MODE_CHANGE_EFFECTIVE_OFF_HIRA_B,
          KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_OFF_HIRA_B,
          KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_OFF_HIRA_B,
          KEY_MODE_CHANGE_EFFECTIVE_OFF_NON_B}
         },
         {
          {KEY_MODE_CHANGE_INVALID_OFF_HIRA_B,
           KEY_MODE_CHANGE_INVALID_OFF_ENG_B,
           KEY_MODE_CHANGE_INVALID_OFF_NUM_B,
           KEY_MODE_CHANGE_INVALID_OFF_NON_B},

           {KEY_MODE_CHANGE_INVALID_OFF_HIRA_B,
            KEY_MODE_CHANGE_INVALID_OFF_ENG_OFF_HIRA_B,
            KEY_MODE_CHANGE_INVALID_OFF_NUM_OFF_HIRA_B,
            KEY_MODE_CHANGE_INVALID_OFF_NON_B}
         },
    };

    /** Hand writing(IME) Package Name */
    private static final String HANDWRITING_PACKAGE_NAME =
             "com.sevenknowledge.sevennotestabproduct.lg001.tab50";

    /** Hand writing(IME) Service Name */
    private static final String HANDWRITING_SERVICE_ID =
            "com.sevenknowledge.sevennotestabproduct.lg001.tab50/com.sevenknowledge.mazec.MazecIms";

    /** Docomo VoiceEditor Package Name */
    private static final String DOCOMO_VOICEEDITOR_PACKAGE_NAME =
            "com.nttdocomo.android.voiceeditor";

    /** Toggle cycle table for full-width HIRAGANA */
    private static final String[][] JP_FULL_HIRAGANA_CYCLE_TABLE = {
        // You can use following command to convert ascii to utf8.
        //  $ native2ascii -reverse <file>
        {"\u3042", "\u3044", "\u3046", "\u3048", "\u304a", "\u3041", "\u3043", "\u3045", "\u3047", "\u3049"},
        {"\u304b", "\u304d", "\u304f", "\u3051", "\u3053"},
        {"\u3055", "\u3057", "\u3059", "\u305b", "\u305d"},
        {"\u305f", "\u3061", "\u3064", "\u3066", "\u3068", "\u3063"},
        {"\u306a", "\u306b", "\u306c", "\u306d", "\u306e"},
        {"\u306f", "\u3072", "\u3075", "\u3078", "\u307b"},
        {"\u307e", "\u307f", "\u3080", "\u3081", "\u3082"},
        {"\u3084", "\u3086", "\u3088", "\u3083", "\u3085", "\u3087"},
        {"\u3089", "\u308a", "\u308b", "\u308c", "\u308d"},
        {"\u308f", "\u3092", "\u3093", "\u308e", "\u30fc"},
        {"\u3001", "\u3002", "\uff1f", "\uff01", "\u30fb", "\u3000"},
    };

    /** Flick toggle cycle table for full-width HIRAGANA */
    private static final String[][] JP_FULL_HIRAGANA_CYCLE_TABLE_FLICK = {
        {"\u3042", "\u3044", "\u3046", "\u3048", "\u304a"},
        {"\u304b", "\u304d", "\u304f", "\u3051", "\u3053"},
        {"\u3055", "\u3057", "\u3059", "\u305b", "\u305d"},
        {"\u305f", "\u3061", "\u3064", "\u3066", "\u3068"},
        {"\u306a", "\u306b", "\u306c", "\u306d", "\u306e"},
        {"\u306f", "\u3072", "\u3075", "\u3078", "\u307b"},
        {"\u307e", "\u307f", "\u3080", "\u3081", "\u3082"},
        {"\u3084", "\uff08", "\u3086", "\uff09", "\u3088"},
        {"\u3089", "\u308a", "\u308b", "\u308c", "\u308d"},
        {"\u308f", "\u3092", "\u3093", "\u30fc",    null},
        {"\u3001", "\u3002", "\uff1f", "\uff01",    null},
    };

    /** Replace table for full-width HIRAGANA */
    private static final HashMap<String, String> JP_FULL_HIRAGANA_REPLACE_TABLE = new HashMap<String, String>() {{
          put("\u3042", "\u3041"); put("\u3044", "\u3043"); put("\u3046", "\u3045"); put("\u3048", "\u3047"); put("\u304a", "\u3049");
          put("\u3041", "\u3042"); put("\u3043", "\u3044"); put("\u3045", "\u30f4"); put("\u3047", "\u3048"); put("\u3049", "\u304a");
          put("\u304b", "\u304c"); put("\u304d", "\u304e"); put("\u304f", "\u3050"); put("\u3051", "\u3052"); put("\u3053", "\u3054");
          put("\u304c", "\u304b"); put("\u304e", "\u304d"); put("\u3050", "\u304f"); put("\u3052", "\u3051"); put("\u3054", "\u3053");
          put("\u3055", "\u3056"); put("\u3057", "\u3058"); put("\u3059", "\u305a"); put("\u305b", "\u305c"); put("\u305d", "\u305e");
          put("\u3056", "\u3055"); put("\u3058", "\u3057"); put("\u305a", "\u3059"); put("\u305c", "\u305b"); put("\u305e", "\u305d");
          put("\u305f", "\u3060"); put("\u3061", "\u3062"); put("\u3064", "\u3063"); put("\u3066", "\u3067"); put("\u3068", "\u3069");
          put("\u3060", "\u305f"); put("\u3062", "\u3061"); put("\u3063", "\u3065"); put("\u3067", "\u3066"); put("\u3069", "\u3068");
          put("\u3065", "\u3064"); put("\u30f4", "\u3046");
          put("\u306f", "\u3070"); put("\u3072", "\u3073"); put("\u3075", "\u3076"); put("\u3078", "\u3079"); put("\u307b", "\u307c");
          put("\u3070", "\u3071"); put("\u3073", "\u3074"); put("\u3076", "\u3077"); put("\u3079", "\u307a"); put("\u307c", "\u307d");
          put("\u3071", "\u306f"); put("\u3074", "\u3072"); put("\u3077", "\u3075"); put("\u307a", "\u3078"); put("\u307d", "\u307b");
          put("\u3084", "\u3083"); put("\u3086", "\u3085"); put("\u3088", "\u3087");
          put("\u3083", "\u3084"); put("\u3085", "\u3086"); put("\u3087", "\u3088");
          put("\u308f", "\u308e");
          put("\u308e", "\u308f");
          put("\u309b", "\u309c");
          put("\u309c", "\u309b");
    }};

    /** Dakuten replace table for full-width HIRAGANA */
    private static final HashMap<String, String> JP_FULL_HIRAGANA_DAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
          put("\u3045", "\u30f4"); put("\u3046", "\u30f4");
          put("\u304b", "\u304c"); put("\u304d", "\u304e"); put("\u304f", "\u3050"); put("\u3051", "\u3052"); put("\u3053", "\u3054");
          put("\u3055", "\u3056"); put("\u3057", "\u3058"); put("\u3059", "\u305a"); put("\u305b", "\u305c"); put("\u305d", "\u305e");
          put("\u305f", "\u3060"); put("\u3061", "\u3062"); put("\u3064", "\u3065"); put("\u3066", "\u3067"); put("\u3068", "\u3069");
          put("\u3063", "\u3065");
          put("\u306f", "\u3070"); put("\u3072", "\u3073"); put("\u3075", "\u3076"); put("\u3078", "\u3079"); put("\u307b", "\u307c");
          put("\u3071", "\u3070"); put("\u3074", "\u3073"); put("\u3077", "\u3076"); put("\u307a", "\u3079"); put("\u307d", "\u307c");
          put("\u309c", "\u309b");
    }};

    /** Handakuten replace table for full-width HIRAGANA */
    private static final HashMap<String, String> JP_FULL_HIRAGANA_HANDAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
          put("\u306f", "\u3071"); put("\u3072", "\u3074"); put("\u3075", "\u3077"); put("\u3078", "\u307a"); put("\u307b", "\u307d");
          put("\u3070", "\u3071"); put("\u3073", "\u3074"); put("\u3076", "\u3077"); put("\u3079", "\u307a"); put("\u307c", "\u307d");
          put("\u309b", "\u309c");
    }};

    /** Capital replace table for full-width HIRAGANA */
    private static final HashMap<String, String> JP_FULL_HIRAGANA_CAPITAL_REPLACE_TABLE = new HashMap<String, String>() {{
          put("\u3042", "\u3041"); put("\u3044", "\u3043"); put("\u3046", "\u3045"); put("\u3048", "\u3047"); put("\u304a", "\u3049");
          put("\u3041", "\u3042"); put("\u3043", "\u3044"); put("\u3045", "\u3046"); put("\u3047", "\u3048"); put("\u3049", "\u304a");
          put("\u30f4", "\u3045");
          put("\u3064", "\u3063"); put("\u3063", "\u3064"); put("\u3065", "\u3063");
          put("\u3084", "\u3083"); put("\u3086", "\u3085"); put("\u3088", "\u3087");
          put("\u3083", "\u3084"); put("\u3085", "\u3086"); put("\u3087", "\u3088");
          put("\u308f", "\u308e");
          put("\u308e", "\u308f");
    }};

    /** Toggle cycle table for full-width KATAKANA */
    private static final String[][] JP_FULL_KATAKANA_CYCLE_TABLE = {
        {"\u30a2", "\u30a4", "\u30a6", "\u30a8", "\u30aa", "\u30a1", "\u30a3",
         "\u30a5", "\u30a7", "\u30a9"},
        {"\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3"},
        {"\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd"},
        {"\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8", "\u30c3"},
        {"\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce"},
        {"\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db"},
        {"\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2"},
        {"\u30e4", "\u30e6", "\u30e8", "\u30e3", "\u30e5", "\u30e7"},
        {"\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed"},
        {"\u30ef", "\u30f2", "\u30f3", "\u30ee", "\u30fc"},
        {"\u3001", "\u3002", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Flick toggle cycle table for full-width KATAKANA */
    private static final String[][] JP_FULL_KATAKANA_CYCLE_TABLE_FLICK = {
        {"\u30a2", "\u30a4", "\u30a6", "\u30a8", "\u30aa"},
        {"\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3"},
        {"\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd"},
        {"\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8"},
        {"\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce"},
        {"\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db"},
        {"\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2"},
        {"\u30e4", "\uff08", "\u30e6", "\uff09", "\u30e8"},
        {"\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed"},
        {"\u30ef", "\u30f2", "\u30f3", "\u30fc",    null},
        {"\u3001", "\u3002", "\uff1f", "\uff01",    null}
    };

    /** Replace table for full-width KATAKANA */
    private static final HashMap<String,String> JP_FULL_KATAKANA_REPLACE_TABLE = new HashMap<String,String>() {{
        put("\u30a2", "\u30a1"); put("\u30a4", "\u30a3"); put("\u30a6", "\u30a5"); put("\u30a8", "\u30a7"); put("\u30aa", "\u30a9");
        put("\u30a1", "\u30a2"); put("\u30a3", "\u30a4"); put("\u30a5", "\u30f4"); put("\u30a7", "\u30a8"); put("\u30a9", "\u30aa");
        put("\u30ab", "\u30ac"); put("\u30ad", "\u30ae"); put("\u30af", "\u30b0"); put("\u30b1", "\u30b2"); put("\u30b3", "\u30b4");
        put("\u30ac", "\u30ab"); put("\u30ae", "\u30ad"); put("\u30b0", "\u30af"); put("\u30b2", "\u30b1"); put("\u30b4", "\u30b3");
        put("\u30b5", "\u30b6"); put("\u30b7", "\u30b8"); put("\u30b9", "\u30ba"); put("\u30bb", "\u30bc"); put("\u30bd", "\u30be");
        put("\u30b6", "\u30b5"); put("\u30b8", "\u30b7"); put("\u30ba", "\u30b9"); put("\u30bc", "\u30bb"); put("\u30be", "\u30bd");
        put("\u30bf", "\u30c0"); put("\u30c1", "\u30c2"); put("\u30c4", "\u30c3"); put("\u30c6", "\u30c7"); put("\u30c8", "\u30c9");
        put("\u30c0", "\u30bf"); put("\u30c2", "\u30c1"); put("\u30c3", "\u30c5"); put("\u30c7", "\u30c6"); put("\u30c9", "\u30c8");
        put("\u30c5", "\u30c4"); put("\u30f4", "\u30a6");
        put("\u30cf", "\u30d0"); put("\u30d2", "\u30d3"); put("\u30d5", "\u30d6"); put("\u30d8", "\u30d9"); put("\u30db", "\u30dc");
        put("\u30d0", "\u30d1"); put("\u30d3", "\u30d4"); put("\u30d6", "\u30d7"); put("\u30d9", "\u30da"); put("\u30dc", "\u30dd");
        put("\u30d1", "\u30cf"); put("\u30d4", "\u30d2"); put("\u30d7", "\u30d5"); put("\u30da", "\u30d8"); put("\u30dd", "\u30db");
        put("\u30e4", "\u30e3"); put("\u30e6", "\u30e5"); put("\u30e8", "\u30e7");
        put("\u30e3", "\u30e4"); put("\u30e5", "\u30e6"); put("\u30e7", "\u30e8");
        put("\u30ef", "\u30ee");
        put("\u30ee", "\u30ef");
    }};

    /** Dakuten replace table for full-width KATAKANA */
    private static final HashMap<String, String> JP_FULL_KATAKANA_DAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
        put("\u30a6", "\u30f4"); put("\u30a5", "\u30f4");
        put("\u30ab", "\u30ac"); put("\u30ad", "\u30ae"); put("\u30af", "\u30b0"); put("\u30b1", "\u30b2"); put("\u30b3", "\u30b4");
        put("\u30b5", "\u30b6"); put("\u30b7", "\u30b8"); put("\u30b9", "\u30ba"); put("\u30bb", "\u30bc"); put("\u30bd", "\u30be");
        put("\u30bf", "\u30c0"); put("\u30c1", "\u30c2"); put("\u30c4", "\u30c5"); put("\u30c6", "\u30c7"); put("\u30c8", "\u30c9");
        put("\u30c3", "\u30c5");
        put("\u30cf", "\u30d0"); put("\u30d2", "\u30d3"); put("\u30d5", "\u30d6"); put("\u30d8", "\u30d9"); put("\u30db", "\u30dc");
        put("\u30d1", "\u30d0"); put("\u30d4", "\u30d3"); put("\u30d7", "\u30d6"); put("\u30da", "\u30d9"); put("\u30dd", "\u30dc");
    }};

    /** Handakuten replace table for full-width KATAKANA */
    private static final HashMap<String, String> JP_FULL_KATAKANA_HANDAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
        put("\u30cf", "\u30d1"); put("\u30d2", "\u30d4"); put("\u30d5", "\u30d7"); put("\u30d8", "\u30da"); put("\u30db", "\u30dd");
        put("\u30d0", "\u30d1"); put("\u30d3", "\u30d4"); put("\u30d6", "\u30d7"); put("\u30d9", "\u30da"); put("\u30dc", "\u30dd");
    }};

    /** Capital replace table for full-width KATAKANA */
    private static final HashMap<String, String> JP_FULL_KATAKANA_CAPITAL_REPLACE_TABLE = new HashMap<String, String>() {{
        put("\u30a2", "\u30a1"); put("\u30a4", "\u30a3"); put("\u30a6", "\u30a5"); put("\u30a8", "\u30a7"); put("\u30aa", "\u30a9");
        put("\u30a1", "\u30a2"); put("\u30a3", "\u30a4"); put("\u30a5", "\u30a6"); put("\u30a7", "\u30a8"); put("\u30a9", "\u30aa");
        put("\u30f4", "\u30a5");
        put("\u30c4", "\u30c3"); put("\u30c3", "\u30c4"); put("\u30c5", "\u30c3");
        put("\u30e4", "\u30e3"); put("\u30e6", "\u30e5"); put("\u30e8", "\u30e7");
        put("\u30e3", "\u30e4"); put("\u30e5", "\u30e6"); put("\u30e7", "\u30e8");
        put("\u30ef", "\u30ee");
        put("\u30ee", "\u30ef");
    }};

    /** Toggle cycle table for half-width KATAKANA */
    private static final String[][] JP_HALF_KATAKANA_CYCLE_TABLE = {
        {"\uff71", "\uff72", "\uff73", "\uff74", "\uff75", "\uff67", "\uff68", "\uff69", "\uff6a", "\uff6b"},
        {"\uff76", "\uff77", "\uff78", "\uff79", "\uff7a"},
        {"\uff7b", "\uff7c", "\uff7d", "\uff7e", "\uff7f"},
        {"\uff80", "\uff81", "\uff82", "\uff83", "\uff84", "\uff6f"},
        {"\uff85", "\uff86", "\uff87", "\uff88", "\uff89"},
        {"\uff8a", "\uff8b", "\uff8c", "\uff8d", "\uff8e"},
        {"\uff8f", "\uff90", "\uff91", "\uff92", "\uff93"},
        {"\uff94", "\uff95", "\uff96", "\uff6c", "\uff6d", "\uff6e"},
        {"\uff97", "\uff98", "\uff99", "\uff9a", "\uff9b"},
        {"\uff9c", "\uff66", "\uff9d", "\uff70"},
        {"\uff64", "\uff61", "?", "!", "\uff65", " "},
    };

    /** Flick toggle cycle table for half-width KATAKANA */
    private static final String[][] JP_HALF_KATAKANA_CYCLE_TABLE_FLICK = {
        {"\uff71", "\uff72", "\uff73", "\uff74", "\uff75"},
        {"\uff76", "\uff77", "\uff78", "\uff79", "\uff7a"},
        {"\uff7b", "\uff7c", "\uff7d", "\uff7e", "\uff7f"},
        {"\uff80", "\uff81", "\uff82", "\uff83", "\uff84"},
        {"\uff85", "\uff86", "\uff87", "\uff88", "\uff89"},
        {"\uff8a", "\uff8b", "\uff8c", "\uff8d", "\uff8e"},
        {"\uff8f", "\uff90", "\uff91", "\uff92", "\uff93"},
        {"\uff94", "\u0028", "\uff95", "\u0029", "\uff96"},
        {"\uff97", "\uff98", "\uff99", "\uff9a", "\uff9b"},
        {"\uff9c", "\uff66", "\uff9d", "\uff70",    null},
        {"\uff64", "\uff61",      "?",      "!",    null},
    };

    /** Replace table for half-width KATAKANA */
    private static final HashMap<String,String> JP_HALF_KATAKANA_REPLACE_TABLE = new HashMap<String,String>() {{
        put("\uff71", "\uff67");  put("\uff72", "\uff68");  put("\uff73", "\uff69");  put("\uff74", "\uff6a");  put("\uff75", "\uff6b");
        put("\uff67", "\uff71");  put("\uff68", "\uff72");  put("\uff69", "\uff73\uff9e");  put("\uff6a", "\uff74");  put("\uff6b", "\uff75");
        put("\uff76", "\uff76\uff9e"); put("\uff77", "\uff77\uff9e"); put("\uff78", "\uff78\uff9e"); put("\uff79", "\uff79\uff9e"); put("\uff7a", "\uff7a\uff9e");
        put("\uff76\uff9e", "\uff76"); put("\uff77\uff9e", "\uff77"); put("\uff78\uff9e", "\uff78"); put("\uff79\uff9e", "\uff79"); put("\uff7a\uff9e", "\uff7a");
        put("\uff7b", "\uff7b\uff9e"); put("\uff7c", "\uff7c\uff9e"); put("\uff7d", "\uff7d\uff9e"); put("\uff7e", "\uff7e\uff9e"); put("\uff7f", "\uff7f\uff9e");
        put("\uff7b\uff9e", "\uff7b"); put("\uff7c\uff9e", "\uff7c"); put("\uff7d\uff9e", "\uff7d"); put("\uff7e\uff9e", "\uff7e"); put("\uff7f\uff9e", "\uff7f");
        put("\uff80", "\uff80\uff9e"); put("\uff81", "\uff81\uff9e"); put("\uff82", "\uff6f");  put("\uff83", "\uff83\uff9e"); put("\uff84", "\uff84\uff9e");
        put("\uff80\uff9e", "\uff80"); put("\uff81\uff9e", "\uff81"); put("\uff6f", "\uff82\uff9e"); put("\uff83\uff9e", "\uff83"); put("\uff84\uff9e", "\uff84");
        put("\uff82\uff9e", "\uff82");
        put("\uff8a", "\uff8a\uff9e"); put("\uff8b", "\uff8b\uff9e"); put("\uff8c", "\uff8c\uff9e"); put("\uff8d", "\uff8d\uff9e"); put("\uff8e", "\uff8e\uff9e");
        put("\uff8a\uff9e", "\uff8a\uff9f");put("\uff8b\uff9e", "\uff8b\uff9f");put("\uff8c\uff9e", "\uff8c\uff9f");put("\uff8d\uff9e", "\uff8d\uff9f");put("\uff8e\uff9e", "\uff8e\uff9f");
        put("\uff8a\uff9f", "\uff8a"); put("\uff8b\uff9f", "\uff8b"); put("\uff8c\uff9f", "\uff8c"); put("\uff8d\uff9f", "\uff8d"); put("\uff8e\uff9f", "\uff8e");
        put("\uff94", "\uff6c");  put("\uff95", "\uff6d");  put("\uff96", "\uff6e");
        put("\uff6c", "\uff94");  put("\uff6d", "\uff95");  put("\uff6e", "\uff96");
        put("\uff73\uff9e", "\uff73");
    }};

    /** Dakuten replace table for half-width KATAKANA */
    private static final HashMap<String, String> JP_HALF_KATAKANA_DAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
        put("\uff69", "\uff73\uff9e"); put("\uff73", "\uff73\uff9e");
        put("\uff76", "\uff76\uff9e"); put("\uff77", "\uff77\uff9e"); put("\uff78", "\uff78\uff9e"); put("\uff79", "\uff79\uff9e"); put("\uff7a", "\uff7a\uff9e");
        put("\uff7b", "\uff7b\uff9e"); put("\uff7c", "\uff7c\uff9e"); put("\uff7d", "\uff7d\uff9e"); put("\uff7e", "\uff7e\uff9e"); put("\uff7f", "\uff7f\uff9e");
        put("\uff80", "\uff80\uff9e"); put("\uff81", "\uff81\uff9e"); put("\uff82", "\uff82\uff9e");  put("\uff83", "\uff83\uff9e"); put("\uff84", "\uff84\uff9e");
        put("\uff6f", "\uff82\uff9e");
        put("\uff8a", "\uff8a\uff9e"); put("\uff8b", "\uff8b\uff9e"); put("\uff8c", "\uff8c\uff9e"); put("\uff8d", "\uff8d\uff9e"); put("\uff8e", "\uff8e\uff9e");
        put("\uff8a\uff9f", "\uff8a\uff9e"); put("\uff8b\uff9f", "\uff8b\uff9e"); put("\uff8c\uff9f", "\uff8c\uff9e"); put("\uff8d\uff9f", "\uff8d\uff9e"); put("\uff8e\uff9f", "\uff8e\uff9e");
    }};

    /** Handakuten replace table for half-width KATAKANA */
    private static final HashMap<String, String> JP_HALF_KATAKANA_HANDAKUTEN_REPLACE_TABLE = new HashMap<String, String>() {{
        put("\uff8a", "\uff8a\uff9f"); put("\uff8b", "\uff8b\uff9f"); put("\uff8c", "\uff8c\uff9f"); put("\uff8d", "\uff8d\uff9f"); put("\uff8e", "\uff8e\uff9f");
        put("\uff8a\uff9e", "\uff8a\uff9f");put("\uff8b\uff9e", "\uff8b\uff9f");put("\uff8c\uff9e", "\uff8c\uff9f");put("\uff8d\uff9e", "\uff8d\uff9f");put("\uff8e\uff9e", "\uff8e\uff9f");
    }};

    /** Capital replace table for half-width KATAKANA */
    private static final HashMap<String, String> JP_HALF_KATAKANA_CAPITAL_REPLACE_TABLE = new HashMap<String, String>() {{
        put("\uff71", "\uff67");  put("\uff72", "\uff68");  put("\uff73", "\uff69");  put("\uff74", "\uff6a");  put("\uff75", "\uff6b");
        put("\uff67", "\uff71");  put("\uff68", "\uff72");  put("\uff69", "\uff73");  put("\uff6a", "\uff74");  put("\uff6b", "\uff75");
        put("\uff73\uff9e", "\uff69");
        put("\uff82", "\uff6f"); put("\uff6f", "\uff82"); put("\uff82\uff9e", "\uff6f");
        put("\uff94", "\uff6c"); put("\uff95", "\uff6d"); put("\uff96", "\uff6e");
        put("\uff6c", "\uff94"); put("\uff6d", "\uff95"); put("\uff6e", "\uff96");
    }};

    /** Toggle cycle table for full-width alphabet */
    private static final String[][] JP_FULL_ALPHABET_CYCLE_TABLE = {
        {"\uff20", "\uff3f", "\uff0f", "\uff1a", "\uff5e", "\uff11"},
        {"\uff41", "\uff42", "\uff43", "\uff21", "\uff22", "\uff23", "\uff12"},
        {"\uff44", "\uff45", "\uff46", "\uff24", "\uff25", "\uff26", "\uff13"},
        {"\uff47", "\uff48", "\uff49", "\uff27", "\uff28", "\uff29", "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c", "\uff2a", "\uff2b", "\uff2c", "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f", "\uff2d", "\uff2e", "\uff2f", "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff54", "\uff55", "\uff56", "\uff34", "\uff35", "\uff36", "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Toggle cycle table for full-width alphabet shift */
    private static final String[][] JP_FULL_ALPHABET_SHIFT_CYCLE_TABLE = {
        {"\uff20", "\uff3f", "\uff0f", "\uff1a", "\uff5e", "\uff11"},
        {"\uff21", "\uff22", "\uff23", "\uff41", "\uff42", "\uff43", "\uff12"},
        {"\uff24", "\uff25", "\uff26", "\uff44", "\uff45", "\uff46", "\uff13"},
        {"\uff27", "\uff28", "\uff29", "\uff47", "\uff48", "\uff49", "\uff14"},
        {"\uff2a", "\uff2b", "\uff2c", "\uff4a", "\uff4b", "\uff4c", "\uff15"},
        {"\uff2d", "\uff2e", "\uff2f", "\uff4d", "\uff4e", "\uff4f", "\uff16"},
        {"\uff30", "\uff31", "\uff32", "\uff33", "\uff50", "\uff51", "\uff52", "\uff53", "\uff17"},
        {"\uff34", "\uff35", "\uff36", "\uff54", "\uff55", "\uff56", "\uff18"},
        {"\uff37", "\uff38", "\uff39", "\uff3a", "\uff57", "\uff58", "\uff59", "\uff5a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Toggle cycle table for full-width alphabet (EMAIL) */
    private static final String[][] JP_FULL_ALPHABET_EMAIL_CYCLE_TABLE = {
        {"\uff20", "\uff3f", "\uff07", "\uff02", "\uff11", "\uff08", "\uff09", "\uff0f", "\uff1a"},
        {"\uff41", "\uff42", "\uff43", "\uff21", "\uff22", "\uff23", "\uff12"},
        {"\uff44", "\uff45", "\uff46", "\uff24", "\uff25", "\uff26", "\uff13"},
        {"\uff47", "\uff48", "\uff49", "\uff27", "\uff28", "\uff29", "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c", "\uff2a", "\uff2b", "\uff2c", "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f", "\uff2d", "\uff2e", "\uff2f", "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff54", "\uff55", "\uff56", "\uff34", "\uff35", "\uff36", "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Toggle cycle table for full-width alphabet shift (EMAIL) */
    private static final String[][] JP_FULL_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE = {
        {"\uff20", "\uff3f", "\uff07", "\uff02", "\uff11", "\uff08", "\uff09", "\uff0f", "\uff1a"},
        {"\uff21", "\uff22", "\uff23", "\uff41", "\uff42", "\uff43", "\uff12"},
        {"\uff24", "\uff25", "\uff26", "\uff44", "\uff45", "\uff46", "\uff13"},
        {"\uff27", "\uff28", "\uff29", "\uff47", "\uff48", "\uff49", "\uff14"},
        {"\uff2a", "\uff2b", "\uff2c", "\uff4a", "\uff4b", "\uff4c", "\uff15"},
        {"\uff2d", "\uff2e", "\uff2f", "\uff4d", "\uff4e", "\uff4f", "\uff16"},
        {"\uff30", "\uff31", "\uff32", "\uff33", "\uff50", "\uff51", "\uff52", "\uff53", "\uff17"},
        {"\uff34", "\uff35", "\uff36", "\uff54", "\uff55", "\uff56", "\uff18"},
        {"\uff37", "\uff38", "\uff39", "\uff3a", "\uff57", "\uff58", "\uff59", "\uff5a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Toggle cycle table for full-width alphabet (URL) */
    private static final String[][] JP_FULL_ALPHABET_URL_CYCLE_TABLE = {
        {"\uff0f", "\uff3f", "\uff07", "\uff02", "\uff11", "\uff08", "\uff09", "\uff20", "\uff1a"},
        {"\uff41", "\uff42", "\uff43", "\uff21", "\uff22", "\uff23", "\uff12"},
        {"\uff44", "\uff45", "\uff46", "\uff24", "\uff25", "\uff26", "\uff13"},
        {"\uff47", "\uff48", "\uff49", "\uff27", "\uff28", "\uff29", "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c", "\uff2a", "\uff2b", "\uff2c", "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f", "\uff2d", "\uff2e", "\uff2f", "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff54", "\uff55", "\uff56", "\uff34", "\uff35", "\uff36", "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Toggle cycle table for full-width alphabet shift (URL) */
    private static final String[][] JP_FULL_ALPHABET_URL_SHIFT_CYCLE_TABLE = {
        {"\uff0f", "\uff3f", "\uff07", "\uff02", "\uff11", "\uff08", "\uff09", "\uff20", "\uff1a"},
        {"\uff21", "\uff22", "\uff23", "\uff41", "\uff42", "\uff43", "\uff12"},
        {"\uff24", "\uff25", "\uff26", "\uff44", "\uff45", "\uff46", "\uff13"},
        {"\uff27", "\uff28", "\uff29", "\uff47", "\uff48", "\uff49", "\uff14"},
        {"\uff2a", "\uff2b", "\uff2c", "\uff4a", "\uff4b", "\uff4c", "\uff15"},
        {"\uff2d", "\uff2e", "\uff2f", "\uff4d", "\uff4e", "\uff4f", "\uff16"},
        {"\uff30", "\uff31", "\uff32", "\uff33", "\uff50", "\uff51", "\uff52", "\uff53", "\uff17"},
        {"\uff34", "\uff35", "\uff36", "\uff54", "\uff55", "\uff56", "\uff18"},
        {"\uff37", "\uff38", "\uff39", "\uff3a", "\uff57", "\uff58", "\uff59", "\uff5a", "\uff19"},
        {"\uff0d", "\uff10"},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01", "\u30fb", "\u3000"}
    };

    /** Flick toggle cycle table for full-width alphabet */
    private static final String[][] JP_FULL_ALPHABET_CYCLE_TABLE_FLICK = {
        {"\uff20", "\uff3f", "\uff0f", "\uff1a", "\uff11"},
        {"\uff41", "\uff42", "\uff43",     null, "\uff12"},
        {"\uff44", "\uff45", "\uff46",     null, "\uff13"},
        {"\uff47", "\uff48", "\uff49",     null, "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c",     null, "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f",     null, "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff17"},
        {"\uff54", "\uff55", "\uff56",     null, "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff19"},
        {"\uff0d",     null, "\uff10",     null,     null},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01",     null}
    };

    /** Flick toggle cycle table for full-width alphabet shift */
    private static final String[][] JP_FULL_ALPHABET_SHIFT_CYCLE_TABLE_FLICK = {
        {"\uff20", "\uff3f", "\uff0f", "\uff1a", "\uff11"},
        {"\uff21", "\uff22", "\uff23",     null, "\uff12"},
        {"\uff24", "\uff25", "\uff26",     null, "\uff13"},
        {"\uff27", "\uff28", "\uff29",     null, "\uff14"},
        {"\uff2a", "\uff2b", "\uff2c",     null, "\uff15"},
        {"\uff2d", "\uff2e", "\uff2f",     null, "\uff16"},
        {"\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff34", "\uff35", "\uff36",     null, "\uff18"},
        {"\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d",     null, "\uff10",     null,     null},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01",     null}
    };

    /** Flick toggle cycle table for full-width alphabet (EMAIL) */
    private static final String[][] JP_FULL_ALPHABET_EMAIL_CYCLE_TABLE_FLICK = {
        {"\uff20", "\uff3f", "\uff07", "\uff02", "\uff11"},
        {"\uff41", "\uff42", "\uff43",     null, "\uff12"},
        {"\uff44", "\uff45", "\uff46",     null, "\uff13"},
        {"\uff47", "\uff48", "\uff49",     null, "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c",     null, "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f",     null, "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff17"},
        {"\uff54", "\uff55", "\uff56",     null, "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff19"},
        {"\uff0d",     null, "\uff10",     null,     null},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01",     null}
    };

    /** Flick toggle cycle table for full-width alphabet shift (EMAIL) */
    private static final String[][] JP_FULL_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE_FLICK = {
        {"\uff20", "\uff3f", "\uff07", "\uff02", "\uff11"},
        {"\uff21", "\uff22", "\uff23",     null, "\uff12"},
        {"\uff24", "\uff25", "\uff26",     null, "\uff13"},
        {"\uff27", "\uff28", "\uff29",     null, "\uff14"},
        {"\uff2a", "\uff2b", "\uff2c",     null, "\uff15"},
        {"\uff2d", "\uff2e", "\uff2f",     null, "\uff16"},
        {"\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff34", "\uff35", "\uff36",     null, "\uff18"},
        {"\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d",     null, "\uff10",     null,     null},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01",     null}
    };

    /** Flick toggle cycle table for full-width alphabet (URL) */
    private static final String[][] JP_FULL_ALPHABET_URL_CYCLE_TABLE_FLICK = {
        {"\uff0f", "\uff3f", "\uff07", "\uff02", "\uff11"},
        {"\uff41", "\uff42", "\uff43",     null, "\uff12"},
        {"\uff44", "\uff45", "\uff46",     null, "\uff13"},
        {"\uff47", "\uff48", "\uff49",     null, "\uff14"},
        {"\uff4a", "\uff4b", "\uff4c",     null, "\uff15"},
        {"\uff4d", "\uff4e", "\uff4f",     null, "\uff16"},
        {"\uff50", "\uff51", "\uff52", "\uff53", "\uff17"},
        {"\uff54", "\uff55", "\uff56",     null, "\uff18"},
        {"\uff57", "\uff58", "\uff59", "\uff5a", "\uff19"},
        {"\uff0d",     null, "\uff10",     null,     null},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01",     null}
    };

    /** Flick toggle cycle table for full-width alphabet shift (URL) */
    private static final String[][] JP_FULL_ALPHABET_URL_SHIFT_CYCLE_TABLE_FLICK = {
        {"\uff0f", "\uff3f", "\uff07", "\uff02", "\uff11"},
        {"\uff21", "\uff22", "\uff23",     null, "\uff12"},
        {"\uff24", "\uff25", "\uff26",     null, "\uff13"},
        {"\uff27", "\uff28", "\uff29",     null, "\uff14"},
        {"\uff2a", "\uff2b", "\uff2c",     null, "\uff15"},
        {"\uff2d", "\uff2e", "\uff2f",     null, "\uff16"},
        {"\uff30", "\uff31", "\uff32", "\uff33", "\uff17"},
        {"\uff34", "\uff35", "\uff36",     null, "\uff18"},
        {"\uff37", "\uff38", "\uff39", "\uff3a", "\uff19"},
        {"\uff0d",     null, "\uff10",     null,     null},
        {"\uff0e", "\uff0c", "\uff1f", "\uff01",     null}
    };

    /** Replace table for full-width alphabet */
    private static final HashMap<String,String> JP_FULL_ALPHABET_REPLACE_TABLE = new HashMap<String,String>() {{
        put("\uff21", "\uff41"); put("\uff22", "\uff42"); put("\uff23", "\uff43"); put("\uff24", "\uff44"); put("\uff25", "\uff45");
        put("\uff41", "\uff21"); put("\uff42", "\uff22"); put("\uff43", "\uff23"); put("\uff44", "\uff24"); put("\uff45", "\uff25");
        put("\uff26", "\uff46"); put("\uff27", "\uff47"); put("\uff28", "\uff48"); put("\uff29", "\uff49"); put("\uff2a", "\uff4a");
        put("\uff46", "\uff26"); put("\uff47", "\uff27"); put("\uff48", "\uff28"); put("\uff49", "\uff29"); put("\uff4a", "\uff2a");
        put("\uff2b", "\uff4b"); put("\uff2c", "\uff4c"); put("\uff2d", "\uff4d"); put("\uff2e", "\uff4e"); put("\uff2f", "\uff4f");
        put("\uff4b", "\uff2b"); put("\uff4c", "\uff2c"); put("\uff4d", "\uff2d"); put("\uff4e", "\uff2e"); put("\uff4f", "\uff2f");
        put("\uff30", "\uff50"); put("\uff31", "\uff51"); put("\uff32", "\uff52"); put("\uff33", "\uff53"); put("\uff34", "\uff54");
        put("\uff50", "\uff30"); put("\uff51", "\uff31"); put("\uff52", "\uff32"); put("\uff53", "\uff33"); put("\uff54", "\uff34");
        put("\uff35", "\uff55"); put("\uff36", "\uff56"); put("\uff37", "\uff57"); put("\uff38", "\uff58"); put("\uff39", "\uff59");
        put("\uff55", "\uff35"); put("\uff56", "\uff36"); put("\uff57", "\uff37"); put("\uff58", "\uff38"); put("\uff59", "\uff39");
        put("\uff3a", "\uff5a");
        put("\uff5a", "\uff3a");
    }};

    /** Toggle cycle table for half-width alphabet */
    private static final String[][] JP_HALF_ALPHABET_CYCLE_TABLE = {
        {"@", "_", "/", ":", "~", "1"},
        {"a", "b", "c", "A", "B", "C", "2"},
        {"d", "e", "f", "D", "E", "F", "3"},
        {"g", "h", "i", "G", "H", "I", "4"},
        {"j", "k", "l", "J", "K", "L", "5"},
        {"m", "n", "o", "M", "N", "O", "6"},
        {"p", "q", "r", "s", "P", "Q", "R", "S", "7"},
        {"t", "u", "v", "T", "U", "V", "8"},
        {"w", "x", "y", "z", "W", "X", "Y", "Z", "9"},
        {"-", "0"},
        {".", ",", "?", "!", ";", " "}
    };

    /** Toggle cycle table for half-width alphabet shift */
    private static final String[][] JP_HALF_ALPHABET_SHIFT_CYCLE_TABLE = {
        {"@", "_", "/", ":", "~", "1"},
        {"A", "B", "C", "a", "b", "c", "2"},
        {"D", "E", "F", "d", "e", "f", "3"},
        {"G", "H", "I", "g", "h", "i", "4"},
        {"J", "K", "L", "j", "k", "l", "5"},
        {"M", "N", "O", "m", "n", "o", "6"},
        {"P", "Q", "R", "S", "p", "q", "r", "s", "7"},
        {"T", "U", "V", "t", "u", "v", "8"},
        {"W", "X", "Y", "Z", "w", "x", "y", "z", "9"},
        {"-", "0"},
        {".", ",", "?", "!", ";", " "}
    };

    /** Toggle cycle table for half-width alphabet (EMAIL) */
    private static final String[][] JP_HALF_ALPHABET_EMAIL_CYCLE_TABLE = {
        {"@", "_", "'", "\"", "1", "(", ")", "/", ":"},
        {"a", "b", "c", "A", "B", "C", "2"},
        {"d", "e", "f", "D", "E", "F", "3"},
        {"g", "h", "i", "G", "H", "I", "4"},
        {"j", "k", "l", "J", "K", "L", "5"},
        {"m", "n", "o", "M", "N", "O", "6"},
        {"p", "q", "r", "s", "P", "Q", "R", "S", "7"},
        {"t", "u", "v", "T", "U", "V", "8"},
        {"w", "x", "y", "z", "W", "X", "Y", "Z", "9"},
        {"-", "0"},
        {".", ",", "?", "!", ";", " "}
    };

    /** Toggle cycle table for half-width alphabet shift (EMAIL) */
    private static final String[][] JP_HALF_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE = {
        {"@", "_", "'", "\"", "1", "(", ")", "/", ":"},
        {"A", "B", "C", "a", "b", "c", "2"},
        {"D", "E", "F", "d", "e", "f", "3"},
        {"G", "H", "I", "g", "h", "i", "4"},
        {"J", "K", "L", "j", "k", "l", "5"},
        {"M", "N", "O", "m", "n", "o", "6"},
        {"P", "Q", "R", "S", "p", "q", "r", "s", "7"},
        {"T", "U", "V", "t", "u", "v", "8"},
        {"W", "X", "Y", "Z", "w", "x", "y", "z", "9"},
        {"-", "0"},
        {".", ",", "?", "!", ";", " "}
    };

    /** Toggle cycle table for half-width alphabet (URL) */
    private static final String[][] JP_HALF_ALPHABET_URL_CYCLE_TABLE = {
        {"/", "_", "'", "\"", "1", "(", ")", "@", ":"},
        {"a", "b", "c", "A", "B", "C", "2"},
        {"d", "e", "f", "D", "E", "F", "3"},
        {"g", "h", "i", "G", "H", "I", "4"},
        {"j", "k", "l", "J", "K", "L", "5"},
        {"m", "n", "o", "M", "N", "O", "6"},
        {"p", "q", "r", "s", "P", "Q", "R", "S", "7"},
        {"t", "u", "v", "T", "U", "V", "8"},
        {"w", "x", "y", "z", "W", "X", "Y", "Z", "9"},
        {"-", "0"},
        {".", ",", "?", "!", ";", " "}
    };

    /** Toggle cycle table for half-width alphabet shift (URL) */
    private static final String[][] JP_HALF_ALPHABET_URL_SHIFT_CYCLE_TABLE = {
        {"/", "_", "'", "\"", "1", "(", ")", "@", ":"},
        {"A", "B", "C", "a", "b", "c", "2"},
        {"D", "E", "F", "d", "e", "f", "3"},
        {"G", "H", "I", "g", "h", "i", "4"},
        {"J", "K", "L", "j", "k", "l", "5"},
        {"M", "N", "O", "m", "n", "o", "6"},
        {"P", "Q", "R", "S", "p", "q", "r", "s", "7"},
        {"T", "U", "V", "t", "u", "v", "8"},
        {"W", "X", "Y", "Z", "w", "x", "y", "z", "9"},
        {"-", "0"},
        {".", ",", "?", "!", ";", " "}
    };

    /** Flick toggle cycle table for half-width alphabet */
    private static final String[][] JP_HALF_ALPHABET_CYCLE_TABLE_FLICK = {
        {"@", "_", "/", ":", "1"},
        {"a", "b", "c",null, "2"},
        {"d", "e", "f",null, "3"},
        {"g", "h", "i",null, "4"},
        {"j", "k", "l",null, "5"},
        {"m", "n", "o",null, "6"},
        {"p", "q", "r", "s", "7"},
        {"t", "u", "v",null, "8"},
        {"w", "x", "y", "z", "9"},
        {"-",null, "0",null,null},
        {".", ",", "?", "!",null}
    };

    /** Flick toggle cycle table for half-width alphabet shift */
    private static final String[][] JP_HALF_ALPHABET_SHIFT_CYCLE_TABLE_FLICK = {
        {"@", "_", "/", ":", "1"},
        {"A", "B", "C",null, "2"},
        {"D", "E", "F",null, "3"},
        {"G", "H", "I",null, "4"},
        {"J", "K", "L",null, "5"},
        {"M", "N", "O",null, "6"},
        {"P", "Q", "R", "S", "7"},
        {"T", "U", "V",null, "8"},
        {"W", "X", "Y", "Z", "9"},
        {"-",null, "0",null,null},
        {".", ",", "?", "!",null}
    };

    /** Flick toggle cycle table for half-width alphabet (EMAIL) */
    private static final String[][] JP_HALF_ALPHABET_EMAIL_CYCLE_TABLE_FLICK = {
        {"@", "_", "'", "\"", "1"},
        {"a", "b", "c",null, "2"},
        {"d", "e", "f",null, "3"},
        {"g", "h", "i",null, "4"},
        {"j", "k", "l",null, "5"},
        {"m", "n", "o",null, "6"},
        {"p", "q", "r", "s", "7"},
        {"t", "u", "v",null, "8"},
        {"w", "x", "y", "z", "9"},
        {"-",null, "0",null,null},
        {".", ",", "?", "!",null}
    };

    /** Flick toggle cycle table for half-width alphabet shift (EMAIL) */
    private static final String[][] JP_HALF_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE_FLICK = {
        {"@", "_", "'", "\"", "1"},
        {"A", "B", "C",null, "2"},
        {"D", "E", "F",null, "3"},
        {"G", "H", "I",null, "4"},
        {"J", "K", "L",null, "5"},
        {"M", "N", "O",null, "6"},
        {"P", "Q", "R", "S", "7"},
        {"T", "U", "V",null, "8"},
        {"W", "X", "Y", "Z", "9"},
        {"-",null, "0",null,null},
        {".", ",", "?", "!",null}
    };

    /** Flick toggle cycle table for half-width alphabet (URL) */
    private static final String[][] JP_HALF_ALPHABET_URL_CYCLE_TABLE_FLICK = {
        {"/", "_", "'", "\"", "1"},
        {"a", "b", "c",null, "2"},
        {"d", "e", "f",null, "3"},
        {"g", "h", "i",null, "4"},
        {"j", "k", "l",null, "5"},
        {"m", "n", "o",null, "6"},
        {"p", "q", "r", "s", "7"},
        {"t", "u", "v",null, "8"},
        {"w", "x", "y", "z", "9"},
        {"-",null, "0",null,null},
        {".", ",", "?", "!",null}
    };

    /** Flick toggle cycle table for half-width alphabet (URL) */
    private static final String[][] JP_HALF_ALPHABET_URL_SHIFT_CYCLE_TABLE_FLICK = {
        {"/", "_", "'", "\"", "1"},
        {"A", "B", "C",null, "2"},
        {"D", "E", "F",null, "3"},
        {"G", "H", "I",null, "4"},
        {"J", "K", "L",null, "5"},
        {"M", "N", "O",null, "6"},
        {"P", "Q", "R", "S", "7"},
        {"T", "U", "V",null, "8"},
        {"W", "X", "Y", "Z", "9"},
        {"-",null, "0",null,null},
        {".", ",", "?", "!",null}
    };

    /** Replace table for half-width alphabet */
    private static final HashMap<String,String> JP_HALF_ALPHABET_REPLACE_TABLE = new HashMap<String,String>() {{
        put("A", "a"); put("B", "b"); put("C", "c"); put("D", "d"); put("E", "e");
        put("a", "A"); put("b", "B"); put("c", "C"); put("d", "D"); put("e", "E");
        put("F", "f"); put("G", "g"); put("H", "h"); put("I", "i"); put("J", "j");
        put("f", "F"); put("g", "G"); put("h", "H"); put("i", "I"); put("j", "J");
        put("K", "k"); put("L", "l"); put("M", "m"); put("N", "n"); put("O", "o");
        put("k", "K"); put("l", "L"); put("m", "M"); put("n", "N"); put("o", "O");
        put("P", "p"); put("Q", "q"); put("R", "r"); put("S", "s"); put("T", "t");
        put("p", "P"); put("q", "Q"); put("r", "R"); put("s", "S"); put("t", "T");
        put("U", "u"); put("V", "v"); put("W", "w"); put("X", "x"); put("Y", "y");
        put("u", "U"); put("v", "V"); put("w", "W"); put("x", "X"); put("y", "Y");
        put("Z", "z");
        put("z", "Z");
    }};

    /** Empty replace table for FLICK_DIRECTION_NEUTRAL_INDEX */
    private static final HashMap<String,String> JP_EMPTY_REPLACE_TABLE = new HashMap<String,String>();

    /** Character table for full-width number */
    private static final char[] INSTANT_CHAR_CODE_FULL_NUMBER =
        "\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19\uff10\uff03\uff0a".toCharArray();

    /** Flick toggle cycle table for full-width number */
    private static final String[][] JP_FULL_NUMBER_CYCLE_TABLE_FLICK = {
      {"\uff11", "\uff0e", "\uff20", "\uff0d", null},
      {"\uff12", "\uff0f", "\uff1a", "\uff3f", null},
      {"\uff13", "\uff5e", "\uff05", "\uff3e", null},
      {"\uff14", "\uff3b", "\u2018", "\uff3d", "\u2019"},
      {"\uff15", "\uff1c", "\uff04", "\uff1e", "\uffe5"},
      {"\uff16", "\uff5b", "\uff06", "\uff5d", "\u201d"},
      {"\uff17", "\uff3c", null, "\uff5c", null},
      {"\uff18", "\uff08", null, "\uff09", null},
      {"\uff19", "\uff1d", null, "\uff1b", null},
      {"\uff10", null, "\uff0b", null, null},
      {"\uff03", "\uff0c", null, "\uff0e", null},
      {"\uff0a", "\uff01", null, "\uff1f", null}
    };

    /** Character table for half-width number */
    private static final char[] INSTANT_CHAR_CODE_HALF_NUMBER =
        "1234567890#*".toCharArray();

    /** Flick toggle cycle table for half-width number */
    private static final String[][] JP_HALF_NUMBER_CYCLE_TABLE_FLICK = {
      {"\u0031", "\u002e", "\u0040", "\u002d", null},
      {"\u0032", "\u002f", "\u003a", "\u005f", null},
      {"\u0033", "\u007e", "\u0025", "\u005e", null},
      {"\u0034", "\u005b", "\u0060", "\u005d", "\u0027"},
      {"\u0035", "\u003c", "\u0024", "\u003e", "\u00a5"},
      // Since """ and "\" are special characters in Java, the export preview is abnormal if using unicode.
      {"\u0036", "\u007b", "\u0026", "\u007d", "\""},
      {"\u0037", "\\", null, "\u007c", null},
      {"\u0038", "\u0028", null, "\u0029", null},
      {"\u0039", "\u003d", null, "\u003b", null},
      {"\u0030", null, "\u002b", null, null},
      {"\u0023", "\u002c", null, "\u002e", null},
      {"\u002a", "\u0021", null, "\u003f", null}
    };

    /** Toggle cycle table for lattice input key */
    private static final String[] JP_LATTICE_CYCLE_TABLE = {"\u300c", "\u300d"};

    /** The constant for KeyMode. It means that input mode is invalid. */
    private static final int INVALID_KEYMODE = -1;

    /** KeyIndex of "Moji" key on 12 keyboard (depends on the definition of keyboards) */
    private static final int KEY_INDEX_CHANGE_MODE_12KEY = 15;

    /** KeyIndex of "Moji" key on QWERTY keyboard (depends on the definition of keyboards) */
    private static final int KEY_INDEX_CHANGE_MODE_QWERTY = 29;

    /** Contextual menu position for settings */
    private static final int POS_SETTINGS = 0;

    /** Contextual menu position for methods */
    private static final int POS_METHOD = 1;

    /** Contextual menu position for mushroom */
    private static final int POS_MUSHROOM = 2;

    /** Contextual menu position for change 12key or qwerty keyboard */
    private static final int POS_KEYBOARDTYPE = 3;

    /** Contextual menu position for change input mode */
    private static final int POS_INPUTMODE = 4;

    /** Contextual menu position for user dictionary */
    private static final int POS_USER_DICTIONARY = 5;

    /** Contextual menu position for keyboard theme */
    private static final int POS_KEYBOARDTHEME = 6;

    /** .com key insert text */
    private static final String INPUT_COM_TEXT = ".com";

    /** Contextual menu position for 10key */
    private static final int POS_10KEY = 0;

    /** Contextual menu position for qwerty */
    private static final int POS_QWERTY = 1;

    /** Contextual menu position for handwriting */
    private static final int POS_HWR = 2;

    /** Keyboard width scale */
    private static final float KEYBOARD_SCALE_WIDTH_DEFAULT = 1.0f;

    /** Flag to call DOCOMO voice editor app,
        this value is changed to false in the build script of KDDI */
    private static final boolean ENABLE_DOCOMO = false;

    /** Key ID */
    private static final int KEY_ID_NAVI_SELECT_PREVIEW      = 601;
    private static final int KEY_ID_NAVI_DESELECT_PREVIEW    = 602;
    private static final int KEY_ID_NAVI_CLOSE_PREVIEW       = 603;
    private static final int KEY_ID_NAVI_COPY_PREVIEW        = 604;
    private static final int KEY_ID_NAVI_PASTE_PREVIEW       = 605;
    private static final int KEY_ID_NAVI_CUT_PREVIEW         = 606;
    private static final int KEY_ID_NAVI_ALL_PREVIEW         = 607;
    private static final int KEY_ID_NAVI_SELECT      = 611;
    private static final int KEY_ID_NAVI_DESELECT    = 612;
    private static final int KEY_ID_NAVI_CLOSE       = 613;
    private static final int KEY_ID_NAVI_ALL         = 614;
    private static final int KEY_ID_NAVI_COPY        = 615;
    private static final int KEY_ID_NAVI_PASTE       = 616;
    private static final int KEY_ID_NAVI_CUT         = 617;
    private static final int KEY_ID_NAVI_COPY_OFF    = 618;
    private static final int KEY_ID_NAVI_PASTE_OFF   = 619;
    private static final int KEY_ID_NAVI_CUT_OFF     = 620;

    /** Drawable size */
    private static final int DRAWABLE_SIZE           = 200;

    //////////////////////////////////// property
    /** Type of input mode */
    private int mInputType = INPUT_TYPE_TOGGLE;

    /**
     * Character table to input when mInputType becomes INPUT_TYPE_INSTANT.
     * <br>
     * (Either INSTANT_CHAR_CODE_FULL_NUMBER or INSTANT_CHAR_CODE_HALF_NUMBER)
     */
    private char[] mCurrentInstantTable = null;

    /**
     * Input mode that is not able to be changed.
     * <br>
     * If ENABLE_CHANGE_KEYMODE is set, input mode can change.
     */
    private int[] mLimitedKeyMode = null;

    /**
     * Input mode that is given the first priority.
     * <br>
     * If ENABLE_CHANGE_KEYMODE is set, input mode can change.
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

    /** Auto caps mode */
    private boolean mEnableAutoCaps = true;

    /** Flick mode */
    private boolean mEnableFlick = false;

    /** Toggle for flick mode */
    private boolean mEnableFlickToggle = true;

    /** The preview popup mode */
    private boolean mEnablePopup = true;

    /** Whether the InputType is null */
    private boolean mIsInputTypeNull = false;

    /** Whether being able to use EmojiUNI6 */
    private boolean mEnableEmojiUNI6 = false;

    /** Whether being able to use Emoji */
    private boolean mEnableEmoji = false;

    /** Whether being able to use DecoEmoji */
    private boolean mEnableDecoEmoji = false;

    /** Whether being able to use Symbol */
    private boolean mEnableSymbol = false;

    /** The menu item */
    private int[] mMenuItem;

    /** Whether keyboard type is not save. */
    private boolean mIsKeyboardTypeNotSave;

    /** View objects (one touch emoji list) */
    protected OneTouchEmojiListViewManager mOneTouchEmojiListViewManager
            = new OneTouchEmojiListViewManager();

    /** One touch emoji list width */
    protected int mOneTouchEmojiListWidth = 0;

    /** Navigation keypad selection mode */
    private boolean mSelectionMode = false;

    /** KeyIndex of "Select/Deselect" key on Navigation Keypad */
    private int mSelectKeyIndex;

    /** KeyIndex of "Copy" key on Navigation Keypad */
    private int mCopyKeyIndex;

    /** KeyIndex of "Paste" key on Navigation Keypad */
    private int mPasteKeyIndex;

    /** KeyIndex of "Cut" key on Navigation Keypad */
    private int mCutKeyIndex;

    /** KeyIndex of "Close" key on Navigation Keypad */
    private int mCloseKeyIndex;

    /** KeyIndex of "All" key on Navigation Keypad */
    private int mAllKeyIndex;

    /** Listener that is received the clipboard changing. */
    private OnPrimaryClipChangedListener mClipListener;

    /** Whether current input area is password. */
    private boolean mInPasswordArea;

    /** Input mode before toggle to voice input mode. */
    private int mKeyModeBefToggleToVoice = KEYMODE_JA_FULL_HIRAGANA;

    /** State of the shift key in number input mode */
    private boolean mNumberModeShiftStateFlg = false;

    /** Whether state of the shift key have saved value in Japanese or English input mode */
    private boolean mJaEnModeShiftStateSaved = false;

    /** State of the shift key in Japanese or English input mode */
    private int mOldShiftOnJaEnMode = 0;

    /** Saved state of the caps lock in Japanese or English input mode */
    private boolean mOldCapsLockJaEnMode = false;

    /** Input mode when return from the list of symbols */
    private int mSymbolBackKeyMode = 0;

    //////////////////////////////////// method
    /** Default constructor */
    public DefaultSoftKeyboardJAJP(IWnnLanguageSwitcher wnn) {
        super(wnn);
        mCurrentLanguage     = LANG_JA;
        if (OpenWnn.isTabletMode()) {
            mCurrentKeyboardType = KEYBOARD_QWERTY;
        } else {
            mCurrentKeyboardType = KEYBOARD_12KEY;
        }
        mShiftOn             = KEYBOARD_SHIFT_OFF;
        if (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage())) {
        mCurrentKeyMode      = KEYMODE_JA_FULL_HIRAGANA;
        } else {
            mCurrentKeyMode = KEYMODE_JA_HALF_ALPHABET;
        }

        mClipListener = new OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                mSelectionMode = false;
                if (mCurrentKeyboardType == KEYBOARD_NAVIGATION) {
                    setNavigationKeyState();
                }
            }
        };
    }

    /**
     * Shows the dialog when long press a key.
     *
     * @param key Keyboard.Key
     * @return boolean
     */
    @Override public boolean onLongPress(Keyboard.Key key) {
        if (key.codes[0] == KEYCODE_QWERTY_SHIFT) {
                mKeyboardView.dismissKeyPreview();
                if (getSoftLockEnabled()) {
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
                    if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                        setUndoKey(mCanUndo);
                    }
                }
                return true;
        } else if (key.codes[0] == KEYCODE_JP12_LEFT) {
                wrapStartNavigationKeypad();
                return true;
        } else {
            return super.onLongPress(key);
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#createKeyboards */
    @Override protected void createKeyboards(OpenWnn parent) {

        mUndoKey = mWnn.getResources().getString(R.string.ti_key_12key_undo_txt);
        /* WnnKeyboardFactory[# of Languages][portrait/landscape][# of keyboard type][shift off/on][voiceinput off/on][max # of key-modes][noinput/input] */
        /* WnnKeyboardFactory[# of Languages][portrait/landscape][# of keyboard type]
        [shift off/on][max # of key-modes][input key type][noinput/input] */
        mKeyboard = new WnnKeyboardFactory[3][2][4][2][12][4][2];

        set5key();

        if (mHardKeyboardHidden) {
            /* Create the suitable keyboard object */
            if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
                createKeyboardsPortrait(parent);
            } else {
                createKeyboardsLandscape(parent);
            }
            if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_12KEY));
            } else {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_QWERTY));
            }
        } else if (mEnableHardware12Keyboard) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                              IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_12KEY));
        } else {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                          IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_QWERTY));
        }
    }

    /**
     * Commit the pre-edit string for committing operation that is not explicit.
     * <br>
     * (ex. when a candidate is selected)
     */
    private void commitText() {
        if (!mNoInput) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.COMMIT_COMPOSING_TEXT));
        }
    }

    /**
     * Change input mode.
     * <br>
     * @param keyMode   The type of input mode
     */
    public void changeKeyMode(int keyMode) {
        int targetMode = filterKeyMode(keyMode);

        if (WnnKeyboardFactory.getSplitMode()
                && ONETOUCHEMOJILIST_RIGHT.equals(mOneTouchEmojiListMode)) {
            mOneTouchEmojiListMode = ONETOUCHEMOJILIST_LEFT;
        }

        if (targetMode == INVALID_KEYMODE) {
            return;
        }

        commitText();

        if (IWnnImeJaJp.isLatticeInputMode()) {
            IWnnImeJaJp.setLatticeInputMode(false);
        }

        int keyboardTypeBack = mCurrentKeyboardType;
        if (targetMode != KEYMODE_JA_VOICE) {
                // KEYMODE_JA_VOICE mode, since there is no keyboard surface, this process is skipped.
            mCurrentKeyboardType = getKeyboardTypePref(targetMode);
            mCurrentKeyMode = targetMode;
        }
        mPrevInputKeyCode = 0;
        if (mCurrentKeyboardType == KEYBOARD_12KEY) {
            mCapsLock = false;
        }
        if (mCurrentKeyboardType != keyboardTypeBack) {
            changeKeyboardType(mCurrentKeyboardType);
        } else {
            setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol);
        }

        if (!mJaEnModeShiftStateSaved && (mCurrentKeyboardType == KEYBOARD_QWERTY)
                && (targetMode == KEYMODE_JA_HALF_NUMBER
                    || targetMode == KEYMODE_JA_FULL_NUMBER)) {
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
                if (keyboardView != null) {
                    mJaEnModeShiftStateSaved = true;
                    mOldShiftOnJaEnMode = keyboardView.isShifted()
                            ? KEYBOARD_SHIFT_ON : KEYBOARD_SHIFT_OFF;
                    mOldCapsLockJaEnMode = keyboardView.isCapsLock();
                    mShiftOn = mNumberModeShiftStateFlg ? KEYBOARD_SHIFT_ON : KEYBOARD_SHIFT_OFF;
                    mCapsLock = mNumberModeShiftStateFlg;
                }
            }
        }

        if (mJaEnModeShiftStateSaved) {
            if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                if ((targetMode != KEYMODE_JA_HALF_NUMBER)
                        && (targetMode != KEYMODE_JA_FULL_NUMBER)) {
                    mJaEnModeShiftStateSaved = false;
                    mShiftOn = mOldShiftOnJaEnMode;
                    mCapsLock = mOldCapsLockJaEnMode;
                } else {
                    mShiftOn = mNumberModeShiftStateFlg ? KEYBOARD_SHIFT_ON : KEYBOARD_SHIFT_OFF;
                    mCapsLock = mNumberModeShiftStateFlg;
                }
            }
        }

        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
            if (keyboardView != null) {
                keyboardView.setShifted(mShiftOn == KEYBOARD_SHIFT_ON);
                keyboardView.setCapsLock(mCapsLock);
            }
            if (targetMode != KEYMODE_JA_VOICE) {
                mCurrentKeyMode = targetMode;
            }
            mPrevInputKeyCode = 0;
        } else {
            if (mCapsLock) {
                mCapsLock = false;
            }
        }

        Keyboard kbd = getModeChangeKeyboard(targetMode);
        setKeyMode();

        int mode = OpenWnnEvent.Mode.DIRECT;

        switch (targetMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = OpenWnnEvent.Mode.DEFAULT;
            break;

        case KEYMODE_JA_HALF_ALPHABET:
            if (USE_ENGLISH_PREDICT) {
                mInputType = INPUT_TYPE_TOGGLE;
                mode = OpenWnnEvent.Mode.NO_LV1_CONV;
            } else {
                mInputType = INPUT_TYPE_TOGGLE;
                mode = OpenWnnEvent.Mode.DIRECT;
            }
            break;

        case KEYMODE_JA_FULL_NUMBER:
            mInputType = INPUT_TYPE_INSTANT;
            mode = OpenWnnEvent.Mode.DIRECT;
            mCurrentInstantTable = INSTANT_CHAR_CODE_FULL_NUMBER;
            break;

        case KEYMODE_JA_HALF_NUMBER:
            mInputType = INPUT_TYPE_INSTANT;
            mode = OpenWnnEvent.Mode.DIRECT;
            mCurrentInstantTable = INSTANT_CHAR_CODE_HALF_NUMBER;
            break;

        case KEYMODE_JA_FULL_KATAKANA:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = IWnnImeJaJp.ENGINE_MODE_FULL_KATAKANA;
            break;

        case KEYMODE_JA_FULL_ALPHABET:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = OpenWnnEvent.Mode.DIRECT;
            break;

        case KEYMODE_JA_HALF_KATAKANA:
            mInputType = INPUT_TYPE_TOGGLE;
            mode = IWnnImeJaJp.ENGINE_MODE_HALF_KATAKANA;
            break;

        case KEYMODE_JA_VOICE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.VOICE_INPUT));
            return;

        default:
            break;
        }

        setStatusIcon();
        changeKeyboard(kbd);
        setUndoKey(mCanUndo);
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, mode));

        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
            if (keyboardView != null) {
                if (getSoftLockEnabled()
                        && (mShiftOn == KEYBOARD_SHIFT_ON)
                        && keyboardView.isCapsLock()) {
                    mCapsLock = true;
                    mShiftOn = KEYBOARD_SHIFT_ON;
                    keyboardView.setShifted(true);
                    keyboardView.setCapsLock(true);
                }
            }
        }
    }

     /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#initView */
     @Override public View initView(OpenWnn parent, int width, int height) {
        if (OpenWnn.isDebugging()) {
            Log.d("OpenWnn", "DefaultSoftKeyboardJAJP.initView()" + (mKeyboardView != null));
        }
        boolean isSplitViewMode = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isSplitViewMode();
        InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.setFloatingLandscape(false);
        if(isSplitViewMode) {
            if (parent.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.setFloatingLandscape(true);
            }
        }

        View view = super.initView(parent, width, height);

        mOneTouchEmojiListViewManager.initView(parent, this);
        if (!OpenWnn.getCurrentIme().isKeepInput()) {
            changeKeyboardType(getKeyboardTypePref(mCurrentKeyMode));
        }

        WnnKeyboardFactory keyboard
                = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType]
                        [mShiftOn][mCurrentKeyMode][mCurrentKeyInputType][0];
        if (keyboard != null && mKeyboardView != null) {
            if (OpenWnn.getCurrentIme().isKeepInput()) {
                mCurrentKeyboard = keyboard.getKeyboard();
                if (mIsSymbolKeyboard) {
                    setSymbolKeyboard();
                } else {
                    setNormalKeyboard();
                    if (mKeyboardView instanceof MultiTouchKeyboardView) {
                        if (mCapsLock) {
                            ((MultiTouchKeyboardView) mKeyboardView).setShifted(true);
                            if (getSoftLockEnabled()) {
                                ((MultiTouchKeyboardView) mKeyboardView).setCapsLock(true);
                            } else {
                                ((MultiTouchKeyboardView) mKeyboardView).setCapsLockMode(true);
                            }
                        }
                    } else {
                        setShifted(mShiftOn);
                    }
                }
            } else {
                changeKeyMode(mCurrentKeyMode);
            }
        }
        ClipboardManager m = (ClipboardManager)parent.getSystemService(Context.CLIPBOARD_SERVICE);
        m.removePrimaryClipChangedListener(mClipListener);
        m.addPrimaryClipChangedListener(mClipListener);

        return view;
     }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#changeKeyboardType */
    @Override public void changeKeyboardType(int type) {
        if (mCapsLock) {
            mCapsLock = false;
        }
        if ((type == KEYBOARD_12KEY)
                && mCurrentKeyMode != KEYMODE_JA_FULL_HIRAGANA
                && mCurrentKeyMode != KEYMODE_JA_FULL_KATAKANA
                && mCurrentKeyMode != KEYMODE_JA_HALF_KATAKANA) {
            mNumberModeShiftStateFlg = false;
            mJaEnModeShiftStateSaved = false;
            mOldCapsLockJaEnMode = false;
            mOldShiftOnJaEnMode = KEYBOARD_SHIFT_OFF;
            if ((mKeyboardView != null)
                    && (mKeyboardView instanceof MultiTouchKeyboardView)) {
                mKeyboardView.setShifted(false);
                mShiftOn = KEYBOARD_SHIFT_OFF;
                ((MultiTouchKeyboardView) mKeyboardView).setCapsLock(false);
            }
        }
        commitText();
        Keyboard kbd = getTypeChangeKeyboard(type);
        if (kbd != null) {
            mCurrentKeyboardType = type;
            changeKeyboard(kbd);
            setKeyMode();
            setShiftByEditorInfo(true);
            setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol);
        }
        if (type == KEYBOARD_12KEY) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                          IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_12KEY));
        } else {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                          IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_QWERTY));
        }
        mShiftOn = KEYBOARD_SHIFT_OFF;

        setKeyboardTypePref(mCurrentKeyboardType);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#onKey */
    @Override public void onKey(int primaryCode, int[] keyCodes) {

        if (OpenWnn.getCurrentIme() == null) {
            Log.e("OpenWnn", "DefaultSoftKeyboardJAJP::onKey()  Unprocessing onCreate() ");
            return;
        }

        super.onKey(primaryCode,keyCodes);
        if (mIsKeyProcessFinish) {
            return;
        }

        if (((mPrevInputKeyCode == KEYCODE_PERIOD_LATTICE_LEFT)
                || (mPrevInputKeyCode == KEYCODE_PERIOD_LATTICE_RIGHT))
                && ((primaryCode != KEYCODE_PERIOD_LATTICE_LEFT)
                        && (primaryCode != KEYCODE_PERIOD_LATTICE_RIGHT))) {
            commitText();
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, OpenWnnEvent.Mode.DEFAULT));
            mPrevInputKeyCode = 0;
            mNoInput = true;
            IWnnImeJaJp.setLatticeInputMode(false);
        }

        // If inputType is Null, only ModeKey and DirectionKey is effective,
        // except that InputMode is QWERTY half-width Number or QWERTY half-width Alphabet.
        if (mIsInputTypeNull && (!((mCurrentKeyboardType == KEYBOARD_QWERTY)
            && ((mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)
                || (mCurrentKeyMode == KEYMODE_JA_HALF_NUMBER))) ||
                (mCurrentKeyboardType == KEYBOARD_NAVIGATION))) {
        switch (primaryCode) {
        case KEYCODE_JP12_TOGGLE_MODE:
        case KEYCODE_QWERTY_TOGGLE_MODE:
            nextLanguageMode();
            break;

            case KEYCODE_KEYPAD_UP:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KeyEvent.KEYCODE_DPAD_UP)));
                break;

            case KEYCODE_KEYPAD_DOWN:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KeyEvent.KEYCODE_DPAD_DOWN)));
                break;

            case KEYCODE_KEYPAD_LEFT:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KeyEvent.KEYCODE_DPAD_LEFT)));
                break;

            case KEYCODE_KEYPAD_RIGHT:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KeyEvent.KEYCODE_DPAD_RIGHT)));
                break;

            case KEYCODE_JP12_ENTER:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KeyEvent.KEYCODE_ENTER)));
                break;

            default:
                break;

            }
            return;
        }

        Context context = OpenWnn.superGetContext();
        int oldCurrentKeyMode = mCurrentKeyMode;

        switch (primaryCode) {
        case KEYCODE_JP12_TOGGLE_MODE:
        case KEYCODE_QWERTY_TOGGLE_MODE:
            nextLanguageMode();
            break;

        case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
        case KEYCODE_JP12_BACKSPACE:
            mSelectionMode = false;
            if (mCurrentKeyboardType == KEYBOARD_NAVIGATION) {
                setNavigationKeyState();
            }

            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
            break;

        case DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT:
        case DefaultSoftKeyboard.KEYCODE_QWERTY_NUM_SHIFT:
        case DefaultSoftKeyboard.KEYCODE_JP12_PHONE_SHIFT:
        case DefaultSoftKeyboard.KEYCODE_JP12_ALPHABET_SHIFT:
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
                if (keyboardView != null) {
                    if (mCurrentKeyboardType == KEYBOARD_QWERTY && (mCurrentKeyMode == KEYMODE_JA_HALF_NUMBER || mCurrentKeyMode == KEYMODE_JA_FULL_NUMBER)) {
                        mNumberModeShiftStateFlg = !mNumberModeShiftStateFlg;
                        if (!mJaEnModeShiftStateSaved) {
                            mJaEnModeShiftStateSaved = true;
                            mOldShiftOnJaEnMode = keyboardView.isShifted()
                                    ? KEYBOARD_SHIFT_ON : KEYBOARD_SHIFT_OFF;
                            mOldCapsLockJaEnMode = keyboardView.isCapsLock();
                        }
                    }
                    if (getSoftLockEnabled()) {
                        if (keyboardView.isShifted()) {
                            if (keyboardView.isCapsLock()) {
                                keyboardView.setShifted(false);
                                keyboardView.setCapsLock(false);
                                mCapsLock = false;
                                mShiftOn = KEYBOARD_SHIFT_OFF;
                            } else {
                                mCapsLock = true;
                                mShiftOn = KEYBOARD_SHIFT_ON;
                                keyboardView.setCapsLock(true);
                            }
                        } else {
                            mCapsLock = false;
                            mShiftOn = KEYBOARD_SHIFT_ON;
                            keyboardView.setShifted(true);
                            keyboardView.setCapsLock(false);
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
            } else {
                if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                    toggleShiftLock();
                    setUndoKey(mCanUndo);
                    if (!mNoInput) {
                        HashMap replaceTable = getReplaceTable();
                        if (replaceTable == null) {
                            Log.e("OpenWnn", "not founds replace table");
                        } else {
                            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.REPLACE_CHAR, replaceTable));
                            mPrevInputKeyCode = primaryCode;
                        }
                    }
                } else if (getSoftLockEnabled()) {
                    if (mShiftOn == KEYBOARD_SHIFT_OFF) {
                        setShifted(KEYBOARD_SHIFT_ON);
                    } else {
                        if (!mCapsLock) {
                            // For toggleShiftLock() lock the shift key.
                            mShiftOn = KEYBOARD_SHIFT_OFF;
                        }
                        toggleShiftLock();
                    }
                } else {
                    toggleShiftLock();
                }
            }
            break;

        case DefaultSoftKeyboard.KEYCODE_QWERTY_ALT: // Unused
            processAltKey();
            break;

        case KEYCODE_QWERTY_ENTER:
        case KEYCODE_JP12_ENTER:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));

            EditorInfo edit = mWnn.getCurrentInputEditorInfo();
            switch (edit.imeOptions
                    & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
                    case EditorInfo.IME_ACTION_DONE:
                        //set hide softkeyboard flag at here.
                        mWnn.mOriginalInputMethodSwitcher.mIsHideSelf = true;
                    break;
            }

            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
                if (keyboardView != null) {
                    if (getSoftLockEnabled() && keyboardView.isCapsLock()) {
                        mCapsLock = true;
                        mShiftOn = KEYBOARD_SHIFT_ON;
                        keyboardView.setShifted(true);
                        keyboardView.setCapsLock(true);
                    }
                }
            }
            break;

        case KEYCODE_JP12_REVERSE:
            if (!mEnableHardware12Keyboard
                    && (mKeyboardView.getKeyboard().getKeys().get(0).label != null)
                    && (mKeyboardView.getKeyboard().getKeys().get(0).label.equals(mUndoKey))) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.UNDO));
                break;
            }

            if (!mNoInput) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_REVERSE_CHAR, mCurrentCycleTable));
            }
            break;

        case KEYCODE_QWERTY_KBD:
            changeKeyboardType(KEYBOARD_12KEY);
            break;

        case KEYCODE_JP12_KBD:
            changeKeyboardType(KEYBOARD_QWERTY);
            break;

        case KEYCODE_JP12_EMOJI:
        case KEYCODE_QWERTY_EMOJI:
            toggleSymbolNumericMode();
            break;

        case KEYCODE_4KEY_MODE:
        case KEYCODE_JP12_CLOSE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)));
            break;

        case KEYCODE_4KEY_UP:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP));
            break;

        case KEYCODE_4KEY_DOWN:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN));
            break;

        case KEYCODE_4KEY_CLEAR:
            mWnnSwitcher.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            return;

        case KEYCODE_JP12_1:
        case KEYCODE_JP12_2:
        case KEYCODE_JP12_3:
        case KEYCODE_JP12_4:
        case KEYCODE_JP12_5:
        case KEYCODE_JP12_6:
        case KEYCODE_JP12_7:
        case KEYCODE_JP12_8:
        case KEYCODE_JP12_9:
        case KEYCODE_JP12_0:
        case KEYCODE_JP12_SHARP:
            /* Processing to input by ten key */
            if (mInputType == INPUT_TYPE_INSTANT) {
                /* Send a input character directly if instant input type is selected */
                commitText();
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR,
                                              mCurrentInstantTable[getTableIndex(primaryCode)]));
            } else {
                if (mEnableFlick && !mEnableFlickToggle && isEnableFlickMode(primaryCode)) {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOUCH_OTHER_KEY));
                }
                if ((mPrevInputKeyCode != primaryCode)) {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOUCH_OTHER_KEY));
                    if ((mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)
                            && (primaryCode == KEYCODE_JP12_SHARP)) {
                        /* Commit text by symbol character (',' '.') when alphabet input mode is selected */
                        commitText();
                    }
                }

                /* Convert the key code to the table index and send the toggle event with the table index */
                String[][] cycleTable = getCycleTable();
                if (cycleTable == null) {
                    Log.e("OpenWnn", "not founds cycle table");
                } else {
                    int index = getTableIndex(primaryCode);
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, cycleTable[index]));
                    mCurrentCycleTable = cycleTable[index];
                }
                mPrevInputKeyCode = primaryCode;
            }
            break;

        case KEYCODE_JP12_ASTER:
            if (mInputType == INPUT_TYPE_INSTANT) {
                commitText();
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR,
                                              mCurrentInstantTable[getTableIndex(primaryCode)]));
            } else {
                if (!mNoInput) {
                    /* Processing to toggle Dakuten, Handakuten, and capital */
                    HashMap<String, String> replaceTable = getReplaceTable();
                    if (replaceTable == null) {
                        Log.e("OpenWnn", "not founds replace table");
                    } else {
                        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.REPLACE_CHAR, replaceTable));
                        mPrevInputKeyCode = primaryCode;
                    }
                }
            }
            break;

        case KEYCODE_PERIOD_LATTICE_LEFT:
        case KEYCODE_PERIOD_LATTICE_RIGHT:
            if (mPrevInputKeyCode != primaryCode) {
                IWnnImeJaJp.setLatticeInputMode(true);
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_FULL_KATAKANA));
                mNoInput = false;
            }
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, (char)primaryCode));
            mPrevInputKeyCode = primaryCode;
            break;

        case KEYCODE_SWITCH_FULL_HIRAGANA:
        case KEYCODE_SWITCH_FULL_HIRAGANA_MENU:
            /* Change mode to Full width hiragana */
            changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
            break;

        case KEYCODE_SWITCH_FULL_KATAKANA:
            /* Change mode to Full width katakana */
            changeKeyMode(KEYMODE_JA_FULL_KATAKANA);
            break;

        case KEYCODE_SWITCH_FULL_ALPHABET:
            /* Change mode to Full width alphabet */
            changeKeyMode(KEYMODE_JA_FULL_ALPHABET);
            break;

        case KEYCODE_SWITCH_FULL_NUMBER:
        case KEYCODE_SWITCH_FULL_NUMBER_TEMP_INPUT:
        case KEYCODE_SWITCH_FULL_NUMBER_MENU:
            /* Change mode to Full width numeric */
            changeKeyMode(KEYMODE_JA_FULL_NUMBER);
            break;

        case KEYCODE_SWITCH_HALF_KATAKANA:
            /* Change mode to Half width katakana */
            changeKeyMode(KEYMODE_JA_HALF_KATAKANA);
            break;

        case KEYCODE_SWITCH_HALF_ALPHABET:
        case KEYCODE_SWITCH_HALF_ALPHABET_MENU:
            /* Change mode to Half width alphabet */
            changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
            break;

        case KEYCODE_SWITCH_HALF_NUMBER:
        case KEYCODE_SWITCH_HALF_NUMBER_TEMP_INPUT:
        case KEYCODE_SWITCH_HALF_NUMBER_MENU:
            /* Change mode to Half width numeric */
            changeKeyMode(KEYMODE_JA_HALF_NUMBER);
            break;

        case KEYCODE_SWITCH_VOICE:
            /* Change mode to voice */
            if (mEnableVoiceInput) {
                if (ENABLE_DOCOMO) {
                    switchToVoiceEditorIME();
                } else {
                    mKeyModeBefToggleToVoice = mCurrentKeyMode;
                    changeKeyMode(KEYMODE_JA_VOICE);
                }
            } else {
                if (mCurrentKeyboardType != KEYBOARD_NAVIGATION) {
                    showKeyboardTypeSwitchDialog();
                }
            }
            break;


        case KEYCODE_SELECT_CASE:
            int shifted = (mShiftOn == 0) ? 1 : 0;
            Keyboard newKeyboard = getShiftChangeKeyboard(shifted);
            if (newKeyboard != null) {
                mShiftOn = shifted;
                changeKeyboard(newKeyboard);
            }
            break;

        case KEYCODE_JP12_SPACE:
            if ((mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA) && !mNoInput) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CONVERT));
            } else {
                if (isInputModeHalfWidthCharacter()) {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, ' '));
                } else {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, '\u3000')); /* Full-width space */
                }
            }
            break;

        case KEYCODE_EISU_KANA:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_EISU_KANA));
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

        case KEYCODE_FUNCTION_KEY:
            int functionIndex = FunctionKey_FUNC_VOICEINPUT;
            if (!mKeyboardView.isFunctionEnable(FunctionKey_FUNC_VOICEINPUT)) {
                functionIndex = FunctionKey_FUNC_KBD;
            }
            switch (functionIndex) {
                case FunctionKey_FUNC_VOICEINPUT:
                    if (ENABLE_DOCOMO) {
                        switchToVoiceEditorIME();
                    } else {
                        mKeyModeBefToggleToVoice = mCurrentKeyMode;
                        changeKeyMode(KEYMODE_JA_VOICE);
                    }
                    break;
                case FunctionKey_FUNC_HANDWRITING:
                    OpenWnn.mOriginalInputMethodSwitcher.changeInputMethod(0);
                    break;
                case FunctionKey_FUNC_KBD:
                    if (mCurrentKeyboardType == KEYBOARD_12KEY) {
                        changeKeyboardType(KEYBOARD_QWERTY);
                    } else {
                        changeKeyboardType(KEYBOARD_12KEY);
                    }
                default:
                    break;
            }
            break;

        case KEYCODE_JP12_UP:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_UP)));
            break;

        case KEYCODE_JP12_DOWN:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_DOWN)));
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

        case KEYCODE_COM_URL:
        case KEYCODE_COM_EMAIL:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.COMMIT_INPUT_TEXT,
                                            INPUT_COM_TEXT .toCharArray()));
        break;

        case KEYCODE_NOP:
            break;


        case KEYCODE_INPUT_MODE:
            if (mCurrentKeyboardType != KEYBOARD_NAVIGATION) {
                showKeyboardTypeSwitchDialog();
            }
            break;

        case KEYCODE_KEYPAD_SELECT:
            mSelectionMode = !mSelectionMode;
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KEYCODE_KEYPAD_SELECT)));
            setNavigationKeyState();
            break;

        case KEYCODE_KEYPAD_CLOSE:
            mSelectionMode = false;
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KEYCODE_KEYPAD_CLOSE)));
            mCurrentKeyboardType = mPrevKeyboardType;
            changeKeyboardType(mCurrentKeyboardType);
            break;

        case KEYCODE_KEYPAD_UP:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_UP)));
            break;

        case KEYCODE_KEYPAD_DOWN:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_DOWN)));
            break;

        case KEYCODE_KEYPAD_LEFT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_LEFT)));
            break;

        case KEYCODE_KEYPAD_RIGHT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_RIGHT)));
            break;

        case KEYCODE_KEYPAD_HOME:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KEYCODE_KEYPAD_HOME)));
            break;


        case KEYCODE_KEYPAD_END:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KEYCODE_KEYPAD_END)));
            break;


        case KEYCODE_KEYPAD_ALL:
            mSelectionMode = true;
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KEYCODE_KEYPAD_ALL)));
            setNavigationKeyState();
           break;

        case KEYCODE_KEYPAD_COPY:
            // NOTE: Update state in onPrimaryClipChanged().

            if (isNavigationKeyEnable(mCopyKeyIndex)) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KEYCODE_KEYPAD_COPY)));
            }
            break;

        case KEYCODE_KEYPAD_PASTE:
            mSelectionMode = false;

            if (isNavigationKeyEnable(mPasteKeyIndex)) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KEYCODE_KEYPAD_PASTE)));
            }
            setNavigationKeyState();
            break;

        case KEYCODE_KEYPAD_CUT:
            // NOTE: Update state in onPrimaryClipChanged().

            if (isNavigationKeyEnable(mCutKeyIndex)) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN,
                                                           KEYCODE_KEYPAD_CUT)));
            }
            break;

        case KEYCODE_KEYBOARD_SETTING:
            // Setting Menu link
            mWnn.requestHideSelf(0);
            Intent intent = new Intent();
            Context c = getCurrentView().getContext();
            intent.setClass(c, ControlPanelStandard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(intent);
            break;

        case KEYCODE_FUNCTION_SETTINGS:
            showLongPressMenu();
            break;

        case KEYCODE_FUNCTION_KEYBOARDTYPE:
            switchKeyboardtype();
            break;

        case KEYCODE_FUNCTION_HANDWRITING:
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

        case KEYCODE_NUM_MOJI:
            if (OpenWnn.mOriginalInputMethodSwitcher.mSymbolMode) {
                ((IWnnLanguageSwitcher)mWnn).changeMode(
                        InputMethodSwitcher.IME_TYPE_HANDWRITING);
            } else if (((IWnnLanguageSwitcher)mWnn).getSymbolBackHangul()) {
                ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ko");
                ((IWnnLanguageSwitcher)mWnn).setSymbolBackHangul(false);
            } else {
                changeKeyMode(mSymbolBackKeyMode);
            }
            break;

        default:
            if (primaryCode >= 0) {
                if (mKeyboardView.isShifted()) {
                    primaryCode = Character.toUpperCase(primaryCode);
                }

                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, (char)primaryCode));
                if (mKeyboardView instanceof MultiTouchKeyboardView) {
                    MultiTouchKeyboardView keyboardView = (MultiTouchKeyboardView) mKeyboardView;
                    if (keyboardView != null) {
                        if (getSoftLockEnabled() && keyboardView.isCapsLock()) {
                            mCapsLock = true;
                            mShiftOn = KEYBOARD_SHIFT_ON;
                            keyboardView.setShifted(true);
                            keyboardView.setCapsLock(true);
                        }
                    }
                }

            } else {
                switch (primaryCode) {
                case KEYCODE_TOGGLE_FULL_KUTEN:
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_FULL_KUTEN));
                    break;

                case KEYCODE_TOGGLE_HALF_KUTEN:
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_HALF_KUTEN));
                    break;

                case KEYCODE_TOGGLE_FULL_PERIOD:
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_FULL_PERIOD));
                    break;

                case KEYCODE_TOGGLE_HALF_PERIOD:
                    if (mPrevInputKeyCode != primaryCode) {
                        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOUCH_OTHER_KEY));
                        /* Commit text by symbol character (',' '.') */
                        commitText();
                    }
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.TOGGLE_CHAR, CYCLE_TABLE_HALF_PERIOD));
                    break;

                default:
                    break;
                }
            }
            mPrevInputKeyCode = primaryCode;
            break;
        }

        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            mCapsLock = ((MultiTouchKeyboardView) mKeyboardView).isCapsLock();
        }
        /* update shift key's state */
        if (!mCapsLock && (primaryCode != DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT)
                && (primaryCode != DefaultSoftKeyboard.KEYCODE_JP12_ALPHABET_SHIFT)) {
            setShiftByEditorInfo(false);
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setPreferences */
    @Override public void setPreferences(SharedPreferences pref, EditorInfo editor) {

        mNumberModeShiftStateFlg = false;
        mJaEnModeShiftStateSaved = false;
        mOldCapsLockJaEnMode = false;
        mOldShiftOnJaEnMode = KEYBOARD_SHIFT_OFF;

        boolean preEnableFlick = mEnableFlick;
        Resources res = mWnn.getResources();

        mEnableFlick = pref.getBoolean("flick_input", res.getBoolean(R.bool.flick_input_default_value));
        mEnableFlickToggle = pref.getBoolean("flick_toggle_input", res.getBoolean(R.bool.flick_toggle_input_default_value));
        mEnablePopup = pref.getBoolean("popup_preview", res.getBoolean(R.bool.popup_preview_default_value));

        //#endif /* EMOJI-DOCOMO */
        {
            mOneTouchEmojiListMode = ONETOUCHEMOJILIST_HIDDEN;
        }

        super.setPreferences(pref, editor);

        // Change 10-Key number keyboard if flick setting is changed
        int halfId;
        int fullId;
        int halfIdkana;
        int fullIdkana;
        int halfIdkana_input;
        int fullIdkana_input;
        WnnKeyboardFactory[][][][] keyboard12Key = mKeyboard[LANG_JA][mDisplayMode][KEYBOARD_12KEY];

        if (preEnableFlick != mEnableFlick) {
            if (mEnableFlick) {
                halfId = R.xml.keyboard_12key_half_num;
                fullId = R.xml.keyboard_12key_full_num;
                halfIdkana = R.xml.keyboard_12key_half_katakana;
                fullIdkana = R.xml.keyboard_12key_full_katakana;
                halfIdkana_input = R.xml.keyboard_12key_half_katakana_input;
                fullIdkana_input = R.xml.keyboard_12key_full_katakana_input;
            } else {
                halfId = R.xml.keyboard_12key_half_num_flick_off;
                fullId = R.xml.keyboard_12key_full_num_flick_off;
                halfIdkana = R.xml.keyboard_12key_half_katakana_flick_off;
                fullIdkana = R.xml.keyboard_12key_full_katakana_flick_off;
                halfIdkana_input = R.xml.keyboard_12key_half_katakana_input_flick_off;
                fullIdkana_input = R.xml.keyboard_12key_full_katakana_input_flick_off;
            }
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(mWnn, halfId);
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(mWnn, fullId);
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_HALF_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            keyboard12Key[KEYBOARD_SHIFT_ON][KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                    = keyboard12Key[KEYBOARD_SHIFT_OFF][KEYMODE_JA_FULL_NUMBER]
                            [KEYINPUTTYPE_NORMAL][0];
            mLastInputType = -1;
        }

        if (OpenWnn.getCurrentIme().isKeepInput()) {
            return;
        }

        int inputType = editor.inputType;
        if (mHardKeyboardHidden) {
            if (mIsInputTypeNull) {
                mIsInputTypeNull = false;
            }
        } else if (mEnableHardware12Keyboard && mIsInputTypeNull) {
            mIsInputTypeNull = false;
            changeKeyboardType(KEYBOARD_12KEY);
        }

        if (ENABLE_DOCOMO) {
            // disable voice input if current input type is not to be supported by DocomoVoiceEditor
            VoiceEditorClient viClient = new VoiceEditorClient();
            if (!viClient.isVoiceEditorEnabled(inputType)) {
                disableVoiceInput();
            }
        }

        boolean prediction = pref.getBoolean("opt_prediction", true);

        mLimitedKeyMode = null;
        mPreferenceKeyMode = INVALID_KEYMODE;
        boolean lastNoInput = mNoInput;
        mNoInput = true;
        mDisableKeyInput = false;
        mCurrentKeyInputType = KEYINPUTTYPE_NORMAL;
        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            ((MultiTouchKeyboardView) mKeyboardView).setCapsLock(false);
        }
        mCapsLock = false;
        mInPasswordArea = false;
        if (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            mKeyModeBefToggleToVoice = KEYMODE_JA_FULL_HIRAGANA;
        } else {
            mKeyModeBefToggleToVoice = KEYMODE_JA_HALF_ALPHABET;
        }

        boolean autoCaps = false;
        boolean forceShift = true;
        boolean setDefault = false;
        int changeKeyMode = KEYMODE_DEFAULTMODE;
        int imeOptions = editor.imeOptions;

        int defaultKeyMode = IWnnImeJaJp.getDefaultKeyMode(editor);
        if ((defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_NOTHING)
                || (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG)
                || (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_HIRAGANA_ONLY)
                || (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_ALPHABET_ONLY)) {

            switch (inputType & EditorInfo.TYPE_MASK_CLASS) {

            case EditorInfo.TYPE_CLASS_NUMBER:
                switch (inputType & EditorInfo.TYPE_MASK_VARIATION) {
                case EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD:
                    mInPasswordArea = true;
                    break;
                default:
                    break;
                }

                switch (inputType & EditorInfo.TYPE_MASK_FLAGS) {
                case EditorInfo.TYPE_NUMBER_FLAG_DECIMAL:
                    mPreferenceKeyMode = KEYMODE_JA_NUMERIC_DECIMAL;
                break;
                case EditorInfo.TYPE_NUMBER_FLAG_SIGNED:
                    mPreferenceKeyMode = KEYMODE_JA_NUMERIC_SIGNED;
                    break;
                default:
                    mPreferenceKeyMode = KEYMODE_JA_NUMERIC;
                    break;
                }
                mCurrentKeyboardType = KEYBOARD_12KEY;

                break;

            case EditorInfo.TYPE_CLASS_DATETIME:
                mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
                break;

            case EditorInfo.TYPE_CLASS_PHONE:
                forceShift = false;
                if (mHardKeyboardHidden) {
                    mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_PHONE};
                } else if (mEnableHardware12Keyboard) {
                    mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_NUMBER};
                } else {
                    mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET};
                }
                mCurrentKeyboardType = KEYBOARD_12KEY;
                break;

            case EditorInfo.TYPE_CLASS_TEXT:
                switch (inputType & EditorInfo.TYPE_MASK_VARIATION) {

                case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                    mInPasswordArea = true;
                case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET, KEYMODE_JA_HALF_NUMBER};
                    disableVoiceInput();
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                    mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
                    mCurrentKeyInputType = KEYINPUTTYPE_MAIL;
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_URI:
                    mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
                    if ((editor.imeOptions
                            & (EditorInfo.IME_MASK_ACTION
                                    | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) ==
                            EditorInfo.IME_ACTION_NEXT) {
                        mCurrentKeyInputType = KEYINPUTTYPE_MULTI_URL;
                    } else {
                        mCurrentKeyInputType = KEYINPUTTYPE_URL;
                    }
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                    disableVoiceInput();
                    break;

                default:
                    changeKeyMode = KEYMODE_JA_FULL_HIRAGANA;
                    break;
                }

                final int capsFlag = EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS
                                     | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
                                     | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS;
                if ((inputType & capsFlag) != 0) {
                    autoCaps = true;
                }

                break;

            default:
                if (inputType == EditorInfo.TYPE_NULL) {
                    mIsInputTypeNull = true;
                }
                changeKeyMode = KEYMODE_JA_FULL_HIRAGANA;
                break;
            }

            if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_HIRAGANA_ONLY) {
                mPreferenceKeyMode = KEYMODE_JA_FULL_HIRAGANA;
                mLimitedKeyMode = new int[] {KEYMODE_JA_FULL_HIRAGANA};
            } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_ALPHABET_ONLY) {
                mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
                mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_ALPHABET};
            } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG) {
                if ((mCurrentKeyMode != KEYMODE_JA_HALF_PHONE)
                        && (mCurrentKeyMode != KEYMODE_JA_NUMERIC)
                        && (mCurrentKeyMode != KEYMODE_JA_NUMERIC_DECIMAL)
                        && (mCurrentKeyMode != KEYMODE_JA_NUMERIC_SIGNED)) {
                    changeKeyMode = mCurrentKeyMode;
                }
                mLastInputType = -1;
            }

            if ((imeOptions & EditorInfo.IME_FLAG_FORCE_ASCII) != 0) {
                if (mLimitedKeyMode == null && mPreferenceKeyMode == INVALID_KEYMODE) {
                     mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
                }
            }
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_HIRAGANA) {
            mPreferenceKeyMode = KEYMODE_JA_FULL_HIRAGANA;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_ALPHABET) {
            changeKeyMode = KEYMODE_JA_FULL_ALPHABET;
            mCurrentKeyMode = KEYMODE_JA_FULL_ALPHABET;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_NUMBER) {
            changeKeyMode = KEYMODE_JA_FULL_NUMBER;
            mCurrentKeyMode = KEYMODE_JA_FULL_NUMBER;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_KATAKANA) {
            changeKeyMode = KEYMODE_JA_FULL_KATAKANA;
            mCurrentKeyMode = KEYMODE_JA_FULL_KATAKANA;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_ALPHABET) {
            mPreferenceKeyMode = KEYMODE_JA_HALF_ALPHABET;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_NUMBER) {
            mPreferenceKeyMode = KEYMODE_JA_HALF_NUMBER;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_KATAKANA) {
            changeKeyMode = KEYMODE_JA_HALF_KATAKANA;
            mCurrentKeyMode = KEYMODE_JA_HALF_KATAKANA;
            mLastInputType = -1;
        } else if (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_PHONE) {
            mPreferenceKeyMode = KEYMODE_JA_HALF_PHONE;
            mLastInputType = -1;
            if (mHardKeyboardHidden) {
                forceShift = false;
                mLimitedKeyMode = new int[] {KEYMODE_JA_HALF_PHONE};
            }
        } //else

        if ((mCurrentKeyMode == KEYMODE_JA_HALF_PHONE)
                || (mCurrentKeyMode == KEYMODE_JA_NUMERIC)
                || (mCurrentKeyMode == KEYMODE_JA_NUMERIC_DECIMAL)
                || (mCurrentKeyMode == KEYMODE_JA_NUMERIC_SIGNED)) {
            changeKeyMode = KEYMODE_DEFAULTMODE;
        }
        forceCloseVoiceInputKeyboard();

        mEnableAutoCaps = autoCaps && pref.getBoolean("auto_caps", res.getBoolean(R.bool.auto_caps_default_value));

        boolean hasInputTypeChanged = (inputType != mLastInputType);
        boolean hasImeOptionsChanged = (imeOptions != mLastImeOptions);
        if (inputType != EditorInfo.TYPE_NULL || hasImeOptionsChanged) {
            if (changeKeyMode == KEYMODE_DEFAULTMODE) {
                setDefaultKeyboard();
            } else {
                changeKeyMode(mCurrentKeyMode);
            }
            setDefault = true;
            setShiftByEditorInfo(forceShift);

            int type = getKeyboardTypePref(mCurrentKeyMode);
            if (mCurrentKeyboardType != type) {
                changeKeyboardType(type);
            } else if ((!setDefault) && (!lastNoInput)) {
                Keyboard newKeyboard = getKeyboardInputted(false);
                changeKeyboard(newKeyboard);
            }
        }
        if ((hasInputTypeChanged || hasImeOptionsChanged) && defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_NOTHING) {
            mLastInputType = inputType;
            mLastImeOptions = imeOptions;
        }

        setStatusIcon();

    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#closing */
    @Override public void closing() {
        super.closing();
        mOneTouchEmojiListViewManager.close();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#onUpdateState */
    @Override public void onUpdateState(OpenWnn parent) {
        super.onUpdateState(parent);
        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            mCapsLock = ((MultiTouchKeyboardView) mKeyboardView).isCapsLock();
        }
        if (!mCapsLock && !mKeepShiftMode) {
            setShiftByEditorInfo(false);
        }
    }

    /**
     * Change the keyboard to default.
     */
    public void setDefaultKeyboard() {
        Locale locale = Locale.getDefault();
        int keymode = KEYMODE_JA_FULL_HIRAGANA;

        if (mPreferenceKeyMode != INVALID_KEYMODE) {
            keymode = mPreferenceKeyMode;
        } else if (mLimitedKeyMode != null) {
            keymode = mLimitedKeyMode[0];
        } else {
            if (!locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
                keymode = KEYMODE_JA_HALF_ALPHABET;
            }
        }
        changeKeyMode(keymode);
    }


    /**
     * Change the key-mode to the next input mode.
     */
    public void nextKeyMode() {
        /* Search the current mode in the toggle cycle table */
        boolean found = false;
        int index = 0;
        int length = getModeCycleCount();
        int[] table = getModeCycleTable();
        if (mCurrentKeyMode != KEYMODE_JA_VOICE) {
            for (index = 0; index < length; index++) {
                if (table[index] == mCurrentKeyMode) {
                    found = true;
                    break;
                }
            }
        }

        if (mCurrentKeyMode == KEYMODE_JA_VOICE) {
            changeKeyMode(mKeyModeBefToggleToVoice);
        } else if (!found) {
            /* If the current mode not exists, set the default mode */
            setDefaultKeyboard();
        } else {
            /* If the current mode exists, set the next input mode */
            int keyMode = INVALID_KEYMODE;
            for (int i = 0; i < length; i++) {
                index = (++index) % length;

                keyMode = filterKeyMode(table[index]);
                if (keyMode != INVALID_KEYMODE) {
                    break;
                }
            }

            if (keyMode != INVALID_KEYMODE) {
                // Change the input mode
                changeKeyMode(keyMode);
            }
        }
    }

    /**
     * Create the keyboard for portrait mode.
     * <br>
     * @param parent  The context
     */
    private void createKeyboardsPortrait(OpenWnn parent) {
        WnnKeyboardFactory[][][] keyList;

        /* qwerty shift_off (portrait) */
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_alphabet);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_symbols);
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_katakana);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_symbols);
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_katakana);
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone);
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_voice);
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric);
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_decimal);
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_signed);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet_mail);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_multi_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];

        /* qwerty shift_on (portrait) */
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_shift);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_shift_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_alphabet_shift);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_symbols_shift);
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_katakana_shift);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet_shift);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_symbols_shift);
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_katakana_shift);
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone_shift);
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_shift_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_shift_mail);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_shift_multi_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];

        /* 12-keys shift_off (portrait) */
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12keyjp);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12keyjp_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_lower);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_lower_input);
        if (mEnableFlick) {
            keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_num);
        } else {
            keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_num_flick_off);
        }
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_katakana);
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_katakana_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_lower);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_lower_input);
        if (mEnableFlick) {
            keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_num);
        } else {
            keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_num_flick_off);
        }
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_katakana);
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_katakana_input);
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone);
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_voice);
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric);
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_decimal);
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_signed);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_url_lower);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_url_lower_input);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_url_lower);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_url_lower_input);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_mail_lower);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_mail_lower_input);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_mail_lower);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_mail_lower_input);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0];

        /* 12-keys shift_on (portrait) */
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_JA_FULL_HIRAGANA]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_mail);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_mail_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_url);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_url_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_FULL_NUMBER]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER];
        keyList[KEYMODE_JA_FULL_KATAKANA]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_mail);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_mail_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_url);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_url_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_NUMBER]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER];
        keyList[KEYMODE_JA_HALF_KATAKANA]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone_shift);
        keyList[KEYMODE_JA_VOICE]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE];
        keyList[KEYMODE_JA_NUMERIC]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL];
        keyList[KEYMODE_JA_NUMERIC_SIGNED]
                = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED];

        /* navigation keypad shift-off */
        WnnKeyboardFactory keyboardFactory
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_keypad);
        keyList = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_NAVIGATION][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]= keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;

        /* navigation keypad shift-on */
        mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_NAVIGATION][KEYBOARD_SHIFT_ON]
            = mKeyboard[LANG_JA][PORTRAIT][KEYBOARD_NAVIGATION][KEYBOARD_SHIFT_OFF];

    }

    /**
     * Create the keyboard for landscape mode.
     * <br>
     * @param parent  The context
     */
    private void createKeyboardsLandscape(OpenWnn parent) {
        WnnKeyboardFactory[][][] keyList;
        /* qwerty shift_off (landscape) */
        keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_alphabet);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_symbols);
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_katakana);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_symbols);
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_katakana);
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone);
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_voice);
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric);
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_decimal);
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_signed);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet_mail);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_multi_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];

        /* qwerty shift_on (landscape) */
        keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_shift);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_shift_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_alphabet_shift);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_symbols_shift);
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_full_katakana_shift);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_alphabet_shift);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_symbols_shift);
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_jp_half_katakana_shift);
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone_shift);
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_shift_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_shift_mail);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_qwerty_jp_half_alphabet_shift_multi_url);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];

        /* 12-keys shift_off (landscape) */
        keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12keyjp);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12keyjp_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_lower);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_lower_input);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_num);
        if (mEnableFlick) {
            keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_num);
        } else {
            keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_num_flick_off);
        }
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_katakana);
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_katakana_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_lower);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_lower_input);
        if (mEnableFlick) {
            keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_num);
        } else {
            keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0]
                    = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_num_flick_off);
        }
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_katakana);
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_katakana_input);
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone);
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_voice);
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric);
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_decimal);
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_numeric_signed);
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_url_lower);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_url_lower_input);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_url_lower);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_url_lower_input);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_mail_lower);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_mail_lower_input);
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_mail_lower);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_half_alphabet_mail_lower_input);
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][1];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MAIL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0];

        /* 12-keys shift_on (landscape) */
        keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_JA_FULL_HIRAGANA]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_HIRAGANA];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_mail);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                        R.xml.keyboard_12key_full_alphabet_mail_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_url);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_full_alphabet_url_input);
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_FULL_NUMBER]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_NUMBER];
        keyList[KEYMODE_JA_FULL_KATAKANA]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_FULL_KATAKANA];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_mail);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][1]
                = new WnnKeyboardFactory(parent,
                                         R.xml.keyboard_12key_half_alphabet_mail_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_url);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_half_alphabet_url_input);
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0];
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][1]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_ON]
                        [KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][1];
        keyList[KEYMODE_JA_HALF_NUMBER]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_NUMBER];
        keyList[KEYMODE_JA_HALF_KATAKANA]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_HALF_KATAKANA];
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0]
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_phone_shift);
        keyList[KEYMODE_JA_VOICE]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_VOICE];
        keyList[KEYMODE_JA_NUMERIC]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC];
        keyList[KEYMODE_JA_NUMERIC_DECIMAL]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_DECIMAL];
        keyList[KEYMODE_JA_NUMERIC_SIGNED]
                = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_12KEY][KEYBOARD_SHIFT_OFF]
                        [KEYMODE_JA_NUMERIC_SIGNED];

        /* navigation keypad shift-off */
        WnnKeyboardFactory keyboardFactory
                = new WnnKeyboardFactory(parent, R.xml.keyboard_12key_keypad);
        keyList = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_NAVIGATION][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_NORMAL][0]= keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_NORMAL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MAIL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_HIRAGANA][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_ALPHABET][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_NUMBER][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_FULL_KATAKANA][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_ALPHABET][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_NUMBER][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_KATAKANA][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_HALF_PHONE][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_VOICE][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_DECIMAL][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;
        keyList[KEYMODE_JA_NUMERIC_SIGNED][KEYINPUTTYPE_MULTI_URL][0] = keyboardFactory;

        /* navigation keypad shift-on */
        mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_NAVIGATION][KEYBOARD_SHIFT_ON]
            = mKeyboard[LANG_JA][LANDSCAPE][KEYBOARD_NAVIGATION][KEYBOARD_SHIFT_OFF];
    }

    /**
     * Convert the key code to the table index.
     * <br>
     * @param keyCode   The key code
     * @return          The index of the key code table for input
     */
    private int getTableIndex(int keyCode) {
        int index =
            (keyCode == KEYCODE_JP12_1)     ?  0 :
            (keyCode == KEYCODE_JP12_2)     ?  1 :
            (keyCode == KEYCODE_JP12_3)     ?  2 :
            (keyCode == KEYCODE_JP12_4)     ?  3 :
            (keyCode == KEYCODE_JP12_5)     ?  4 :
            (keyCode == KEYCODE_JP12_6)     ?  5 :
            (keyCode == KEYCODE_JP12_7)     ?  6 :
            (keyCode == KEYCODE_JP12_8)     ?  7 :
            (keyCode == KEYCODE_JP12_9)     ?  8 :
            (keyCode == KEYCODE_JP12_0)     ?  9 :
            (keyCode == KEYCODE_JP12_SHARP) ? 10 :
            (keyCode == KEYCODE_JP12_ASTER) ? 11 :
            0;

        return index;
    }

    /**
     * Get the toggle cycle table for input that is appropriate in current mode.
     *
     * @return      The toggle cycle table for input
     */
    private String[][] getCycleTable() {
        String[][] cycleTable = null;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            cycleTable = JP_FULL_HIRAGANA_CYCLE_TABLE;
            break;

        case KEYMODE_JA_FULL_KATAKANA:
            cycleTable = JP_FULL_KATAKANA_CYCLE_TABLE;
            break;

        case KEYMODE_JA_FULL_ALPHABET:
            switch(mCurrentKeyInputType) {
            case KEYINPUTTYPE_MAIL:
                if (mShiftOn == 0) {
                    cycleTable = JP_FULL_ALPHABET_EMAIL_CYCLE_TABLE;
                } else {
                    cycleTable = JP_FULL_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE;
                }
                break;

            case KEYINPUTTYPE_URL:
            case KEYINPUTTYPE_MULTI_URL:
                if (mShiftOn == 0) {
                    cycleTable = JP_FULL_ALPHABET_URL_CYCLE_TABLE;
                } else {
                    cycleTable = JP_FULL_ALPHABET_URL_SHIFT_CYCLE_TABLE;
                }
                break;

            default:
                if (mShiftOn == 0) {
            cycleTable = JP_FULL_ALPHABET_CYCLE_TABLE;
                } else {
                    cycleTable = JP_FULL_ALPHABET_SHIFT_CYCLE_TABLE;
                }
            break;

            }
            break;

        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            /* Because these modes belong to direct input group, No toggle cycle table exists */
            break;

        case KEYMODE_JA_HALF_ALPHABET:
            switch(mCurrentKeyInputType) {
            case KEYINPUTTYPE_MAIL:
                if (mShiftOn == 0) {
                    cycleTable = JP_HALF_ALPHABET_EMAIL_CYCLE_TABLE;
                } else {
                    cycleTable = JP_HALF_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE;
                }
                break;

            case KEYINPUTTYPE_URL:
            case KEYINPUTTYPE_MULTI_URL:
                if (mShiftOn == 0) {
                    cycleTable = JP_HALF_ALPHABET_URL_CYCLE_TABLE;
                } else {
                    cycleTable = JP_HALF_ALPHABET_URL_SHIFT_CYCLE_TABLE;
                }
                break;

            default:
                if (mShiftOn == 0) {
            cycleTable = JP_HALF_ALPHABET_CYCLE_TABLE;
                } else {
                    cycleTable = JP_HALF_ALPHABET_SHIFT_CYCLE_TABLE;
                }
            break;

            }
            break;

        case KEYMODE_JA_HALF_KATAKANA:
            cycleTable = JP_HALF_KATAKANA_CYCLE_TABLE;
            break;

        default:
            break;
        }
        return cycleTable;
    }

    /**
     * Get the flick toggle cycle table for input that is appropriate in current mode.
     *
     * @return      The flick toggle cycle table for input.
     */
    private String[][] getFlickCycleTable() {
        String[][] cycleTable = null;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            cycleTable = JP_FULL_HIRAGANA_CYCLE_TABLE_FLICK;
            break;

        case KEYMODE_JA_FULL_KATAKANA:
            cycleTable = JP_FULL_KATAKANA_CYCLE_TABLE_FLICK;
            break;

        case KEYMODE_JA_FULL_ALPHABET:
            switch(mCurrentKeyInputType) {
            case KEYINPUTTYPE_MAIL:
                if (mShiftOn == 0) {
                    cycleTable = JP_FULL_ALPHABET_EMAIL_CYCLE_TABLE_FLICK;
                } else {
                    cycleTable = JP_FULL_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE_FLICK;
                }
                break;

            case KEYINPUTTYPE_URL:
            case KEYINPUTTYPE_MULTI_URL:
                if (mShiftOn == 0) {
                    cycleTable = JP_FULL_ALPHABET_URL_CYCLE_TABLE_FLICK;
                } else {
                    cycleTable = JP_FULL_ALPHABET_URL_SHIFT_CYCLE_TABLE_FLICK;
                }
                break;

            default:
                if (mShiftOn == 0) {
            cycleTable = JP_FULL_ALPHABET_CYCLE_TABLE_FLICK;
                } else {
                    cycleTable = JP_FULL_ALPHABET_SHIFT_CYCLE_TABLE_FLICK;
                }
            break;

            }

            break;

        case KEYMODE_JA_FULL_NUMBER:
            cycleTable = JP_FULL_NUMBER_CYCLE_TABLE_FLICK;
            break;

        case KEYMODE_JA_HALF_NUMBER:
            cycleTable = JP_HALF_NUMBER_CYCLE_TABLE_FLICK;
            break;

        case KEYMODE_JA_HALF_ALPHABET:
            switch(mCurrentKeyInputType) {
            case KEYINPUTTYPE_MAIL:
                if (mShiftOn == 0) {
                    cycleTable = JP_HALF_ALPHABET_EMAIL_CYCLE_TABLE_FLICK;
                } else {
                    cycleTable = JP_HALF_ALPHABET_EMAIL_SHIFT_CYCLE_TABLE_FLICK;
                }
                break;

            case KEYINPUTTYPE_URL:
            case KEYINPUTTYPE_MULTI_URL:
                if (mShiftOn == 0) {
                    cycleTable = JP_HALF_ALPHABET_URL_CYCLE_TABLE_FLICK;
                } else {
                    cycleTable = JP_HALF_ALPHABET_URL_SHIFT_CYCLE_TABLE_FLICK;
                }
                break;

            default:
                if (mShiftOn == 0) {
            cycleTable = JP_HALF_ALPHABET_CYCLE_TABLE_FLICK;
                } else {
                    cycleTable = JP_HALF_ALPHABET_SHIFT_CYCLE_TABLE_FLICK;
                }
            break;

            }
            break;

        case KEYMODE_JA_HALF_KATAKANA:
            cycleTable = JP_HALF_KATAKANA_CYCLE_TABLE_FLICK;
            break;

        default:
            break;
        }
        return cycleTable;
    }

    /**
     * Get the replace table that is appropriate in current mode.
     *
     * @return      The replace table
     */
    private HashMap<String, String> getReplaceTable() {
        HashMap<String, String> hashTable = null;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            hashTable = JP_FULL_HIRAGANA_REPLACE_TABLE;
            break;
        case KEYMODE_JA_FULL_KATAKANA:
            hashTable = JP_FULL_KATAKANA_REPLACE_TABLE;
            break;

        case KEYMODE_JA_FULL_ALPHABET:
            hashTable = JP_FULL_ALPHABET_REPLACE_TABLE;
            break;

        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            /* Because these modes belong to direct input group, No replacing table exists */
            break;

        case KEYMODE_JA_HALF_ALPHABET:
            hashTable = JP_HALF_ALPHABET_REPLACE_TABLE;
            break;

        case KEYMODE_JA_HALF_KATAKANA:
            hashTable = JP_HALF_KATAKANA_REPLACE_TABLE;
            break;

        default:
            break;
        }
        return hashTable;
    }

    /**
     * Get the replace HashMap table that is appropriate in current mode.
     *
     * @return      The replace HashMap table
     */
    private HashMap<String, String> getReplaceTable(int direction) {
        HashMap<String, String> hashTable = null;
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            // get corresponding replace HashMap table according to flick direction for HIRAGANA KeyMode
            switch (direction) {
            case FLICK_DIRECTION_NEUTRAL_INDEX:
            case FLICK_DIRECTION_DOWN_INDEX:
                hashTable = JP_EMPTY_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_LEFT_INDEX:
                hashTable = JP_FULL_HIRAGANA_DAKUTEN_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_UP_INDEX:
                hashTable = JP_FULL_HIRAGANA_CAPITAL_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_RIGHT_INDEX:
                hashTable = JP_FULL_HIRAGANA_HANDAKUTEN_REPLACE_TABLE;
                break;

            default:
                hashTable = null;
                break;
            }
            break;

        case KEYMODE_JA_FULL_KATAKANA:
            // get corresponding replace HashMap table according to flick direction for FULL_KATAKANA KeyMode
            switch (direction) {
            case FLICK_DIRECTION_NEUTRAL_INDEX:
            case FLICK_DIRECTION_DOWN_INDEX:
                hashTable = JP_EMPTY_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_LEFT_INDEX:
                hashTable = JP_FULL_KATAKANA_DAKUTEN_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_UP_INDEX:
                hashTable = JP_FULL_KATAKANA_CAPITAL_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_RIGHT_INDEX:
                hashTable = JP_FULL_KATAKANA_HANDAKUTEN_REPLACE_TABLE;
                break;

            default:
                hashTable = null;
                break;
            }
            break;

        case KEYMODE_JA_FULL_ALPHABET:
            // get corresponding replace HashMap table according to flick direction for HALF_ALPHABET KeyMode
            switch (direction) {
            case FLICK_DIRECTION_NEUTRAL_INDEX:
            case FLICK_DIRECTION_LEFT_INDEX:
            case FLICK_DIRECTION_RIGHT_INDEX:
            case FLICK_DIRECTION_DOWN_INDEX:
                hashTable = JP_EMPTY_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_UP_INDEX:
                hashTable = JP_FULL_ALPHABET_REPLACE_TABLE;
                break;

            default:
                hashTable = null;
                break;
            }
            break;

        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            // Because these modes belong to direct input group, No replacing table exists
            hashTable = null;
            break;

        case KEYMODE_JA_HALF_ALPHABET:
            // get corresponding replace HashMap table according to flick direction for HALF_ALPHABET KeyMode
            switch (direction) {
            case FLICK_DIRECTION_NEUTRAL_INDEX:
            case FLICK_DIRECTION_LEFT_INDEX:
            case FLICK_DIRECTION_RIGHT_INDEX:
            case FLICK_DIRECTION_DOWN_INDEX:
                hashTable = JP_EMPTY_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_UP_INDEX:
                hashTable = JP_HALF_ALPHABET_REPLACE_TABLE;
                break;

            default:
                hashTable = null;
                break;
            }
            break;

        case KEYMODE_JA_HALF_KATAKANA:
            // get corresponding replace HashMap table according to flick direction for HALF_KATAKANA KeyMode
            switch (direction) {
            case FLICK_DIRECTION_NEUTRAL_INDEX:
            case FLICK_DIRECTION_DOWN_INDEX:
                hashTable = JP_EMPTY_REPLACE_TABLE;
                 break;

            case FLICK_DIRECTION_LEFT_INDEX:
                hashTable = JP_HALF_KATAKANA_DAKUTEN_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_UP_INDEX:
                hashTable = JP_HALF_KATAKANA_CAPITAL_REPLACE_TABLE;
                break;

            case FLICK_DIRECTION_RIGHT_INDEX:
                hashTable = JP_HALF_KATAKANA_HANDAKUTEN_REPLACE_TABLE;
                break;

            default:
                hashTable = null;
                break;
            }
            break;

        default:
            hashTable = null;
            break;
        }
        return hashTable;
    }

    /**
     * Set the status icon that is appropriate in the current mode.
     */
    private void setStatusIcon() {
        int icon = 0;

        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            icon = R.drawable.stat_full_hiragana;
            mTextView.setText(R.string.ti_key_12key_switch_full_hiragana_txt);
            break;

        case KEYMODE_JA_FULL_KATAKANA:
            icon = R.drawable.stat_full_katakana;
            mTextView.setText(R.string.ti_key_12key_switch_full_katakana_txt);
            break;

        case KEYMODE_JA_FULL_ALPHABET:
            icon = R.drawable.stat_full_alphabet;
            mTextView.setText(R.string.ti_key_12key_switch_full_alphabet_txt);
            break;

        case KEYMODE_JA_FULL_NUMBER:
            icon = R.drawable.stat_full_numeric;
            mTextView.setText(R.string.ti_key_12key_switch_full_number_txt);
            break;

        case KEYMODE_JA_HALF_KATAKANA:
            icon = R.drawable.stat_half_katakana;
            mTextView.setText(R.string.ti_key_12key_switch_half_katakana_txt);
            break;

        case KEYMODE_JA_HALF_ALPHABET:
            icon = R.drawable.stat_half_alphabet;
            mTextView.setText(R.string.ti_key_12key_switch_half_alphabet_txt);
            break;

        case KEYMODE_JA_HALF_NUMBER:
        case KEYMODE_JA_HALF_PHONE:
        case KEYMODE_JA_NUMERIC:
        case KEYMODE_JA_NUMERIC_DECIMAL:
        case KEYMODE_JA_NUMERIC_SIGNED:
            icon = R.drawable.stat_half_numeric;
            mTextView.setText(R.string.ti_key_12key_switch_half_number_txt);
            break;

        default:
            break;

        }

        if (mWnn.isInputViewShown()) {
            mWnn.showStatusIcon(icon);
        }
    }

    /**
     * Get the shift key state from the editor.
     * <br>
     * @param editor    The editor information
     * @return          The state id of the shift key (0:off, 1:on)
     */
    protected int getShiftKeyState(EditorInfo editor) {
        InputConnection connection = mWnn.getCurrentInputConnection();
        if (connection != null) {
            int caps = connection.getCursorCapsMode(editor.inputType);
            return (caps == 0) ? 0 : 1;
        } else {
            return 0;
        }
    }

    /**
     * Set the shift key state from {@link android.view.inputmethod#EditorInfo}.
     *
     * @param force {@code true} if force to set the keyboard.
     */
    @Override protected void setShiftByEditorInfo(boolean force) {
        if (mWnn != null) {
            if (mWnn.isKeepInput()) {
                return;
            }

            int shift;
            if (mEnableAutoCaps && (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)) {
                shift = getShiftKeyState(mWnn.getCurrentInputEditorInfo());
            } else {
                shift = KEYBOARD_SHIFT_OFF;
            }

            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                boolean isShift = (shift == KEYBOARD_SHIFT_ON) ? true : false;
                if (force || (isShift != ((MultiTouchKeyboardView) mKeyboardView).isShifted())) {
                    setShiftedByMultiTouchKeyboard(isShift);
                }
            } else {
                if (force || (shift != mShiftOn)) {
                    setShifted(shift);
                    setUndoKey(mCanUndo);
                }
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setHardKeyboardHidden */
    @Override public void setHardKeyboardHidden(boolean hidden) {
        if (mWnn != null) {
            if (!hidden) {
                if (mEnableHardware12Keyboard) {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                  IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_12KEY));
                } else {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                  IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_QWERTY));
                }
            }

            if (mHardKeyboardHidden != hidden) {
                if ((mLimitedKeyMode != null)
                    || (!mEnableHardware12Keyboard
                        && (mCurrentKeyMode != KEYMODE_JA_FULL_HIRAGANA)
                        && (mCurrentKeyMode != KEYMODE_JA_HALF_ALPHABET))) {

                    mLastInputType = EditorInfo.TYPE_NULL;
                    mLastImeOptions = IME_OPTIONS_INIT;
                    if (mWnn.isInputViewShown()) {
                        setDefaultKeyboard();
                    }
                }

                if (hidden) {
                    mLastInputType = -1;
                    mLastImeOptions = IME_OPTIONS_INIT;
                }
            }
        }
        super.setHardKeyboardHidden(hidden);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setHardware12Keyboard */
    @Override public void setHardware12Keyboard(boolean type12Key) {
        if (mWnn != null) {
            if (mEnableHardware12Keyboard != type12Key) {
                if (type12Key) {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                  IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_12KEY));
                } else {
                    mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                  IWnnImeJaJp.ENGINE_MODE_OPT_TYPE_QWERTY));
                }
            }
        }
        super.setHardware12Keyboard(type12Key);
    }

    /**
     * Sets the undo-key.
     *
     * @param undo {@code true}: the undo-key will be enabled; {@code false}: disable
     */
    public void setUndoKey(boolean undo) {
        boolean hasChanged = false;
        undo = false;
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

        if (mCurrentKeyboardType == KEYBOARD_12KEY) {
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
                    mKeyboardView.setKeyboard(mCurrentKeyboard, mCurrentKeyboardType);
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
     * Change the key-mode to the allowed one which is restricted
     *  by the text input field or the type of the keyboard.
     *
     * @param keyMode The key-mode
     * @return the key-mode allowed
     */
    private int filterKeyMode(int keyMode) {
        int targetMode = keyMode;
        int[] limits = mLimitedKeyMode;

        if (!mHardKeyboardHidden) { /* for hardware keyboard */
            if (!mEnableHardware12Keyboard && (targetMode != KEYMODE_JA_FULL_HIRAGANA)
                && (targetMode != KEYMODE_JA_HALF_ALPHABET)) {

                Locale locale = Locale.getDefault();
                int keymode = KEYMODE_JA_HALF_ALPHABET;
                if (locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
                    switch (targetMode) {
                    case KEYMODE_JA_FULL_HIRAGANA:
                    case KEYMODE_JA_FULL_KATAKANA:
                    case KEYMODE_JA_HALF_KATAKANA:
                        keymode = KEYMODE_JA_FULL_HIRAGANA;
                        break;
                    default:
                        /* half-alphabet */
                        break;
                    }
                }
                targetMode = keymode;
            }
        }

        /* restrict by the type of the text field */
        if (limits != null) {
            boolean hasAccepted = false;
            boolean hasRequiredChange = true;
            int size = limits.length;
            int nowMode = mCurrentKeyMode;

            for (int i = 0; i < size; i++) {
                if (targetMode == limits[i]) {
                    hasAccepted = true;
                    break;
                }
                if (nowMode == limits[i]) {
                    hasRequiredChange = false;
                }
            }

            if (!hasAccepted) {
                if (hasRequiredChange) {
                    targetMode = mLimitedKeyMode[0];
                } else {
                    targetMode = INVALID_KEYMODE;
                }
            }
        }

        return targetMode;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#inputByFlickDirection */
    @Override protected void inputByFlickDirection(int direction, boolean isCommit) {
        if (isEnableFlickMode(mFlickPressKey)) {
            switch (direction) {
            case FLICK_DIRECTION_NEUTRAL:
                inputByFlick(FLICK_DIRECTION_NEUTRAL_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_LEFT:
                inputByFlick(FLICK_DIRECTION_LEFT_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_UP:
                inputByFlick(FLICK_DIRECTION_UP_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_RIGHT:
                inputByFlick(FLICK_DIRECTION_RIGHT_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_DOWN:
                inputByFlick(FLICK_DIRECTION_DOWN_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE1:
                inputByFlick(FLICK_DIRECTION_SLIDE1_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE2:
                inputByFlick(FLICK_DIRECTION_SLIDE2_INDEX, isCommit);
                break;

            case FLICK_DIRECTION_SLIDE3:
                inputByFlick(FLICK_DIRECTION_SLIDE3_INDEX, isCommit);
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
        FlickKeyboardView keyboardView = (FlickKeyboardView) mKeyboardView;
        if (IWnnImeJaJp.isLatticeInputMode() && isCommit) {
            commitText();
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, OpenWnnEvent.Mode.DEFAULT));
            mPrevInputKeyCode = 0;
            mNoInput = true;
            IWnnImeJaJp.setLatticeInputMode(false);
        }
        if (mFlickPressKey == KEYCODE_JP12_ASTER && hasDakutenCapitalConversion(mCurrentKeyMode)) {
            // Flick on JP12_ASTER KEY and current input mode has dakuten-handakuten-capital convert function
            // will invoke char type switch function.
            if (!mNoInput) {
                // Processing to flick Dakuten, Handakuten, and capital
                HashMap<String, String> replaceTable = getReplaceTable(directionIndex);
                if (replaceTable == null) {
                    Log.i("OpenWnn", "not founds replace table");
                } else {
                    if (isCommit) {
                        if (directionIndex == FLICK_DIRECTION_NEUTRAL_INDEX) {
                            mIsKeyProcessFinish = false;
                        } else if (!replaceTable.isEmpty()) {
                            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.REPLACE_CHAR, replaceTable));
                        }
                    } else {
                        if (mEnablePopup) {
                            if (directionIndex == FLICK_DIRECTION_NEUTRAL_INDEX) {
                                keyboardView.setFlickedKeyGuide(true);
                            } else {
                                // Get current edit char
                                int cursor = mWnn.getComposingText().getCursor(ComposingText.LAYER1);
                                StrSegment prevChar = mWnn.getComposingText().getStrSegment(ComposingText.LAYER1, cursor - 1);
                                // Add Null Check for Kclockwork coding review result.
                                if (prevChar != null) {
                                    String search = prevChar.string;
                                    String replaceChar = (String)replaceTable.get(search);
                                    // Popup the preview of the current char
                                    if (replaceChar == null) {
                                        keyboardView.clearFlickedKeyTop();
                                    } else {
                                        keyboardView.setFlickedKeyTop(replaceChar, true);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (mEnablePopup) {
                    if (directionIndex == FLICK_DIRECTION_NEUTRAL_INDEX) {
                        keyboardView.setFlickedKeyGuide(true);
                    } else {
                        keyboardView.clearFlickedKeyTop();
                    }
                }
            }
        } else if (mFlickPressKey == KEYCODE_JP12_TOGGLE_MODE) {
            if (directionIndex == FLICK_DIRECTION_SLIDE_NEUTRAL_INDEX) {
                if (!isCommit) {
                    keyboardView.setFlickedKeyTop(null, true);
                } else {
                    nextKeyMode();
                }
            } else if (directionIndex == FLICK_DIRECTION_SLIDE_NORMAL_INDEX) {
                if (!isCommit) {
                    keyboardView.setFlickedKeyTop(null, true);
                }
            } else {
                int[] table = getSlideCycleTable();
                int mode = table[directionIndex];
                boolean enableModeKey = isEnableModekey(mode);
                if (isCommit) {
                    if (enableModeKey) {
                        changeKeyMode(mode);
                    }
                } else {
                    boolean hiraEnableModeKey = isEnableModekey(KEYMODE_JA_FULL_HIRAGANA);
                    int keyTop = getSlideKeyTop(directionIndex, enableModeKey, hiraEnableModeKey);
                    Drawable icon = null;
                    icon = new KeyDrawable(mWnn.getResources(), keyTop,
                            mWnn.getResources().getDimensionPixelSize(R.dimen.key_mode_change_width), DRAWABLE_SIZE);
                        keyboardView.setFlickedPreview(icon);
                    }
                }
        } else if (mFlickPressKey == KEYCODE_JP12_EMOJI) {
            mIsShowEmojiFlick = false;
            if (directionIndex == FLICK_DIRECTION_SLIDE_NEUTRAL_INDEX) {
                if (!isCommit) {
                    keyboardView.setFlickedKeyTop(null, true);
                    mIsShowEmojiFlick = true;
                }
            } else if (directionIndex == FLICK_DIRECTION_SLIDE_NORMAL_INDEX) {
                if (!isCommit) {
                    keyboardView.setFlickedKeyTop(null, true);
                    mIsShowEmojiFlick = true;
                }
            } else {
                if (isCommit) {
                    switch (directionIndex) {
                    case FLICK_DIRECTION_SLIDE3_INDEX:
                        switchNumericMode();
                        break;
                    case FLICK_DIRECTION_SLIDE4_INDEX:
                        switchSymbolMode();
                        break;
                    default:
                        switchNumericMode();
                        break;
                    }
                } else {
                    int keyTop;
                    Drawable icon = null;
                    switch (directionIndex) {
                    case FLICK_DIRECTION_SLIDE3_INDEX:
                        keyTop = KEY_MODE_CHANGE_SYMNUM_NUM_B;
                        break;
                    case FLICK_DIRECTION_SLIDE4_INDEX:
                        keyTop = KEY_MODE_CHANGE_SYMNUM_SYM_B;
                        break;
                    default:
                        keyTop = KEY_MODE_CHANGE_SYMNUM_NUM_B;
                        break;
                    }
                    icon = new KeyDrawable(mWnn.getResources(), keyTop,
                            mWnn.getResources().getDimensionPixelSize(R.dimen.key_emoji_change_width), DRAWABLE_SIZE);
                    keyboardView.setFlickedPreview(icon);
                    mIsShowEmojiFlick = true;
                }
            }
        } else {
            // Flick on other key will invoke normal flick function
            int index = getTableIndex(mFlickPressKey);
            String[][] cycleTable = getFlickCycleTable();
            if (cycleTable != null) {
                mCurrentCycleTable = cycleTable[index];
                if (isCommit && (cycleTable[index][directionIndex] != null)) {
                    String inputString = cycleTable[index][directionIndex];
                    if ((mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET)
                            && (mFlickPressKey == KEYCODE_JP12_SHARP)) {
                        /* Commit text by symbol character (',' '.') when alphabet input mode is selected */
                        commitText();
                    }
                    if (hasDakutenCapitalConversion(mCurrentKeyMode)) {
                        // The current input mode is not number mode
                        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.FLICK_INPUT_CHAR, inputString.charAt(0)));
                    } else if (!mIsInputTypeNull) {
                        // The current input mode is number mode
                        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, inputString.charAt(0)));
                    }
                } else {
                    if (mEnablePopup) {
                        if ((directionIndex == FLICK_DIRECTION_NEUTRAL_INDEX)) {
                            keyboardView.setFlickedKeyGuide(true);
                        } else {
                            String str = cycleTable[index][directionIndex];
                            if (str == null) {
                                keyboardView.clearFlickedKeyTop();
                            } else {
                                keyboardView.setFlickedKeyTop(str, true);
                            }
                        }
                    }
                }
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#isEnableFlickMode */
    @Override protected boolean isEnableFlickMode(int key){
        boolean isFlickMode = false;
        if (key == KEYCODE_JP12_TOGGLE_MODE) {
            isFlickMode = true;
        } else {
        switch(mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
        case KEYMODE_JA_FULL_ALPHABET:
        case KEYMODE_JA_FULL_KATAKANA:
        case KEYMODE_JA_HALF_ALPHABET:
        case KEYMODE_JA_HALF_KATAKANA:
        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            if (mEnableFlick) {
                isFlickMode = true;
            }
            break;

        default:
            break;

        }
        }
        return isFlickMode;
    }

    /**
     * Gets the keyboard type setting and change the keyboard type accordingly.
     *
     * @param mode      Input mode,
     * from {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYMODE_JA_FULL_HIRAGANA} to
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYMODE_JA_HALF_PHONE}
     * @return Keyboard type response to special input mode
     */
    private int getKeyboardTypePref(int mode) {

        // Input mode has changed or Keyboard start showing, then Keyboard Type can save again.
        mIsKeyboardTypeNotSave = false;

        if ((mode == KEYMODE_JA_VOICE) || (mode == KEYMODE_JA_NUMERIC)
            || (mode == KEYMODE_JA_NUMERIC_DECIMAL) || (mode == KEYMODE_JA_NUMERIC_SIGNED)
            || (mode == KEYMODE_JA_HALF_PHONE)) {
            return KEYBOARD_12KEY;
        }

        int keyboardType;
        SharedPreferences keyboardTypePref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        Editor keyboardTypePrefEditor = keyboardTypePref.edit();
        String inputModeKey = getInputModeKey(mode);
        String inputModeValue = keyboardTypePref.getString(inputModeKey, "");

        if ("".equals(inputModeValue)) {
            if ((mode == KEYMODE_JA_FULL_ALPHABET)
                    || (mode == KEYMODE_JA_HALF_ALPHABET)
                    || (mode == KEYMODE_JA_FULL_NUMBER)
                    || (mode == KEYMODE_JA_HALF_NUMBER)) {
                inputModeValue = Integer.toString(KEYBOARD_QWERTY);
            } else {
                inputModeValue = Integer.toString(KEYBOARD_12KEY);
            }
            keyboardTypePrefEditor.putString(inputModeKey, inputModeValue);
            keyboardTypePrefEditor.commit();
        }

        switch (Integer.valueOf(inputModeValue)) {
        // Change keyboard type according to the keyboard type settings.
        case KEYBOARD_QWERTY:
            // Set QWERTY keyboard as keyboard type.
            keyboardType = KEYBOARD_QWERTY;
            break;

        case KEYBOARD_12KEY:
            // Set 5*4 keyboard as keyboard type.
        default:
            keyboardType = KEYBOARD_12KEY;
            break;
        }
        return keyboardType;
    }

    /**
     * Sets the keyboard type to preferences settings.
     *
     * @param type      Keyboard type,
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_QWERTY} or
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_12KEY}.
     */
    private void setKeyboardTypePref(int type) {
        if ((mCurrentKeyMode == KEYMODE_JA_VOICE)
                || (mCurrentKeyMode == KEYMODE_JA_NUMERIC)
                || (mCurrentKeyMode == KEYMODE_JA_NUMERIC_DECIMAL)
                || (mCurrentKeyMode == KEYMODE_JA_NUMERIC_SIGNED)
                || (mCurrentKeyboardType == KEYBOARD_NAVIGATION)) {
            return;
        }

        if (mWnn == null) {
            return;
        }

        if (mWnn.isKeepInput() || mIsKeyboardTypeNotSave) {
            mIsKeyboardTypeNotSave = true;
            return;
        }

        // Save the last keyboard type to SharedPreferences.
        SharedPreferences keyboardTypePre = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        Editor keyboardTypePrefEditor = keyboardTypePre.edit();
        String inputModeKey = getInputModeKey(mCurrentKeyMode);

        keyboardTypePrefEditor.putString(inputModeKey, String.valueOf(type));
        keyboardTypePrefEditor.commit();
    }

    /**
     * Determines whether the key mode has dakuten-handakuten-capital convert function.
     *
     * @param keyMode    input mode,
     * @return {@code true}  if the key mode has dakuten-handakuten-capital convert function;
     *         {@code false} if the key mode hasn't dakuten-handakuten-capital convert function.
     */
    private boolean hasDakutenCapitalConversion(int keyMode) {
        if ((keyMode ==  KEYMODE_JA_FULL_HIRAGANA
                || keyMode == KEYMODE_JA_HALF_KATAKANA || keyMode == KEYMODE_JA_FULL_KATAKANA
                || keyMode == KEYMODE_JA_HALF_ALPHABET || keyMode == KEYMODE_JA_FULL_ALPHABET)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shows input mode choosing dialog.
     *
     * @return boolean
     */
    public boolean showInputModeSwitchDialog() {
        // Create a build for dialog.
        BaseInputView baseInputView = (BaseInputView)getCurrentView();
        AlertDialog.Builder builder = new AlertDialog.Builder(baseInputView.getContext(), R.style.Dialog);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.ti_dialog_button_cancel_txt, null);

        // Set the dialog menu items.
        final int[] itemValues;
        if (mLimitedKeyMode != null) {
            itemValues = mLimitedKeyMode;
        } else {
                itemValues = new int[] { KEYMODE_JA_FULL_HIRAGANA,
                        KEYMODE_JA_FULL_KATAKANA, KEYMODE_JA_HALF_KATAKANA,
                        KEYMODE_JA_FULL_ALPHABET, KEYMODE_JA_HALF_ALPHABET,
                        KEYMODE_JA_FULL_NUMBER, KEYMODE_JA_HALF_NUMBER };
        }

        // Set titles.
        Resources r = baseInputView.getResources();
        SparseArray<CharSequence> itemTitleList = new SparseArray<CharSequence>();
        itemTitleList.put(KEYMODE_JA_FULL_HIRAGANA, r.getString(R.string.ti_input_mode_full_hirakana_title_txt));
        itemTitleList.put(KEYMODE_JA_FULL_KATAKANA, r.getString(R.string.ti_input_mode_full_katakana_title_txt));
        itemTitleList.put(KEYMODE_JA_HALF_KATAKANA, r.getString(R.string.ti_input_mode_half_katakana_title_txt));
        itemTitleList.put(KEYMODE_JA_FULL_ALPHABET, r.getString(R.string.ti_input_mode_full_alphabet_title_txt));
        itemTitleList.put(KEYMODE_JA_HALF_ALPHABET, r.getString(R.string.ti_input_mode_half_alphabet_title_txt));
        itemTitleList.put(KEYMODE_JA_FULL_NUMBER, r.getString(R.string.ti_input_mode_full_number_title_txt));
        itemTitleList.put(KEYMODE_JA_HALF_NUMBER, r.getString(R.string.ti_input_mode_half_number_title_txt));

        final CharSequence[] itemTitles = new CharSequence[itemValues.length];
        for (int i = 0; i < itemValues.length; i++) {
            itemTitles[i] = itemTitleList.get(itemValues[i]);
        }

        // Dialog items' process.
        builder.setSingleChoiceItems(itemTitles, findIndexOfValue(itemValues,
                mCurrentKeyMode), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface inputModeSwitchDialog, int position) {
                if ((position >= 0) && (position < itemValues.length)) {
                    setSymbolBackKeyMode(itemValues[position]);
                    changeKeyMode(itemValues[position]);
                }
                inputModeSwitchDialog.dismiss();
            }
        });

        builder.setTitle(r.getString(R.string.ti_long_press_dialog_input_mode_txt));
        baseInputView.showDialog(builder);
        return true;
    }
    /**
     * Finds the index of a value in a int[].
     *
     * @param value   the int[] to search in,
     * @param mode    the value need to find index,
     * @return the index of the value.
     */
    private int findIndexOfValue(int[] value, int mode) {
        for (int i = 0; i < value.length; i++) {
            if (value[i] == mode) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get SoftLock Enabled.
     *
     * @return {@code true} if Soft lock enabled.
     */
    protected boolean getSoftLockEnabled() {
        return (mCurrentKeyMode == KEYMODE_JA_FULL_HIRAGANA
                || mCurrentKeyMode == KEYMODE_JA_FULL_ALPHABET
                || mCurrentKeyMode == KEYMODE_JA_FULL_KATAKANA
                || mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET
                || mCurrentKeyMode == KEYMODE_JA_HALF_KATAKANA);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#getLongpressMushroomKey */
    @Override protected boolean getLongpressMushroomKey(Keyboard.Key key) {
        boolean result = false;
        if (key != null) {
            switch (key.codes[0]) {
            case KEYCODE_JP12_EMOJI:
            case KEYCODE_QWERTY_EMOJI:
            case KEYCODE_EISU_KANA:
                result = true;
                break;

            default:
                break;

            }
        }
        return result;
    }

    /**
     * Get Slide Modekey Popup drawable id.
     *
     * @return Modekey Popup drawable id.
     */
    protected int getSlideKeyTop(int index, boolean enableModeKey, boolean hiraEnalbeMode) {
        int enableModeKeyIndex = enableModeKey ? 0 : 1;
        int hiraEnableModeKeyIndex = hiraEnalbeMode ? 0 : 1;

        return SLIDE_DRAWABLE_ID_TABLE[enableModeKeyIndex][hiraEnableModeKeyIndex][index];
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#getModeCycleCount */
    @Override protected int getModeCycleCount() {
        return JP_MODE_CYCLE_TABLE.length;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#getSlideCycleCount */
    @Override protected int getSlideCycleCount() {
        return JP_MODE_CYCLE_TABLE.length;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#isFlickKey */
    @Override protected boolean isFlickKey(int key) {
        boolean ret;

        switch(key) {
        case KEYCODE_JP12_1:
        case KEYCODE_JP12_2:
        case KEYCODE_JP12_3:
        case KEYCODE_JP12_4:
        case KEYCODE_JP12_5:
        case KEYCODE_JP12_6:
        case KEYCODE_JP12_7:
        case KEYCODE_JP12_9:
        case KEYCODE_JP12_0:
        case KEYCODE_JP12_8:
        case KEYCODE_JP12_ASTER:
        case KEYCODE_JP12_EMOJI:
            ret = true;
            break;
        case KEYCODE_JP12_SHARP:
            if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                ret = false;
            } else {
                ret = true;
            }
            break;

        default:
            ret = false;
            break;

        }
        return ret;
    }

    /**
     * Get Modekey enable .
     *
     * @return Modekey enable.
     */
    public boolean isEnableModekey(int mode) {
        boolean isModekey = true;

        if (mLimitedKeyMode != null) {
            int length = mLimitedKeyMode.length;
            isModekey = false;
            for (int i = 0; i < length; i++) {
                if (mLimitedKeyMode[i] == mode) {
                    isModekey = true;
                    break;
                }
            }
        }
        return isModekey;
    }

    /**
     * Set emojiuni6 enabled.
     *
     */
    public void setEnableEmojiUNI6(boolean enableEmojiUNI6) {
        mEnableEmojiUNI6 = enableEmojiUNI6;
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
        int widthPixels = dm.widthPixels;
        boolean isFloatingLandscape = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isFloatingLandscape();
        if (isFloatingLandscape) {
            widthPixels = dm.heightPixels;
        }

        boolean isSplitCurrent = isSplitMode();
        boolean isOneHandedCurrent =  isOneHandedMode();
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isNotExpandedFloatingMode();

        if (mKeyboardView != null) {
            int leftPadding = 0;
            if (isSplitCurrent) {
                if (mCurrentKeyboardType == KEYBOARD_12KEY || isNaviKeyboard()) {
                    leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key_split;
                } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                    leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_qwerty_split;
                } // else {}
            } else if (mCurrentKeyboardType == KEYBOARD_12KEY || isNaviKeyboard()) {
                leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_12key_default;
            } else if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
                leftPadding = R.dimen.one_touch_emoji_list_padding_keyboard_left_qwerty_default;
            } // else {}
            mKeyboardView.setPopupOffset(mWnn.getResources().getDimensionPixelSize(leftPadding), 0);
            mKeyboardView.setPadding(mWnn.getResources().getDimensionPixelSize(leftPadding),
                    mKeyboardView.getPaddingTop(), 0, mKeyboardView.getPaddingBottom());
        }

        if (!(isOneHandedCurrent || isFloatingShown)) {
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
        } else if ((isOneHandedCurrent || isFloatingShown)) {
            mOneTouchEmojiListWidth = mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width_one_handed) * columnNum;
        } else {
            mOneTouchEmojiListWidth = mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width) * columnNum;
        }

        boolean isShowOneTouchEmojiList =
                (isSplitCurrent || (!isSplitCurrent && isNormalInputMode() && !isNaviKeyboard()))
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
                    } else if (mCurrentKeyboardType == KEYBOARD_12KEY || isNaviKeyboard()) {
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
                        int emojiViewX = (widthPixels - mOneTouchEmojiListWidth) / 2
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
                                = (widthPixels - mOneTouchEmojiListWidth) / (float)widthPixels;
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

                    if (mCurrentKeyboardType == KEYBOARD_12KEY || isNaviKeyboard()) {
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
                    int emojiViewX = (widthPixels - mOneTouchEmojiListWidth) / 2
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
                    = (widthPixels - mOneTouchEmojiListWidth) / (float)widthPixels;
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

        if (((isOneHandedCurrent && !mIsSymbolKeyboard) || isFloatingShown)) {
            mOneHandedRightView.setLayoutParams(
                    new LinearLayout.LayoutParams(mOneHandedViewWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            mOneHandedLeftView.setLayoutParams(
                    new LinearLayout.LayoutParams(mOneHandedViewWidth,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            keyboardWidthScale
                -= mOneHandedViewWidth / (float)widthPixels;
        }

        if ((mKeyboardView != null) && (mMainView != null) && (mCurrentKeyboard != null)) {
            if (((Float.compare(WnnKeyboardFactory.getWidthScale(), keyboardWidthScale) != 0)
                    && !mIsSymbolKeyboard)
                    || ((isSplitPrev != isSplitCurrent) && !mIsSymbolKeyboard)) {
                WnnKeyboardFactory.setWidthScale(keyboardWidthScale);
                WnnKeyboardFactory.setKeyboardMode(isShowOneTouchEmojiList);
                mKeyboard = new WnnKeyboardFactory[3][2][4][2][12][4][2];
                if (mDisplayMode == DefaultSoftKeyboard.PORTRAIT) {
                    createKeyboardsPortrait(mWnn);
                } else {
                    createKeyboardsLandscape(mWnn);
                }
                mCurrentKeyboard = null;
                Keyboard keyboard = getModeChangeKeyboard(mCurrentKeyMode);
                changeKeyboard(keyboard);
                setShifted(mShiftOn);
                setUndoKey(mCanUndo);
                mKeyboardView.clearWindowInfo();
            }
            mKeyboardView.invalidateAllKeys();
        }

        mOneTouchEmojiListViewManager.set1stBaseWidth(mOneTouchEmojiListWidth);
        mOneTouchEmojiListViewManager.set2ndBaseWidth(widthPixels - mOneHandedViewWidth);
        mOneTouchEmojiListViewManager.updateView(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol,
                mOneTouchEmojiListMode);
    }

    /**
     * Pressed key display the long press menu.
     *
     * @return {@code true}  if the key display the long press menu;
     *         {@code false} if the key don't display the long press menu;
    */
    private boolean isEnableLongPressMenu(Keyboard.Key key) {
        boolean isLongPressMenu = false;

        switch (key.codes[0]) {
        case KEYCODE_JP12_TOGGLE_MODE:
        case KEYCODE_SWITCH_FULL_HIRAGANA_MENU:
        case KEYCODE_SWITCH_FULL_NUMBER_MENU:
        case KEYCODE_SWITCH_HALF_ALPHABET_MENU:
        case KEYCODE_SWITCH_HALF_NUMBER_MENU:
            isLongPressMenu = true;
            break;

        default:
            break;

        }
        return isLongPressMenu;
    }

    /**
     * Get Modekey cycle table .
     *
     * @return Modekey cycle table.
     */
    private int[] getModeCycleTable() {
        return JP_MODE_CYCLE_TABLE;
    }

    /**
     * Get Slide cycle table .
     *
     * @return Slide cycle table.
     */
    private int[] getSlideCycleTable() {
        return JP_MODE_CYCLE_TABLE;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#isVoiceInputMode */
    @Override protected boolean isVoiceInputMode() {
        return (mCurrentKeyMode == KEYMODE_JA_VOICE);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#setNormalKeyboard */
    @Override public void setNormalKeyboard() {
        boolean isInitFlg = false;
        if (mCurrentKeyboard != null) {
            if (mIsSymbolKeyboard) {
                isInitFlg = true;
                int type = getKeyboardTypePref(mCurrentKeyMode);
                if (mCurrentKeyboardType != type) {
                    changeKeyboardType(type);
                }
            }
        }
        super.setNormalKeyboard();

        if (isInitFlg) {
            setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbol);
            if (isToggleSymbolToNumeric()) {
                changeKeyMode(KEYMODE_JA_HALF_NUMBER);
                setToggleSymbolToNumeric(false);
            } else {
                changeKeyMode(mSymbolBackKeyMode);
            }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(baseInputView.getContext(), R.style.Dialog);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.ti_dialog_button_cancel_txt, null);

        // Set the items in the menu.
        Resources r = baseInputView.getResources();
        CharSequence itemSettingMenu = r.getString(R.string.ti_long_press_dialog_setting_menu_txt);
        CharSequence itemInputMethod = r.getString(R.string.ti_long_press_dialog_input_method_txt);
        CharSequence itemInputMode = r.getString(R.string.ti_long_press_dialog_input_mode_txt);
        CharSequence itemInputTheme = r.getString(R.string.ti_long_press_dialog_keyboard_theme_txt);
        CharSequence itemMushroom = r.getString(R.string.ti_dialog_option_menu_item_mushroom_txt);
        CharSequence[] dialogItem;

        if (mEnableMushroom) {
            mMenuItem = new int[] {POS_INPUTMODE, POS_SETTINGS, POS_MUSHROOM, POS_METHOD};
            dialogItem = new CharSequence[] {itemInputMode, itemSettingMenu, itemMushroom, itemInputMethod};
        } else {
            mMenuItem = new int[] {POS_INPUTMODE, POS_SETTINGS, POS_METHOD};
            dialogItem = new CharSequence[] {itemInputMode, itemSettingMenu, itemInputMethod};
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

                case POS_INPUTMODE:
                    // Input mode choose
                    showInputModeSwitchDialog();
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
     * Whether input mode of the half width character.
     *
     * @return {@code true}   if input mode of the half width character.
     *           {@code false} if input mode of the full width character.
     */
    private boolean isInputModeHalfWidthCharacter() {
        return (mCurrentKeyMode == KEYMODE_JA_HALF_ALPHABET
                || mCurrentKeyMode == KEYMODE_JA_HALF_KATAKANA
                || mCurrentKeyMode == KEYMODE_JA_HALF_NUMBER
                || mCurrentKeyMode == KEYMODE_JA_HALF_PHONE);
    }

    /**
     * Set the symbol keyboard.
     */
    @Override public void setSymbolKeyboard() {
        super.setSymbolKeyboard();
        mOneTouchEmojiListViewManager.hideView();
        hideOneHandedView();
    }

    /** from DefaultSoftKeyboard class */
    @Override public void onText(CharSequence text) {
        char chars[] = text.toString().toCharArray();
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.COMMIT_INPUT_TEXT, chars));
    }


    /**
     * Get keyboard layout settings from display orientation and preferences .
     *
     * @param mode KEYMODE
     */
    private String getInputModeKey(int mode) {
        String key = "input_mode_type_comm";
        switch (mode) {
        case KEYMODE_JA_FULL_HIRAGANA:
        case KEYMODE_JA_FULL_KATAKANA:
        case KEYMODE_JA_HALF_KATAKANA:
            key = key + "_ja";
            break;

        case KEYMODE_JA_FULL_ALPHABET:
        case KEYMODE_JA_HALF_ALPHABET:
            key = key + "_en";
            break;

        case KEYMODE_JA_FULL_NUMBER:
        case KEYMODE_JA_HALF_NUMBER:
            key = key + "_num";
            break;

        default:
            key = key + "_ja";
            break;

        }

        switch(mDisplayMode) {
        case DefaultSoftKeyboard.PORTRAIT:
            key = key + "_portrait";
            break;

        case DefaultSoftKeyboard.LANDSCAPE:
            key = key + "_landscape";
            break;

        default:
            key = key + "_portrait";
            break;

        }

        return key;

    }

    /**
     * Display keyboard type select dialog.
     *
     * @return {@code true}  if the dialog displayed;
     *         {@code false} if otherwise;
     */
    private boolean showKeyboardTypeSwitchDialog() {

        // Create the build for the dialog.
        BaseInputView baseInputView = (BaseInputView)getCurrentView();
        AlertDialog.Builder builder = new AlertDialog.Builder(baseInputView.getContext(), R.style.Dialog);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.ti_dialog_button_cancel_txt, null);

        // Set the items in the menu.
        Resources r = baseInputView.getResources();
        mMenuItem = new int[] { POS_10KEY, POS_QWERTY, POS_HWR };
        CharSequence[] dialogItem = new CharSequence[] {
                        r.getString(R.string.ti_keyboard_type_10key_title_txt),
                        r.getString(R.string.ti_keyboard_type_qwerty_title_txt),
                        r.getString(R.string.ti_keyboard_type_Hand_Writing_title_txt)};

        int itemValue = POS_10KEY;
        switch(mCurrentKeyboardType){
        case KEYBOARD_12KEY:
            itemValue = POS_10KEY;
            break;

        case KEYBOARD_QWERTY:
            itemValue = POS_QWERTY;
            break;

        default:
            break;

        }


        // Set the process for items of menu.
        builder.setSingleChoiceItems(dialogItem, findIndexOfValue(mMenuItem, itemValue),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface switchDialog, int position) {
                switch(mMenuItem[position]){
                case POS_10KEY:
                    if (mCurrentKeyMode == KEYMODE_JA_VOICE) {
                        setDefaultKeyboard();
                    }
                    changeKeyboardType(KEYBOARD_12KEY);
                    break;

                case POS_QWERTY:
                    if (mCurrentKeyMode == KEYMODE_JA_VOICE) {
                        setDefaultKeyboard();
                    }
                    changeKeyboardType(KEYBOARD_QWERTY);
                    break;

                case POS_HWR:
                    OpenWnn.mOriginalInputMethodSwitcher.changeInputMethod(0);
                    break;

                default:
                    break;

                }
                switchDialog.dismiss();
            }
        });

        builder.setTitle(r.getString(R.string.ti_long_press_dialog_keyboard_type_txt));
        baseInputView.showDialog(builder);
        return true;
    }

    /**
     * Set navigation keypad keys state.
     */
    private void setNavigationKeyState() {
        Context context = getCurrentView().getContext();
        Resources r = context.getResources();

        Keyboard keyboard = mKeyboardView.getKeyboard();
        Key select = keyboard.getKeys().get(mSelectKeyIndex);
        Key copy = keyboard.getKeys().get(mCopyKeyIndex);
        Key paste = keyboard.getKeys().get(mPasteKeyIndex);
        Key cut = keyboard.getKeys().get(mCutKeyIndex);
        Key selectSplit = null;
        Key pasteSplit = null;
        if (WnnKeyboardFactory.getSplitMode() && (mDisplayMode == DefaultSoftKeyboard.LANDSCAPE)) {
            selectSplit = keyboard.getKeys().get(mSelectKeyIndex + 1);
            pasteSplit = keyboard.getKeys().get(mPasteKeyIndex + 1);
        }
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        // Update Select.
        Key close = keyboard.getKeys().get(mCloseKeyIndex);
        Key all = keyboard.getKeys().get(mAllKeyIndex);

        close.icon = new KeyDrawable(r, KEY_ID_NAVI_CLOSE, DRAWABLE_SIZE, DRAWABLE_SIZE);
        close.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_CLOSE_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
        close.setSecondKey(true);

        all.icon = new KeyDrawable(r, KEY_ID_NAVI_ALL, DRAWABLE_SIZE, DRAWABLE_SIZE);
        all.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_ALL_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
        all.setSecondKey(true);

        if (mSelectionMode) {
            select.icon = new KeyDrawable(r, KEY_ID_NAVI_DESELECT, DRAWABLE_SIZE, DRAWABLE_SIZE);
            select.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_DESELECT_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            if (selectSplit != null) {
                selectSplit.icon = new KeyDrawable(r, KEY_ID_NAVI_DESELECT,
                        DRAWABLE_SIZE, DRAWABLE_SIZE);
                selectSplit.iconPreview = new KeyDrawable(r,
                        KEY_ID_NAVI_DESELECT_PREVIEW, DRAWABLE_SIZE,
                        DRAWABLE_SIZE);
            }
        } else {
            select.icon = new KeyDrawable(r, KEY_ID_NAVI_SELECT, DRAWABLE_SIZE, DRAWABLE_SIZE);
            select.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_SELECT_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            if (selectSplit != null) {
                selectSplit.icon = new KeyDrawable(r, KEY_ID_NAVI_SELECT,
                        DRAWABLE_SIZE, DRAWABLE_SIZE);
                selectSplit.iconPreview = new KeyDrawable(r,
                        KEY_ID_NAVI_SELECT_PREVIEW, DRAWABLE_SIZE,
                        DRAWABLE_SIZE);
            }
        }

        // Update Copy/Cut.
        if (mSelectionMode && !mInPasswordArea) {
            copy.icon = new KeyDrawable(r, KEY_ID_NAVI_COPY, DRAWABLE_SIZE, DRAWABLE_SIZE);
            copy.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_COPY_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            copy.setSecondKey(false);

            cut.icon = new KeyDrawable(r, KEY_ID_NAVI_CUT, DRAWABLE_SIZE, DRAWABLE_SIZE);
            cut.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_CUT_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            cut.setSecondKey(false);

        } else {
            copy.icon = new KeyDrawable(r, KEY_ID_NAVI_COPY_OFF, DRAWABLE_SIZE, DRAWABLE_SIZE);
            copy.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_COPY_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            copy.setSecondKey(true);

            cut.icon = new KeyDrawable(r, KEY_ID_NAVI_CUT_OFF, DRAWABLE_SIZE, DRAWABLE_SIZE);
            cut.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_CUT_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            cut.setSecondKey(true);
        }

        // Update Paste.
        ClipboardManager m = (ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData data = m.getPrimaryClip();
        String type = ClipDescription.MIMETYPE_TEXT_PLAIN;
        if ((data != null) && (0 < data.getItemCount())
                && (data.getDescription().getMimeType(0).equals(type))) {
            paste.icon = new KeyDrawable(r, KEY_ID_NAVI_PASTE, DRAWABLE_SIZE, DRAWABLE_SIZE);
            paste.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_PASTE_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            paste.setSecondKey(false);
            if (pasteSplit != null) {
                pasteSplit.icon = new KeyDrawable(r, KEY_ID_NAVI_PASTE,
                        DRAWABLE_SIZE, DRAWABLE_SIZE);
                pasteSplit.iconPreview = new KeyDrawable(r,
                        KEY_ID_NAVI_PASTE_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
                pasteSplit.setSecondKey(false);
            }
        } else {
            paste.icon = new KeyDrawable(r, KEY_ID_NAVI_PASTE_OFF, DRAWABLE_SIZE, DRAWABLE_SIZE);
            paste.iconPreview = new KeyDrawable(r, KEY_ID_NAVI_PASTE_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
            paste.setSecondKey(true);
            if (pasteSplit != null) {
                pasteSplit.icon = new KeyDrawable(r, KEY_ID_NAVI_PASTE_OFF,
                        DRAWABLE_SIZE, DRAWABLE_SIZE);
                pasteSplit.iconPreview = new KeyDrawable(r,
                        KEY_ID_NAVI_PASTE_PREVIEW, DRAWABLE_SIZE, DRAWABLE_SIZE);
                pasteSplit.setSecondKey(true);
            }
        }

        mKeyboardView.invalidateAllKeys();
    }

    /**
     * Check navigation key state.
     *
     * @param keyIndex Navigation key index
     * @return True if the specified key is enabled, false otherwise.
     */
    private boolean isNavigationKeyEnable(int keyIndex) {
        return !mKeyboardView.getKeyboard().getKeys().get(keyIndex).isSecondKey;
    }

    /**
     * Get selection mode.
     *
     * @return True if selection mode is active, false otherwise.
     */
    public boolean getSelectionMode() {
        return mSelectionMode;
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
        } else if (mCurrentKeyboardType == KEYBOARD_NAVIGATION) {
            mSelectionMode = false;
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KEYCODE_KEYPAD_CLOSE)));
            mCurrentKeyboardType = mPrevKeyboardType;
            changeKeyboardType(mCurrentKeyboardType);
            if (!mCapsLock) {
                setShiftByEditorInfo(true);
            }
            ret = true;
        } // else
        return ret;
    }

    /**
     * Called when selection is changed.
     *
     * @param select  {@code true} if selected.
     */
    public void onUpdateSelection(boolean select) {
        if (mCurrentKeyboardType == KEYBOARD_NAVIGATION) {
            if (mSelectionMode != select) {
                onKey(DefaultSoftKeyboard.KEYCODE_KEYPAD_SELECT, null);
            }
        }
        mSelectionMode = select;
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
                "DefaultSoftKeyboardJAJP::switchToVoiceEditorIME() NameNotFoundException");
            return;
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getCurrentView().getContext(),
                    R.string.ti_dcm_voiceeditor_Invalid_txt, Toast.LENGTH_LONG)
                    .show();
            Log.e("iWnn",
                "DefaultSoftKeyboardJAJP::switchToVoiceEditorIME() IllegalArgumentException");
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
     * Returns the visibility of One Touch Emoji List.
     *
     * @return {@code true}:True if One Touch Emoji List is visible; {@code false}:if not
     */
    public boolean isShownOneTouchEmojiList() {
        return !ONETOUCHEMOJILIST_HIDDEN.equals(mOneTouchEmojiListMode) && (mEnableEmoji
                || mEnableDecoEmoji || mEnableSymbol);
    }

    /**
     * Switch KeyboardType.
     */
    private void switchKeyboardtype() {

        if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
            changeKeyboardType(KEYBOARD_12KEY);
        } else {
            changeKeyboardType(KEYBOARD_QWERTY);
        }
        return;
    }

    public void wrapStartNavigationKeypad() {
        mPrevKeyboardType = mCurrentKeyboardType;
        changeKeyboardType(KEYBOARD_NAVIGATION);

        mSelectKeyIndex = MultiTouchKeyboardView.getKeyIndex(mKeyboardView.getKeyboard(),
                                                             KEYCODE_KEYPAD_SELECT);
        mCopyKeyIndex = MultiTouchKeyboardView.getKeyIndex(mKeyboardView.getKeyboard(),
                                                           KEYCODE_KEYPAD_COPY);
        mPasteKeyIndex = MultiTouchKeyboardView.getKeyIndex(mKeyboardView.getKeyboard(),
                                                            KEYCODE_KEYPAD_PASTE);
        mCutKeyIndex = MultiTouchKeyboardView.getKeyIndex(mKeyboardView.getKeyboard(),
                                                          KEYCODE_KEYPAD_CUT);
        mCloseKeyIndex = MultiTouchKeyboardView.getKeyIndex(mKeyboardView.getKeyboard(),
                                                           KEYCODE_KEYPAD_CLOSE);
        mAllKeyIndex = MultiTouchKeyboardView.getKeyIndex(mKeyboardView.getKeyboard(),
                                                          KEYCODE_KEYPAD_ALL);
        setNavigationKeyState();
    }

    /**
     * Called when the instance is destroyed.
     */
    @Override public void onDestroy() {
        if (mWnn != null) {
            ClipboardManager m = (ClipboardManager)mWnn.getSystemService(Context.CLIPBOARD_SERVICE);
            m.removePrimaryClipChangedListener(mClipListener);
        }

        mOneTouchEmojiListViewManager.onDestroy();
    }

    /**
     * Saved Input mode when return from the list of symbols.
     *
     * @param Keymode   Input mode when return from the list of symbols.
     */
    private void setSymbolBackKeyMode(int Keymode) {
        if (!(mIsSymbolKeyboard)) {
            if ((KEYMODE_JA_HALF_NUMBER != mCurrentKeyMode) && (KEYMODE_JA_FULL_NUMBER != mCurrentKeyMode)) {
                mSymbolBackKeyMode =  mCurrentKeyMode;
            }
        }
    }

    /**
     * Change to numeric mode.
     */
    private void switchNumericMode() {
        commitText();
        setSymbolBackKeyMode(mCurrentKeyMode);
        changeKeyMode(KEYMODE_JA_HALF_NUMBER);
    }

    /**
     * Change to symbols list mode.
     */
    private void switchSymbolMode() {
        commitText();
        setSymbolBackKeyMode(mCurrentKeyMode);

        if (mEnableEmoji) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_SYMBOL_EMOJI));
        } else if (!mEnableEmojiUNI6) {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_SYMBOL_SYMBOL));
        } else {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_SYMBOL_EMOJI_UNI6));
        }
    }

    /**
     * toggle to numeric mode or symbols list mode.
     */
    private void toggleSymbolNumericMode() {
        if (!(mIsSymbolKeyboard)) {
            if ((KEYMODE_JA_HALF_NUMBER != mCurrentKeyMode) && (KEYMODE_JA_FULL_NUMBER != mCurrentKeyMode)) {
                switchNumericMode();
            } else if ((KEYMODE_JA_HALF_NUMBER == mCurrentKeyMode) || (KEYMODE_JA_FULL_NUMBER == mCurrentKeyMode)){
                switchSymbolMode();
            }
        }
    }

    /**
     * Input language change to next mode.
     */
    public void nextLanguageMode() {
        switch (mCurrentKeyMode) {
        case KEYMODE_JA_FULL_HIRAGANA:
            if (mEnableInputEn || isSpecialInputEditor()) {
                changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
            } else if (mEnableInputKo) {
                if (isEnableKoreanImeByEdit()) {
                    ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ko");
                }
            } else {
                // DO NOTHING
            }
            break;
        case KEYMODE_JA_HALF_ALPHABET:
            if (mEnableInputKo) {
                if (isEnableKoreanImeByEdit()) {
                    ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ko");
                }
            } else if (mEnableInputJa) {
                changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
            } else {
                // DO NOTHING
            }
            break;
        default:
            if (mEnableInputJa) {
                changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
            } else if (mEnableInputEn) {
                changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
            } else {
                // DO NOTHING
            }
            break;
        }
    }

    /**
     * Determine Korean availability by current input editor.
     *
     * @return When the Korean available, true.
     */
    public boolean isEnableKoreanImeByEdit() {
        boolean ret = false;
        OpenWnn wnn = OpenWnn.getCurrentIme();
        if(wnn != null ) {
            EditorInfo edit = wnn.getCurrentInputEditorInfo();
            switch(edit.inputType & EditorInfo.TYPE_MASK_CLASS){
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_PHONE:
                // can not change korean keyboard
                ret =  false;
                break;
            case EditorInfo.TYPE_CLASS_TEXT:
                switch(edit.inputType & EditorInfo.TYPE_MASK_VARIATION){
                case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                    // can not change korean keyboard
                    ret =  false;
                    break;
                default:
                    ret =  true;
                    break;
                }
                break;
            default:
                ret =  true;
                break;
            }
        }
        return ret;
    }

    /**
     * initialize the keyboard input language.
     */
    public void initInputKeyMode() {
        if (mWnnSwitcher.isFromHangulIme()) {
            mWnnSwitcher.setIsFromHangulIme(false);
            if (isEnableKoreanImeByEdit()) {
                if (mEnableInputJa && !mFromKoreanToEnglish) {
                    changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
                } else {
                    changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
                    mFromKoreanToEnglish = false;
                }
            }
        } else if (KEYMODE_JA_FULL_HIRAGANA == mCurrentKeyMode) {
            if (!isSpecialInputEditor() && !mEnableInputJa) {
                changeKeyMode(KEYMODE_JA_HALF_ALPHABET);
            }
        } else if (KEYMODE_JA_HALF_ALPHABET == mCurrentKeyMode) {
            if (!isSpecialInputEditor() && !mEnableInputEn) {
                changeKeyMode(KEYMODE_JA_FULL_HIRAGANA);
            }
        } else {
            // DO NOTHING
        }

        return;
    }

}
