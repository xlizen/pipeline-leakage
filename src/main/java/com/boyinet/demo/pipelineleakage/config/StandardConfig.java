package com.boyinet.demo.pipelineleakage.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * @author lengchunyun
 */
@Configuration
@Data
public class StandardConfig {
    @Value("${standard.diffPressure:1100}")
    private BigDecimal standardDiffPressure;
}
