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

import org.hamcrest.core.IsNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.cardreader.provider.nfc.Whitebox;
import de.gematik.ti.cardreader.provider.nfc.control.MockContext;

/**
 * Test {@link NfcCardReader}
 *
 */
public class NfcCardReaderTest {

    private static final CommandApdu MANAGE_CHANNEL_COMMAND_CLOSE = new CommandApdu(0x00, 0x70, 0x80, 0x00);

    private static NfcCardReader nfcCardReader;
    private static Context context;
    private static NfcAdapter nfcAdapter;
    private static Tag tag;
    private static IsoDep isoDep;
    private static NfcCard card;

    @BeforeClass
    public static void init() {
        String[] techList = new String[] { IsoDep.class.getName() };
        context = new MockContext();
        nfcAdapter = Mockito.mock(NfcAdapter.class);
        tag = Mockito.mock(Tag.class);
        isoDep = Mockito.mock(IsoDep.class);
        Mockito.when(tag.getTechList()).thenReturn(techList);
        nfcCardReader = new NfcCardReader(nfcAdapter, context);
        card = Mockito.mock(NfcCard.class);

    }

    @AfterClass
    public static void close() {
        nfcCardReader = null;
    }

    @Test
    public void testOnTagDiscoveredShouldCreateCard() {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", null);
        Assert.assertThat(Whitebox.getInternalState(nfcCardReader, "nfcCard"), IsNull.nullValue());

        nfcCardReader.onTagDiscovered(tag);
        Assert.assertThat(Whitebox.getInternalState(nfcCardReader, "nfcCard"), IsNull.notNullValue());
    }

    @Test
    public void testOnTagDiscoveredShouldResetCard() throws CardException {
        ResponseApdu responseApdu = Mockito.mock(ResponseApdu.class);
        Mockito.when(isoDep.isConnected()).thenReturn(true);
        Mockito.when(card.transceive(MANAGE_CHANNEL_COMMAND_CLOSE)).thenReturn(responseApdu);
        Mockito.when(responseApdu.getSW()).thenReturn(0x9000);
        Whitebox.setInternalState(nfcCardReader, "nfcCard", card);

        nfcCardReader.onTagDiscovered(tag);
        Assert.assertThat(Whitebox.getInternalState(nfcCardReader, "nfcCard"), IsNull.notNullValue());
    }

    @Test
    public void testOnTagRemovedShouldRemoveCard() {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", card);
        Assert.assertThat(Whitebox.getInternalState(nfcCardReader, "nfcCard"), IsNull.notNullValue());

        nfcCardReader.onTagRemoved();
        Assert.assertThat(Whitebox.getInternalState(nfcCardReader, "nfcCard"), IsNull.nullValue());
    }

    @Test
    public void testUpdateActivityWithEnabledAdapter() {
        Mockito.when(nfcAdapter.isEnabled()).thenReturn(true);
        Whitebox.setInternalState(nfcCardReader, "isOnline", false);
        Activity activity = new Activity();
        nfcCardReader.updateActivity(activity);
        Assert.assertTrue(nfcCardReader.isOnline());
    }

    @Test
    public void testUpdateActivityWithDisabledAdapter() {
        Mockito.when(nfcAdapter.isEnabled()).thenReturn(false);
        Whitebox.setInternalState(nfcCardReader, "isOnline", true);
        Activity activity = new Activity();
        nfcCardReader.updateActivity(activity);
        Assert.assertFalse(nfcCardReader.isOnline());
    }

    @Test
    public void testIsInitialized() {
        Whitebox.setInternalState(nfcCardReader, "isInitialized", true);
        Assert.assertTrue(nfcCardReader.isInitialized());
        Whitebox.setInternalState(nfcCardReader, "isInitialized", false);
        Assert.assertFalse(nfcCardReader.isInitialized());
    }

    @Test
    public void testConnect() throws CardException {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", card);
        ICard iCard = nfcCardReader.connect();
        Assert.assertNotNull(iCard);
    }

    @Test(expected = CardException.class)
    public void connectWithoutCardShouldThrowException() throws CardException {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", null);
        nfcCardReader.connect();
        Assert.fail("Exception erwartet, aber nicht bekommen.");
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("NFC CardReader", nfcCardReader.getName());
    }

    @Test
    public void testIsCardPresent() {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", card);
        Mockito.when(card.isCardPresent()).thenReturn(true);
        Assert.assertTrue(nfcCardReader.isCardPresent());

        Mockito.when(card.isCardPresent()).thenReturn(false);
        Assert.assertFalse(nfcCardReader.isCardPresent());

        Whitebox.setInternalState(nfcCardReader, "nfcCard", null);
        Mockito.when(card.isCardPresent()).thenReturn(true);
        Assert.assertFalse(nfcCardReader.isCardPresent());
    }

    @Test
    public void testWaitForCardAbsent() {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", card);
        boolean result = nfcCardReader.waitForCardAbsent(1000);
        Assert.assertTrue(result);
    }

    @Test
    public void testWaitForCardPresent() {
        boolean result = nfcCardReader.waitForCardPresent(1000);
        Assert.assertFalse(result);
    }

    @Test
    public void testRemoveCard() {
        Whitebox.setInternalState(nfcCardReader, "nfcCard", card);
        nfcCardReader.removeCard();
        Assert.assertThat(Whitebox.getInternalState(nfcCardReader, "nfcCard"), IsNull.nullValue());
    }

}
