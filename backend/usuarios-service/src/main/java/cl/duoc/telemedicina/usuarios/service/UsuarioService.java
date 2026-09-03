package cl.duoc.telemedicina.usuarios.service;

import cl.duoc.telemedicina.usuarios.entity.Medico;
import cl.duoc.telemedicina.usuarios.entity.Paciente;
import cl.duoc.telemedicina.usuarios.repository.MedicoRepository;
import cl.duoc.telemedicina.usuarios.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    // --- Pacientes ---
    public List<Paciente> obtenerTodosPacientes() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> obtenerPacientePorRut(String rut) {
        return pacienteRepository.findByRut(rut);
    }

    public Paciente guardarPaciente(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    // --- Médicos ---
    public List<Medico> obtenerTodosMedicos() {
        return medicoRepository.findAll();
    }

    public Optional<Medico> obtenerMedicoPorRut(String rut) {
        return medicoRepository.findByRut(rut);
    }

    public List<Medico> obtenerMedicosPorEspecialidad(String especialidad) {
        return medicoRepository.findByEspecialidad(especialidad);
    }

    public Medico guardarMedico(Medico medico) {
        return medicoRepository.save(medico);
    }
}
