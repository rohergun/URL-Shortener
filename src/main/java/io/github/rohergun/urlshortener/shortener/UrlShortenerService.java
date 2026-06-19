package io.github.rohergun.urlshortener.shortener;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Helper;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {
    private final UrlRepository urlRepository;
    private final UrlValidator validator;

    @Value("${app.base-url}")
    private String baseUrl;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String shorten(String longUrl) {
        validator.validate(longUrl);
        String code = generateCode();

        while (urlRepository.findByShortCode(code).isPresent()) {
            code = generateCode();
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode(code);
        urlMapping.setLongUrl(longUrl);
        urlMapping.setCreatedAt(Instant.now().getEpochSecond());
        urlMapping.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond());

        urlRepository.save(urlMapping);
        return baseUrl + "/" + code;
    }

    public String resolve(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(UrlMapping::getLongUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL Not found"));
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
