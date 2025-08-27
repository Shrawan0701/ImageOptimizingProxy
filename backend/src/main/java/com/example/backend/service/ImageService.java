
package com.example.backend.service;

import com.example.backend.db.ImageRequestLogRepository;
import com.example.backend.util.Hashing;
import com.example.backend.util.ContentTypes;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.imageio.IIOImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@Service
public class ImageService {

    private final WebClient webClient = WebClient.create();
    private final ReactiveRedisTemplate<String, byte[]> redis;
    private final ImageRequestLogRepository logs;
    private final String allowedOriginHosts;
    private final long maxBufferBytes;

    public ImageService(ImageRequestLogRepository logs,
                        ReactiveRedisTemplate<String, byte[]> redis,
                        @Value("${app.allowedOriginHosts:*}") String allowedOriginHosts,
                        @Value("${app.maxBufferBytes:20971520}") long maxBufferBytes) {
        this.logs = logs; this.redis = redis; this.allowedOriginHosts = allowedOriginHosts; this.maxBufferBytes = maxBufferBytes;
    }

    public String cacheKeyForUrl(String url, Integer w, Integer h, String fmt, Integer q) {
        String raw = "url|" + url + "|" + (w==null?"":w) + "|" + (h==null?"":h) + "|" + (fmt==null?"":fmt) + "|" + (q==null?"":q);
        return Hashing.sha256(raw);
    }

    public String cacheKeyForUpload(byte[] file, Integer w, Integer h, String fmt, Integer q) {
        String raw = "upload|" + Hashing.sha256(file) + "|" + (w==null?"":w) + "|" + (h==null?"":h) + "|" + (fmt==null?"":fmt) + "|" + (q==null?"":q);
        return Hashing.sha256(raw);
    }

    public Mono<byte[]> fetchBytes(String url) {
        return webClient.get().uri(url).retrieve().bodyToMono(byte[].class)
                .flatMap(bytes -> bytes.length > maxBufferBytes ? Mono.error(new IllegalArgumentException("Image too large")) : Mono.just(bytes));
    }

    public Mono<byte[]> transform(byte[] input, Integer w, Integer h, String fmt, Integer q) {
        return Mono.fromCallable(() -> {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(input));
            if (src == null) throw new IllegalArgumentException("Unsupported image");
            Thumbnails.Builder<BufferedImage> b = Thumbnails.of(src);
            if (w != null && h != null) b.size(w, h);
            else if (w != null) b.width(w);
            else if (h != null) b.height(h);
            else b.scale(1.0);

            String target = (fmt==null?"jpeg":fmt.toLowerCase());
            if ("avif".equals(target)) target = "webp"; // fallback
            float quality = q==null?0.8f:Math.max(0.01f, Math.min(q,100)/100f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage outImg = b.asBufferedImage();

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(target);
            if (!writers.hasNext()) {
                writers = ImageIO.getImageWritersByFormatName("jpeg"); target="jpeg";
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            try (MemoryCacheImageOutputStream mco = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(mco);
                writer.write(null, new IIOImage(outImg, null, null), param);
            } finally {
                // writer.dispose() handled by GC in this minimal scaffold
            }
            return baos.toByteArray();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
