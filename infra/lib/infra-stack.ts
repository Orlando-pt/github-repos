import * as cdk from "aws-cdk-lib";
import { Vpc } from "aws-cdk-lib/aws-ec2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import { ApplicationLoadBalancedFargateService } from "aws-cdk-lib/aws-ecs-patterns";
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { Construct } from "constructs";

export class InfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // VPC
    const vpc = new Vpc(this, "GithubReposVpc", {
      maxAzs: 2,
      natGateways: 1,
    });

    // ECS Fargate Cluster in the VPC
    const appCluster = new ecs.Cluster(this, "GithubReposEcs", {
      vpc: vpc,
      clusterName: "GithubReposCluster",
    });

    // App Secrets Manager
    // TODO: add env to jenkins
    const appSecrets = new Secret(this, "GithubReposSecrets", {
      secretName: "app/github-repos",
      secretObjectValue: {
        githubToken: cdk.SecretValue.unsafePlainText(
          process.env.GITHUB_TOKEN || ""
        ),
      },
    });

    // SB app in Fargate + ALB
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

    // Health Check for ALB
    sbApp.targetGroup.configureHealthCheck({
      path: "/api/actuator/health",
      interval: cdk.Duration.seconds(30),
      timeout: cdk.Duration.seconds(10),
      healthyThresholdCount: 3,
    });

    // API Gateway
  }
}
