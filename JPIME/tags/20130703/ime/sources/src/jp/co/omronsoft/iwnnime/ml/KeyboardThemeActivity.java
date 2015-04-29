/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * The Keyboard Theme select screen.
 */
public class KeyboardThemeActivity extends Activity {

    /** Associate KeyboardThemeName and packageName */
    private HashMap<String, ActivityInfo> mKeyboardThemeData = new HashMap<String, ActivityInfo>();

    /** The KeyboardThemeInfomation */
    static private ActivityInfo mKeyboardThemeInfo;

    /** The KeyboardThemeImage */
    private ImageView mKeyboardThemeImage;

    /** The PackageManager */
    private PackageManager mPm = null;

    /**
     * Called when the activity is starting.
     * 
     * @see android.app.Activity#onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.keyboard_theme);

        mPm = getPackageManager();

        mKeyboardThemeImage = (ImageView) findViewById(R.id.themeImage);
        mKeyboardThemeImage.setImageDrawable(getResources().getDrawable(
                R.drawable.ime_keypad_thumbnail));

        ListView listView = (ListView) findViewById(R.id.themeList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.keyboard_theme_list);
        listView.setAdapter(adapter);

        // デフォルトテーマを設定
        String defaultTheme = getResources().getString(
                R.string.ti_preference_keyboard_add_default_txt);
        adapter.add(defaultTheme);

        mKeyboardThemeData.put(defaultTheme, null);
        listView.setItemChecked(0, true);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view,
                    int position, long id) {

                ListView list = (ListView) parent;
                mKeyboardThemeInfo = mKeyboardThemeData.get(list
                        .getItemAtPosition(position));
                if (mKeyboardThemeInfo != null) {
                    setKeyboardThemeImage(mKeyboardThemeInfo.packageName);
                } else {
                    mKeyboardThemeImage.setImageDrawable(getResources()
                            .getDrawable(R.drawable.ime_keypad_thumbnail));
                }

            }
        });

        //スキン情報の取得
        List<ResolveInfo> resolveInfo = KeyBoardSkinAddListPreference
                .getKeyboardSkinInfo(this);

        for (int i = 0; i < resolveInfo.size(); i++) {
            ResolveInfo info = resolveInfo.get(i);
            ActivityInfo actInfo = info.activityInfo;
            if (actInfo != null) {
                adapter.add((String) info.loadLabel(mPm));
                mKeyboardThemeData.put((String) info.loadLabel(mPm), actInfo);

                // 現在のテーマを選択
                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(this);
                if (pref.getString("keyboard_skin_add", "")
                        .equals(actInfo.name)) {
                    listView.setItemChecked(adapter.getCount() - 1, true);
                    setKeyboardThemeImage(actInfo.packageName);
                }
            }
        }

        // OKボタン
        Button applyButton = (Button) findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setKeyboardTheme();
                SharedPreferences pref = 
                        PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
                KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
                if (keyskin.getPackageManger() == null) {
                    keyskin.init(OpenWnn.superGetContext());
                }
                keyskin.setPreferences(pref);
                finish();
            }
        });

        // Cancelボタン
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem actionItem = menu.add(getResources().getString(
                R.string.ti_download_theme_button_txt));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        actionItem.setIcon(R.drawable.ime_symbol_help_icon_download);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(
                "com.lge.apps.jp.phone");
        intent.putExtra("CategoryID", "IMETHEME");
        startActivity(intent);
        return true;
    }

    /**
     * Called when the activity is resume.
     * 
     * @see android.app.Activity#onResume
     */
    @Override protected void onResume() {
        if (mKeyboardThemeInfo != null && mKeyboardThemeInfo.name != null) {
            setKeyboardThemeImage(mKeyboardThemeInfo.packageName);
        }
        super.onResume();
    }

    /**
     * Called when the activity is destroy.
     *
     * @see android.app.Activity#onDestroy
     */
    @Override protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Called when the keyboard theme image update
     *
     */
    private void setKeyboardThemeImage(String packageName) {
        try {
            if (packageName != null) {
                Resources res = mPm.getResourcesForApplication(packageName);
                int resourceId = res.getIdentifier("ime_keypad_thumbnail",
                        "drawable", packageName);
                if (resourceId != 0) {
                    mKeyboardThemeImage.setImageDrawable(res
                            .getDrawable(resourceId));
                } else {
                    mKeyboardThemeImage.setImageDrawable(getResources()
                            .getDrawable(R.drawable.ime_keypad_thumbnail));
                }
            } else {
                mKeyboardThemeImage.setImageDrawable(getResources().getDrawable(
                        R.drawable.ime_keypad_thumbnail));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the keyboard theme set
     * 
     */
    private void setKeyboardTheme() {
        if (mKeyboardThemeInfo != null && mKeyboardThemeInfo.name != null) {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("keyboard_skin_add", mKeyboardThemeInfo.name);
            editor.commit();
        } else {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("keyboard_skin_add", "");
            editor.commit();
        }
    }

}
