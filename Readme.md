### Manually editing pom.xml file everytime when nexus ip changes due to new infra or new project or any other senario is a pain therefore.

What you should do instead: Parameterize those URLs using a custom Maven property. Ive replaced YOUR_NEXUS_IP:8081 with a placeholder variable like ${nexus.ip}:8081 inside pom.xml

How this helps : When you run your compilation step inside your Jenkinsfile later, you can dynamically inject the current active Nexus private IP straight from an environment variable using the standard Maven define flag:
`mvn deploy -Dnexus.ip=${NEXUS_PRIVATE_IP}`
This keeps your code 100% static and lets your infrastructure remain completely dynamic. 