package com.khundadze.PlaylistConverter.oauthTokenTest;

import com.khundadze.PlaylistConverter.services.OAuthTokenEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OAuthTokenEncryptorTest {

    private OAuthTokenEncryptor encryptor;

    @BeforeEach
    void setUp() {
        // Use test password and salt
        String testPassword = "test-secret-key-1234567890";
        String testSalt = "12345678"; // must be 8 bytes
        encryptor = new OAuthTokenEncryptor(testPassword, testSalt);
    }

    @Test
    void testEncryptDecrypt_returnsOriginal() {
        String original = "my-secret-token-123";

        String encrypted = encryptor.encrypt(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted, "Encrypted text should differ from original");

        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(original, decrypted, "Decrypted text should match original");
    }

    @Test
    void testEncryptDecrypt_sameInput() {
        String original = "same-token";

        String encrypted1 = encryptor.encrypt(original);
        String encrypted2 = encryptor.encrypt(original);

        // Decrypt both
        String decrypted1 = encryptor.decrypt(encrypted1);
        String decrypted2 = encryptor.decrypt(encrypted2);

        // Both should match the original
        assertEquals(original, decrypted1, "Decrypted1 should match original");
        assertEquals(original, decrypted2, "Decrypted2 should match original");
    }


    @Test
    void testDecryptInvalidData_throwsException() {
        String invalidData = "invalid-encrypted-text";

        assertThrows(RuntimeException.class, () -> encryptor.decrypt(invalidData));
    }

    @Test
    void testEmptyStringEncryption() {
        String original = "";

        String encrypted = encryptor.encrypt(original);
        assertNotNull(encrypted);

        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
}
