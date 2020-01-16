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
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.card.ICardChannel;
import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ICommandApdu;
import de.gematik.ti.cardreader.provider.api.command.IResponseApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.cardreader.provider.nfc.security.SecureMessaging;
import de.gematik.ti.utils.codec.Hex;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=NfcCardChannel]
 *
 */
public class NfcCardChannel implements ICardChannel {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCard.class);
    private static final CommandApdu MANAGE_CHANNEL_COMMAND_CLOSE = new CommandApdu(0x00, 0x70, 0x80, 0x00);
    private static final int RESPONSE_SUCCESS = 0x9000;
    private static final int LOW_CHANNEL_NUMBER_VALUE = 4;
    private static final int MAX_CHANNEL_NO_VALUE = 20;

    private final int channelNo;
    private final NfcCard nfcCard;
    private boolean isTrustedChannelEstablished = false;
    private SecureMessaging secureMessaging;

    private boolean channelClosed = false;

    NfcCardChannel(NfcCard nfcCard) {
        this(nfcCard, 0);
    }

    NfcCardChannel(NfcCard nfcCard, int channelNo) {
        this.nfcCard = nfcCard;
        this.channelNo = channelNo;

    }

    /**
     * Returns the connected card object
     *
     * @return NfcCard
     */
    @Override
    public ICard getCard() {
        return nfcCard;
    }

    /**
     * Returns the number of channel
     *
     * @return channelNo
     */
    @Override
    public int getChannelNumber() {
        return channelNo;
    }

    @Override
    public boolean isExtendedLengthSupported() {
        boolean isExtendedLengthSupported = getMaxMessageLength() > 255 && getMaxResponseLength() > 255;
        LOG.debug("isExtendedLengthSupported: " + isExtendedLengthSupported);
        return isExtendedLengthSupported;
    }

    @Override
    public int getMaxMessageLength() {
        return 261;
    }

    @Override
    public int getMaxResponseLength() {
        return 261;
    }

    /**
     * Returns the responseApdu after transmitting a commandApdu
     *
     * @param commandApdu
     * @return responseApdu
     * @throws CardException
     */
    @Override
    public IResponseApdu transmit(ICommandApdu commandApdu) throws CardException {

        LOG.debug("Command: " + de.gematik.ti.utils.codec.Hex.encodeHexString(commandApdu.getBytes()));

        ICommandApdu command = commandApdu;
        ICommandApdu encryptedCommand = null;

        ResponseApdu responseApdu = null;
        checkChannelClosed();
        nfcCard.checkCardOpen();
        if (channelNo > 0) {
            command = modifyCommandForLogicalChannel(command);
        }

        if (isTrustedChannelEstablished && secureMessaging == null) {
            secureMessaging = new SecureMessaging(nfcCard.getPaceKey());
            LOG.debug("KEnc: " + Hex.encodeHexString(nfcCard.getPaceKey().getEnc()));
            LOG.debug("KMac: " + Hex.encodeHexString(nfcCard.getPaceKey().getMac()));

            LOG.debug("Trusted Channel is established.");
        }

        if (secureMessaging != null) {
            try {
                encryptedCommand = secureMessaging.encrypt(command);
            } catch (IOException e) {
                LOG.error("encrypting command failed" + e);
                throw new CardException("encrypting command failed", e);
            }
            LOG.debug("encrypted command: " + Hex.encodeHexString(encryptedCommand.getBytes()));

            responseApdu = nfcCard.transceive(encryptedCommand);
            LOG.debug("encrypted response: " + Hex.encodeHexString(responseApdu.getBytes()));

            try {
                responseApdu = secureMessaging.decrypt(responseApdu);
            } catch (IOException e) {
                LOG.error("decrypting response failed" + e);
            } catch (GeneralSecurityException e) {
                LOG.error("decrypting response failed" + e);
            }
            LOG.debug("plain response: " + Hex.encodeHexString(responseApdu.getBytes()));

        } else {
            responseApdu = nfcCard.transceive(command);
        }

        return responseApdu;
    }

    /**
     * Closes the logical channel
     *
     * @throws CardException
     */
    @Override
    public void close() throws CardException {
        if (channelNo != 0) {
            checkChannelClosed();
            nfcCard.checkCardOpen();
            try {
                CommandApdu command = modifyCommandForLogicalChannel(MANAGE_CHANNEL_COMMAND_CLOSE);
                IResponseApdu response = transmit(command);
                if (response.getSW() != RESPONSE_SUCCESS) {
                    throw new CardException("closing logical channel " + channelNo + " failed.");
                }
            } finally {
                channelClosed = true;
            }
        } else {
            throw new CardException("Basic channel cannot be closed.");
        }
    }

    CommandApdu modifyCommandForLogicalChannel(ICommandApdu command) throws CardException {
        byte cla = (byte) command.getCla();
        if (channelNo < LOW_CHANNEL_NUMBER_VALUE) {
            cla &= 0xfc; // NOCS(hoa): set 1st and 2nd bit to 0
            cla |= channelNo; // set 1st and 2nd bit to channelNo
        } else if (channelNo < MAX_CHANNEL_NO_VALUE) {
            cla |= 0x40; // NOCS(hoa): set bit 7 to indicate channelNo > 3, see [ISO 7816-4#5.1.1]
            cla |= channelNo - LOW_CHANNEL_NUMBER_VALUE;
        } else {
            throw new CardException("Channel number: " + channelNo + " not allowed");
        }
        return new CommandApdu(cla, command.getIns(), command.getP1(), command.getP2(), command.getData());
    }

    private void checkChannelClosed() throws CardException {
        if (channelClosed) {
            throw new CardException("Logical Channel " + channelNo + " is already closed");
        }
    }

    public void setTrustedChannelEstablished(boolean trustedChannelEstablished) {
        isTrustedChannelEstablished = trustedChannelEstablished;
    }
}
