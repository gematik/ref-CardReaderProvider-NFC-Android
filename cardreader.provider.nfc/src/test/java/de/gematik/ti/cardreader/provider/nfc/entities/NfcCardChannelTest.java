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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.IResponseApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.cardreader.provider.nfc.Whitebox;
import de.gematik.ti.cardreader.provider.nfc.control.NfcCardChecker;
import de.gematik.ti.utils.codec.Hex;

/**
 * Test {@link NfcCardChannel}
 *
 */
public class NfcCardChannelTest {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCardChecker.class);

    private static final CommandApdu MANAGE_CHANNEL_COMMAND_OPEN = new CommandApdu(0x00, 0x70, 0x00, 0x00, 1);
    private static final CommandApdu MANAGE_CHANNEL_COMMAND_CLOSE = new CommandApdu(0x01, 0x70, 0x80, 0x00);
    private static NfcCardChannel nfcBasicChannel;
    private static NfcCardChannel nfcLogicalChannel;

    private static NfcCard nfcCard;
    private static Tag tag;
    private static IsoDep isoDep;
    private IResponseApdu expectedResponseApdu;

    @BeforeClass
    public static void init() {

        tag = Mockito.mock(Tag.class);
        isoDep = Mockito.mock(IsoDep.class);

        nfcCard = new NfcCard(tag, () -> {
        });
    }

    @Before
    public void setupBefore() throws CardException, IOException {
        nfcBasicChannel = new NfcCardChannel(nfcCard);
        Whitebox.setInternalState(nfcCard, "isoDep", isoDep);
        Mockito.when(isoDep.isConnected()).thenReturn(true);
        Mockito.when(isoDep.getMaxTransceiveLength()).thenReturn(0);
        Mockito.when(isoDep.getTimeout()).thenReturn(0);

        int[] responseLength = new int[] { 3, 0 };
        byte[] responseBuffer = Hex.decode("019000");
        byte[] response = new byte[responseLength[0]];
        System.arraycopy(responseBuffer, 0, response, 0, responseLength[0]);
        expectedResponseApdu = new ResponseApdu(response);
        Mockito.when(isoDep.transceive(MANAGE_CHANNEL_COMMAND_OPEN.getBytes())).thenReturn(expectedResponseApdu.getBytes());
        nfcLogicalChannel = (NfcCardChannel) nfcCard.openLogicalChannel();
    }

    @AfterClass
    public static void close() {
    }

    @Test
    public void testGetCard() {
        Assert.assertNotNull(nfcBasicChannel.getCard());
        Assert.assertNotNull(nfcLogicalChannel.getCard());
    }

    @Test
    public void testGetChannelNumber() {
        Assert.assertEquals(0, nfcBasicChannel.getChannelNumber());
        Assert.assertEquals(1, nfcLogicalChannel.getChannelNumber());
    }

    @Test
    public void testIsExtendedLengthSupported() {
        Assert.assertTrue(nfcBasicChannel.isExtendedLengthSupported());
        Assert.assertTrue(nfcLogicalChannel.isExtendedLengthSupported());
    }

    @Test
    public void testGetMaxMessageLength() {
        Assert.assertEquals(261, nfcBasicChannel.getMaxMessageLength());
        Assert.assertEquals(261, nfcLogicalChannel.getMaxMessageLength());
    }

    @Test
    public void testGetMaxResponseLength() {
        Assert.assertEquals(261, nfcBasicChannel.getMaxResponseLength());
        Assert.assertEquals(261, nfcLogicalChannel.getMaxResponseLength());
    }

    @Test
    public void testTransmitCommand() throws CardException, IOException {
        // basicChannel without trusted channel
        Whitebox.setInternalState(nfcCard, "basicChannel", nfcBasicChannel);
        IResponseApdu responseApdu = nfcBasicChannel.transmit(MANAGE_CHANNEL_COMMAND_OPEN);
        Assert.assertEquals(expectedResponseApdu, responseApdu);

        // logical channel without trusted channel
        final CommandApdu logicalChannelCommand = new CommandApdu(0x01, 0x70, 0x00, 0x00);
        Mockito.when(isoDep.transceive(logicalChannelCommand.getBytes())).thenReturn(expectedResponseApdu.getBytes());

        responseApdu = nfcLogicalChannel.transmit(MANAGE_CHANNEL_COMMAND_OPEN);
        Assert.assertEquals(expectedResponseApdu, responseApdu);
    }

    @Test(expected = CardException.class)
    public void testCloseBasicChannel() throws CardException, IOException {
        nfcBasicChannel.close();
        Assert.fail("Exception erwartet, aber nicht bekommen.");
    }

}
