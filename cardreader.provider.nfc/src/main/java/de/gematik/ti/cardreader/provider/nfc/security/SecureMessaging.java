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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.DERTaggedObject;

import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ICommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.cardreader.provider.nfc.security.tagobjects.DataObject;
import de.gematik.ti.cardreader.provider.nfc.security.tagobjects.LengthObject;
import de.gematik.ti.cardreader.provider.nfc.security.tagobjects.MacObject;
import de.gematik.ti.cardreader.provider.nfc.security.tagobjects.StatusObject;
import de.gematik.ti.openhealthcard.events.response.entities.PaceKey;
import de.gematik.ti.utils.codec.Hex;
import de.gematik.ti.utils.primitives.Bytes;

/**
 * include::{userguide}/NFCCRP_Overview.adoc[tag=SecureMessaging]
 *
 */

public class SecureMessaging {

    private static final Logger LOG = LoggerFactory.getLogger(SecureMessaging.class);

    private static final byte SECURE_MESSAGING_COMMAND = (byte) 0x0C;
    private static final byte[] PADDING_INDICATOR = new byte[] { (byte) 0x01 };

    private static final int BLOCK_SIZE = 16;
    private static final int MAC_SIZE = 8;
    private static final int STATUS_SIZE = (byte) 0x02;
    private static final int MIN_RESPONSE_SIZE = 12;
    private static final int HEADER_SIZE = 4;
    private static final int ENCRYPT_MODE = 1;
    private static final int DECRYPT_MODE = 2;
    private static final int DO_81_TAG = 0x81;
    private static final int DO_87_TAG = 0x87;
    private static final int DO_99_TAG = 0x99;
    private static final int DO_8E_TAG = 0x8E;
    private static final int LENGTH_TAG = 0x80;
    private static final int BYTE_MASK = 0x0F;
    private static final int BYTE_MASK_FF = 0xFF;
    private static final String MALFORMED_SECURE_MESSAGING_APDU = "Malformed Secure Messaging APDU";

    private final byte[] secureMessagingSSC;
    private final PaceKey paceKey;

    private int le = -1;
    private byte[] header;

    private byte[] statusBytes;
    private byte[] dataBytes;
    private byte[] macBytes;
    private MacObject commandMacObject;
    private MacObject responseMacObject;

    /**
     * Constructor for a new secure messaging instance
     *
     * @param paceKey
     *            Session key for encoding and Session key for message authentication
     */
    public SecureMessaging(final PaceKey paceKey) { // NOCS(SAB): Zugriff auf SecureMessaging durch cardreader.provider.nfc
        this.paceKey = paceKey;
        secureMessagingSSC = new byte[BLOCK_SIZE];
    }

    /**
     * Encrypts a plain APDU
     *
     * @param commandApdu
     *            plain Command APDU
     * @return encrypted Command APDU
     * @throws IOException
     *             if an error occurred
     */
    public CommandApdu encrypt(final ICommandApdu commandApdu) throws IOException {
        ByteArrayOutputStream commandDataOutput = new ByteArrayOutputStream();
        byte[] apduToEncrypt = commandApdu.getBytes();

        LOG.debug("Plain Apdu vor dem Verschlüsseln: " + Hex.encodeHexString(apduToEncrypt));

        incrementSSC();
        checkCommandApduSize(apduToEncrypt);

        header = Bytes.copyByteArray(apduToEncrypt, 0, HEADER_SIZE);
        setSecureMessagingCommand();

        byte[] data = commandApdu.getData();

        if (data.length > 0) {
            data = Bytes.padData(data, BLOCK_SIZE);
            data = encryptData(data);
            data = Bytes.concatNullables(PADDING_INDICATOR, data);
            DataObject dataObject = new DataObject(data);

            commandDataOutput.write(dataObject.getTaggedObject().getEncoded());
        }

        if (commandApdu.getNe() != null) {
            le = commandApdu.getNe();
            LengthObject lengthObject = new LengthObject(le);
            commandDataOutput.write(lengthObject.getTaggedObject().getEncoded());
        }

        commandMacObject = new MacObject(header, commandDataOutput, paceKey.getMac(), secureMessagingSSC);
        return createEncryptedCommand(commandDataOutput, commandMacObject.getTaggedObject(), header);

    }

    /**
     * Decrypts an encrypted Response APDU
     *
     * @param responseApdu
     *            encrypted Response APDU
     * @return plain Response APDU
     * @throws IOException
     *             if an error occurred
     * @throws GeneralSecurityException
     *             if an error occurred
     */
    public ResponseApdu decrypt(final ResponseApdu responseApdu) throws IOException, GeneralSecurityException {
        byte[] responseApduBytes = responseApdu.getBytes();
        statusBytes = new byte[2];
        dataBytes = null;
        macBytes = new byte[MAC_SIZE];

        ByteArrayInputStream responseApduInput = new ByteArrayInputStream(responseApduBytes);
        ByteArrayOutputStream responseDataOutput = new ByteArrayOutputStream();

        checkResponseApduSize(responseApduBytes);
        incrementSSC();

        int dataTag = getResponseObjects(responseApduInput);

        if (dataBytes != null) {

            LOG.debug("DataBytes: + " + Hex.encodeHexString(dataBytes));
            DataObject dataObject = new DataObject(dataBytes, dataTag);

            responseDataOutput.write(dataObject.getTaggedObject().getEncoded());
        }

        StatusObject statusObject = new StatusObject(statusBytes);
        responseDataOutput.write(statusObject.getTaggedObject().getEncoded());

        responseMacObject = new MacObject(responseDataOutput, paceKey.getMac(), secureMessagingSSC);

        if (!(verifyMac(responseMacObject.getMac(), macBytes))) {
            throw new GeneralSecurityException("Secure Messaging MAC verification failed");
        }

        return createDecryptedResponse(dataTag);
    }

    private void incrementSSC() {

        for (int i = secureMessagingSSC.length - 1; i >= 0; i--) {
            secureMessagingSSC[i]++;
            if (secureMessagingSSC[i] != 0) {
                break;
            }
        }
    }

    private void checkCommandApduSize(final byte[] apduToEncrypt) {
        if (apduToEncrypt.length < HEADER_SIZE) {
            throw new IllegalArgumentException("apdu must be at least 4 bytes long");
        }
    }

    private void setSecureMessagingCommand() {
        if (header[0] == (byte) (header[0] | SECURE_MESSAGING_COMMAND)) {
            throw new IllegalArgumentException("Malformed APDU.");
        } else {
            header[0] = (byte) (header[0] | SECURE_MESSAGING_COMMAND);
        }
    }

    private CommandApdu createEncryptedCommand(final ByteArrayOutputStream outputStream, final DERTaggedObject do8E, final byte[] header) throws IOException {

        byte[] secureData = Bytes.concatNullables(outputStream.toByteArray(), do8E.getEncoded());

        int ne = 0;

        if (le > CommandApdu.EXPECTED_LENGTH_WILDCARD_SHORT) {
            ne = CommandApdu.EXPECTED_LENGTH_WILDCARD_EXTENDED;
        }
        return new CommandApdu(header[0] & BYTE_MASK_FF, header[1] & BYTE_MASK_FF, header[2] & BYTE_MASK_FF, header[3] & BYTE_MASK_FF, secureData, ne);
    }

    private byte[] encryptData(final byte[] paddedData) {
        Cipher cipher;
        byte[] encryptedData = new byte[0];
        try {
            cipher = getCipher(ENCRYPT_MODE);
            if (cipher != null) {
                encryptedData = cipher.doFinal(paddedData);
            } else {
                LOG.error("encrypt data failed, Cipher not found!");
            }

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            LOG.error("encrypt data failed " + e);
        }
        return encryptedData;
    }

    private void checkResponseApduSize(final byte[] responseApduBytes) {
        if (responseApduBytes.length < MIN_RESPONSE_SIZE) {
            throw new IllegalArgumentException("Malformed Secure Messaging APDU.");
        }
    }

    private int getResponseObjects(final ByteArrayInputStream inputStream) throws IOException {
        byte tag = (byte) inputStream.read();
        int i;
        int dataTag = 0x00;

        if (tag == (byte) DO_81_TAG || tag == (byte) DO_87_TAG) {
            dataTag = tag;
            int size = inputStream.read();
            if (size > LENGTH_TAG) {
                byte[] sizeBytes = new byte[size & BYTE_MASK];
                i = inputStream.read(sizeBytes, 0, sizeBytes.length);
                checkExpectedLength(i, sizeBytes.length);
                size = new BigInteger(1, sizeBytes).intValue();
            }

            dataBytes = new byte[size];

            i = inputStream.read(dataBytes, 0, dataBytes.length);
            checkExpectedLength(i, dataBytes.length);
            tag = (byte) inputStream.read();
        }

        if (tag == (byte) DO_99_TAG) {
            if (inputStream.read() == STATUS_SIZE) {
                i = inputStream.read(statusBytes, 0, STATUS_SIZE);
                checkExpectedLength(i, STATUS_SIZE);
                tag = (byte) inputStream.read();
            }
        } else {
            throw new IOException(MALFORMED_SECURE_MESSAGING_APDU);
        }

        if (tag == (byte) DO_8E_TAG) {
            if (inputStream.read() == (byte) MAC_SIZE) {
                i = inputStream.read(macBytes, 0, MAC_SIZE);
                checkExpectedLength(i, MAC_SIZE);
            }
        } else {
            throw new IOException(MALFORMED_SECURE_MESSAGING_APDU);
        }
        if (inputStream.available() != 2) {
            throw new IOException(MALFORMED_SECURE_MESSAGING_APDU);
        }
        return dataTag;
    }

    private boolean verifyMac(final byte[] mac, final byte[] macObject) throws GeneralSecurityException {
        LOG.debug("calculated mac: " + Hex.encodeHexString(mac));
        LOG.debug("extracted mac: " + Hex.encodeHexString(macObject));

        if (mac == null || macObject == null || (mac.length != macObject.length)) {
            throw new GeneralSecurityException("Secure Messaging MAC verification failed");
        }
        return java.util.Arrays.equals(mac, macObject);
    }

    private ResponseApdu createDecryptedResponse(final int dataTag) throws BadPaddingException, IllegalBlockSizeException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (dataBytes != null) {
            if (dataTag == (byte) DO_87_TAG) {
                dataBytes = removePaddingIndicator(dataBytes);
                Cipher cipher = getCipher(DECRYPT_MODE);
                if (cipher != null) {
                    byte[] dataDecrypted = cipher.doFinal(dataBytes);
                    outputStream.write(Bytes.unPadData(dataDecrypted));
                    LOG.debug("data decrypted: " + Hex.encodeHexString(dataDecrypted));
                } else {
                    LOG.error("decrypt data response failed, Cipher not found!");
                    // TODO send Error with EventBus
                }
            } else {
                outputStream.write(dataBytes);
            }

        }
        outputStream.write(statusBytes);
        return new ResponseApdu(outputStream.toByteArray());
    }

    private byte[] removePaddingIndicator(byte[] dataBytes) {
        byte[] ret = new byte[dataBytes.length - 1];
        System.arraycopy(dataBytes, 1, ret, 0, ret.length);
        return ret;
    }

    private Cipher getCipher(final int mode) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            Key key = new SecretKeySpec(paceKey.getEnc(), "AES");
            byte[] iv = getCipherIV();
            AlgorithmParameterSpec aps = new IvParameterSpec(iv);
            cipher.init(mode, key, aps);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            LOG.error("encrypt or decrypt data failed " + e);
        }
        return cipher;
    }

    private byte[] getCipherIV() {
        Cipher cipher;
        byte[] iv = new byte[0];
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            Key key = new SecretKeySpec(paceKey.getEnc(), "AES");
            cipher.init(SecureMessaging.ENCRYPT_MODE, key);
            iv = cipher.doFinal(secureMessagingSSC);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            LOG.error("encrypt or decrypt data failed " + e);
        }
        return iv;
    }

    private void checkExpectedLength(final int i, final int expected) throws IOException {
        if (i < expected) {
            throw new IOException(MALFORMED_SECURE_MESSAGING_APDU);
        }
    }
}
