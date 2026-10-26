package com.banco.consumer;

import com.banco.model.AlertaFraude;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BancoAlertasConsumer {

    private final List<AlertaFraude> recentAlertas = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "banco-alertas",
            groupId = "banco-alertas-listener-group",
            containerFactory = "alertaKafkaListenerContainerFactory"
    )
    public void listen(AlertaFraude alerta) {
        if (alerta != null) {
            recentAlertas.add(0, alerta);
            if (recentAlertas.size() > 50) {
                recentAlertas.remove(recentAlertas.size() - 1);
            }
            System.out.println("🔔 [CONSUMER BANCO-ALERTAS] Recibida alerta en tiempo real: " + alerta.getMotivo() + " | Usuario: " + alerta.getUsuario() + " | Monto: $" + alerta.getMonto());
        }
    }

    public List<AlertaFraude> getRecentAlertas() {
        return recentAlertas;
    }

    public void clearAlertas() {
        recentAlertas.clear();
    }
}
