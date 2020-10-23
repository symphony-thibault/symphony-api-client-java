# BDK Extension App Spring Boot Starter
The Symphony BDK for Java provides a _Starter_ module that aims to ease extension app backend developments within a
[Spring Boot](https://spring.io/projects/spring-boot) application.
 
## Features
- Configure extension app through `application.yaml`
- Expose a REST API to perform [Circle of Trust authentication](https://developers.symphony.com/extension/docs/application-authentication).

## Installation

The following listing shows the `pom.xml` file that has to be created when using Maven:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>bdk-app-spring-boot</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>bdk-app-spring-boot</name>
    
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.symphony.platformsolutions</groupId>
                <artifactId>symphony-bdk-bom</artifactId>
                <version>1.3.2.BETA</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>com.symphony.platformsolutions</groupId>
            <artifactId>symphony-bdk-app-spring-boot-starter</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>2.3.4.RELEASE</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```
The following listing shows the `build.gradle` file that has to be created when using Gradle:
```groovy
plugins {
    id 'java-library'
    id 'org.springframework.boot' version '2.3.4.RELEASE'
}

dependencies {
    implementation platform('com.symphony.platformsolutions:symphony-bdk-bom:1.3.2.BETA')
    
    implementation 'com.symphony.platformsolutions:symphony-bdk-app-spring-boot-starter'
}
```

## Create a Simple Backend for Extension Application
As a first step, you have to initialize your environment through the Spring Boot `src/main/resources/application.yaml` file: 
```yaml
bdk:
    host: acme.symphony.com
    bot:
      username: bot-username
      privateKeyPath: /path/to/rsa/privatekey.pem
      
    app:
        appId: app-id
        privateKeyPath: /path/tp/rsa/app_privatekey.pem

bdk-app:
    auth:
      enabled: true # activate the CircleOfTrust endpoints (default is true)
      jwtCookie:
        enabled: true # activate the jwt cookie storage (default is false)
        expireIn: 1d # jwt cookie duration (default value is 1d, see https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-conversion-duration) 
    cors: # enable Cross-Origin Resource Sharing (CORS) communication
      "[/**]": # url mapping
        allowed-origins: "*" # list of allowed origins path pattern that be specific origins,
        allowed-credentials: true # Access-Control-Allow-Credentials response header for CORS request
        allowed-method: ["POST", "GET"] # list of HTTP methods to allow
        allowed-headers: ["header1", "header2"] # list of headers that a request can list as allowed
        exposed-headers: ["header1", "header2"] # list of response headers that a response can have and can be exposed
        
logging:
  level:
    com.symphony: debug # in development mode, it is strongly recommended to set the BDK logging level at DEBUG
``` 
> You can notice here that the `bdk` property inherits from the [`BdkConfig`](https://javadoc.io/doc/com.symphony.platformsolutions/symphony-bdk-core/latest/com/symphony/bdk/core/config/model/BdkConfig.html) class.

As required by Spring Boot, you have to create an `src/main/java/com/example/bot/ExtAppSpringApplication.java` class:
```java
@SpringBootApplication
public class ExtAppSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExtAppSpringApplication.class, args);
    }
}
```

## Circle of Trust

By configuring the property `bdk.app.auth.enabled=true`, the Application backend will provide Apis for performing
the [Circle of Trust](https://developers.symphony.com/extension/docs/application-authentication) of Symphony:

```yaml
openapi: 3.0.1
info:
  title: Circle of Trust API
  version: 1.4.0-SNAPSHOT
servers:
- url: https://acme.symphony.com
tags:
- name: Circle Of Trust
  description: For extension app authentication. See [Circle of Trust](https://developers.symphony.com/extension/docs/application-authentication) documentation.
paths:
  '/bdk/v1/app/auth':
    post:
      tags:
      - Circle Of Trust
      summary: Application Authenticate
      operationId: appAuth
      requestBody:
        content: {}
      responses:
        200: 
          description: Success
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AppToken'
        400:
          description: Bad Request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        401:
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
  '/bdk/v1/app/tokens':
    post:
      tags:
      - Circle Of Trust
      summary: Validate Tokens
      operationId: validateTokens
      requestBody:
        content:
          application/json:
            schema: 
              $ref: '#/components/schemas/TokenPair'
      responses:
        204: 
          description: Success
          content: {}
        400:
          description: Bad Request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        401:
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  '/bdk/v1/app/jwt':
    post:
      tags:
      - Circle Of Trust
      summary: Validate JWT
      operationId: validateJwt
      requestBody:
        content:
          application/json:
            schema: 
              $ref: '#/components/schemas/Jwt'
      responses:
        200: 
          description: Success
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserInfo'
        400:
          description: Bad Request
          content: 
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        401:
          description: Unauthorized
          content: 
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
components:
  schemas:
    AppToken:
      type: object
      properties:
        appToken:
          type: string
    TokenPair:
      type: object
      properties:
        appToken:
          type: string
        symphonyToken:
          type: string
    Jwt:
      type: object
      properties:
        jwt:
          type: string
    UserInfo:
      type: object
      properties:
        userId:
          type: number
          format: int64
    Error:
      type: object
      properties:
        status:
          type: number
          format: int32
        errorCode:
          type: string
        message:
          type: array
          items: 
            type: string
```
