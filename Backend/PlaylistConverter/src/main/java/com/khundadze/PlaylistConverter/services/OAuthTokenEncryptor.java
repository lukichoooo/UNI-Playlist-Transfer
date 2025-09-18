package com.khundadze.PlaylistConverter.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class OAuthTokenEncryptor {

    private final TextEncryptor encryptor;

    public OAuthTokenEncryptor(
            @Value("${app.encryption.password}") String password,
            @Value("${app.encryption.salt}") String salt) {
        this.encryptor = Encryptors.text(password, salt);
    }

    public String encrypt(String plaintext) {
        return encryptor.encrypt(plaintext);
    }

    public String decrypt(String ciphertext) {
        return encryptor.decrypt(ciphertext);
    }
}
