package com.example.backend.controller;

import com.example.backend.service.ImageService;
import com.example.backend.util.ContentTypes;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ImageController {

    private final ImageService srv;
    private final ReactiveRedisTemplate<String, byte[]> redisTemplate;

    public ImageController(ImageService srv, ReactiveRedisTemplate<String, byte[]> redisTemplate) {
        this.srv = srv;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/image")
    public Mono<ResponseEntity<byte[]>> proxy(@RequestParam("url") String url,
                                              @RequestParam(value="w", required=false) Integer w,
                                              @RequestParam(value="h", required=false) Integer h,
                                              @RequestParam(value="fmt", required=false) String fmt,
                                              @RequestParam(value="q", required=false) Integer q,
                                              @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch) {

        String key = srv.cacheKeyForUrl(url, w, h, fmt, q);
        String etag = '"' + key + '"';

        if (etag.equals(ifNoneMatch)) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build());
        }

        return redisTemplate.opsForValue().get(key)
                .flatMap(cachedBytes -> buildResponse(cachedBytes, etag, fmt))
                .switchIfEmpty(
                        srv.fetchBytes(url)
                                .flatMap(bytes -> srv.transform(bytes, w, h, fmt, q))
                                .flatMap(out -> redisTemplate.opsForValue().set(key, out, Duration.ofDays(30)).then(Mono.just(out)))
                                .flatMap(out -> buildResponse(out, etag, fmt))
                )
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(400)
                        .body(("Bad request: " + ex.getMessage()).getBytes())));
    }

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public Mono<ResponseEntity<byte[]>> upload(@RequestPart("file") MultipartFile file,
                                               @RequestParam(value="w", required=false) Integer w,
                                               @RequestParam(value="h", required=false) Integer h,
                                               @RequestParam(value="fmt", required=false) String fmt,
                                               @RequestParam(value="q", required=false) Integer q,
                                               @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch) {
        try {
            byte[] fb = file.getBytes();
            String key = srv.cacheKeyForUpload(fb, w, h, fmt, q);
            String etag = '"' + key + '"';

            if (etag.equals(ifNoneMatch)) {
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build());
            }

            return redisTemplate.opsForValue().get(key)
                    .flatMap(cachedBytes -> buildResponse(cachedBytes, etag, fmt))
                    .switchIfEmpty(
                            srv.transform(fb, w, h, fmt, q)
                                    .flatMap(out -> redisTemplate.opsForValue().set(key, out, Duration.ofDays(30)).then(Mono.just(out)))
                                    .flatMap(out -> buildResponse(out, etag, fmt))
                    )
                    .onErrorResume(ex -> Mono.just(ResponseEntity.status(400)
                            .body(("Bad request: " + ex.getMessage()).getBytes())));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(400).body(("Bad request: " + e.getMessage()).getBytes()));
        }
    }

    private Mono<ResponseEntity<byte[]>> buildResponse(byte[] bytes, String etag, String fmt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable().getHeaderValue());
        headers.setETag(etag);
        headers.setContentType(ContentTypes.mediaTypeForFormat(fmt));
        headers.setContentLength(bytes.length);
        return Mono.just(new ResponseEntity<>(bytes, headers, HttpStatus.OK));
    }
}
