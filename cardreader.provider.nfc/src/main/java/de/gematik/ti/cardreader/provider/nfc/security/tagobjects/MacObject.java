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

import java.io.ByteArrayOutputStream;

import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.macs.CMac;
import org.spongycastle.crypto.params.KeyParameter;

import de.gematik.ti.utils.primitives.Bytes;

/**
 * Mac object with TAG 8E (cryptographic checksum)
 *
 */
public class MacObject {

    private static final int DO_8E_TAG = 0x0E;
    private static final int BLOCK_SIZE = 16;
    private static final int MAC_SIZE = 8;
    private byte[] header = null;
    private final byte[] kMac;
    private final byte[] ssc;
    private final ByteArrayOutputStream dataOutput;
    private byte[] mac;

    /**
     * Constructor
     *
     * @param header
     *            byte array with extracted header from plain CommandApdu
     * @param commandDataOutput
     *            ByteArrayOutputStream with extracted data and expected length from plain CommandApdu
     * @param kMac
     *            byte array with Session key for message authentication
     * @param ssc
     *            byte array with send sequence counter
     */
    public MacObject(final byte[] header, final ByteArrayOutputStream commandDataOutput, final byte[] kMac, final byte[] ssc) {// NOCS(SAB): Zur Berechnung
        this.header = header;
        this.dataOutput = commandDataOutput;
        this.kMac = kMac;
        this.ssc = ssc;

        calculateMac();
    }

    /**
     * Constructor
     *
     * @param responseDataOutput
     *            ByteArrayOutputStream with extracted data and response status from encrypted ResponseApdu
     * @param kMac
     *            byte array with Session key for message authentication
     * @param ssc
     *            byte array with send sequence counter
     */
    public MacObject(final ByteArrayOutputStream responseDataOutput, final byte[] kMac, final byte[] ssc) {
        this.dataOutput = responseDataOutput;
        this.kMac = kMac;
        this.ssc = ssc;
        calculateMac();
    }

    public DERTaggedObject getTaggedObject() {
        return new DERTaggedObject(false, DO_8E_TAG, new DEROctetString(mac));
    }

    public byte[] getMac() {
        return mac;
    }

    private void calculateMac() {
        mac = new byte[BLOCK_SIZE];
        CMac cbcMac = getCMac(ssc, kMac);

        synchronized (cbcMac) {
            if (header != null) {
                byte[] paddedHeader = Bytes.padData(header, BLOCK_SIZE);
                cbcMac.update(paddedHeader, 0, paddedHeader.length);
            }

            if (dataOutput.size() > 0) {
                byte[] paddedData = Bytes.padData(dataOutput.toByteArray(), BLOCK_SIZE);
                cbcMac.update(paddedData, 0, paddedData.length);
            }

            cbcMac.doFinal(mac, 0);
            mac = Bytes.copyByteArray(mac, 0, MAC_SIZE);
        }
    }

    public static CMac getCMac(final byte[] secureMessagingSSC, final byte[] kMac) {
        CMac cmac = new CMac(new AESEngine());
        cmac.init(new KeyParameter(kMac));
        cmac.update(secureMessagingSSC, 0, secureMessagingSSC.length);
        return cmac;
    }

}
