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

import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.nfc.Whitebox;
import de.gematik.ti.cardreader.provider.nfc.entities.NfcCardReader;

/**
 * Test {@link NfcCardReaderController}
 *
 */
public class NfcCardReaderControllerTest {

    private static NfcCardReader nfcCardReader;
    private static Context context;
    private static NfcAdapter nfcAdapter;
    private static NfcManager nfcManager;
    private static NfcCardReaderController nfcCardReaderController;

    @BeforeClass
    public static void init() {
        context = new MockContext();
        nfcManager = Mockito.mock(NfcManager.class);
        nfcAdapter = Mockito.mock(NfcAdapter.class);
        nfcCardReader = new NfcCardReader(nfcAdapter, context);
        Mockito.when(nfcAdapter.isEnabled()).thenReturn(true);
        nfcCardReaderController = NfcCardReaderController.getInstance();
        nfcCardReaderController.setContext(context);
        Whitebox.setInternalState(nfcCardReaderController, "nfcManager", nfcManager);
        Whitebox.setInternalState(nfcCardReaderController, "nfcAdapter", nfcAdapter);

    }

    @AfterClass
    public static void close() {
        Whitebox.setInternalState(nfcCardReaderController, "instance", null);
    }

    @Test
    public void testGetCardReaders() {
        Collection<ICardReader> cardReaders = nfcCardReaderController.getCardReaders();
        Assert.assertTrue(cardReaders.isEmpty());
    }

    @Test
    public void testSetReaderOnline() {
        Whitebox.setInternalState(nfcCardReaderController, "nfcCardReader", nfcCardReader);
        nfcCardReader.setOnline(false);
        nfcCardReaderController.setReaderOnline();
        Assert.assertTrue(nfcCardReader.isOnline());
    }

    @Test
    public void testSetReaderOffine() {
        Whitebox.setInternalState(nfcCardReaderController, "nfcCardReader", nfcCardReader);
        nfcCardReader.setOnline(true);
        nfcCardReaderController.setReaderOffline();
        Assert.assertFalse(nfcCardReader.isOnline());
    }

}
