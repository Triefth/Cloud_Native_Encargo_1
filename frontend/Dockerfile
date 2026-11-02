# Dockerfile multi-stage para construir la aplicación React con Vite y servirla con Nginx
FROM node:18-alpine AS build

WORKDIR /app

# Instalar dependencias
COPY package*.json ./
RUN npm ci

# Variables de entorno en tiempo de build de Vite
ARG VITE_AZURE_CLIENT_ID
ARG VITE_AZURE_TENANT_ID
ARG VITE_AZURE_AUTHORITY
ARG VITE_AZURE_API_SCOPE
ARG VITE_AZURE_REDIRECT_URI

ENV VITE_AZURE_CLIENT_ID=$VITE_AZURE_CLIENT_ID
ENV VITE_AZURE_TENANT_ID=$VITE_AZURE_TENANT_ID
ENV VITE_AZURE_AUTHORITY=$VITE_AZURE_AUTHORITY
ENV VITE_AZURE_API_SCOPE=$VITE_AZURE_API_SCOPE
ENV VITE_AZURE_REDIRECT_URI=$VITE_AZURE_REDIRECT_URI

# Copiar código fuente y compilar
COPY . .
RUN chmod +x node_modules/.bin/* || true
RUN chmod -R +x node_modules/.bin || true
RUN npm run build

# Etapa final: Servidor web ultraligero Nginx con soporte HTTPS
FROM nginx:alpine

# Generar certificado SSL autofirmado para habilitar HTTPS en la IP pública de AWS
# Necesario para que el navegador habilite Web Crypto (window.crypto.subtle) para Azure Entra ID / MSAL
RUN apk add --no-cache openssl && \
    mkdir -p /etc/nginx/ssl && \
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/nginx.key \
    -out /etc/nginx/ssl/nginx.crt \
    -subj "/C=CL/ST=RM/L=Santiago/O=Telemedicina/CN=telemedicina"

# Copiar bundle compilado al directorio de Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuración optimizada de Nginx para SPA (HTTP + HTTPS)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
