
package com.example.backend.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.OffsetDateTime;

@Table("image_request_log")
public class ImageRequestLog {

    @Id
    private Long id;

    @Column("created_at")
    private OffsetDateTime createdAt;

    private String url;
    private String source;
    private String params;
    @Column("cache_key")
    private String cacheKey;
    private Integer status;
    @Column("content_type")
    private String contentType;
    private Integer bytes;
    @Column("duration_ms")
    private Long durationMs;

    // getters and setters omitted for brevity in this scaffold
}
