package cl.duoc.telemedicina.clinicas.service;

import cl.duoc.telemedicina.clinicas.entity.ClinicaRural;
import cl.duoc.telemedicina.clinicas.entity.ConfiguracionEhr;
import cl.duoc.telemedicina.clinicas.repository.ClinicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClinicaServiceTest {

    @Mock
    private ClinicaRepository clinicaRepository;

    @InjectMocks
    private ClinicaService clinicaService;

    private ClinicaRural sampleClinica;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleClinica = ClinicaRural.builder()
                .id(1L)
                .nombre("Clínica Rural San Pedro")
                .comuna("San Pedro de Atacama")
                .region("Antofagasta")
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(clinicaRepository.findAll()).thenReturn(List.of(sampleClinica));
        List<ClinicaRural> result = clinicaService.obtenerTodas();
        assertEquals(1, result.size());
        assertEquals("Clínica Rural San Pedro", result.get(0).getNombre());
    }

    @Test
    void testActualizarConfiguracionEhr() {
        when(clinicaRepository.findById(1L)).thenReturn(Optional.of(sampleClinica));
        when(clinicaRepository.save(any(ClinicaRural.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfiguracionEhr ehr = ConfiguracionEhr.builder()
                .nombreSoftwareEhr("Rayen")
                .tipoProtocolo("REST_JSON")
                .build();

        ClinicaRural result = clinicaService.actualizarConfiguracionEhr(1L, ehr);
        assertNotNull(result.getConfiguracionEhr());
        assertEquals("Rayen", result.getConfiguracionEhr().getNombreSoftwareEhr());
    }
}
