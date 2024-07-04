/*
 * Copyright 2013 Gerhard Klostermeier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.jxvtc.WaterCardTool.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.text.HtmlCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.jxvtc.WaterCardTool.Common;
import de.jxvtc.WaterCardTool.R;


/**
 * Main App entry point showing the main menu.
 * Some stuff about the App:
 * <ul>
 * <li>Error/Debug messages (Log.e()/Log.d()) are hard coded</li>
 * <li>This is my first App, so please by decent with me ;)</li>
 * </ul>
 * @author Gerhard Klostermeier
 */
public class MainMenu extends Activity {


    private static final String LOG_TAG =
        MainMenu.class.getSimpleName();

    private final static int FILE_CHOOSER_DUMP_FILE = 1;
    private final static int FILE_CHOOSER_KEY_FILE = 2;
    private boolean mDonateDialogWasShown = false;
    private boolean mInfoExternalNfcDialogWasShown = false;
    private boolean mHasNoNfc = false;
    private Button mWriteTag;
    private Intent mOldIntent = null;

    /**
     * Nodes (stats) MCT passes through during its startup.
     */
    enum StartUpNode {
        FirstUseDialog, DonateDialog, HasNfc, HasMifareClassicSupport,
        HasNfcEnabled, HasExternalNfc, ExternalNfcServiceRunning,
        HandleNewIntent
    }

    /**
     * Check for NFC hardware and MIFARE Classic support.
     * The directory structure and the std. keys files will be created as well.
     * Also, at the first run of this App, a warning
     * notice and a donate message will be displayed.
     * @see #initFolders()
     * @see #copyStdKeysFiles()
     */
    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);



        // Show App version and footer.
        TextView tv = findViewById(R.id.textViewMainFooter);
        TextView IP = findViewById(R.id.IP);
        TextView Name = findViewById(R.id.Username);
        TextView Score = findViewById(R.id.Score);
        TextView ID = findViewById(R.id.ID);

        Name.setText("用户：" + Login.SeverName);
        Score.setText("积分：" + Login.SeverScore);
        ID.setText("ID：" + Login.SeverID);
        IP.setText("IP：" + Login.IP);
        tv.setText("代理数量：NULL");







        // Restore state.
        if (savedInstanceState != null) {
            mDonateDialogWasShown = savedInstanceState.getBoolean(
                "donate_dialog_was_shown");
            mInfoExternalNfcDialogWasShown = savedInstanceState.getBoolean(
                "info_external_nfc_dialog_was_shown");
            mHasNoNfc = savedInstanceState.getBoolean("has_no_nfc");
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mOldIntent = savedInstanceState.getParcelable("old_intent", Intent.class );
            } else {
                mOldIntent = savedInstanceState.getParcelable("old_intent");
            }

        }

        // Bind main layout buttons.
        mWriteTag = findViewById(R.id.buttonMainWriteTag);

        initFolders();
        copyStdKeysFiles();
    }


    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }


    /**
     * Save important state data before this activity gets destroyed.
     * @param outState The state to put data into.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("donate_dialog_was_shown", mDonateDialogWasShown);
        outState.putBoolean("info_external_nfc_dialog_was_shown", mInfoExternalNfcDialogWasShown);
        outState.putBoolean("has_no_nfc", mHasNoNfc);
        outState.putParcelable("old_intent", mOldIntent);
    }

    /**
     * Each phase of the MCTs startup is called "node" (see {@link StartUpNode})
     * and can be started by this function. The following nodes will be
     * started automatically (e.g. if the "has NFC support?" node is triggered
     * the "has MIFARE classic support?" node will be run automatically
     * after that).
     * @param startUpNode The node of the startup checks chain.
     * @see StartUpNode
     */
    @SuppressWarnings("deprecation")
    private void runStartUpNode(StartUpNode startUpNode) {
        SharedPreferences sharedPref =
            getPreferences(Context.MODE_PRIVATE);
        Editor sharedEditor = sharedPref.edit();
        switch (startUpNode) {
            case FirstUseDialog:
                boolean isFirstRun = sharedPref.getBoolean(
                    "is_first_run", true);
                if (isFirstRun) {
                    createFirstUseDialog().show();
                } else {
                    runStartUpNode(StartUpNode.HasNfc);
                }
                break;
            case HasNfc:
                Common.setNfcAdapter(NfcAdapter.getDefaultAdapter(this));
                if (Common.getNfcAdapter() == null) {
                    mHasNoNfc = true;
                    runStartUpNode(StartUpNode.HasExternalNfc);
                } else {
                    runStartUpNode(StartUpNode.HasMifareClassicSupport);
                }
                break;
            case HasMifareClassicSupport:
                if (!Common.hasMifareClassicSupport()
                    && !Common.useAsEditorOnly()) {
                    runStartUpNode(StartUpNode.HasExternalNfc);
                } else {
                    runStartUpNode(StartUpNode.HasNfcEnabled);
                }
                break;
            case HasNfcEnabled:
                Common.setNfcAdapter(NfcAdapter.getDefaultAdapter(this));
                if (!Common.getNfcAdapter().isEnabled()) {
                    if (!Common.useAsEditorOnly()) {
                        createNfcEnableDialog().show();
                    } else {
                        runStartUpNode(StartUpNode.DonateDialog);
                    }
                } else {
                    // Use MCT with internal NFC controller.
                    useAsEditorOnly(false);
                    Common.enableNfcForegroundDispatch(this);
                    runStartUpNode(StartUpNode.DonateDialog);
                }
                break;
            case HasExternalNfc:
                if (!Common.hasExternalNfcInstalled(this)
                    && !Common.useAsEditorOnly()) {
                    if (mHasNoNfc) {
                        // Here because the phone is not NFC enabled.
                        createInstallExternalNfcDialog().show();
                    } else {
                        // Here because phone does not support MIFARE Classic.
                        AlertDialog ad = createHasNoMifareClassicSupportDialog();
                        ad.show();
                        // Make links clickable.
                        ((TextView) ad.findViewById(android.R.id.message))
                            .setMovementMethod(
                                LinkMovementMethod.getInstance());
                    }
                } else {
                    runStartUpNode(StartUpNode.ExternalNfcServiceRunning);
                }
                break;
            case ExternalNfcServiceRunning:
                int isExternalNfcRunning =
                    Common.isExternalNfcServiceRunning(this);
                if (isExternalNfcRunning == 0) {
                    // External NFC is not running.
                    if (!Common.useAsEditorOnly()) {
                        createStartExternalNfcServiceDialog().show();
                    } else {
                        runStartUpNode(StartUpNode.DonateDialog);
                    }
                } else if (isExternalNfcRunning == 1) {
                    // External NFC is running. Use MCT with External NFC.
                    useAsEditorOnly(false);
                    runStartUpNode(StartUpNode.DonateDialog);
                } else {
                    // Can not check if External NFC is running.
                    if (!Common.useAsEditorOnly()
                        && !mInfoExternalNfcDialogWasShown) {
                        createInfoExternalNfcServiceDialog().show();
                        mInfoExternalNfcDialogWasShown = true;
                    } else {
                        runStartUpNode(StartUpNode.DonateDialog);
                    }
                }
                break;
            case DonateDialog:
                if (Common.IS_DONATE_VERSION) {
                    runStartUpNode(StartUpNode.HandleNewIntent);
                    break;
                }
                if (mDonateDialogWasShown) {
                    runStartUpNode(StartUpNode.HandleNewIntent);
                    break;
                }
                int currentVersion = 0;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        currentVersion = (int) PackageInfoCompat.getLongVersionCode(
                            getPackageManager().getPackageInfo(getPackageName(),
                                PackageManager.PackageInfoFlags.of(0)));
                    } else {
                        currentVersion = (int) PackageInfoCompat.getLongVersionCode(
                            getPackageManager().getPackageInfo(getPackageName(), 0));
                    }
                } catch (NameNotFoundException e) {
                    Log.d(LOG_TAG, "Version not found.");
                }
                int lastVersion = sharedPref.getInt("mct_version",
                    currentVersion - 1);
                boolean showDonateDialog = sharedPref.getBoolean(
                    "show_donate_dialog", true);

                if (lastVersion < currentVersion || showDonateDialog) {
                    // This is either a new version of MCT or the user
                    // wants to see the donate dialog.
                    if (lastVersion < currentVersion) {
                        // Update the version.
                        sharedEditor.putInt("mct_version", currentVersion);
                        sharedEditor.putBoolean("show_donate_dialog", true);
                        sharedEditor.apply();
                    }
                    createDonateDialog().show();
                    mDonateDialogWasShown = true;
                } else {
                    runStartUpNode(StartUpNode.HandleNewIntent);
                }
                break;
            case HandleNewIntent:
                Common.setPendingComponentName(null);
                Intent intent = getIntent();
                if (intent != null && intent.getAction() != null) {
                    boolean isIntentWithTag = intent.getAction().equals(
                        NfcAdapter.ACTION_TECH_DISCOVERED);
                    if (isIntentWithTag && intent != mOldIntent) {
                        // If MCT was called by another app or the dispatch
                        // system with a tag delivered by intent, handle it as
                        // new tag intent.
                        mOldIntent = intent;
                        onNewIntent(getIntent());
                    } else {
                        // Last node. Do nothing.
                        break;
                    }
                }
                break;
        }
    }

    /**
     * Set whether to use the app in editor only mode or not.
     * @param useAsEditorOnly True if the app should be used in editor
     * only mode.
     */
    private void useAsEditorOnly(boolean useAsEditorOnly) {
        Common.setUseAsEditorOnly(useAsEditorOnly);
        mWriteTag.setEnabled(!useAsEditorOnly);
    }

    /**
     * Create the dialog which is displayed once the app was started for the
     * first time. After showing the dialog, {@link #runStartUpNode(StartUpNode)}
     * with {@link StartUpNode#HasNfc} will be called.
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createFirstUseDialog() {
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_first_run_title)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.dialog_first_run)
            .setPositiveButton(R.string.action_ok,
                (dialog, which) -> dialog.cancel())
            .setOnCancelListener(
                dialog -> {
                    SharedPreferences sharedPref =
                        getPreferences(Context.MODE_PRIVATE);
                    Editor sharedEditor = sharedPref.edit();
                    sharedEditor.putBoolean("is_first_run", false);
                    sharedEditor.apply();
                    // Continue with "has NFC" check.
                    runStartUpNode(StartUpNode.HasNfc);
                })
            .create();
    }

    /**
     * Create the dialog which is displayed if the device does not have
     * MIFARE classic support. After showing the dialog,
     * {@link #runStartUpNode(StartUpNode)} with {@link StartUpNode#DonateDialog}
     * will be called or the app will be exited.
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createHasNoMifareClassicSupportDialog() {
        CharSequence styledText = HtmlCompat.fromHtml(
            getString(R.string.dialog_no_mfc_support_device),
            HtmlCompat.FROM_HTML_MODE_LEGACY);
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_no_mfc_support_device_title)
            .setMessage(styledText)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton(R.string.action_exit_app,
                (dialog, id) -> {
                    // Exit the App.
                    finish();
                })
            .setOnCancelListener(
                dialog -> finish())
            .create();
    }

    /**
     * Create a dialog that send user to NFC settings if NFC is off.
     * Alternatively the user can chose to use the App in editor only
     * mode or exit the App.
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createNfcEnableDialog() {
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_nfc_not_enabled_title)
            .setMessage(R.string.dialog_nfc_not_enabled)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(R.string.action_nfc,
                (dialog, which) -> {
                    // Goto NFC Settings.
                    startActivity(new Intent(
                        Settings.ACTION_NFC_SETTINGS));
                })
            .setNegativeButton(R.string.action_exit_app,
                (dialog, id) -> {
                    // Exit the App.
                    finish();
                })
            .setOnCancelListener(
                dialog -> finish())
            .create();
    }

    /**
     * Create the dialog which is displayed if the device has not "External NFC"
     * installed. After showing the dialog, {@link #runStartUpNode(StartUpNode)}
     * with {@link StartUpNode#DonateDialog} will be called or MCT will
     * redirect the user to the play store page of "External NFC"  or
     * the app will be exited.
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createInstallExternalNfcDialog() {
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_no_nfc_support_title)
            .setMessage(R.string.dialog_no_nfc_support)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton(R.string.action_exit_app,
                (dialog, id) -> {
                    // Exit the App.
                    finish();
                })
            .setOnCancelListener(
                dialog -> finish())
            .create();
    }

    /**
     * Create the dialog which is displayed if the "External NFC" service is
     * not running. After showing the dialog,
     * {@link #runStartUpNode(StartUpNode)} with {@link StartUpNode#DonateDialog}
     * will be called or MCT will redirect the user to the settings of
     * "External NFC" or the app will be exited.
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createStartExternalNfcServiceDialog() {
        final Context context = this;
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_start_external_nfc_title)
            .setMessage(R.string.dialog_start_external_nfc)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.action_start_external_nfc,
                (dialog, which) -> {
                    useAsEditorOnly(true);
                    Common.openApp(context, "eu.dedb.nfc.service");
                })
            .setNegativeButton(R.string.action_exit_app,
                (dialog, id) -> {
                    // Exit the App.
                    finish();
                })
            .setOnCancelListener(
                dialog -> finish())
            .create();
    }

    /**
     * Create the dialog which is displayed if it is not clear if the
     * "External NFC" service running. After showing the dialog,
     * {@link #runStartUpNode(StartUpNode)} with {@link StartUpNode#DonateDialog}
     * will be called or MCT will redirect the user to the settings of
     * "External NFC".
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createInfoExternalNfcServiceDialog() {
        final Context context = this;
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_info_external_nfc_title)
            .setMessage(R.string.dialog_info_external_nfc)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.action_external_nfc_is_running,
                (dialog, which) -> {
                    // External NFC is running. Do "nothing".
                    runStartUpNode(StartUpNode.DonateDialog);
                })
            .setNeutralButton(R.string.action_start_external_nfc,
                (dialog, which) -> Common.openApp(context, "eu.dedb.nfc.service"))
            .setOnCancelListener(
                dialog -> {
                    // Only use Editor.
                    useAsEditorOnly(true);
                    runStartUpNode(StartUpNode.DonateDialog);
                })
            .create();
    }

    /**
     * Create the donate dialog. After showing the dialog,
     * {@link #runStartUpNode(StartUpNode)} with
     * {@link StartUpNode#HandleNewIntent} will be called.
     * @return The created alert dialog.
     * @see #runStartUpNode(StartUpNode)
     */
    private AlertDialog createDonateDialog() {
        View dialogLayout = getLayoutInflater().inflate(
            R.layout.dialog_donate,
            findViewById(android.R.id.content), false);
        TextView textView = dialogLayout.findViewById(
            R.id.textViewDonateDialog);
        textView.setText(Login.SeverInfo);
        final CheckBox showDonateDialogCheckBox = dialogLayout
            .findViewById(R.id.checkBoxDonateDialog);
        return new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_donate_title)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setView(dialogLayout)
            .setPositiveButton(R.string.action_ok,
                (dialog, which) -> dialog.cancel())
            .setOnCancelListener(
                dialog -> {
                    if (showDonateDialogCheckBox.isChecked()) {
                        // Do not show the donate dialog again.
                        SharedPreferences sharedPref =
                            getPreferences(Context.MODE_PRIVATE);
                        Editor sharedEditor = sharedPref.edit();
                        sharedEditor.putBoolean(
                            "show_donate_dialog", false);
                        sharedEditor.apply();
                    }
                    runStartUpNode(StartUpNode.HandleNewIntent);
                })
            .create();
    }

    /**
     * 创建MCT所需的目录，并清除tmp文件夹。
     */
    @SuppressLint("ApplySharedPref")
    private void initFolders() {
        // Create keys directory.
        File path = Common.getFile(Common.KEYS_DIR);

        if (!path.exists() && !path.mkdirs()) {
            // Could not create directory.
            Log.e(LOG_TAG, "Error while creating '" + Common.HOME_DIR
                + "/" + Common.KEYS_DIR + "' directory.");
            return;
        }

        // Create dumps directory.
        path = Common.getFile(Common.DUMPS_DIR);
        if (!path.exists() && !path.mkdirs()) {
            // Could not create directory.
            Log.e(LOG_TAG, "Error while creating '" + Common.HOME_DIR
                + "/" + Common.DUMPS_DIR + "' directory.");
            return;
        }

        // Create tmp directory.
        path = Common.getFile(Common.TMP_DIR);
        if (!path.exists() && !path.mkdirs()) {
            // Could not create directory.
            Log.e(LOG_TAG, "Error while creating '" + Common.HOME_DIR
                + Common.TMP_DIR + "' directory.");
            return;
        }
        // Try to clean up tmp directory.
        File[] tmpFiles = path.listFiles();
        if (tmpFiles != null) {
            for (File file : tmpFiles) {
                file.delete();
            }
        }
    }

    /**
     * Add a menu with "preferences", "about", etc. to the Activity.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu_functions, menu);
        return true;
    }

    /**
     * Add the menu with the tools.
     * It will be shown if the user clicks on "Tools".
     */


    /**
     * Resume by triggering MCT's startup system
     * ({@link #runStartUpNode(StartUpNode)}).
     * @see #runStartUpNode(StartUpNode)
     */
    @Override
    public void onResume() {
        super.onResume();

        // Show App version and footer.
        TextView tv = findViewById(R.id.textViewMainFooter);
        TextView IP = findViewById(R.id.IP);
        TextView Name = findViewById(R.id.Username);
        TextView Score = findViewById(R.id.Score);
        TextView ID = findViewById(R.id.ID);

        Name.setText("用户：" + Login.SeverName);
        Score.setText("积分：" + Login.SeverScore);
        ID.setText("ID：" + Login.SeverID);
        IP.setText("IP：" + Login.IP);
        tv.setText("代理数量：" + Login.SeverCount);

        useAsEditorOnly(Common.useAsEditorOnly());
        // The start up nodes will also enable the NFC foreground dispatch if all
        // conditions are met (has NFC & NFC enabled).
        runStartUpNode(StartUpNode.FirstUseDialog);
    }

    /**
     * Disable NFC foreground dispatch system.
     * @see Common#disableNfcForegroundDispatch(Activity)
     */
    @Override
    public void onPause() {
        super.onPause();
        Common.disableNfcForegroundDispatch(this);
    }

    /**
     将新的 Intent 处理为新的标签 Intent，如果标签/设备不支持 MIFARE Classic，则运行 {@link TagInfoTool}。
     @see Common#treatAsNewTag(Intent, android.content.Context)
     @see TagInfoTool
     */
    @Override
    public void onNewIntent(Intent intent) {
        if(Common.getPendingComponentName() != null) {
            intent.setComponent(Common.getPendingComponentName());
            startActivity(intent);
        } else {
            int typeCheck = Common.treatAsNewTag(intent, this);
            if (typeCheck == -1 || typeCheck == -2) {
                // Device or tag does not support MIFARE Classic.
                // Run the only thing that is possible: The tag info tool.
                Intent i = new Intent(this, TagInfoTool.class);
                startActivity(i);
            }
        }
    }

    /**
     * Show the {@link ReadTag}.
     * @param view The View object that triggered the method
     * (in this case the read tag button).
     * @see ReadTag
     */

    /**
     * Show the {@link WriteTag}.
     * @param view The View object that triggered the method
     * (in this case the write tag button).
     * @see WriteTag
     */
    public void onShowWriteTag(View view) {
        Intent intent = new Intent(this, WriteTag.class);
        startActivity(intent);
    }

    /**
     * Show the {@link HelpAndInfo}.
     * @param view The View object that triggered the method
     * (in this case the help/info button).
     */
    public void onShowHelp(View view) {
        Intent intent = new Intent(this, HelpAndInfo.class);
        startActivity(intent);
    }

    /**
     * Show the tools menu (as context menu).
     * @param view The View object that triggered the method
     * (in this case the tools button).
     */


    /**
     * Open a file chooser ({@link FileChooser}). The
     * Activity result will be processed in
     * {@link #onActivityResult(int, int, Intent)}.
     * If the dump files folder is empty display an additional error
     * message.
     * @param view The View object that triggered the method
     * (in this case the show/edit tag dump button).
     * @see FileChooser
     * @see #onActivityResult(int, int, Intent)
     */





    /**
     * Show the {@link Preferences}.
     */
    private void onShowPreferences() {
        Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
    }

    /**
     * Show the about dialog.
     */
    private void onShowAboutDialog() {
        CharSequence styledText = HtmlCompat.fromHtml(
            getString(R.string.dialog_about_mct, Common.getVersionCode()),
            HtmlCompat.FROM_HTML_MODE_LEGACY);
        AlertDialog ad = new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_about_mct_title)
            .setMessage(styledText)
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton(R.string.action_ok,
                (dialog, which) -> {
                    // Do nothing.
                }).create();
        ad.show();
        // Make links clickable.
        ((TextView)ad.findViewById(android.R.id.message)).setMovementMethod(
            LinkMovementMethod.getInstance());
    }



    /**
     * Handle (start) the selected tool from the tools menu.
     * @see TagInfoTool
     * @see ValueBlockTool
     * @see AccessConditionTool
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        if (id == R.id.menuMainTagInfo) {
            intent = new Intent(this, TagInfoTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainValueBlockTool) {
            intent = new Intent(this, ValueBlockTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainAccessConditionTool) {
            intent = new Intent(this, AccessConditionTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainDiffTool) {
            intent = new Intent(this, DiffTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainBccTool) {
            intent = new Intent(this, BccTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainCloneUidTool) {
            intent = new Intent(this, CloneUidTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainImportExportTool) {
            intent = new Intent(this, ImportExportTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainUidLogTool) {
            intent = new Intent(this, UidLogTool.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuMainDataConversionTool) {
            intent = new Intent(this, DataConversionTool.class);
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case FILE_CHOOSER_DUMP_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, DumpEditor.class);
                    intent.putExtra(FileChooser.EXTRA_CHOSEN_FILE,
                        data.getStringExtra(
                            FileChooser.EXTRA_CHOSEN_FILE));
                    startActivity(intent);
                }
                break;
            case FILE_CHOOSER_KEY_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, KeyEditor.class);
                    intent.putExtra(FileChooser.EXTRA_CHOSEN_FILE,
                        data.getStringExtra(
                            FileChooser.EXTRA_CHOSEN_FILE));
                    startActivity(intent);
                }
                break;
        }
    }

    /**
     * Copy the standard key files ({@link Common#STD_KEYS} and
     * {@link Common#STD_KEYS_EXTENDED}) form assets to {@link Common#KEYS_DIR}.
     * @see Common#KEYS_DIR
     * @see Common#HOME_DIR
     * @see Common#copyFile(InputStream, OutputStream)
     */
    private void copyStdKeysFiles() {
        File std = Common.getFile(
            Common.KEYS_DIR + "/" + Common.STD_KEYS);
        File extended = Common.getFile(
            Common.KEYS_DIR + "/" + Common.STD_KEYS_EXTENDED);
        AssetManager assetManager = getAssets();

        // Copy std.keys.
        try {
            InputStream in = assetManager.open(
                Common.KEYS_DIR + "/" + Common.STD_KEYS);
            OutputStream out = new FileOutputStream(std);
            Common.copyFile(in, out);
            in.close();
            out.flush();
            out.close();
        } catch(IOException e) {
            Log.e(LOG_TAG, "Error while copying 'std.keys' from assets "
                + "to internal storage.");
        }

        // Copy extended-std.keys.
        try {
            InputStream in = assetManager.open(
                Common.KEYS_DIR + "/" + Common.STD_KEYS_EXTENDED);
            OutputStream out = new FileOutputStream(extended);
            Common.copyFile(in, out);
            in.close();
            out.flush();
            out.close();
        } catch(IOException e) {
            Log.e(LOG_TAG, "Error while copying 'extended-std.keys' "
                + "from assets to internal storage.");
        }

    }







}
