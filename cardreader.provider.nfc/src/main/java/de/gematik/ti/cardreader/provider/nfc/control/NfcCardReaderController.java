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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.listener.InitializationStatus;
import de.gematik.ti.cardreader.provider.nfc.entities.NfcCardReader;
import de.gematik.ti.openhealthcard.common.AbstractAndroidCardReaderController;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=NfcCardReaderController]
 *
 */
@Singleton
public final class NfcCardReaderController extends AbstractAndroidCardReaderController {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCardReaderController.class);

    private static NfcCardReaderController instance;
    private final ArrayList<ICardReader> cardReaders = new ArrayList<>();
    private NfcManager nfcManager;
    private NfcAdapter nfcAdapter;
    private NfcReceiver nfcReceiver;
    private boolean initialized = false;
    private final Map<ICardReader, NfcCardChecker> cardCheckerMap = new HashMap<>();
    private Context context;
    private NfcCardReader nfcCardReader;
    private final NfcActivityLifecycleCallback activityLifecycleCallback = new NfcActivityLifecycleCallback();

    private NfcCardReaderController() {
    }

    /**
     * Returns an instance of NfcCardReaderController class
     *
     * @return NfcCardReaderController (this)
     */
    public static NfcCardReaderController getInstance() {
        if (instance == null) {
            instance = new NfcCardReaderController();
        }
        return instance;
    }

    /**
     * Returns a list of connected cardReaders The List contains one default NfcCardReader object only if the device supports NFC and NFC is enabled
     *
     * @return list of connected cardReaders
     */
    @Override
    public Collection<ICardReader> getCardReaders() {
        checkContext();

        if (!initialized) {

            init();
        }

        if (!cardReaders.isEmpty()) {

            LOG.debug("Nfc-Reader is initialized");
            if (nfcCardReader.isOnline()) {
                LOG.debug("Nfc-Reader is online");
            } else {
                LOG.debug("Nfc-Reader is offline");
            }
        }
        return cardReaders;
    }

    private void init() {
        context = getContext();
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(activityLifecycleCallback);
    }

    private void initNfcReceiver() {
        checkContext();
        context = getContext();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

        nfcReceiver = new NfcReceiver();
        context.registerReceiver(nfcReceiver, filter);
    }

    void setReaderOnline() {
        if (!nfcCardReader.isOnline() || !cardCheckerMap.containsKey(nfcCardReader)) {
            nfcCardReader.setOnline(true);
            informAboutReaderConnection(nfcCardReader, InitializationStatus.INIT_SUCCESS);
            cardCheckerMap.put(nfcCardReader, new NfcCardChecker(nfcCardReader));
        }

    }

    void setReaderOffline() {
        if (nfcCardReader.isOnline() || cardCheckerMap.containsKey(nfcCardReader)) {
            nfcCardReader.setOnline(false);
            informAboutReaderDisconnection(nfcCardReader);
            stopPreciseTerminalCardChecker(nfcCardReader);
        }
    }

    private void stopPreciseTerminalCardChecker(final ICardReader cardReader) {
        final NfcCardChecker nfcCardChecker = cardCheckerMap.remove(cardReader);
        if (nfcCardChecker != null) {
            nfcCardChecker.shutdown();
        }
    }

    private synchronized void initializeController(final Activity activity) {
        if (nfcManager == null) {
            nfcManager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        }
        if (nfcAdapter == null && nfcManager != null) {
            nfcAdapter = nfcManager.getDefaultAdapter();
        }

        if (nfcAdapter == null) {
            LOG.error("Device doesn't support NFC");
            return;
        } else {
            createOrUpdateCardReader(activity);
        }

        if (!initialized && nfcAdapter != null && activity != null) {
            initNfcReceiver();
            initialized = true;
        }
    }

    private void createOrUpdateCardReader(final Activity activity) {
        if (nfcCardReader == null) {
            nfcCardReader = new NfcCardReader(nfcAdapter, activity);
            cardReaders.add(nfcCardReader);
        } else {
            nfcCardReader.updateActivity(activity);
        }

        if (nfcAdapter.isEnabled()) {
            LOG.debug("NFC enabled");
            setReaderOnline();
        } else if (nfcCardReader.isOnline()) {
            LOG.debug("NFC disabled");
            setReaderOffline();
        }
    }

    private class NfcActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(final Activity activity) {
            initializeController(activity);
        }

        @Override
        public void onActivityResumed(final Activity activity) {
        }

        @Override
        public void onActivityPaused(final Activity activity) {
        }

        @Override
        public void onActivityStopped(final Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(final Activity activity) {

        }

    }
}
