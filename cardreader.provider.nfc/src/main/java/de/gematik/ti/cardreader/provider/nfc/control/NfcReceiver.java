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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=NfcReceiver]
 */
class NfcReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(NfcReceiver.class);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();

        if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
            final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
            switch (state) {
                case NfcAdapter.STATE_OFF:
                    LOG.debug("NfcAdapter.STATE_OFF");
                    NfcCardReaderController.getInstance().setReaderOffline();
                    break;
                case NfcAdapter.STATE_TURNING_OFF:
                    LOG.debug("NfcAdapter.STATE_TURNING_OFF");
                    break;
                case NfcAdapter.STATE_ON:
                    LOG.debug("NfcAdapter.STATE_ON");
                    NfcCardReaderController.getInstance().setReaderOnline();
                    break;
                case NfcAdapter.STATE_TURNING_ON:
                    LOG.debug("NfcAdapter.STATE_TURNING_ON");
                    break;
                default:
                    LOG.debug("State not handled");
            }
        }
    }
}
