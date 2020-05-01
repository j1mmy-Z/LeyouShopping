package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leyou.worker")
@Data
public class IdWorkerProperties {
    private Long workerId;

    private Long dataCenterId;
}
