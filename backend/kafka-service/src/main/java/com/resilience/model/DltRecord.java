package com.resilience.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Representa un registro capturado en el Dead Letter Topic (.DLT)
 * Incluye Header Enrichment con stacktrace, excepción original y causa.
 * Según PDF 1 (Página 8 de Duoc UC).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DltRecord implements Serializable {
    private String id;
    private String originalTopic;
    private int partition;
    private long offset;
    private String key;
    private String payload;
    private String exceptionMessage;
    private String exceptionStacktrace;
    private String correlationId;
    private long timestamp;
    private Map<String, String> headers;
}
