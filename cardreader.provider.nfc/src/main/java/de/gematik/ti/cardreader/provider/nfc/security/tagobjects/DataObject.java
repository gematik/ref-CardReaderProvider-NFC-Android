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
 * Data object with TAG 87
 *
 */
public class DataObject {
    private static final int DO_87_TAG = 0x07;
    private static final int DO_81_EXTRACTED_TAG = 0x81;
    private static final int DO_81_TAG = 0x01;
    private int tag;

    private final byte[] dataBytes;

    /**
     * Constructor
     *
     * @param dataBytes
     *            byte array with extracted data from plain CommandApdu or encrypted ResponseApdu
     */
    public DataObject(final byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    /**
     * Constructor
     *
     * @param dataBytes
     *            byte array with extracted data from plain CommandApdu or encrypted ResponseApdu
     * @param tag
     *            int with extracted tag number
     */
    public DataObject(final byte[] dataBytes, final int tag) {
        this.dataBytes = dataBytes;
        this.tag = tag;
    }

    public DERTaggedObject getTaggedObject() {
        if (tag == (byte) DO_81_EXTRACTED_TAG) {
            return new DERTaggedObject(false, DO_81_TAG, new DEROctetString(dataBytes));
        } else {
            return new DERTaggedObject(false, DO_87_TAG, new DEROctetString(dataBytes));
        }
    }
}
