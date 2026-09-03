package cl.duoc.telemedicina.citas.controller;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    public ResponseEntity<List<Cita>> obtenerTodas() {
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return citaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{rut}")
    public ResponseEntity<List<Cita>> obtenerPorPaciente(@PathVariable String rut) {
        return ResponseEntity.ok(citaService.obtenerPorPaciente(rut));
    }

    @GetMapping("/medico/{rut}")
    public ResponseEntity<List<Cita>> obtenerPorMedico(@PathVariable String rut) {
        return ResponseEntity.ok(citaService.obtenerPorMedico(rut));
    }

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nueva = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Cita> confirmarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.confirmarCita(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<Cita> reprogramarCita(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String fechaStr = payload.get("nuevaFechaHora");
        LocalDateTime nuevaFecha = LocalDateTime.parse(fechaStr);
        return ResponseEntity.ok(citaService.reprogramarCita(id, nuevaFecha));
    }
}
