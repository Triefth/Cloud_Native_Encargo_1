# Guía Paso a Paso para Desplegar Backend y Frontend en 2 Instancias EC2 Distintas

Esta guía detalla el procedimiento completo para aprovisionar las 2 instancias EC2 con **Terraform** y subir el **Backend** y **Frontend** de manera independiente.

---

## 1. Obtener las IPs y la Llave SSH de Terraform

En la carpeta `terraform/`:

```bash
cd terraform
terraform init
terraform apply
```

Una vez completado el apply, obtén las IPs públicas y la llave SSH:

```bash
# Ver las IPs asignadas
terraform output backend_public_ip
terraform output frontend_public_ip

# Extraer la llave privada SSH generada a un archivo local
terraform output -raw ec2_ssh_private_key > deploy_key.pem

# En Linux/macOS o Git Bash (en Windows asegúrate de los permisos):
chmod 400 deploy_key.pem
```

---

## 2. Despliegue de la Instancia 1: Backend (Microservicios + BFF)

La instancia de backend albergará los 8 contenedores Java/Spring Boot (`bff-service`, `citas-service`, `consultas-service`, `fichas-service`, `notificaciones-service`, `reportes-service`, `usuarios-service`, `clinicas-service`).

### Paso 2.1: Copiar la carpeta backend a la EC2 Backend

Desde tu máquina local:

```bash
scp -i terraform/deploy_key.pem -r backend/ ubuntu@<BACKEND_PUBLIC_IP>:~/backend/
```

*(O clona directamente el repositorio en la instancia con `git clone`)*

### Paso 2.2: Conectarse por SSH a la EC2 Backend

```bash
ssh -i terraform/deploy_key.pem ubuntu@<BACKEND_PUBLIC_IP>
```

### Paso 2.3: Configurar variables de entorno

```bash
cd ~/backend
cp .env.example .env
nano .env
```

Configura en `.env`:
- `AZURE_ISSUER`: Issuer de Microsoft Entra ID.
- `AZURE_AUDIENCE`: Audiencia (`api://telemedicina-rural-api` o el Client ID).
- `FRONTEND_ORIGIN`: `http://<FRONTEND_PUBLIC_IP>` (la IP de la EC2 Frontend para autorizar CORS).

### Paso 2.4: Iniciar los microservicios con Docker Compose

```bash
docker compose up -d --build
```

### Paso 2.5: Comprobar funcionamiento

```bash
# Ver contenedores en ejecución
docker compose ps

# Probar endpoint público del BFF
curl http://localhost:8080/api/bff/auth/status
```

---

## 3. Despliegue de la Instancia 2: Frontend (React 18 + Vite + Nginx)

La instancia de frontend compilará la SPA y la servirá mediante Nginx en el puerto 80.

### Paso 3.1: Configurar el proxy Nginx con la IP del Backend

En tu máquina local (o en la instancia), edita `frontend/nginx.conf` y reemplaza `BACKEND_HOST` por la IP pública de la EC2 Backend:

```nginx
location /api/ {
    proxy_pass http://<BACKEND_PUBLIC_IP>:8080/api/;
    ...
}
```

### Paso 3.2: Copiar la carpeta frontend a la EC2 Frontend

```bash
scp -i terraform/deploy_key.pem -r frontend/ ubuntu@<FRONTEND_PUBLIC_IP>:~/frontend/
```

### Paso 3.3: Conectarse por SSH a la EC2 Frontend

```bash
ssh -i terraform/deploy_key.pem ubuntu@<FRONTEND_PUBLIC_IP>
```

### Paso 3.4: Configurar variables de entorno

```bash
cd ~/frontend
cp .env.example .env
nano .env
```

Configura en `.env`:
```env
VITE_AZURE_CLIENT_ID=<CLIENT_ID_SPA>
VITE_AZURE_TENANT_ID=<TENANT_ID>
VITE_AZURE_AUTHORITY=https://<tenant>.ciamlogin.com/<tenant-id>/<user-flow>
VITE_AZURE_API_SCOPE=api://telemedicina-rural-api/read
VITE_AZURE_REDIRECT_URI=http://<FRONTEND_PUBLIC_IP>
```

### Paso 3.5: Compilar y levantar la SPA con Nginx

```bash
docker compose up -d --build
```

### Paso 3.6: Comprobar funcionamiento

Abre tu navegador e ingresa a:
```
http://<FRONTEND_PUBLIC_IP>
```

---

## 4. Resumen de Puertos y Flujo de Comunicación

| Componente | Instancia EC2 | Puerto | Función |
|---|---|---|---|
| **BFF Gateway** | Backend EC2 | `8080` | Entrada principal a los microservicios y Spring Security |
| **Microservicios (7)** | Backend EC2 | `8081-8087` | Lógica interna (Citas, Consultas, Fichas, etc.) |
| **Nginx Web Server** | Frontend EC2 | `80` | Entrega de bundle React SPA y proxy hacia Backend |
| **SSH** | Ambas | `22` | Acceso seguro administrativo con llave SSH |
