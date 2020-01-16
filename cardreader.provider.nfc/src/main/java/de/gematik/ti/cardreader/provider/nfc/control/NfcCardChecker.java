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

import de.gematik.ti.cardreader.provider.api.CardEventTransmitter;
import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.nfc.entities.NfcCardReader;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=NfcCardChecker]
 *
 */
public class NfcCardChecker extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCardChecker.class);
    private static final int TIMEOUT = 5000;

    private final ICardReader cardReader;
    private volatile boolean active = true;
    private final CardEventTransmitter cardEventTransmitter;
    private boolean currentCardState;

    public NfcCardChecker(final ICardReader cardReader) {
        this.cardReader = cardReader;
        cardEventTransmitter = NfcCardReaderController.getInstance().createCardEventTransmitter(cardReader);
        start();
    }

    /**
     * Stop the Checker and could not restarted
     */
    public void shutdown() {
        active = false;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(TIMEOUT);
        } catch (final InterruptedException e) {
            LOG.debug("Start Timer error", e);
        }
        currentCardState = isCardPresent();
        while (active) {
            if (currentCardState) {
                LOG.trace("NFCReader: waitForCardAbsent");
                final boolean absent = ((NfcCardReader) cardReader).waitForCardAbsent(TIMEOUT);
                LOG.trace("NFCReader: waitForCardAbsent return with " + absent);
            } else {
                LOG.trace("NFCReader: waitForCardPresent");
                final boolean present = ((NfcCardReader) cardReader).waitForCardPresent(TIMEOUT);
                LOG.trace("NFCReader: waitForCardPresent return with " + present);
            }
            if (active) {
                checkCardStateAndSendEvent();
            }
        }

    }

    private void checkCardStateAndSendEvent() {
        final boolean cardPresent = isCardPresent();
        if (currentCardState != cardPresent) {
            if (!cardPresent) {
                LOG.debug("NFC Card Absent (cardPresent=" + cardPresent + ")");
                cardEventTransmitter.informAboutCardAbsent();
                ((NfcCardReader) cardReader).removeCard();
            } else {
                LOG.debug("NFC Card Present (cardPresent=" + cardPresent + ")");
            }
            currentCardState = cardPresent;
        }
    }

    private boolean isCardPresent() {
        try {
            return cardReader.isCardPresent();
        } catch (final CardException e) {
            LOG.error("Card State Unknown", e);
            return false;
        }
    }
}
