
# Image Optimizing Reverse Proxy - Starter Project

Includes:
- Backend: Spring Boot WebFlux, Thumbnailator, Reactive Redis, R2DBC Postgres (simple schema init)
- Frontend: React (Vite) - upload + URL preview + download
- Infra: Nginx config to serve frontend and proxy /api to backend (with proxy cache)
- Docker Compose to run Postgres, Redis, Backend, and Nginx (frontend built into Nginx image)

Run:
1. Copy `.env.example` to `.env` and edit if necessary.
2. `docker compose build`
3. `docker compose up -d`
4. Visit http://localhost/
