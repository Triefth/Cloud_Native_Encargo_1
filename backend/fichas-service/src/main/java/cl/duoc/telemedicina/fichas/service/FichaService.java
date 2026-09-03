package cl.duoc.telemedicina.fichas.service;

import cl.duoc.telemedicina.fichas.entity.AtencionRemotaRecord;
import cl.duoc.telemedicina.fichas.entity.FichaMedica;
import cl.duoc.telemedicina.fichas.repository.FichaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FichaService {

    @Autowired
    private FichaRepository fichaRepository;

    public List<FichaMedica> obtenerTodas() {
        return fichaRepository.findAll();
    }

    public Optional<FichaMedica> obtenerPorRut(String rut) {
        return fichaRepository.findByRutPaciente(rut);
    }

    public FichaMedica crearFicha(FichaMedica ficha) {
        return fichaRepository.save(ficha);
    }

    public FichaMedica registrarAtencionRemota(String rutPaciente, Long consultaId, String medico, String especialidad, String resumen) {
        FichaMedica ficha = fichaRepository.findByRutPaciente(rutPaciente)
                .orElseGet(() -> fichaRepository.save(FichaMedica.builder()
                        .rutPaciente(rutPaciente)
                        .nombreCompleto("Paciente Rural " + rutPaciente)
                        .clinicaOrigen("Clínica Rural San Pedro")
                        .build()));

        AtencionRemotaRecord record = AtencionRemotaRecord.builder()
                .fichaMedica(ficha)
                .consultaId(consultaId)
                .nombreMedico(medico)
                .especialidad(especialidad)
                .resumenAtencion(resumen)
                .fechaAtencion(LocalDateTime.now())
                .origenSistema("Plataforma Telemedicina Rural DSY1107")
                .build();

        ficha.getAtencionesRemotas().add(record);
        return fichaRepository.save(ficha);
    }
}
