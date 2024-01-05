package ru.scraping.data.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.scraping.data.exceptions.ServiceException;
import ru.scraping.data.service.HashService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class HashServiceImpl implements HashService {

    private static final String HASH_SERVICE_ERROR_MSG = "Unexpected hash service error";

    @Override
    public String hash(String toHashStr) {
        try {
            return hashToBase64(toHashStr, Optional.empty());
        } catch (Exception ex) {
            log.error(HASH_SERVICE_ERROR_MSG);
            throw new ServiceException(HASH_SERVICE_ERROR_MSG, ex);
        }
    }

    private String hashToHex(String toHashStr, Optional<String> salt) throws NoSuchAlgorithmException {
        byte[] bytes = hash(toHashStr, salt);

        StringBuilder sp = new StringBuilder();

        for (byte aByte : bytes) {
            sp.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sp.toString().toUpperCase();
    }

    private String hashToBase64(String hashMe, Optional<String> salt) throws NoSuchAlgorithmException {
        return Base64.getEncoder()
                     .encodeToString(hash(hashMe, salt))
                     .toUpperCase();
    }

    private byte[] hash(String hashMe, Optional<String> salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");

        md.update(hashMe.getBytes(StandardCharsets.UTF_8));

        salt.ifPresent(s -> {
            try {
                md.update(s.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return md.digest();
    }
}
