# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 构建与运行

```bash
# 构建并运行测试
./mvnw clean verify

# 跳过测试进行打包
./mvnw clean package -DskipTests

# 仅运行测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=DemoApplicationTests

# 运行应用（HTTPS 端口 8888，上下文路径 /SpringBoot2Demo）
./mvnw spring-boot:run
```

## 架构

这是一个基于 **Spring Boot 3.5.14 + JDK 21** 的演示项目，展示使用自签名证书配置 **双向 TLS (mTLS)**。

**单模块 Maven 项目**，包含两个 Java 类：
- `DemoApplication.java` — 主类 + `@RestController`。在 static 块中注册 BouncyCastle 安全提供程序（PKCS12 密钥库使用 BC 提供程序所必需），启动 Spring Boot，并暴露 `GET /demo/current` 接口，以 JSON 格式返回当前时间。
- `DemoApplicationTests.java` — 上下文加载测试。

**HTTPS/mTLS 配置**（`src/main/resources/application.yml`）：
- 服务监听端口 8888，上下文路径 `/SpringBoot2Demo`
- 要求客户端证书认证（`client-auth: need`）
- 服务端密钥库: `cert/server.p12`（PKCS12，BC 提供程序）
- 信任库: `cert/truststore.jks`（JKS，SUN 提供程序）
- 启用 TLS 1.2 和 1.3

**依赖**: spring-boot-starter-web、spring-boot-starter-test、bcprov-jdk18on（Bouncy Castle）。

**证书**为预生成的自签名证书，存放于 `src/main/resources/cert/`。`files/http_cert_2024/` 目录包含原始证书生成源文件，`docs/` 目录包含重新生成证书的完整分步指南。

## 重要：未跟踪文件

`files/` 和 `docs/` 目录**未被 git 跟踪**（本地存在但被 gitignore 或从未提交）。它们包含：
- `docs/springboot-https-config.md` — 自签名 CA、服务端及客户端证书生成的完整指南
- `files/http_cert_2024/` — 原始证书文件（密钥、CSR、CRT、P12、JKS）
