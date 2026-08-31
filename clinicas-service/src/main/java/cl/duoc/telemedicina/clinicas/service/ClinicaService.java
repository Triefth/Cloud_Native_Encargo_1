package cl.duoc.telemedicina.clinicas.service;

import cl.duoc.telemedicina.clinicas.entity.ClinicaRural;
import cl.duoc.telemedicina.clinicas.entity.ConfiguracionEhr;
import cl.duoc.telemedicina.clinicas.repository.ClinicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicaService {

    @Autowired
    private ClinicaRepository clinicaRepository;

    public List<ClinicaRural> obtenerTodas() {
        return clinicaRepository.findAll();
    }

    public Optional<ClinicaRural> obtenerPorId(Long id) {
        return clinicaRepository.findById(id);
    }

    public Optional<ClinicaRural> obtenerPorNombre(String nombre) {
        return clinicaRepository.findByNombre(nombre);
    }

    public ClinicaRural guardarClinica(ClinicaRural clinica) {
        return clinicaRepository.save(clinica);
    }

    public ClinicaRural actualizarConfiguracionEhr(Long clinicaId, ConfiguracionEhr config) {
        ClinicaRural clinica = clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new RuntimeException("Clínica no encontrada con ID: " + clinicaId));
        clinica.setConfiguracionEhr(config);
        return clinicaRepository.save(clinica);
    }
}
