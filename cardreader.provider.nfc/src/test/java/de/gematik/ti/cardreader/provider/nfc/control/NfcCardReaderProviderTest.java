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

import de.gematik.ti.cardreader.provider.api.ICardReaderController;
import de.gematik.ti.cardreader.provider.api.entities.IProviderDescriptor;

/**
 * Test {@link NfcCardReaderProvider}
 *
 */
public class NfcCardReaderProviderTest {
    private static NfcCardReaderProvider nfcCardReaderProvider;

    @BeforeClass
    public static void init() {
        nfcCardReaderProvider = new NfcCardReaderProvider();
    }

    @AfterClass
    public static void close() {
        nfcCardReaderProvider = null;
    }

    @Test
    public void testGetCardReaderController() {
        ICardReaderController nfcCardReaderController = nfcCardReaderProvider.getCardReaderController();
        Assert.assertNotNull(nfcCardReaderController);
    }

    @Test
    public void testGetDescriptor() {
        IProviderDescriptor descriptor = nfcCardReaderProvider.getDescriptor();
        String expectedProviderName = "Gematik NFC-Provider";
        String expectedProviderDesciption = "Dieser Provider stellt die NFC Kartenleser bereit.";
        String expectedProviderLicense = "Gematik interner Gebrauch, details tbd";

        Assert.assertEquals(expectedProviderName, descriptor.getName());
        Assert.assertEquals(expectedProviderDesciption, descriptor.getDescription());
        Assert.assertEquals(expectedProviderLicense, descriptor.getLicense());
    }
}
