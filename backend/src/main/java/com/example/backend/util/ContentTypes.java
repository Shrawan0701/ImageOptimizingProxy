
package com.example.backend.util;

import org.springframework.http.MediaType;

public class ContentTypes {
    public static MediaType mediaTypeForFormat(String fmt) {
        if (fmt == null) return MediaType.IMAGE_JPEG;
        switch (fmt.toLowerCase()) {
            case "jpg": case "jpeg": return MediaType.IMAGE_JPEG;
            case "png": return MediaType.IMAGE_PNG;
            case "webp": return MediaType.parseMediaType("image/webp");
            case "avif": return MediaType.parseMediaType("image/avif");
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
