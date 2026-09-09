#!/bin/bash
set -e

# 1. Configurar memoria SWAP de 4GB (Crítico para que t2.medium compile 8 microservicios sin colapsar la RAM)
if [ ! -f /swapfile ]; then
    echo "=== Configurando 4GB de memoria Swap ==="
    sudo fallocate -l 4G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=4096
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

# 2. Actualizar paquetes e instalar Docker
echo "=== Actualizando paquetes e instalando Docker ==="
sudo apt-get update -y
sudo apt-get install -y docker.io docker-compose-v2 git curl ufw
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# 3. Configurar Firewall UFW (SSH: 22, BFF Gateway: 8080)
echo "=== Configurando Firewall UFW ==="
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
echo "y" | sudo ufw enable || true

# 4. Iniciar automáticamente los microservicios si existe docker-compose.yml
if [ -f "docker-compose.yml" ]; then
    echo "=== Levantando microservicios con Docker Compose ==="
    if [ ! -f ".env" ] && [ -f ".env.example" ]; then
        cp .env.example .env
    fi
    sudo docker compose up -d --build
    echo "=== Estado de los microservicios ==="
    sudo docker compose ps
fi

echo "=== Despliegue de Backend completado exitosamente ==="
