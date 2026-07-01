FROM node:16-alpine AS builder

WORKDIR /workspace

COPY . .

RUN if [ -f frontend/package.json ]; then \
      mkdir -p /app && cp -a frontend/. /app/; \
    else \
      mkdir -p /app && cp -a . /app/; \
    fi

WORKDIR /app

RUN npm install
RUN npm run build:prod

RUN if [ -f /workspace/deploy/nginx/nginx.conf ]; then \
      cp /workspace/deploy/nginx/nginx.conf /tmp/default.conf; \
    else \
      printf '%s\n' \
        'server {' \
        '    listen 80;' \
        '    server_name _;' \
        '    client_max_body_size 200m;' \
        '    root /usr/share/nginx/html;' \
        '    index index.html;' \
        '    location / {' \
        '        try_files $uri $uri/ /index.html;' \
        '    }' \
        '    location = /api/websocket {' \
        '        proxy_pass http://backend:8080/websocket;' \
        '        proxy_http_version 1.1;' \
        '        proxy_set_header Host $host;' \
        '        proxy_set_header X-Real-IP $remote_addr;' \
        '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' \
        '        proxy_set_header X-Forwarded-Proto $scheme;' \
        '        proxy_set_header Upgrade $http_upgrade;' \
        '        proxy_set_header Connection "upgrade";' \
        '    }' \
        '    location /api/ {' \
        '        proxy_pass http://backend:8080;' \
        '        proxy_http_version 1.1;' \
        '        proxy_set_header Host $host;' \
        '        proxy_set_header X-Real-IP $remote_addr;' \
        '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' \
        '        proxy_set_header X-Forwarded-Proto $scheme;' \
        '        proxy_set_header Upgrade $http_upgrade;' \
        '        proxy_set_header Connection "upgrade";' \
        '    }' \
        '    location /websocket {' \
        '        proxy_pass http://backend:8080;' \
        '        proxy_http_version 1.1;' \
        '        proxy_set_header Host $host;' \
        '        proxy_set_header X-Real-IP $remote_addr;' \
        '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' \
        '        proxy_set_header X-Forwarded-Proto $scheme;' \
        '        proxy_set_header Upgrade $http_upgrade;' \
        '        proxy_set_header Connection "upgrade";' \
        '    }' \
        '}' > /tmp/default.conf; \
    fi

FROM nginx:alpine

COPY --from=builder /tmp/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
