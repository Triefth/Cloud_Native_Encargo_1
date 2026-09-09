# Data sources para VPC por defecto y AMI Ubuntu 22.04 LTS dinámica por región
data "aws_vpc" "default" {
  default = true
}

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Llave SSH generada por Terraform: nadie tiene que crear un Key Pair
# a mano en la consola de AWS. La llave privada se expone como output
# sensible y el pipeline de CI/CD la usa directamente para el despliegue.
resource "tls_private_key" "deploy_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "deploy_key" {
  key_name   = "telemedicina-ci-deploy-key-v2"
  public_key = tls_private_key.deploy_key.public_key_openssh
}

# Security Group para la instancia Backend EC2
resource "aws_security_group" "backend_sg" {
  name        = "telemedicina_backend_ec2_sg_v2"
  description = "Permitir trafico SSH y BFF (8080) para Backend"
  vpc_id      = data.aws_vpc.default.id

  # Acceso SSH para administración
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Puerto expuesto por el BFF (8080)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Alias para compatibilidad con referencias existentes
resource "aws_security_group" "ec2_sg" {
  name        = "telemedicina_ec2_sg_v2"
  description = "Security Group legacy / compatibilidad"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Security Group para la instancia Frontend EC2
resource "aws_security_group" "frontend_sg" {
  name        = "telemedicina_frontend_ec2_sg_v2"
  description = "Permitir trafico SSH (22), HTTP (80), HTTPS (443) y Vite (5173) para Frontend"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 5173
    to_port     = 5173
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Elastic IP estática para Backend (se reserva antes de la instancia para conocer la IP)
resource "aws_eip" "backend_eip" {
  domain = "vpc"

  tags = {
    Name = "Telemedicina-Backend-EIP"
  }
}

# Elastic IP estática para Frontend (se reserva antes de la instancia para conocer la IP)
resource "aws_eip" "frontend_eip" {
  domain = "vpc"

  tags = {
    Name = "Telemedicina-Frontend-EIP"
  }
}

# Instancia EC2 para el Backend (8 Microservicios + BFF Gateway)
resource "aws_instance" "backend_server" {
ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.backend_instance_type
  key_name               = aws_key_pair.deploy_key.key_name
  vpc_security_group_ids = [aws_security_group.backend_sg.id]
  user_data_replace_on_change = true

  root_block_device {
    volume_size = 25
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              exec > >(tee -a /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
              set -x

              # 1. Configurar SWAP de 4GB para compilar los microservicios sin agotar RAM
              if [ ! -f /swapfile ]; then
                fallocate -l 4G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=4096
                chmod 600 /swapfile
                mkswap /swapfile
                swapon /swapfile
                echo '/swapfile none swap sw 0 0' >> /etc/fstab
              fi

              # 2. Esperar que cloud-init libere apt-get y luego instalar paquetes
              while fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do sleep 3; done
              apt-get update -y
              apt-get install -y docker.io docker-compose-v2 git curl
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ubuntu

              # 3. Clonar repositorio
              rm -rf /home/ubuntu/app
              git clone -b ${var.git_branch} ${var.git_repo_url} /home/ubuntu/app

              # 4. Configurar variables de entorno para el backend
              cd /home/ubuntu/app/backend
              cat << 'ENVFILE' > .env
              AZURE_ISSUER=${var.azure_issuer}
              AZURE_AUDIENCE=${var.backend_api_identifier_uri}
              FRONTEND_ORIGIN=http://${aws_eip.frontend_eip.public_ip}
              ENVFILE

              chown -R ubuntu:ubuntu /home/ubuntu/app

              # 5. Compilar y levantar los 8 microservicios
              docker compose up -d --build
              EOF

  tags = {
    Name = "Telemedicina-Backend-EC2"
  }
}

# Asociación de la Elastic IP a la instancia Backend
resource "aws_eip_association" "backend_eip_assoc" {
  instance_id   = aws_instance.backend_server.id
  allocation_id = aws_eip.backend_eip.id
}

# Instancia EC2 separada para el Frontend (SPA Web Application)
resource "aws_instance" "frontend_server" {
ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.frontend_instance_type
  key_name               = aws_key_pair.deploy_key.key_name
  vpc_security_group_ids = [aws_security_group.frontend_sg.id]
  user_data_replace_on_change = true

  root_block_device {
    volume_size = 15
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              exec > >(tee -a /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
              set -x

              # 1. Configurar SWAP de 2GB (esencial para npm run build en t2.micro)
              if [ ! -f /swapfile ]; then
                fallocate -l 2G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=2048
                chmod 600 /swapfile
                mkswap /swapfile
                swapon /swapfile
                echo '/swapfile none swap sw 0 0' >> /etc/fstab
              fi

              # 2. Esperar que cloud-init libere apt-get y luego instalar paquetes
              while fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do sleep 3; done
              apt-get update -y
              apt-get install -y docker.io docker-compose-v2 git curl

              # Detener y deshabilitar nginx nativo del sistema
              systemctl stop nginx || true
              systemctl disable nginx || true

              systemctl enable docker
              systemctl start docker
              usermod -aG docker ubuntu

              # 3. Clonar repositorio
              rm -rf /home/ubuntu/app
              git clone -b ${var.git_branch} ${var.git_repo_url} /home/ubuntu/app

              # 4. Configurar variables de entorno y Nginx con la IP de Backend
              cd /home/ubuntu/app/frontend

              sed -i 's/BACKEND_HOST/${aws_eip.backend_eip.public_ip}/g' nginx.conf

              cat << 'ENVFILE' > .env
              VITE_AZURE_CLIENT_ID=${azuread_application.frontend.client_id}
              VITE_AZURE_TENANT_ID=${var.azure_tenant_id}
              VITE_AZURE_AUTHORITY=${var.azure_issuer}
              VITE_AZURE_API_SCOPE=${var.backend_api_identifier_uri}/read
              VITE_AZURE_REDIRECT_URI=http://${aws_eip.frontend_eip.public_ip}/
              BACKEND_HOST=${aws_eip.backend_eip.public_ip}
              ENVFILE

              chown -R ubuntu:ubuntu /home/ubuntu/app

              # 5. Compilar y levantar la SPA con Docker Compose en el puerto 80
              docker compose up -d --build
              EOF

  tags = {
    Name = "Telemedicina-Frontend-EC2"
  }
}

# Asociación de la Elastic IP a la instancia Frontend
resource "aws_eip_association" "frontend_eip_assoc" {
  instance_id   = aws_instance.frontend_server.id
  allocation_id = aws_eip.frontend_eip.id
}