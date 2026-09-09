#!/bin/bash
set -e

# 1. Configurar memoria SWAP de 2GB (Para t2.micro durante el build de Vite)
if [ ! -f /swapfile ]; then
    echo "=== Configurando 2GB de memoria Swap ==="
    sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

# 2. Actualizar paquetes e instalar Docker
echo "=== Actualizando paquetes e instalando Docker y Nginx ==="
sudo apt-get update -y
sudo apt-get install -y docker.io docker-compose-v2 git curl ufw
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# 3. Configurar Firewall UFW (SSH: 22, HTTP: 80, HTTPS: 443, Vite Dev: 5173)
echo "=== Configurando Firewall UFW ==="
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 5173/tcp
echo "y" | sudo ufw enable || true

# 4. Iniciar automáticamente la SPA Frontend con Docker Compose si existe docker-compose.yml
if [ -f "docker-compose.yml" ]; then
    echo "=== Compilando y levantando Frontend con Docker Compose ==="
    if [ ! -f ".env" ] && [ -f ".env.example" ]; then
        cp .env.example .env
    fi
    sudo docker compose up -d --build
    echo "=== Estado del Frontend ==="
    sudo docker compose ps
fi

echo "=== Despliegue de Frontend completado exitosamente ==="
