# ---- Build frontend ----
FROM node:20 AS build-frontend
WORKDIR /app

# Copy package files first (better caching)
COPY frontend/package*.json ./
RUN npm ci --silent || npm install --silent

# Copy frontend source and build
COPY frontend/ ./
RUN npm run build

# ---- Nginx stage ----
FROM nginx:1.27-alpine
COPY --from=build-frontend /app/dist/ /usr/share/nginx/html/
COPY nginx/nginx.conf /etc/nginx/nginx.conf
