package cl.duoc.telemedicina.usuarios.controller;

import cl.duoc.telemedicina.usuarios.entity.Medico;
import cl.duoc.telemedicina.usuarios.entity.Paciente;
import cl.duoc.telemedicina.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // --- Endpoints Pacientes ---
    @GetMapping("/pacientes")
    public ResponseEntity<List<Paciente>> obtenerPacientes() {
        return ResponseEntity.ok(usuarioService.obtenerTodosPacientes());
    }

    @GetMapping("/pacientes/{rut}")
    public ResponseEntity<?> obtenerPacientePorRut(@PathVariable String rut) {
        return usuarioService.obtenerPacientePorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pacientes")
    public ResponseEntity<Paciente> crearPaciente(@Valid @RequestBody Paciente paciente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardarPaciente(paciente));
    }

    // --- Endpoints Médicos ---
    @GetMapping("/medicos")
    public ResponseEntity<List<Medico>> obtenerMedicos() {
        return ResponseEntity.ok(usuarioService.obtenerTodosMedicos());
    }

    @GetMapping("/medicos/{rut}")
    public ResponseEntity<?> obtenerMedicoPorRut(@PathVariable String rut) {
        return usuarioService.obtenerMedicoPorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/medicos/especialidad/{especialidad}")
    public ResponseEntity<List<Medico>> obtenerMedicosPorEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(usuarioService.obtenerMedicosPorEspecialidad(especialidad));
    }

    @PostMapping("/medicos")
    public ResponseEntity<Medico> crearMedico(@Valid @RequestBody Medico medico) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardarMedico(medico));
    }
}
