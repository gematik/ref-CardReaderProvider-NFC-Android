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

import javax.inject.Inject;
import javax.inject.Singleton;

import android.content.Context;

import de.gematik.ti.cardreader.access.ICardReaderControllerInitializer;
import de.gematik.ti.cardreader.provider.api.ICardReaderController;
import de.gematik.ti.openhealthcard.common.interfaces.IAndroidCardReaderController;

/**
 * Initializer for CardReaderController to set the android application context
 *
 */
@Singleton
public class ContextCardReaderControllerInitializer implements ICardReaderControllerInitializer {

    private final Context context;

    /**
     * Create a Instance with the given android application context
     *
     * @param context
     *            - android application context
     */
    @Inject
    public ContextCardReaderControllerInitializer(final Context context) {
        this.context = context;
    }

    /**
     * Initialize the card reader controller if it is an android card reader controller
     * 
     * @param iCardReaderController
     */
    @Override
    public void initializeCardReaderController(ICardReaderController iCardReaderController) {
        if (iCardReaderController instanceof IAndroidCardReaderController) {
            ((IAndroidCardReaderController) iCardReaderController).setContext(context);
        }

    }
}
