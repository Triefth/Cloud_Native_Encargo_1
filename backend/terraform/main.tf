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

# Security Group para la instancia EC2
resource "aws_security_group" "ec2_sg" {
  name        = "telemedicina_ec2_sg"
  description = "Permitir trafico HTTP y SSH"

  # Acceso SSH para administración
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Puerto expuesto por el BFF 
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

# Instancia EC2
resource "aws_instance" "backend_server" {
  ami             = "ami-0c7217cdde317cfec" 
  instance_type   = var.instance_type
  key_name        = aws_key_pair.deploy_key.key_name
  security_groups = [aws_security_group.ec2_sg.name]

  # Disco expandido a 25 GB para soportar multiples imagenes Docker
  root_block_device {
    volume_size = 25
    volume_type = "gp3"
  }

  # Script de automatización: Instala Docker y Docker Compose al iniciar la máquina
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

# IP publica fija: sin esto, cada stop/start de la instancia cambia la IP
# y rompe tanto el secret de despliegue como la integración del API Gateway.
resource "aws_eip" "backend_eip" {
  instance = aws_instance.backend_server.id
  domain   = "vpc"

  tags = {
    Name = "Telemedicina-Backend-EIP"
  }
}