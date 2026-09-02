# Security Group para la instancia EC2
resource "aws_security_group" "ec2_sg" {
  name        = "pedidos360_ec2_sg"
  description = "Permitir trafico HTTP y SSH"

  # Acceso SSH para administración
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Puerto expuesto por el BFF o el API interno (ajusta segun tu docker-compose)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Idealmente, restringir a la IP del API Gateway
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
  ami             = "ami-0c7217cdde317cfec" # Ubuntu Server 22.04 LTS (us-east-1)
  instance_type   = var.instance_type
  key_name        = var.key_name
  security_groups = [aws_security_group.ec2_sg.name]

  tags = {
    Name = "Pedidos360-Backend"
  }
}