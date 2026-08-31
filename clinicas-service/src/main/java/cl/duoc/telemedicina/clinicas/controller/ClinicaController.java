package cl.duoc.telemedicina.clinicas.controller;

import cl.duoc.telemedicina.clinicas.entity.ClinicaRural;
import cl.duoc.telemedicina.clinicas.entity.ConfiguracionEhr;
import cl.duoc.telemedicina.clinicas.service.ClinicaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinicas")
public class ClinicaController {

    @Autowired
    private ClinicaService clinicaService;

    @GetMapping
    public ResponseEntity<List<ClinicaRural>> obtenerTodas() {
        return ResponseEntity.ok(clinicaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return clinicaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClinicaRural> crearClinica(@Valid @RequestBody ClinicaRural clinica) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicaService.guardarClinica(clinica));
    }

    @PutMapping("/{id}/configuracion-ehr")
    public ResponseEntity<ClinicaRural> actualizarConfiguracionEhr(@PathVariable Long id, @RequestBody ConfiguracionEhr config) {
        return ResponseEntity.ok(clinicaService.actualizarConfiguracionEhr(id, config));
    }
}
