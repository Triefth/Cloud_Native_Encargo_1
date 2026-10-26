package com.banco.controller;

import com.banco.consumer.BancoAlertasConsumer;
import com.banco.model.AlertaFraude;
import com.banco.model.Transaccion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/banco")
@CrossOrigin(origins = "*")
public class SimuladorController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private BancoAlertasConsumer alertasConsumer;

    private final List<Transaccion> recentTransacciones = new CopyOnWriteArrayList<>();

    @PostMapping("/transaccion")
    public String enviarTransaccion(@RequestBody Transaccion tx) {
        // Enriquecer con timestamp si falta
        if (tx.getTimestamp() == null) {
            tx.setTimestamp(System.currentTimeMillis());
        }

        // Registrar para historial de simulador
        recentTransacciones.add(0, tx);
        if (recentTransacciones.size() > 50) {
            recentTransacciones.remove(recentTransacciones.size() - 1);
        }

        // Enviar a Kafka (Topic: banco-transacciones)
        // Key: usuario (para garantizar orden por usuario)
        kafkaTemplate.send("banco-transacciones", tx.getUsuario(), tx);

        return "📤 Enviado: " + tx.getMonto() + " de " + tx.getUsuario();
    }

    @GetMapping("/alertas")
    public List<AlertaFraude> obtenerAlertas() {
        return alertasConsumer.getRecentAlertas();
    }

    @DeleteMapping("/alertas")
    public String limpiarAlertas() {
        alertasConsumer.clearAlertas();
        return "Alertas limpiadas";
    }

    @GetMapping("/transacciones")
    public List<Transaccion> obtenerTransacciones() {
        return recentTransacciones;
    }

    @GetMapping("/health")
    public String health() {
        return "Simulador de Banco y Kafka Streams está activo en puerto 8888";
    }
}
