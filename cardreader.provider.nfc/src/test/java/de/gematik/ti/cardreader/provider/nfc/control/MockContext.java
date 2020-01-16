/*
 * Copyright (c) 2020 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.ti.cardreader.provider.nfc.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

/**
 * A mock {@link Context} class. All methods are non-functional and throw UnsupportedOperationException You can use this to inject other dependencies, mocks, or
 * monitors into the classes you are testing.
 */
public class MockContext extends Context {

    @Override
    public AssetManager getAssets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resources getResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageManager getPackageManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentResolver getContentResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Looper getMainLooper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Executor getMainExecutor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getApplicationContext() {
        return new MockApplication();
    }

    @Override
    public void setTheme(final int resid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resources.Theme getTheme() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackageName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackageResourcePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackageCodePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedPreferences getSharedPreferences(final String name, final int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveSharedPreferencesFrom(final Context sourceContext, final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteSharedPreferences(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileInputStream openFileInput(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileOutputStream openFileOutput(final String name, final int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteFile(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFileStreamPath(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] fileList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFilesDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getNoBackupFilesDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getExternalFilesDir(final String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getObbDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getCodeCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getExternalCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDir(final String name, final int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(final String file, final int mode, final SQLiteDatabase.CursorFactory factory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(final String file, final int mode, final SQLiteDatabase.CursorFactory factory,
            final DatabaseErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDatabasePath(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] databaseList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveDatabaseFrom(final Context sourceContext, final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteDatabase(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable getWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable peekWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWallpaper(final Bitmap bitmap) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWallpaper(final InputStream data) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startActivity(final Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startActivity(final Intent intent, final Bundle options) {
        startActivity(intent);
    }

    @Override
    public void startActivities(final Intent[] intents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startActivities(final Intent[] intents, final Bundle options) {
        startActivities(intents);
    }

    @Override
    public void startIntentSender(final IntentSender intent, final Intent fillInIntent, final int flagsMask, final int flagsValues, final int extraFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startIntentSender(final IntentSender intent, final Intent fillInIntent, final int flagsMask, final int flagsValues, final int extraFlags,
            final Bundle options) {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    @Override
    public void sendBroadcast(final Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBroadcast(final Intent intent, final String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendOrderedBroadcast(final Intent intent, final String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendOrderedBroadcast(final Intent intent, final String receiverPermission, final BroadcastReceiver resultReceiver, final Handler scheduler,
            final int initialCode, final String initialData, final Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBroadcastAsUser(final Intent intent, final UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBroadcastAsUser(final Intent intent, final UserHandle user, final String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendOrderedBroadcastAsUser(final Intent intent, final UserHandle user, final String receiverPermission, final BroadcastReceiver resultReceiver,
            final Handler scheduler, final int initialCode, final String initialData, final Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendStickyBroadcast(final Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendStickyOrderedBroadcast(final Intent intent, final BroadcastReceiver resultReceiver, final Handler scheduler, final int initialCode,
            final String initialData, final Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStickyBroadcast(final Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendStickyBroadcastAsUser(final Intent intent, final UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(final Intent intent, final UserHandle user, final BroadcastReceiver resultReceiver, final Handler scheduler,
            final int initialCode, final String initialData, final Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStickyBroadcastAsUser(final Intent intent, final UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        return null;
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final String broadcastPermission, final Handler scheduler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final String broadcastPermission, final Handler scheduler,
            final int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterReceiver(final BroadcastReceiver receiver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentName startService(final Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentName startForegroundService(final Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stopService(final Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean bindService(final Intent service, final ServiceConnection conn, final int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unbindService(final ServiceConnection conn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startInstrumentation(final ComponentName className, final String profileFile, final Bundle arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSystemService(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSystemServiceName(final Class<?> serviceClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkPermission(final String permission, final int pid, final int uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingPermission(final String permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingOrSelfPermission(final String permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkSelfPermission(final String permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforcePermission(final String permission, final int pid, final int uid, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingPermission(final String permission, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingOrSelfPermission(final String permission, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void grantUriPermission(final String toPackage, final Uri uri, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeUriPermission(final Uri uri, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeUriPermission(final String targetPackage, final Uri uri, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkUriPermission(final Uri uri, final int pid, final int uid, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingUriPermission(final Uri uri, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingOrSelfUriPermission(final Uri uri, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkUriPermission(final Uri uri, final String readPermission, final String writePermission, final int pid, final int uid, final int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceUriPermission(final Uri uri, final int pid, final int uid, final int modeFlags, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingUriPermission(final Uri uri, final int modeFlags, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingOrSelfUriPermission(final Uri uri, final int modeFlags, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceUriPermission(final Uri uri, final String readPermission, final String writePermission, final int pid, final int uid,
            final int modeFlags, final String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context createPackageContext(final String packageName, final int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override
    public Context createConfigurationContext(final Configuration overrideConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context createDisplayContext(final Display display) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRestricted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File[] getExternalFilesDirs(final String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File[] getObbDirs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File[] getExternalCacheDirs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File[] getExternalMediaDirs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        throw new UnsupportedOperationException();
    }

}
