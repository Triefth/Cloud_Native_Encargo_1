# Llave SSH generada por Terraform: nadie tiene que crear un Key Pair
# a mano en la consola de AWS. La llave privada se expone como output
# sensible y el pipeline de CI/CD la usa directamente para el despliegue.
resource "tls_private_key" "deploy_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "deploy_key" {
  key_name   = "telemedicina-ci-deploy-key"
  public_key = tls_private_key.deploy_key.public_key_openssh
}

# Security Group para la instancia Backend EC2
resource "aws_security_group" "backend_sg" {
  name        = "telemedicina_backend_ec2_sg"
  description = "Permitir trafico SSH y BFF (8080) para Backend"

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
  name        = "telemedicina_ec2_sg"
  description = "Security Group legacy / compatibilidad"

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
  name        = "telemedicina_frontend_ec2_sg"
  description = "Permitir trafico SSH (22), HTTP (80), HTTPS (443) y Vite (5173) para Frontend"

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

# Instancia EC2 para el Backend (8 Microservicios + BFF Gateway)
resource "aws_instance" "backend_server" {
  ami             = "ami-0c7217cdde317cfec"
  instance_type   = var.backend_instance_type
  key_name        = aws_key_pair.deploy_key.key_name
  security_groups = [aws_security_group.backend_sg.name]

  root_block_device {
    volume_size = 25
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y
              sudo apt-get install -y docker.io docker-compose git
              sudo systemctl start docker
              sudo systemctl enable docker
              sudo usermod -aG docker ubuntu
              EOF

  tags = {
    Name = "Telemedicina-Backend-EC2"
  }
}

# IP publica fija para Backend EC2
resource "aws_eip" "backend_eip" {
  instance = aws_instance.backend_server.id
  domain   = "vpc"

  tags = {
    Name = "Telemedicina-Backend-EIP"
  }
}

# Instancia EC2 separada para el Frontend (SPA Web Application)
resource "aws_instance" "frontend_server" {
  ami             = "ami-0c7217cdde317cfec"
  instance_type   = var.frontend_instance_type
  key_name        = aws_key_pair.deploy_key.key_name
  security_groups = [aws_security_group.frontend_sg.name]

  root_block_device {
    volume_size = 15
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y
              sudo apt-get install -y docker.io docker-compose nginx git
              sudo systemctl start docker
              sudo systemctl enable docker
              sudo usermod -aG docker ubuntu
              EOF

  tags = {
    Name = "Telemedicina-Frontend-EC2"
  }
}

# IP publica fija para Frontend EC2
resource "aws_eip" "frontend_eip" {
  instance = aws_instance.frontend_server.id
  domain   = "vpc"

  tags = {
    Name = "Telemedicina-Frontend-EIP"
  }
}