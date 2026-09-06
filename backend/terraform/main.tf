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
  key_name        = var.key_name
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