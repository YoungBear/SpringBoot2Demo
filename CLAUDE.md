# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build and run tests
./mvnw clean verify

# Run without tests
./mvnw clean package -DskipTests

# Run only tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=DemoApplicationTests

# Run the application (HTTPS on port 8888, context path /SpringBoot2Demo)
./mvnw spring-boot:run
```

## Architecture

This is a **Spring Boot 3.5.14 + JDK 21** demo project showcasing **mutual TLS (mTLS)** with self-signed certificates.

**Single-module Maven project** with two Java classes:
- `DemoApplication.java` — Main class + `@RestController`. Registers BouncyCastle security provider in a static block (required for PKCS12 keystore with BC provider), starts Spring Boot, and exposes `GET /demo/current` which returns current time as JSON.
- `DemoApplicationTests.java` — Context load test.

**HTTPS/mTLS configuration** (`src/main/resources/application.yml`):
- Server listens on port 8888 with `/SpringBoot2Demo` context path
- Requires client certificate authentication (`client-auth: need`)
- Server keystore: `cert/server.p12` (PKCS12, BC provider)
- Trust store: `cert/truststore.jks` (JKS, SUN provider)
- TLS 1.2 and 1.3 enabled

**Dependencies**: spring-boot-starter-web, spring-boot-starter-test, bcprov-jdk18on (Bouncy Castle).

**Certificates** are pre-generated self-signed certs stored in `src/main/resources/cert/`. The `files/http_cert_2024/` directory contains the original certificate generation source files and the `docs/` directory has the full step-by-step guide for regenerating them.

## Important: Untracked Files

The `files/` and `docs/` directories are **not tracked in git** (present locally but gitignored or never committed). They contain:
- `docs/springboot-https-config.md` — Full guide for generating self-signed CA, server, and client certificates
- `files/http_cert_2024/` — Original certificate files (keys, CSRs, CRTs, P12, JKS)
