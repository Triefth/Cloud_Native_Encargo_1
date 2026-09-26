package cl.duoc.telemedicina.notificaciones.service;

import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListenerManagementService {

    @Autowired
    private RabbitListenerEndpointRegistry registry;

    public void pauseListener(String listenerId) {
        try {
            MessageListenerContainer container = registry.getListenerContainer(listenerId);
            if (container != null && container.isRunning()) {
                container.stop();
                System.out.println("[✓] Listener pausado (stop): " + listenerId);
            }
        } catch (Exception e) {
            System.err.println("[✗] Error al pausar listener: " + e.getMessage());
        }
    }

    public void resumeListener(String listenerId) {
        try {
            MessageListenerContainer container = registry.getListenerContainer(listenerId);
            if (container != null && !container.isRunning()) {
                container.start();
                System.out.println("[✓] Listener reanudado (start): " + listenerId);
            }
        } catch (Exception e) {
            System.err.println("[✗] Error al reanudar listener: " + e.getMessage());
        }
    }

    public List<String> getAllListenerIds() {
        return new ArrayList<>(registry.getListenerContainerIds());
    }
}
