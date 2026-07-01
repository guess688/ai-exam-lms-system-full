FROM node:16-alpine AS builder

WORKDIR /app

COPY frontend/package*.json ./

RUN npm install

COPY frontend/ ./

RUN npm run build:prod

FROM nginx:alpine

COPY deploy/nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
