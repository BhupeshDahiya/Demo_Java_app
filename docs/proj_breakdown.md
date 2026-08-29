# DevOps Java Application — Questions & Answers
## 1. Does my GitHub Actions pipeline depend on the Dockerfile in my repository?
Yes. the pipeline explicitly runs:
```bash
- name: Build and tag the image
  env:
    REGISTRY: ${{ steps.login-ecr.outputs.registry }}
  run: |
    docker build -t $REGISTRY/$REPOSITORY:$IMAGE_TAG .
```
The . means Docker uses the Dockerfile in the repository's root directory as the build instructions.

So the flow is:
```bash
GitHub Actions
      |
      v
docker build .
      |
      v
Dockerfile
      |
      v
Docker image
      |
      v
Trivy scan
      |
      v
Amazon ECR
```
## 2. Why did Trivy initially fail with $REGISTRY/$REPOSITORY:$IMAGE_TAG?
Trivy was receiving the literal string:
```bash
$REGISTRY/$REPOSITORY:$IMAGE_TAG
```
instead of the actual image name.

The problem was that the variables were not being expanded correctly inside the Trivy action configuration.

later logs showed the correct value:
```bash
INPUT_IMAGE_REF: ***.dkr.ecr.us-east-1.amazonaws.com/java-demo-app:a1bdd...
```
and:
```bash
Running Trivy with options:
trivy image ***.dkr.ecr.us-east-1.amazonaws.com/java-demo-app:a1bdd...
```
That confirmed the image reference was being passed correctly.

## 3. Why was the Trivy binary cache taking so long?
The first run had:
```bash
Cache not found for input keys:
trivy-binary-v0.70.0-Linux-X64
```
This is not an error.

It simply means GitHub Actions did not have a previously cached Trivy binary.

The setup action therefore:
```bash
Download Trivy
      |
      v
Install Trivy
      |
      v
Save Trivy binary to GitHub Actions cache
```
Once the cache was successfully created, future workflow runs can reuse the binary.

## 4. Why did Trivy download a 109 MB vulnerability database?
Trivy needs a vulnerability database to determine whether packages inside your image contain known vulnerabilities.

The log showed:
```bash
[vulndb] Need to update DB
[vulndb] Downloading vulnerability DB...
```
It downloaded approximately:
```bash
109.27 MiB
```
This is normal.

## 5. Why did Trivy download an approximately 912 MB Java database?
The Docker image contains a Java application:
```bash
app/app.jar
```
Trivy therefore performs Java dependency vulnerability scanning.
The log showed:
```bash
[javadb] Downloading Java DB...
```
and:
```bash
911.95 MiB
```
This database is used to identify vulnerabilities in Java libraries contained inside your JAR.

Trivy also reported:
```bash
Java DB is cached for 3 days.
```
Therefore, subsequent scans should normally be faster while the cached database remains valid.

## 6. Why did Trivy originally find 32 Java vulnerabilities?
The original pom.xml used:
```bash
<version>3.2.4</version>
```
for Spring Boot.

That caused the application to pull older versions of Spring Framework, Tomcat, Jackson, Micrometer, and other dependencies.

Trivy identified vulnerabilities in dependencies such as:
```bash
Jackson
Tomcat
Spring Boot
Spring Framework
Micrometer
```

The issue was not that Java code was necessarily bad.

The main problem was that the application was built using outdated dependencies.

## 7. Why did Trivy later report 0 vulnerabilities in the Java JAR?
After updating the application dependencies, Trivy reported:
```bash
app/app.jar
Type: jar
Vulnerabilities: 0
```
This means Trivy did not find any vulnerabilities matching its database and configured severity criteria in the Java dependencies contained in the application JAR.

This is exactly what we wanted.

## 8. Why did Trivy still find vulnerabilities after the Java dependencies were fixed?
The Docker image had:
```bash
alpine 3.24.1
```
Trivy reported:
```bash
Total: 3
HIGH: 3
CRITICAL: 0
```
The vulnerabilities were in:
```bash
libcrypto3
libssl3
openssl
```
These are operating-system packages inside the Alpine Linux base image.

They are not Java dependencies.

Therefore, fixing pom.xml does not necessarily fix vulnerabilities in the Docker base image.

I had two separate layers:
```bash
Docker Image
|
+-- Alpine Linux
|     |
|     +-- openssl
|     +-- libcrypto3
|     +-- libssl3
|
+-- Java application
      |
      +-- Spring Boot
      +-- Tomcat
      +-- Jackson
      +-- Micrometer
```
## 9. Why did apk upgrade help fix the Alpine vulnerability?
The Dockerfile was changed to:
```bash
FROM eclipse-temurin:21-jre-alpine-3.24

RUN apk update && apk upgrade

WORKDIR /app
COPY target/devops-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
`apk` is Alpine Linux's package manager.

This:
```bash
RUN apk update && apk upgrade
```
tells Alpine to update its package indexes and upgrade installed packages to available patched versions.

This resulted in the next Trivy scan reporting:
```bash
Alpine vulnerabilities: 0
Java vulnerabilities: 0
```
Therefore the security scan passed.

## 10. Why did the Dockerfile had two different stages?
A multi-stage Dockerfile separates building the application from running the application.

```bash
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /artifact
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine-3.24
WORKDIR /app
COPY --from=builder /artifact/target/devops-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
The first stage:
```bash
Maven + JDK
      |
      v
Builds the JAR
```
The second stage:
```bash
JRE
      |
      v
Runs the JAR
```
The advantage is that the final image does not need the entire Maven build environment.

## 11. What is the final DevOps flow of this project?
The project now demonstrates a complete CI/CD and GitOps-style workflow:
```bash
Developer pushes code
         |
         v
GitHub repository
         |
         v
GitHub Actions
         |
         +----------------------+
         |                      |
         v                      v
Maven build/test          Docker build
                                |
                                v
                          Trivy security scan
                                |
                        +-------+-------+
                        |               |
                     FAIL             PASS
                        |               |
                      STOP              v
                                  Push image
                                     to ECR
                                        |
                                        v
                               Update Kubernetes
                               image tag in
                               GitOps repo
                                        |
                                        v
                                 Argo CD detects
                                 Git change
                                        |
                                        v
                                 Deploy to EKS
```

The point of the project is to demonstrate:
```bash
Java application
      +
Maven
      +
Docker
      +
Trivy
      +
AWS ECR
      +
Kubernetes
      +
EKS
      +
Argo CD / GitOps
      +
GitHub Actions
```