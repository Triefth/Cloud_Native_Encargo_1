package cl.duoc.telemedicina.consultas.controller;

import cl.duoc.telemedicina.consultas.entity.Consulta;
import cl.duoc.telemedicina.consultas.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @GetMapping
    public ResponseEntity<List<Consulta>> obtenerTodas() {
        return ResponseEntity.ok(consultaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return consultaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cita/{citaId}")
    public ResponseEntity<?> obtenerPorCitaId(@PathVariable Long citaId) {
        return consultaService.obtenerPorCitaId(citaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{rut}")
    public ResponseEntity<List<Consulta>> obtenerPorPaciente(@PathVariable String rut) {
        return ResponseEntity.ok(consultaService.obtenerPorPaciente(rut));
    }

    @PostMapping("/iniciar")
    public ResponseEntity<Consulta> iniciarConsulta(@RequestBody Map<String, Object> payload) {
        Long citaId = Long.valueOf(payload.get("citaId").toString());
        String rutPaciente = payload.get("rutPaciente").toString();
        String rutMedico = payload.get("rutMedico").toString();

        Consulta creada = consultaService.iniciarConsulta(citaId, rutPaciente, rutMedico);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Consulta> finalizarConsulta(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String diagnostico = (String) payload.get("diagnosticoPreliminar");
        String indicaciones = (String) payload.get("indicacionesMedicas");
        Integer duracion = payload.get("duracionMinutos") != null ? Integer.parseInt(payload.get("duracionMinutos").toString()) : 15;

        Consulta finalizada = consultaService.finalizarConsulta(id, diagnostico, indicaciones, duracion);
        return ResponseEntity.ok(finalizada);
    }
}
