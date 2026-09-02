variable "aws_region" {
  description = "Región de AWS para el despliegue"
  default     = "us-east-1"
}

variable "instance_type" {
  description = "Tipo de instancia EC2"
  default     = "t2.micro" 
}

variable "key_name" {
  description = "Nombre de la llave SSH de AWS (Key Pair)"
  type        = string
}