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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

import de.gematik.ti.cardreader.provider.nfc.Whitebox;
import de.gematik.ti.cardreader.provider.nfc.entities.NfcCardReader;

/**
 * Test {@link NfcReceiver}
 *
 */
public class NfcReceiverTest {

    private static NfcCardReaderController nfcCardReaderController;

    private static NfcReceiver nfcReceiver;
    private static Intent intent;
    private static Context context;
    private static NfcManager nfcManager;
    private static NfcAdapter nfcAdapter;
    private static NfcCardReader nfcCardReader;

    @BeforeClass
    public static void init() {
        nfcReceiver = new NfcReceiver();
        nfcCardReaderController = NfcCardReaderController.getInstance();
        intent = Mockito.mock(Intent.class);
        context = new MockContext();
        nfcManager = Mockito.mock(NfcManager.class);
        nfcAdapter = Mockito.mock(NfcAdapter.class);
        Mockito.when(nfcAdapter.isEnabled()).thenReturn(true);
        nfcCardReader = new NfcCardReader(nfcAdapter, context);
        nfcCardReaderController.setContext(context);
        Whitebox.setInternalState(nfcCardReaderController, "nfcManager", nfcManager);
        Whitebox.setInternalState(nfcCardReaderController, "nfcAdapter", nfcAdapter);
        Whitebox.setInternalState(nfcCardReaderController, "nfcCardReader", nfcCardReader);
    }

    @AfterClass
    public static void close() {
        Whitebox.setInternalState(nfcCardReaderController, "instance", null);
    }

    @Test
    public void nfcAdapterStateOffShouldSetReaderOffline() {
        nfcCardReader.setOnline(true);
        Assert.assertTrue(nfcCardReader.isOnline());
        Mockito.when(intent.getAction()).thenReturn(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        Mockito.when(intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)).thenReturn(NfcAdapter.STATE_OFF);
        nfcReceiver.onReceive(context, intent);
        Assert.assertFalse(nfcCardReader.isOnline());
    }

    @Test
    public void nfcAdapterStateOnShouldSetReaderOnline() {
        nfcCardReader.setOnline(false);
        Mockito.when(intent.getAction()).thenReturn(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        Mockito.when(intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)).thenReturn(NfcAdapter.STATE_ON);
        nfcReceiver.onReceive(context, intent);
        Assert.assertTrue(nfcCardReader.isOnline());
    }
}
