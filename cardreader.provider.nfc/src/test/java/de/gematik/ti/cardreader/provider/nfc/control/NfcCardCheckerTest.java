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
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.nfc.entities.NfcCardReader;

/**
 * Test {@link NfcCardChecker}
 *
 */
public class NfcCardCheckerTest {

    private static final Logger LOG = LoggerFactory.getLogger(NfcCardCheckerTest.class);

    private static final int TIMEOUT = 5000;
    private static NfcCardChecker nfcCardChecker;
    private static ICardReader cardReader;

    @BeforeClass
    public static void init() throws CardException {

        cardReader = Mockito.mock(NfcCardReader.class);
        Mockito.when(((NfcCardReader) cardReader).waitForCardPresent(TIMEOUT)).thenReturn(true);
        Mockito.when(((NfcCardReader) cardReader).waitForCardAbsent(TIMEOUT)).thenReturn(true);
        Mockito.when(cardReader.isCardPresent()).thenReturn(true);
        nfcCardChecker = new NfcCardChecker(cardReader);
    }

    @AfterClass
    public static void close() {
        cardReader = null;
        nfcCardChecker = null;
    }

    @Test
    public void testCardCheckerRun() throws InterruptedException {
        Thread.sleep(100);
        nfcCardChecker.shutdown();
    }

}
