# Tui Challenge

Spring Webflux and kotlin were the chosen technologies to complete this challenge. The service code can
be found at [RepositoryService.kt](./src/main/kotlin/com/tui/githubrepos/service/RepositoryService.kt), and
it was implemented in a non-blocking way to improve performance.

- talk about the .env file
- swagger ui em /swagger-ui.html (generated using ..)
- swagger docs em /v3/api-docs
- create api spec automatically using ./gradlew generateOpenApiDocs
    - we can see it in /build/openapi.json
- Dockerfile
    - docker build -t github-repos:latest .
    - docker run --env-file .env.local.docker -p 8090:8080 --name github-app github-repos

TODO:

- custom error handling for not acceptable media type
- add more information in the controller for swagger
- add native image support