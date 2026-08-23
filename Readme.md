## This is a Spring Boot demo app built primarily as a DevOps practice project, what it does:

### The App itself (simple REST API on port 8080):

- GET / — returns a JSON response with a "DevOps Demo App Running" message + current timestamp

- GET /health — returns {"status": "UP"} as a health check

- GET /logs-test — fires off WARN and ERROR logs (used to test log pipelines)

- GET /validate?input=... — validates that a query param is non-empty, returns valid/invalid status

### The real purpose is DevOps toolchain integration, not the app logic itself. The project is wired up for:

- Nexus — artifact publishing (releases + snapshots) with the parameterized ${nexus.ip} you see in the README

- SonarQube — code quality scanning via the sonar-maven-plugin

- Prometheus + Actuator — metrics exposure at /actuator/prometheus

- ELK Stack — structured JSON logging via logstash-logback-encoder

- Docker — has a Dockerfile and .dockerignore

- Kubernetes — has a full k8s folder with deployment, service, ingress, HPA, and PVC manifests

## APP RELATED FIXES

### Manually editing pom.xml file everytime when nexus ip changes due to new infra or new project or any other senario is tiresome, so 

What we do instead is Parameterize those URLs using a custom Maven property. by replacing YOUR_NEXUS_IP:8081 with a placeholder variable like ${nexus.ip}:8081 inside pom.xml

How this helps : When you run your compilation step inside your Jenkinsfile later, you can dynamically inject the current active Nexus private IP straight from an environment variable using the standard Maven define flag:
`mvn clean deploy -Dnexus.ip=${NEXUS_PRIVATE_IP}`

## TO push docker img to ECR

- Authenticate docker into ecr using the ECR URI `aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.<region>.amazonaws.com/demo-java-app:v1`
- Build and tag image for ECR `docker build -f Dockerfile -t <account>.dkr.ecr.<region>.amazonaws.com/demo-java-app:v1 .`
- Push to ECR `docker push <account>.dkr.ecr.<region>.amazonaws.com/demo-java-app:v1`