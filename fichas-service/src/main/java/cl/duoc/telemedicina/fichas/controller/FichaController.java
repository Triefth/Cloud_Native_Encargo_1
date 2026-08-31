package cl.duoc.telemedicina.fichas.controller;

import cl.duoc.telemedicina.fichas.entity.FichaMedica;
import cl.duoc.telemedicina.fichas.service.FichaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fichas")
public class FichaController {

    @Autowired
    private FichaService fichaService;

    @GetMapping
    public ResponseEntity<List<FichaMedica>> obtenerTodas() {
        return ResponseEntity.ok(fichaService.obtenerTodas());
    }

    @GetMapping("/paciente/{rut}")
    public ResponseEntity<?> obtenerPorRut(@PathVariable String rut) {
        return fichaService.obtenerPorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FichaMedica> crearFicha(@Valid @RequestBody FichaMedica ficha) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fichaService.crearFicha(ficha));
    }

    @PostMapping("/paciente/{rut}/atencion")
    public ResponseEntity<FichaMedica> registrarAtencion(@PathVariable String rut, @RequestBody Map<String, Object> payload) {
        Long consultaId = payload.get("consultaId") != null ? Long.valueOf(payload.get("consultaId").toString()) : null;
        String medico = (String) payload.get("nombreMedico");
        String especialidad = (String) payload.get("especialidad");
        String resumen = (String) payload.get("resumenAtencion");

        FichaMedica actualizada = fichaService.registrarAtencionRemota(rut, consultaId, medico, especialidad, resumen);
        return ResponseEntity.ok(actualizada);
    }
}
