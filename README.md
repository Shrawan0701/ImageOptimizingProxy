# Image Optimizing Proxy

A full-stack image optimization proxy that allows resizing, format conversion, and caching of images from URLs or file uploads. Built with **React**, **Spring Boot (WebFlux)**, **Docker**, **Nginx**, **Redis**, and **PostgreSQL**.

---

## Features

- Optimize images from a URL or by file upload.
- Resize images by width and height.
- Convert image formats (`webp`, `jpeg`, `png`).
- Adjust image quality (`1-100`).
- ETag support for browser caching.
- Caching of transformed images via Nginx and Redis.
- Logs stored in PostgreSQL.
- Fully containerized with Docker Compose for easy deployment.

---

## Tech Stack

- **Frontend:** React, Tailwind CSS (optional)
- **Backend:** Spring Boot, WebFlux, Java 17
- **Database:** PostgreSQL
- **Cache:** Redis
- **Proxy & Static Serving:** Nginx
- **Containerization:** Docker

---

## Getting Started

1. **Clone the repository:**

```bash
git clone https://github.com/Shrawan0701/ImageOptimizingProxy.git
cd ImageOptimizingProxy
```
2. **Create your local .env file from .env.example:**
   
```bash
cp .env.example .env
```
3. **Build and start all services:**
```bash
docker compose up --build -d
```
4. **Open Frontend:**
```bash
http://localhost/
```

## Usage

1. **From URL:**
Enter the image URL.

Set width, height, format, quality.

Click Preview to see the optimized image.

2. **Upload File:**

Select an image from your device.

Set optional transformation parameters.

Click Upload to preview and download the optimized image.

3. **Caching:**

Nginx cache: /var/cache/nginx

Max size: 1 GB

Inactive duration: 30 days

Redis: Stores cache lookups for faster image retrieval.

ETag headers: Allow browsers to cache unchanged images.

4. **Docker Compose Services:**

frontend – React app served via Nginx

backend – Spring Boot API

postgres – Database

redis – Cache

nginx – Reverse proxy and static file server

## Screenshot

<img width="1824" height="912" alt="proxy" src="https://github.com/user-attachments/assets/723187fc-f029-4a0a-b62a-35ea27846ede" />

