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

package de.gematik.ti.cardreader.provider.nfc.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.nfc.control.NfcCardReaderController;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=NfcCardReader]
 *
 */
public class NfcCardReader implements ICardReader, NfcAdapter.ReaderCallback, NfcAdapter.OnTagRemovedListener {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCardReader.class);
    private static final long WAIT_FOR_CARDS = 100L;
    private static final String NAME = "NFC CardReader";
    private final NfcActivityLifecycleCallback activityLifecycleCallback = new NfcActivityLifecycleCallback();
    private NfcCard nfcCard = null;
    private final NfcAdapter adapter;
    private boolean isInitialized = false;
    private boolean isOnline = false;
    private final Context context;

    /**
     * Constructor
     *
     * @param adapter
     * @param context
     */
    public NfcCardReader(final NfcAdapter adapter, final Context context) {
        this.adapter = adapter;
        this.context = context;

        initialize();
    }

    /**
     * Overrides the method from NfcAdapter ReaderCallback If Nfc/Tech tags discovered a new NfcCard object will be created or existing NfcCard object will be
     * reset
     *
     * @param tag
     */
    @Override
    public void onTagDiscovered(final Tag tag) {

        if (tag == null) {
            return;
        }

        if (nfcCard != null) {
            try {
                // reset nfcCard
                nfcCard.disconnect(true);
            } catch (final CardException e) {
                LOG.error("reset card failed " + e);
            }
        } else {
            for (final String tech : tag.getTechList()) {
                if (tech.equals(IsoDep.class.getName())) {
                    nfcCard = new NfcCard(tag, () -> NfcCardReaderController.getInstance().createCardEventTransmitter(this).informAboutCardPresent());
                    break;
                }
            }
        }
    }

    @Override
    public void onTagRemoved() {

        LOG.debug("onTagRemoved");

        if (nfcCard != null) {
            try {
                nfcCard.disconnect(false);
                LOG.debug("remove NFC card");
                nfcCard = null;

            } catch (final CardException e) {
                LOG.error("remove card failed" + e);
            }
        }
    }

    /**
     * Initialize/enable NFC ReaderMode and callback function on top activity
     */
    @Override
    public void initialize() {
        isInitialized = true;
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(activityLifecycleCallback);
    }

    public void updateActivity(final Activity activity) {
        final Bundle options = new Bundle();
        LOG.debug("NFC Activity: " + activity.getComponentName() + " " + activity + " " + activity.getClass());
        if (adapter.isEnabled()) {
            adapter.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    options);
            isOnline = true;
        } else {
            isOnline = false;
        }
    }

    /**
     * Returns the current initialisation status
     *
     * @return true: if cardReader is initialized false: cardReader not operational
     */
    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Establishes a connection to the card. If a connection has previously established this method returns a card object.
     *
     * @return card object
     * @throws CardException
     *             if no card detected
     */
    @Override
    public ICard connect() throws CardException {
        if (nfcCard == null) {
            throw new CardException("no card detected");
        }
        return nfcCard;
    }

    /**
     * Returns the unique NAME of this cardReader.
     *
     * @return this.NAME
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns whether a card is present
     *
     * @return true if card is present false if card is not present
     */
    @Override
    public boolean isCardPresent() {
        return nfcCard != null && nfcCard.isCardPresent();
    }

    /**
     * Waits until a card is absent in this reader or the timeout expires. If the method returns due to an expired timeout, it returns false. Otherwise it
     * return true.
     *
     * @param timeout
     * @return
     */
    public boolean waitForCardAbsent(final long timeout) {
        final long start = System.currentTimeMillis();

        while (isCardPresent()) {
            try {
                Thread.sleep(WAIT_FOR_CARDS);
            } catch (final InterruptedException e) {
            }
            if (System.currentTimeMillis() - start > timeout) {
                return false;
            }
        }
        return true;
    }

    /**
     * Waits until a card is present in this reader or the timeout expires. If the method returns due to an expired timeout, it returns false. Otherwise it
     * return true.
     *
     * @param timeout
     * @return
     */
    public boolean waitForCardPresent(final long timeout) {
        final long start = System.currentTimeMillis();
        while (!isCardPresent()) {
            try {
                Thread.sleep(WAIT_FOR_CARDS);
            } catch (final InterruptedException e) {
            }
            if (System.currentTimeMillis() - start > timeout) {
                return false;
            }
        }
        return true;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(final boolean online) {
        isOnline = online;
    }

    public void removeCard() {
        nfcCard = null;
    }

    private class NfcActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(final Activity activity) {
            updateActivity(activity);
        }

        @Override
        public void onActivityResumed(final Activity activity) {
        }

        @Override
        public void onActivityPaused(final Activity activity) {
        }

        @Override
        public void onActivityStopped(final Activity activity) {
            adapter.disableReaderMode(activity);
        }

        @Override
        public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(final Activity activity) {

        }

    }
}
