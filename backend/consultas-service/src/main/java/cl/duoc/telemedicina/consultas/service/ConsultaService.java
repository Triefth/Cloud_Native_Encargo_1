package cl.duoc.telemedicina.consultas.service;

import cl.duoc.telemedicina.consultas.entity.Consulta;
import cl.duoc.telemedicina.consultas.entity.Consulta.EstadoConsulta;
import cl.duoc.telemedicina.consultas.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    public List<Consulta> obtenerTodas() {
        return consultaRepository.findAll();
    }

    public Optional<Consulta> obtenerPorId(Long id) {
        return consultaRepository.findById(id);
    }

    public Optional<Consulta> obtenerPorCitaId(Long citaId) {
        return consultaRepository.findByCitaId(citaId);
    }

    public List<Consulta> obtenerPorPaciente(String rutPaciente) {
        return consultaRepository.findByRutPaciente(rutPaciente);
    }

    public Consulta iniciarConsulta(Long citaId, String rutPaciente, String rutMedico) {
        String roomId = "hipaa-cpaas-room-" + UUID.randomUUID().toString().substring(0, 8);
        String cpaasUrl = "https://meet.telemedicina-rural.cl/v1/call/" + roomId + "?hipaa=true";

        Consulta consulta = Consulta.builder()
                .citaId(citaId)
                .rutPaciente(rutPaciente)
                .rutMedico(rutMedico)
                .roomName(roomId)
                .cpaasJoinUrl(cpaasUrl)
                .estado(EstadoConsulta.EN_CURSO)
                .fechaInicio(LocalDateTime.now())
                .build();

        return consultaRepository.save(consulta);
    }

    public Consulta finalizarConsulta(Long id, String diagnostico, String indicaciones, Integer duracionMinutos) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada con ID: " + id));

        consulta.setDiagnosticoPreliminar(diagnostico);
        consulta.setIndicacionesMedicas(indicaciones);
        consulta.setDuracionMinutos(duracionMinutos != null ? duracionMinutos : 15);
        consulta.setFechaFin(LocalDateTime.now());
        consulta.setEstado(EstadoConsulta.FINALIZADA);

        return consultaRepository.save(consulta);
    }
}
