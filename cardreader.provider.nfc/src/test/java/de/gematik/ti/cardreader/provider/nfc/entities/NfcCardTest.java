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

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.CardProtocol;
import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.IResponseApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.cardreader.provider.nfc.Whitebox;
import de.gematik.ti.utils.codec.Hex;

/**
 * Test {@link NfcCard}
 *
 */
public class NfcCardTest {

    private static final byte[] ATR_BYTES = new byte[] { (byte) 0x3B, (byte) 0xDD, (byte) 0x00, (byte) 0xFF, (byte) 0x81, (byte) 0x50, (byte) 0xFE, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    private static final CommandApdu MANAGE_CHANNEL_COMMAND_OPEN = new CommandApdu(0x00, 0x70, 0x00, 0x00, 1);
    private static final CommandApdu MANAGE_CHANNEL_COMMAND_CLOSE = new CommandApdu(0x00, 0x70, 0x80, 0x00);
    private static Tag tag;
    private static NfcCard nfcCard;
    private static IsoDep isoDep;
    private static NfcCardChannel basicChannel;
    private static NfcCardChannel logicalChannel;
    private static IResponseApdu responseApdu;

    @BeforeClass
    public static void init() {
        tag = Mockito.mock(Tag.class);
        isoDep = Mockito.mock(IsoDep.class);
        nfcCard = new NfcCard(tag, () -> {
        });

        int[] responseLength = new int[] { 3, 0 };
        byte[] responseBuffer = Hex.decode("019000");
        byte[] response = new byte[responseLength[0]];
        System.arraycopy(responseBuffer, 0, response, 0, responseLength[0]);
        responseApdu = new ResponseApdu(response);

    }

    @Before
    public void setupBefore() {
        basicChannel = new NfcCardChannel(nfcCard);
        Whitebox.setInternalState(nfcCard, "isoDep", isoDep);
        Whitebox.setInternalState(nfcCard, "basicChannel", basicChannel);
        Mockito.when(isoDep.isConnected()).thenReturn(true);
        Mockito.when(isoDep.getMaxTransceiveLength()).thenReturn(0);
        Mockito.when(isoDep.getTimeout()).thenReturn(0);

    }

    @AfterClass
    public static void close() {
    }

    @Test
    public void testGetATR() {
        Mockito.when(isoDep.getHistoricalBytes()).thenReturn(ATR_BYTES);
        byte[] atrBytes = nfcCard.getATR().getBytes();
        Assert.assertEquals(Hex.encodeHexString(ATR_BYTES), Hex.encodeHexString(atrBytes));

        Mockito.when(isoDep.getHistoricalBytes()).thenReturn(null);
        Mockito.when(isoDep.getHiLayerResponse()).thenReturn(ATR_BYTES);
        atrBytes = nfcCard.getATR().getBytes();
        Assert.assertEquals(Hex.encodeHexString(ATR_BYTES), Hex.encodeHexString(atrBytes));

        Mockito.when(isoDep.getHiLayerResponse()).thenReturn(null);
        atrBytes = nfcCard.getATR().getBytes();
        Assert.assertEquals(Hex.encodeHexString(new byte[0]), Hex.encodeHexString(atrBytes));
    }

    @Test
    public void testGetProtocol() {
        CardProtocol expectedProtocol = CardProtocol.T1;
        Assert.assertEquals(expectedProtocol, nfcCard.getProtocol());
    }

    @Test
    public void testOpenBasicChannel() throws CardException {
        Assert.assertEquals(0, nfcCard.openBasicChannel().getChannelNumber());
    }

    @Test
    public void testOpenLogicalChannel() throws Exception {
        int[] responseLength = new int[] { 3, 0 };
        byte[] responseBuffer = Hex.decode("019000");
        byte[] response = new byte[responseLength[0]];
        System.arraycopy(responseBuffer, 0, response, 0, responseLength[0]);
        IResponseApdu responseApdu = new ResponseApdu(response);
        Mockito.when(isoDep.transceive(MANAGE_CHANNEL_COMMAND_OPEN.getBytes())).thenReturn(responseApdu.getBytes());
        logicalChannel = (NfcCardChannel) nfcCard.openLogicalChannel();
        Assert.assertEquals(1, logicalChannel.getChannelNumber());
    }

    @Test
    public void testDisconnect() throws IOException, CardException {
        Mockito.when(isoDep.transceive(MANAGE_CHANNEL_COMMAND_CLOSE.getBytes())).thenReturn(responseApdu.getBytes());
        nfcCard.disconnect(false);
    }

    @Test
    public void testIsCardPresent() {
        Assert.assertTrue(nfcCard.isCardPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void checkCardOpenShouldThrowException() {
        Mockito.when(isoDep.isConnected()).thenReturn(false);
        nfcCard.checkCardOpen();
        Assert.fail("Exception erwartet, aber nicht bekommen.");
    }

    @Test
    public void testIsExtendedLengthSupported() {
        Mockito.when(isoDep.isExtendedLengthApduSupported()).thenReturn(true);
        Assert.assertTrue(nfcCard.isExtendedLengthApduSupported());
        Mockito.when(isoDep.isExtendedLengthApduSupported()).thenReturn(false);
        Assert.assertFalse(nfcCard.isExtendedLengthApduSupported());
    }

}
