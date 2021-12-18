FROM clojure:openjdk-17-tools-deps-slim-bullseye AS builder

WORKDIR /tmp
RUN apt update && apt upgrade -y && \
    apt install -y apt-utils curl make git rlwrap unzip && \
    rm -rf /var/lib/apt/lists/*

RUN curl -sL https://deb.nodesource.com/setup_16.x | bash - && apt install -y nodejs

RUN curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install && \
    chmod +x install && ./install


WORKDIR /project
COPY .git .
COPY modules/frontend ./modules/frontend
COPY version.tmpl .


WORKDIR /project/modules/frontend
RUN  bb setup && bb build && bb dist



FROM nginx:1.21.4-alpine AS runner

WORKDIR /usr/share/nginx/html
COPY --from=builder /project/modules/frontend/dist .

EXPOSE 80
