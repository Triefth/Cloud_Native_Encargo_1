package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.TipoNotificacion;
import cl.duoc.telemedicina.notificaciones.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<Notificacion>> obtenerTodas() {
        return ResponseEntity.ok(notificacionService.obtenerTodas());
    }

    @GetMapping("/paciente/{rut}")
    public ResponseEntity<List<Notificacion>> obtenerPorPaciente(@PathVariable String rut) {
        return ResponseEntity.ok(notificacionService.obtenerPorPaciente(rut));
    }

    @PostMapping("/recordatorio")
    public ResponseEntity<Notificacion> enviarRecordatorio(@RequestBody Map<String, Object> payload) {
        Long citaId = payload.get("citaId") != null ? Long.valueOf(payload.get("citaId").toString()) : null;
        String rutPaciente = (String) payload.get("rutPaciente");
        String tipoStr = (String) payload.get("tipo");
        String mensaje = (String) payload.get("mensaje");

        TipoNotificacion tipo = tipoStr != null ? TipoNotificacion.valueOf(tipoStr.toUpperCase()) : TipoNotificacion.SMS;

        Notificacion creada = notificacionService.enviarRecordatorio(citaId, rutPaciente, tipo, mensaje);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/lectura")
    public ResponseEntity<Notificacion> marcarLectura(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarConfirmado(id));
    }
}
