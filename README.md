# TUI Challenge

Hi! In this file you will find the most important information about the challenge resolution.

## Chosen technologies

I choose to use [Spring Webflux](https://www.baeldung.com/spring-webflux), which provides reactive programming support
for web applications, and kotlin to implement this challenge. I
created the Dockerfile
to build the docker image, sending it to the AWS ECR to be used by the ECS.
The [Jenkinsfile](./Jenkinsfile) contains the description of the pipeline used to build, test and deploy
the application to AWS. Note: I had Jenkins in an ec2 instance, but I had to forget about that
because the instance didn't have enough memory to build the project, so I just installed locally.

## Application Development

The application only had one endpoint, so it was really simple to implement. If we think in an
up-down approach, we start by the
[RepositoryController.kt](./src/main/kotlin/com/tui/githubrepos/controller/RepositoryController.kt)
where we define the endpoint and pass the request to the service layer. The
[RepositoryService.kt](./src/main/kotlin/com/tui/githubrepos/service/RepositoryService.kt) is then
responsible for calling a GitHub client wrapper(
[GithubClient.kt](./src/main/kotlin/com/tui/githubrepos/httpclient/GithubClient.kt)
) that will call the GitHub API and retrieve the data. Webflux allows us to do this in a non-blocking
way, so we can have better performance.

To run the application locally, have in mind that you need to have a **GITHUB_TOKEN** to be able to
call the GitHub API. I had a **.env.local** file with the environment variables.

```shell
$ source .env.local
$ ./gradlew bootRun
```

I tested the service layer using **unit tests** available at
[RepositoryServiceTest.kt](./src/test/kotlin/com/tui/githubrepos/service/RepositoryServiceTest.kt).
The main purpose of these tests is to check if the service logic is working as expected. It is using
**Mockito** to mock the response from the *GitHub client* and also **JUnit** to run the tests.
In terms of **integration tests**, I created the test class
[RepositoryControllerTest.kt](./src/test/kotlin/com/tui/githubrepos/controller/RepositoryControllerTest.kt)
that is responsible for loading the whole application and test the endpoint. It has one test
because I only wanted to see if the endpoint was working with my own GitHub profile.
It was pointless to test error handling in the previous tests because, for example, it's a huge
gamble to put a GitHub username that doesn't exist, today it doesn't exist, but tomorrow is another day.
So, in cases like these, I mock the service and test the error handling in the controller.
Simple enough, but this time I was spending a little too much time building the class, for some
reason it was not compiling. Either way, the idea is in the file
[RepositoryControllerMockTest.kt](./src/test/kotlin/com/tui/githubrepos/controller/RepositoryControllerMockTest.kt).
The premise of the tests was created, it lacks the implementation.

Normally I have two different *gradle tasks* to run the tests, one for unit tests and another for
integration tests. But this time, to simplify, it's only one task.

```shell
$ ./gradlew test
```

There was emphasis on how to deal when a GitHub user doesn't exist. I decided to create a custom
exception that is thrown when the *http client* returns a *404* status code. The exception is then
caught by the following
[ExceptionHandler.kt](./src/main/kotlin/com/tui/githubrepos/exception/handler/ExceptionHandler.kt).
The response is custom just like it was asked in the challenge description.

```json
{
  "status": 404,
  "message": "Username not found: JohnDoe"
}
```

In the case of the client trying to request data in *XML* format, I handled it by telling the
endpoint to produce only *JSON* responses.

```kotlin
@GetMapping("/{username}", produces = [MediaType.APPLICATION_JSON_VALUE])
```

In reality, *Spring Webflux* doesn't support *XML* responses natively, there is some workarounds
as we can see in the
following [link](https://stackoverflow.com/questions/55306194/springboot-webflux-cannot-return-application-xml).
The problem with these workarounds is that they are not intuitive, and when I implemented them they simply didn't work.
We have limited time to complete the challenge, so I decided that the following
message was okay enough. I know the implications of having different formatted responses,
specially for the ones consuming the API, it's not good, and I would never deliver it like this.

```shell
$ curl -H "Accept: application/xml"  http://github-githu-figog1abfakw-1126930585.eu-central-1.elb.amazonaws.com/api/repository/JohnDoe
```

```json
{
  "timestamp": "2024-01-15T22:55:50.391+00:00",
  "path": "/repository/JohnDoe",
  "status": 406,
  "error": "Not Acceptable",
  "requestId": "68bf40e6-4"
}
```

## Code Quality & Standards

The code was written having in mind the best practices and clean code. I followed the
[Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and the
comments are compliant with **JavaDoc**. The other practices are inside the code, and are better
seen than explained.

## Swagger

The **Swagger** documentation is generated automatically using a gradle task. I copied the generated
[swagger file](./swagger.yaml) from the *build* folder for us to have an example.

```shell
$ ./gradlew generateOpenApiDocs
```

This automatic generation can be useful later if we want to customize the **API Gateway**.
With the swagger file we know exactly which endpoints we have and what are the expected responses.

Swagger also has a **UI** component that we can find in the following
[link](http://github-githu-figog1abfakw-1126930585.eu-central-1.elb.amazonaws.com/api/webjars/swagger-ui/index.html).
It's also generated automatically when running the application.

---

## Docker

The [Dockerfile](./Dockerfile) is very simple, it just copies the **jar** file and runs it. I also
added comments to explain what we could do if we wanted also to build the jar. There is also the
option of creating a native image, that basically uses [GraalVM](https://www.graalvm.org/) to
compile the application to a native image, but it was a little of the context (and I'm not sure if
it works seamlessly with Spring Webflux). I decided to only copy the jar and not build it because
we have the **Jenkins pipeline** for that.

Locally, we can run and build the docker image with the following commands:

```shell
$ docker build -t github-repos:latest .
$ docker run --env-file .env.local.docker -p 8090:8080 --name github-app github-repos
```

## AWS & CloudFormation

I used the [AWS CDK](https://aws.amazon.com/cdk/) to create the infrastructure. It's a framework
that allows to create the infrastructure using code, in the end it generates a **CloudFormation**
template. It is a very simple way to create and manage the resource in AWS as you can see in the
stack file [infra-stack.ts](./infra/lib/infra-stack.ts).

I started by creating the VPC.

```typescript
const vpc = new Vpc(this, "GithubReposVpc", {
    maxAzs: 2,
    natGateways: 1,
});
```

Followed by the ECS cluster.

```typescript
const appCluster = new ecs.Cluster(this, "GithubReposEcs", {
    vpc: vpc,
    clusterName: "GithubReposCluster",
});
```

And then the Fargate service with the ALB.

```typescript
    const sbApp = new ApplicationLoadBalancedFargateService(
    this,
    "GithubReposApp",
    {
        cluster: appCluster,
        desiredCount: 1,
        cpu: 256,
        memoryLimitMiB: 512,
        taskImageOptions: {
            image: ecs.ContainerImage.fromAsset(".."),
            containerPort: 8080,
            secrets: {
                GITHUB_TOKEN: ecs.Secret.fromSecretsManager(
                    appSecrets,
                    "githubToken"
                ),
            },
        },
        assignPublicIp: true,
        publicLoadBalancer: true,
    }
);
```

[//]: # (TODO: remove the publicLoadBalancer)

Lastly, I just created the Rest Api Gateway to connect to the ALB and expose the application.

```typescript
const foobar = "Still in progress";
const barfoo = "It is taking a little longer than expected, but it will be ready soon.";
const last = "Maybe you will not even see this message, I hope you don't :)"
```

## Jenkins

The following [Jenkinsfile](./Jenkinsfile) describes the pipeline used to build, test
and deploy the application. I will also describe the steps here.

Starting by building the application.

```groovy
stage('Build') {
    steps {
        sh './gradlew build -x test'
    }
}
```

Then, we run the tests. If I didn't have cheated with the tests, I would have two different tasks
to run the unit and integration tests.

```groovy
stage('Test') {
    steps {
        sh './gradlew test'
    }
}
```

After tests, it's usually a good idea to
use [SonarQube](https://www.sonarsource.com/products/sonarqube/downloads/lts/8-9-lts/) to analyze the
code for bugs, vulnerabilities, smells, coverage, etc. But I didn't do it, instead I deployed the
application to AWS.

```groovy
stage('Deploy') {
    dir('infra') {
        sh 'npm install'
        sh 'npx cdk deploy --require-approval never'
    }
}
```

It is also good to point out that we can generate the OpenAPI documentation before this stage
and use it to customize the API Gateway.

## Final considerations

I had a lot of fun doing this challenge. I hope it causes good impressions, and if it doesn't, I
appreciate the opportunity to do it. Bye, have a good day!

[//]: # (change localhost to deployment url TODO)
