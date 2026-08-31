package cl.duoc.telemedicina.reportes.service;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.entity.ReporteOperativo.Severidad;
import cl.duoc.telemedicina.reportes.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    public List<ReporteOperativo> obtenerTodos() {
        return reporteRepository.findAll();
    }

    public List<ReporteOperativo> obtenerPorModulo(String modulo) {
        return reporteRepository.findByModuloServicio(modulo);
    }

    public ReporteOperativo registrarEvento(ReporteOperativo reporte) {
        return reporteRepository.save(reporte);
    }

    public Map<String, Object> generarResumenOperativo() {
        List<ReporteOperativo> todos = reporteRepository.findAll();
        long totalEventos = todos.size();
        long criticos = todos.stream().filter(r -> r.getSeveridad() == Severidad.CRITICAL).count();
        long advertencias = todos.stream().filter(r -> r.getSeveridad() == Severidad.WARNING).count();
        double latenciaPromedio = todos.stream()
                .filter(r -> r.getLatenciaMs() != null)
                .mapToLong(ReporteOperativo::getLatenciaMs)
                .average()
                .orElse(45.0);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalEventosRegistrados", totalEventos);
        resumen.put("eventosCriticos", criticos);
        resumen.put("advertenciasLatencia", advertencias);
        resumen.put("latenciaPromedioMs", Math.round(latenciaPromedio * 100.0) / 100.0);
        resumen.put("disponibilidadEstimadaPct", criticos == 0 ? 99.9 : 98.5);
        resumen.put("estadoPlataforma", criticos == 0 ? "OPERACIONAL_OPTIMO" : "ATENCION_REQUERIDA");

        return resumen;
    }
}
