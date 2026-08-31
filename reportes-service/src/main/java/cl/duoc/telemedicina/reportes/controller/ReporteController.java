package cl.duoc.telemedicina.reportes.controller;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public ResponseEntity<List<ReporteOperativo>> obtenerTodos() {
        return ResponseEntity.ok(reporteService.obtenerTodos());
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        return ResponseEntity.ok(reporteService.generarResumenOperativo());
    }

    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<List<ReporteOperativo>> obtenerPorModulo(@PathVariable String modulo) {
        return ResponseEntity.ok(reporteService.obtenerPorModulo(modulo));
    }

    @PostMapping("/evento")
    public ResponseEntity<ReporteOperativo> registrarEvento(@Valid @RequestBody ReporteOperativo reporte) {
        ReporteOperativo creado = reporteService.registrarEvento(reporte);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
