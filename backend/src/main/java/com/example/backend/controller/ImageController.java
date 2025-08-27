
package com.example.backend.controller;

import com.example.backend.service.ImageService;
import com.example.backend.util.ContentTypes;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ImageController {

    private final ImageService srv;

    public ImageController(ImageService srv) { this.srv = srv; }

    @GetMapping("/image")
    public Mono<ResponseEntity<byte[]>> proxy(@RequestParam("url") String url,
                                             @RequestParam(value="w", required=false) Integer w,
                                             @RequestParam(value="h", required=false) Integer h,
                                             @RequestParam(value="fmt", required=false) String fmt,
                                             @RequestParam(value="q", required=false) Integer q,
                                             @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch) {
        String key = srv.cacheKeyForUrl(url,w,h,fmt,q);
        String etag = '"'+key+'"';
        if (etag.equals(ifNoneMatch)) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build());
        }
        long start = System.currentTimeMillis();
        return srv.fetchBytes(url)
                .flatMap(bytes -> srv.transform(bytes,w,h,fmt,q))
                .map(out -> {
                    MediaType mt = ContentTypes.mediaTypeForFormat(fmt);
                    CacheControl cc = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setCacheControl(cc.getHeaderValue());
                    headers.setETag(etag);
                    headers.setContentType(mt);
                    headers.setContentLength(out.length);
                    return new ResponseEntity<>(out, headers, HttpStatus.OK);
                })
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(400).body(("Bad request: "+ex.getMessage()).getBytes())));
    }

    @PostMapping(value="/upload", consumes = {"multipart/form-data"})
    public Mono<ResponseEntity<byte[]>> upload(@RequestPart("file") MultipartFile file,
                                               @RequestParam(value="w", required=false) Integer w,
                                               @RequestParam(value="h", required=false) Integer h,
                                               @RequestParam(value="fmt", required=false) String fmt,
                                               @RequestParam(value="q", required=false) Integer q,
                                               @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch) {
        try {
            byte[] fb = file.getBytes();
            String key = srv.cacheKeyForUpload(fb,w,h,fmt,q);
            String etag = '"'+key+'"';
            if (etag.equals(ifNoneMatch)) {
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build());
            }
            long start = System.currentTimeMillis();
            return srv.transform(fb,w,h,fmt,q)
                    .map(out -> {
                        MediaType mt = ContentTypes.mediaTypeForFormat(fmt);
                        CacheControl cc = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setCacheControl(cc.getHeaderValue());
                        headers.setETag(etag);
                        headers.setContentType(mt);
                        headers.setContentLength(out.length);
                        return new ResponseEntity<>(out, headers, HttpStatus.OK);
                    })
                    .onErrorResume(ex -> Mono.just(ResponseEntity.status(400).body(("Bad request: "+ex.getMessage()).getBytes())));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(400).body(("Bad request: "+e.getMessage()).getBytes()));
        }
    }
}
