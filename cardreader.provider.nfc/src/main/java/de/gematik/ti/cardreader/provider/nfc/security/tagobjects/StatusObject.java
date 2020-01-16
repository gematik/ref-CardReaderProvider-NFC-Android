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

package de.gematik.ti.cardreader.provider.nfc.security.tagobjects;

import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERTaggedObject;

/**
 * Status object with TAG 99
 *
 */
public class StatusObject {

    private static final int DO_99_TAG = 0x19;

    private final byte[] statusBytes;

    /**
     * Constructor
     *
     * @param statusBytes byte array with extracted response status from encrypted ResponseApdu
     */
    public StatusObject(final byte[] statusBytes) {
        this.statusBytes = statusBytes;
    }

    public DERTaggedObject getTaggedObject() {
        return new DERTaggedObject(false, DO_99_TAG, new DEROctetString(statusBytes));
    }
}
