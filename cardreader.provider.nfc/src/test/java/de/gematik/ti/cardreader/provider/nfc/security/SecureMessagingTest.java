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

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Assert;
import org.junit.Test;

import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.openhealthcard.events.response.entities.PaceKey;
import de.gematik.ti.utils.codec.Hex;

/**
 * Test {@link SecureMessaging}
 * 
 */
public class SecureMessagingTest {

    private final byte[] keyEnc = Hex.decode("68406B4162100563D9C901A6154D2901");
    private final byte[] keyMac = Hex.decode("73FF268784F72AF833FDC9464049AFC9");
    private final PaceKey paceKey = new PaceKey(keyEnc, keyMac);
    private final SecureMessaging secureMessaging = new SecureMessaging(paceKey);
    private CommandApdu commandApdu;
    private byte[] expectedEncryptedApdu;
    private CommandApdu encryptedCommandApdu;

    // test Case 1: |CLA|INS|P1|P2|
    @Test
    public void testEncryptionCase1() throws IOException {

        commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04);

        expectedEncryptedApdu = Hex.decode("0D0203040A8E08D92B4FDDC2BBED8C00");
        encryptedCommandApdu = secureMessaging.encrypt(commandApdu);
        Assert.assertEquals(Hex.encodeHexString(expectedEncryptedApdu), Hex.encodeHexString(encryptedCommandApdu.getBytes()));

        try {
            secureMessaging.encrypt(encryptedCommandApdu);
            Assert.fail("Encrypting an already encrypted Apdu should give an error.");

        } catch (Exception e) {
            // expected
        }
    }

    // test Case 2s: |CLA|INS|P1|P2|LE|
    @Test
    public void testEncryptionCase2s() throws IOException {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);

        commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04, 127);
        expectedEncryptedApdu = Hex.decode("0D0203040D97017F8E0871D8E0418DAE20F300");
        encryptedCommandApdu = secureMessaging.encrypt(commandApdu);
        Assert.assertEquals(Hex.encodeHexString(expectedEncryptedApdu), Hex.encodeHexString(encryptedCommandApdu.getBytes()));
    }

    // test Case 2e: |CLA|INS|P1|P2|EXTLE|
    @Test
    public void testEncryptionCase2e() throws IOException {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);

        commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04, 257);
        expectedEncryptedApdu = Hex.decode("0D02030400000E970201018E089F3EDDFBB1D3971D0000");
        encryptedCommandApdu = secureMessaging.encrypt(commandApdu);
        Assert.assertEquals(Hex.encodeHexString(expectedEncryptedApdu), Hex.encodeHexString(encryptedCommandApdu.getBytes()));
    }

    // test Case 3s. : |CLA|INS|P1|P2|LC|DATA|
    @Test
    public void testEncryptionCase3s() throws IOException {

        byte[] cmdData = new byte[] { 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04, cmdData);
        expectedEncryptedApdu = Hex.decode("0D0203041D871101496C26D36306679609665A385C54DB378E08E7AAD918F260D8EF00");
        encryptedCommandApdu = secureMessaging.encrypt(commandApdu);
        Assert.assertEquals(Hex.encodeHexString(expectedEncryptedApdu), Hex.encodeHexString(encryptedCommandApdu.getBytes()));
    }

    // test Case 4s. : |CLA|INS|P1|P2|LC|DATA|LE|
    @Test
    public void testEncryptionCase4s() throws IOException {
        byte[] cmdData = new byte[] { 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };
        commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04, cmdData, 127);
        // commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04, cmdData, 256);
        expectedEncryptedApdu = Hex.decode("0D02030420871101496C26D36306679609665A385C54DB3797017F8E0863D541F262BD445A00");
        encryptedCommandApdu = secureMessaging.encrypt(commandApdu);
        Assert.assertEquals(Hex.encodeHexString(expectedEncryptedApdu), Hex.encodeHexString(encryptedCommandApdu.getBytes()));
    }

    // test Case 4e: |CLA|INS|P1|P2|EXT('00')|LC|DATA|LE|
    @Test
    public void testEncryptionCase4e() throws IOException {

        byte[] cmdData = new byte[256];
        commandApdu = new CommandApdu(0x01, 0x02, 0x03, 0x04, cmdData, 127);
        expectedEncryptedApdu = Hex.decode("0D02030400012287820111013297D4AA774AB26AF8AD539C0A829BCA4D222D3EE2DB100CF86D7DB5A1FAC12B7623328DEFE3F6FDD41A993A"
                + "C917BC17B364C3DD24740079DE60A3D0231A7185D36A77D37E147025913ADA00CD07736CFDE0DB2E0BB09B75C5773607E54A9D84181A"
                + "CBC6F7726762A8BCE324C0B330548114154A13EDDBFF6DCBC3773DCA9A8494404BE4A5654273F9C2B9EBE1BD615CB39FFD0D3F2A0EEA"
                + "29AA10B810D53EDB550FB741A68CC6B0BDF928F9EB6BC238416AACB4CF3002E865D486CF42D762C86EEBE6A2B25DECE2E88D569854A0"
                + "7D3F146BC134BAF08B6EDCBEBDFF47EBA6AC7B441A1642B03253B588C49B69ABBEC92BA1723B7260DE8AD6158873141AFA7C70CFCF12"
                + "5BA1DF77CA48025D049FCEE497017F8E0856332C83EABDF93C0000");
        encryptedCommandApdu = secureMessaging.encrypt(commandApdu);
        Assert.assertEquals(Hex.encodeHexString(expectedEncryptedApdu), Hex.encodeHexString(encryptedCommandApdu.getBytes()));
    }

    // test Case 1: DO99|DO8E|SW1SW2
    @Test
    public void shouldDecryptDo99Apdu() throws IOException, GeneralSecurityException {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("990290008E08087631D746F872729000"));
        ResponseApdu decryptedApdu = secureMessaging.decrypt(apduToDecrypt);
        ResponseApdu expectedDecryptedApdu = new ResponseApdu(new byte[] { (byte) 0x90, 0x00 });
        Assert.assertEquals(Hex.encodeHexString(expectedDecryptedApdu.getBytes()), Hex.encodeHexString(decryptedApdu.getBytes()));
    }

    // test Case 2: DO87|DO99|DO8E|SW1SW2
    @Test
    public void shouldDecryptDo87Apdu() throws IOException, GeneralSecurityException {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290008E08B7E9ED2A0C89FB3A9000"));
        ResponseApdu decryptedApdu = secureMessaging.decrypt(apduToDecrypt);
        ResponseApdu expectedDecryptedApdu = new ResponseApdu(Hex.decode("05060708090a9000"));
        Assert.assertEquals(Hex.encodeHexString(expectedDecryptedApdu.getBytes()), Hex.encodeHexString(decryptedApdu.getBytes()));
    }

    @Test
    public void decryptShouldFailWithMissingStatusBytes() {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db378E08B7E9ED2A0C89FB3A9000"));
        try {
            secureMessaging.decrypt(apduToDecrypt);
            Assert.fail("Decrypting an APDU without DO99 should fail.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void decryptShouldFailWithMissingStatus() {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290008E08B7E9ED2A0C89FB3A"));
        try {
            secureMessaging.decrypt(apduToDecrypt);
            Assert.fail("Decrypting an APDU with missing status should fail.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void decryptShouldFailWithWrongCCS() {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290008E08A7E9ED2A0C89FB3A9000"));
        try {
            secureMessaging.decrypt(apduToDecrypt);
            Assert.fail("Decrypting an APDU without wrong DO8E should fail.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void decryptShouldFailWithMissingCCS() {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290009000"));
        try {
            secureMessaging.decrypt(apduToDecrypt);
            Assert.fail("Decrypting an APDU without DO8E should fail.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void decryptShouldFailWithNotEncryptedApdu() {
        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(new byte[] { (byte) 0x90, 0x00 });
        try {
            secureMessaging.decrypt(apduToDecrypt);
            Assert.fail("Decrypting an unencrypted APDU should fail.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testDecryption() throws Exception {

        SecureMessaging secureMessaging = new SecureMessaging(paceKey);
        ResponseApdu apduToDecrypt = new ResponseApdu(Hex.decode("990290008E08087631D746F872729000"));
        ResponseApdu decryptedAPDU = secureMessaging.decrypt(apduToDecrypt);
        byte[] expectedDecryptedAPDU = new byte[] { (byte) 0x90, 0x00 };
        Assert.assertEquals(Hex.encodeHexString(expectedDecryptedAPDU), Hex.encodeHexString(decryptedAPDU.getBytes()));
    }
}
