package cl.duoc.telemedicina.clinicas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "configuraciones_ehr")
public class ConfiguracionEhr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreSoftwareEhr;

    private String apiEndpointUrl;

    private String tipoProtocolo;

    private String apiKey;

    private String timeoutMs;

    private Boolean sincronizacionAutomatica;

    public ConfiguracionEhr() {
    }

    public ConfiguracionEhr(Long id, String nombreSoftwareEhr, String apiEndpointUrl, String tipoProtocolo, String apiKey, String timeoutMs, Boolean sincronizacionAutomatica) {
        this.id = id;
        this.nombreSoftwareEhr = nombreSoftwareEhr;
        this.apiEndpointUrl = apiEndpointUrl;
        this.tipoProtocolo = tipoProtocolo;
        this.apiKey = apiKey;
        this.timeoutMs = timeoutMs;
        this.sincronizacionAutomatica = sincronizacionAutomatica;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreSoftwareEhr() { return nombreSoftwareEhr; }
    public void setNombreSoftwareEhr(String nombreSoftwareEhr) { this.nombreSoftwareEhr = nombreSoftwareEhr; }

    public String getApiEndpointUrl() { return apiEndpointUrl; }
    public void setApiEndpointUrl(String apiEndpointUrl) { this.apiEndpointUrl = apiEndpointUrl; }

    public String getTipoProtocolo() { return tipoProtocolo; }
    public void setTipoProtocolo(String tipoProtocolo) { this.tipoProtocolo = tipoProtocolo; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(String timeoutMs) { this.timeoutMs = timeoutMs; }

    public Boolean getSincronizacionAutomatica() { return sincronizacionAutomatica; }
    public void setSincronizacionAutomatica(Boolean sincronizacionAutomatica) { this.sincronizacionAutomatica = sincronizacionAutomatica; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String nombreSoftwareEhr;
        private String apiEndpointUrl;
        private String tipoProtocolo;
        private String apiKey;
        private String timeoutMs;
        private Boolean sincronizacionAutomatica;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nombreSoftwareEhr(String nombreSoftwareEhr) { this.nombreSoftwareEhr = nombreSoftwareEhr; return this; }
        public Builder apiEndpointUrl(String apiEndpointUrl) { this.apiEndpointUrl = apiEndpointUrl; return this; }
        public Builder tipoProtocolo(String tipoProtocolo) { this.tipoProtocolo = tipoProtocolo; return this; }
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder timeoutMs(String timeoutMs) { this.timeoutMs = timeoutMs; return this; }
        public Builder sincronizacionAutomatica(Boolean sincronizacionAutomatica) { this.sincronizacionAutomatica = sincronizacionAutomatica; return this; }

        public ConfiguracionEhr build() {
            return new ConfiguracionEhr(id, nombreSoftwareEhr, apiEndpointUrl, tipoProtocolo, apiKey, timeoutMs, sincronizacionAutomatica);
        }
    }
}
