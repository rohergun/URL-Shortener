package io.github.rohergun.urlshortener.shortener;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.*;
import java.util.Set;

@Component
public class UrlValidator {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final int MAX_URL_LENGTH = 2048;

    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must not be empty");
        }
        if (url.length() > MAX_URL_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL exceeds maximum character of 2048");
        }

        URI uri;
        try {
            uri = new URI(url).parseServerAuthority();
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL is malformed: " + e.getMessage());
        }
        if (!ALLOWED_SCHEMES.contains(uri.getScheme())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL scheme must be http or https");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must have a valid host");
        }

        if (!isValidHost(uri.getHost())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL host is invalid");
        }
    }

    private boolean isValidHost(String host) {
        if (host.equals("localhost")) return false;

        try {
            InetAddress addr = InetAddress.getByName(host);
            return !addr.isLoopbackAddress()
                    && !addr.isSiteLocalAddress()
                    && !addr.isLinkLocalAddress()
                    && !addr.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return true;
        }
    }

}
