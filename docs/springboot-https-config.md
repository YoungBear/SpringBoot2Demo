[TOC]

# spring-boot https证书双向认证配置



本文主要介绍在spring-boot工程中配置https证书双向认证。包含生成自签名证书命令，配置yml等。

**注意：**

- 该文章使用自签名证书，仅作为开发验证使用，实际现网场景请从CA机构申请证书。
- 文章中的命令均在linux环境下执行。
- openssl版本为 OpenSSL 1.1.1k  FIPS 25 Mar 2021。
- keytool对应jre版本为 1.8.0_401。

## 1. 创建CA证书

创建CA证书。相关文件：

- rootca.key CA证书私钥。
- rootca.crt CA证书。
- rootca.p12 PKCS12格式的信任证书库。
- truststore.jks JKS格式的信任证书库。

```shell
# 1. 创建CA私钥 RootCaKey@2024 生成 rootca.key
openssl genpkey -aes256 -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out rootca.key -pass pass:'RootCaKey@2024'
# 2. 创建CA证书请求 生成 rootca.crt
openssl req -x509 -days 3650 -sha256 -key rootca.key -passin pass:'RootCaKey@2024' -out rootca.crt -subj "/C=CN/CN=demorootca.demo.com"
## 生成并导入信任证书库 PKCS12 生成 rootca.p12 TrustStore@2024
keytool -import -noprompt -trustcacerts -alias rootca -file rootca.crt -keystore rootca.p12 -storetype PKCS12 -storepass 'TrustStore@2024'
### 查看
keytool -list -v -keystore rootca.p12 -storetype PKCS12 -storepass 'TrustStore@2024'
## 生成并导入信任证书库 JKS 生成 truststore.jks
keytool -import -noprompt -trustcacerts -alias rootca -file rootca.crt -keystore truststore.jks -storetype JKS -storepass 'TrustStore@2024'
### 查看
keytool -list -v -keystore truststore.jks -storetype JKS -storepass 'TrustStore@2024'
```



## 2. 签发服务端证书

生成服务端证书 server.crt 并且使用ca证书签发。可以使用命令验证。

```shell
openssl genpkey -aes256 -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out server.key -pass pass:'ServerKey@2024'
openssl req -new -key server.key -passin pass:'ServerKey@2024' -out server.csr -subj "/C=CN/CN=server.demo.com"
openssl x509 -req -in server.csr -CA rootca.crt -CAkey rootca.key -passin pass:'RootCaKey@2024' -CAcreateserial -out server.crt -days 3650 -sha256
## 使用CA证书验证服务端证书
openssl verify -verbose -CAfile rootca.crt server.crt
```





## 3. 签发客户端证书

生成客户端证书 client.crt 并且使用ca证书签发。可以使用命令验证。

```shell
openssl genpkey -aes256 -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out client.key -pass pass:'ClientKey@2024'
openssl req -new -key client.key -passin pass:'ClientKey@2024' -out client.csr -subj "/C=CN/CN=client.demo.com"
openssl x509 -req -in client.csr -CA rootca.crt -CAkey rootca.key -passin pass:'RootCaKey@2024' -CAcreateserial -out client.crt -days 3650 -sha256
## 使用CA证书验证客户端证书
openssl verify -verbose -CAfile rootca.crt client.crt
```





## 4. 生成PKCS12服务端证书

通过 openssl 命令生成服务端用的证书 server.p12 和客户端用到的证书 client.p12。可以使用 keytool 命令查看。

即

- server.key+server.crt+rootca.crt -> server.p12

- client.key+client.crt+rootca.crt -> client.p12

```shell
openssl pkcs12 -export -inkey server.key -passin pass:'ServerKey@2024' -in server.crt -chain -CAfile rootca.crt -out server.p12 -password pass:'ServerKeyStore@2024'
## 查看 server.p12证书
keytool -list -v -keystore server.p12 -storepass 'ServerKeyStore@2024'
# 生成PKCS12客户端证书
openssl pkcs12 -export -inkey client.key -passin pass:'ClientKey@2024' -in client.crt -chain -CAfile rootca.crt -out client.p12 -password pass:'ClientKeyStore@2024'
## 查看 client.p12 证书
keytool -list -v -keystore client.p12 -storepass 'ClientKeyStore@2024'
```



## 5. 配置spring-boot工程

将前边生成的证书 server.p12,truststore.jks,rootca.p12文件，拷贝到 src/main/resources/cert/ 目录下。并修改 application.yml，内容如下：

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:cert/server.p12
    key-store-password: 'ServerKeyStore@2024'
    key-store-type: PKCS12
    key-store-provider: SUN
    enabled-protocols: TLSv1.2,TLSv1.3
    trust-store: classpath:cert/truststore.jks
#    trust-store: classpath:cert/rootca.p12
    trust-store-password: 'TrustStore@2024'
    trust-store-type: PKCS12
    trust-store-provider: SUN
    client-auth: need
  servlet:
    context-path: /SpringBoot2Demo
  port: 8888

logging:
  level:
    root: info
```



## 6. 验证请求

使用curl命令和使用postman均可以验证，需要配置客户端证书。

```shell
# 使用命令验证
curl -k --cert-type P12 --cert ./client.p12:'ClientKeyStore@2024' --location --request GET 'https://localhost:8888/SpringBoot2Demo/demo/current'
```

使用postman的时候，需要将 Settings->General->REQUEST->SSL certificate verification 开关关掉，即不校验SSL服务端证书。

## [源代码地址github](https://github.com/YoungBear/SpringBoot2Demo)
## [源代码地址gitee](https://gitee.com/YoungBear2023/SpringBoot2Demo)