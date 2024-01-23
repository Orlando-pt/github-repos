import * as cdk from "aws-cdk-lib";
import {
  CorsHttpMethod,
  HttpApi,
  HttpMethod,
} from "aws-cdk-lib/aws-apigatewayv2";
import { HttpAlbIntegration } from "aws-cdk-lib/aws-apigatewayv2-integrations";
import { Vpc } from "aws-cdk-lib/aws-ec2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import { ApplicationLoadBalancedFargateService } from "aws-cdk-lib/aws-ecs-patterns";
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { Construct } from "constructs";
import { getNameWithEnv } from "../bin/infra";

export class InfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // VPC
    const vpc = new Vpc(this, getNameWithEnv("GithubReposVpc"), {
      maxAzs: 2,
      natGateways: 1,
      restrictDefaultSecurityGroup: false,
    });

    // ECS Fargate Cluster in the VPC
    const appCluster = new ecs.Cluster(this, getNameWithEnv("GithubReposEcs"), {
      vpc: vpc,
      clusterName: getNameWithEnv("GithubReposCluster"),
    });

    // App Secrets Manager
    const appSecrets = new Secret(this, getNameWithEnv("GithubReposSecrets"), {
      secretName: getNameWithEnv("app/github-repos"),
      secretObjectValue: {
        githubToken: cdk.SecretValue.unsafePlainText(
          process.env.GITHUB_TOKEN || ""
        ),
      },
    });

    // SB app in Fargate + ALB
    const sbApp = new ApplicationLoadBalancedFargateService(
      this,
      getNameWithEnv("GithubReposApp"),
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
        assignPublicIp: false,
        publicLoadBalancer: false,
      }
    );

    // Health Check for ALB
    sbApp.targetGroup.configureHealthCheck({
      path: "/api/actuator/health",
      interval: cdk.Duration.seconds(30),
      timeout: cdk.Duration.seconds(10),
      healthyThresholdCount: 3,
    });

    const api = new HttpApi(this, getNameWithEnv("GithubReposApi"));

    api.addRoutes({
      path: "/{proxy+}",
      methods: [HttpMethod.ANY],
      integration: new HttpAlbIntegration(
        getNameWithEnv("GithubReposAppIntegration"),
        sbApp.listener
      ),
    });

    new cdk.CfnOutput(this, getNameWithEnv("GithubReposApiEndpoint"), {
      value: api.apiEndpoint,
    });
  }
}
