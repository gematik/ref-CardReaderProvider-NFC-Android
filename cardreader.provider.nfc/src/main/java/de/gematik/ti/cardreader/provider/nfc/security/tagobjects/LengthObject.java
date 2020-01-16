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

import de.gematik.ti.cardreader.provider.api.command.CommandApdu;

/**
 * Length object with TAG 97
 *
 */
public class LengthObject {

    private static final int DO_97_TAG = 0x17;
    private static final int BYTE_MASK = 0xFF;
    private static final int BYTE_VALUE = 8;

    private static final byte[] NULL = new byte[] { 0x00 };

    private byte[] leData = new byte[0];

    /**
     * Constructor
     *
     * @param le
     *            extracted expected length from plain CommandApdu
     */
    public LengthObject(final int le) {

        if (le >= 0) {
            if (le == CommandApdu.EXPECTED_LENGTH_WILDCARD_SHORT) {
                leData = NULL;
            } else if (le > CommandApdu.EXPECTED_LENGTH_WILDCARD_SHORT) {
                leData = new byte[] { (byte) ((le >> BYTE_VALUE) & BYTE_MASK), (byte) (le & BYTE_MASK) };
            } else {
                leData = new byte[] { (byte) le };
            }
        }
    }

    public DERTaggedObject getTaggedObject() {
        return new DERTaggedObject(false, DO_97_TAG, new DEROctetString(leData));
    }
}
