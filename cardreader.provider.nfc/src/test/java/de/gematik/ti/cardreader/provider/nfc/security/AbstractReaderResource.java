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

package de.gematik.ti.cardreader.provider.nfc.security;

import java.util.Collection;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.cardreader.access.CardReaderControllerManager;
import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.nfc.control.MockContext;

public abstract class AbstractReaderResource extends ExternalResource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractReaderResource.class);

    protected static Collection<ICardReader> cardReaders;
    public ICardReader terminal = null;

    @Override
    protected void before() throws Throwable {
        LOG.debug("Looking for available card reader");
        CardReaderControllerManager.getInstance().getCardReaders();
    }

    @Override
    protected void after() {
        // do nothing
    }

    protected void initializeReader(final String readerIdentifier) throws Exception {
        initializeReader(readerIdentifier, 0);
    }

    protected void initializeReader(final String readerIdentifier, final int portNumber) throws Exception {
        CardReaderControllerManager.getInstance().initialize(new ContextCardReaderControllerInitializer(new MockContext()));

        cardReaders = CardReaderControllerManager.getInstance().getCardReaders();
        LOG.info("Found following card readers: ");
        for (ICardReader i : cardReaders) {
            LOG.info("Name:     " + i.getName());
            LOG.info("toString: " + i.toString());
        }

        LOG.info("\nLooking for available card reader matching the criteria...");

        for (ICardReader i : cardReaders) {
            LOG.debug(String.format("card reader " + ": %-8s: ", i));
            if (i.toString().toLowerCase().contains(readerIdentifier.toLowerCase()) && (0 == portNumber || i.getName().contains(String.valueOf(portNumber)))
                    || i.getName().contains(readerIdentifier)) {
                LOG.info("Found card reader by name \"" + readerIdentifier + "\"...");
                if (i.isCardPresent()) {
                    LOG.info("   ...and card is present.");
                    terminal = i;
                    break;
                } else {
                    LOG.info("   ...but no card is present.");
                }
            }
        }

        if (null == terminal) {
            throw new IllegalStateException("No card reader with present card found");
        }

    }

}
