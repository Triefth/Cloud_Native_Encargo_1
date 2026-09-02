provider "aws" {
  region = "us-east-1"
}

resource "aws_security_group" "microservices_sg" {
  name        = "microservices_sg"
  description = "Permitir trafico HTTP y SSH"

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

resource "aws_instance" "app_server" {
  ami           = "ami-0c7217cdde317cfec" 
  instance_type = "t2.medium" 
  vpc_security_group_ids = [aws_security_group.microservices_sg.id]

  tags = {
    Name = "Backend-Telemedicina"
  }
}