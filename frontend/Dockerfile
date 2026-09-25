# Multi-stage Dockerfile for DevOpsAI React Frontend

# Stage 1: Build Stage
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci --prefer-offline --no-audit

# Copy application source code and build Vite bundle
COPY . .
RUN npm run build

# Stage 2: Nginx Web Server Runtime
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default Nginx static assets
RUN rm -rf ./*

# Copy built bundle from builder stage
COPY --from=builder /app/dist .

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose HTTP port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
