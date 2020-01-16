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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import de.gematik.ti.cardreader.provider.api.card.Atr;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.CardProtocol;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.card.ICardChannel;
import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ICommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.openhealthcard.events.control.RequestTransmitter;
import de.gematik.ti.openhealthcard.events.response.callbacks.IPaceKeyResponseListener;
import de.gematik.ti.openhealthcard.events.response.entities.PaceKey;
import de.gematik.ti.utils.codec.Hex;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=NfcCard]
 *
 */
public class NfcCard implements ICard {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCard.class);
    private static final int ISO_DEP_TIMEOUT = 5000;

    private static final CommandApdu MANAGE_CHANNEL_COMMAND_OPEN = new CommandApdu(0x00, 0x70, 0x00, 0x00, 1);
    private static final CommandApdu MANAGE_CHANNEL_COMMAND_CLOSE = new CommandApdu(0x00, 0x70, 0x80, 0x00);
    private static final int RESPONSE_SUCCESS = 0x9000;

    private IsoDep isoDep;
    private NfcCardChannel basicChannel = null;
    private boolean isExtendedLengthApduSupported = false;
    private PaceKey paceKey;
    private final ICardPresentCallBack presentCallBack;

    /**
     * Constructor
     *
     * @param tag
     *            discovered Tag object
     */
    public NfcCard(final Tag tag, final ICardPresentCallBack presentCallBack) {
        this.presentCallBack = presentCallBack;

        LOG.debug("NFCTag " + Hex.encodeHexString(tag.getId()) + " " + tag.toString());
        isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                LOG.debug("try isoDep.connect()");
                isoDep.connect();
                LOG.debug("isoDep.getMaxTransceiveLength(): " + isoDep.getMaxTransceiveLength());
                LOG.debug("original value for isoDep.getTimeout(): " + isoDep.getTimeout());
                isoDep.setTimeout(ISO_DEP_TIMEOUT);
                LOG.debug("New value for isoDep.getTimeout(): " + isoDep.getTimeout());
                basicChannel = new NfcCardChannel(this);
                new RequestTransmitter().requestPaceKey(new PaceKeyResponseListener(), this);
            } catch (final IOException e) {
                LOG.error("Connect: IsoDep unsuccessful connect", e);
            }
        } else {
            LOG.debug("IsoDep == null");
        }
    }

    /**
     * Returns the ATR of this card
     *
     * @return the ATR of this card.
     */
    @Override
    public Atr getATR() {
        if (isoDep != null) {
            byte[] data = isoDep.getHistoricalBytes();
            LOG.debug("data: " + data);
            if (data == null) {
                data = isoDep.getHiLayerResponse();
                LOG.debug("data " + data);
            }
            if (data != null) {
                return new Atr(data);
            }
        }
        return new Atr(new byte[0]);
    }

    /**
     * Returns the protocol in use for this card, for example "T=0" or "T=1".
     *
     * @return the protocol in use for this card, for example "T=0" or "T=1".
     */
    @Override
    public CardProtocol getProtocol() {
        return CardProtocol.T1;
    }

    /**
     * Returns the CardChannel for the basic logical channel. The basic logical channel has a channel number of 0.
     *
     * @return CardChannel for the basic logical channel. The basic logical channel has a channel number of 0.
     */
    @Override
    public ICardChannel openBasicChannel() throws CardException {
        try {
            checkCardOpen();
        } catch (final Exception e) {
            throw new CardException(e.getMessage(), e);
        }
        return basicChannel;
    }

    /**
     * Opens a new logical channel to the card and returns it.
     *
     * @return a new logical channel to the card
     * @throws CardException
     */
    @Override
    public ICardChannel openLogicalChannel() throws CardException {
        checkCardOpen();

        final ResponseApdu response = transceive(MANAGE_CHANNEL_COMMAND_OPEN);
        if (response.getSW() != RESPONSE_SUCCESS) {
            throw new CardException("openLogicalChannel failed, response code: " + String.format("0x%04x", response.getSW()));
        }
        final int channelNo = response.getData()[0];

        return new NfcCardChannel(this, channelNo);
    }

    protected ResponseApdu transceive(final ICommandApdu commandApdu) throws CardException {
        checkCardOpen();
        final byte[] commandBytes = commandApdu.getBytes();
        final ResponseApdu responseApdu;
        try {
            responseApdu = new ResponseApdu(isoDep.transceive(commandBytes));
        } catch (final IllegalArgumentException e) {
            throw new CardException("Transceive APDU IllegalArgumentException", e);
        } catch (final IOException e) {
            throw new CardException("Transceive APDU IOException", e);
        }
        return responseApdu;
    }

    /**
     * Disconnects the connection with this card and reset the isoDep connection.
     *
     * @param reset
     * @throws CardException
     */
    @Override
    public void disconnect(final boolean reset) throws CardException {
        checkCardOpen();
        final ResponseApdu responseApdu;
        responseApdu = transceive(MANAGE_CHANNEL_COMMAND_CLOSE);
        if (responseApdu.getSW() != RESPONSE_SUCCESS) {
            throw new CardException("RESET CHANNEL unexpected response: " + responseApdu.getSW());
        }
        if (reset) {
            try {
                final Tag tag = isoDep.getTag();
                isoDep.close();
                isoDep = IsoDep.get(tag);
                isoDep.connect();
                isoDep.setTimeout(ISO_DEP_TIMEOUT);
            } catch (final IOException e) {
                throw new CardException("unsuccessful reset of isodep connection", e);
            }
        }
    }

    /**
     * Returns if card is present
     *
     * @return true if IsoDep not null and IsoDep is connected false if IsoDep is null or IsoDep is not connected
     * @throws CardException
     */
    public boolean isCardPresent() {
        final boolean result = isoDep.isConnected();
        LOG.debug("isCardPresent() = " + result);
        return result;
    }

    /**
     * Returns if extended length on IsoDep is supported
     *
     * @return true if extended length is supported false if extended length is not supported
     */
    public boolean isExtendedLengthApduSupported() {
        if (isoDep != null) {
            isExtendedLengthApduSupported = isoDep.isExtendedLengthApduSupported();
        }
        return isExtendedLengthApduSupported;
    }

    void checkCardOpen() {
        final boolean cardOpen = isCardPresent();
        if (!cardOpen) {
            throw new IllegalStateException("card is not connected");
        }
    }

    public PaceKey getPaceKey() {
        return paceKey;
    }

    private class PaceKeyResponseListener implements IPaceKeyResponseListener {

        @Override
        public void handlePaceKey(final PaceKey paceKey) {
            NfcCard.this.paceKey = paceKey;
            basicChannel.setTrustedChannelEstablished(true);
            presentCallBack.inform();
        }

    }

    interface ICardPresentCallBack {
        void inform();
    }
}
