# Stage 1: Build the Spring Boot application
FROM openjdk:17-slim AS build

ARG JAR_FILE=build/libs/housekeeping-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Stage 2: Set up Nginx, Spring Boot application, and Certbot
FROM nginx:alpine

# Install required tools, Java, and Certbot
RUN apk add --no-cache openjdk17-jre certbot certbot-nginx

# Copy the built JAR file and Nginx config
COPY --from=build /app.jar /app.jar
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Set up directories for Certbot
RUN mkdir -p /var/www/certbot /etc/letsencrypt \
    && touch /etc/letsencrypt/options-ssl-nginx.conf

# Expose ports for HTTP and HTTPS
EXPOSE 80 443

# Start Nginx, Certbot, and the Spring Boot application
CMD ["sh", "-c", "certbot certonly --webroot --webroot-path=/var/www/certbot --non-interactive --agree-tos --email djawlgns7@gmail.com -d back.bit-two.com && java -jar /app.jar & nginx -g 'daemon off;'"]
