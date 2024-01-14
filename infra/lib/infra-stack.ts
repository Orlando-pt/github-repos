import * as cdk from "aws-cdk-lib";
import { HttpApi, HttpMethod } from "aws-cdk-lib/aws-apigatewayv2";
import { HttpAlbIntegration } from "aws-cdk-lib/aws-apigatewayv2-integrations";
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
        // assignPublicIp: false,
        // publicLoadBalancer: false,
      }
    );

    // Health Check for ALB
    sbApp.targetGroup.configureHealthCheck({
      path: "/actuator/health",
      interval: cdk.Duration.seconds(30),
      timeout: cdk.Duration.seconds(10),
      healthyThresholdCount: 3,
    });

    // Api Gateway
    const apiGateway = new HttpApi(this, "GithubReposApi", {
      apiName: "GithubReposApi",
      createDefaultStage: true,
    });

    // Api Gateway Integration
    apiGateway.addRoutes({
      path: "/",
      methods: [HttpMethod.ANY],
      integration: new HttpAlbIntegration(
        "GithubReposApiIntegration",
        sbApp.listener,
        {
          method: HttpMethod.ANY,
        }
      ),
    });

    // Output
    new cdk.CfnOutput(this, "GithubReposApiUrl", {
      value: apiGateway.url!,
    });

    // Connect Api Gateway to ALB
    // const apiGateway = new HttpApi(this, "GithubReposApi", {
    //   apiName: "GithubReposApi",
    //   createDefaultStage: true,
    // });

    // const vpcLink = new VpcLink(this, "GithubReposVpcLink", {
    //   vpc: vpc,
    // });

    // apiGateway.addRoutes({
    //   path: "/",
    //   methods: [HttpMethod.ANY],
    //   integration: new HttpAlbIntegration(
    //     "GithubReposApiIntegration",
    //     sbApp.listener,
    //     {
    //       vpcLink: vpcLink,
    //       method: HttpMethod.ANY,
    //     }
    //   ),
    // });

    // const httpVpcLink = new cdk.CfnResource(this, "HttpVpcLink", {
    //   type: "AWS::ApiGatewayV2::VpcLink",
    //   properties: {
    //     Name: "V2 VPC Link",
    //     SubnetIds: vpc.privateSubnets.map((m) => m.subnetId),
    //   },
    // });

    // const apiGateway = new HttpApi(this, "GithubReposApi", {
    //   apiName: "GithubReposApi",
    //   description:
    //     "Http Api Gateway for Github Repos App. Connects to ALB via VPC Link",
    // });

    // const integration = new CfnIntegration(this, "HttpApiGatewayIntegration", {
    //   apiId: apiGateway.httpApiId,
    //   connectionId: httpVpcLink.ref,
    //   connectionType: "VPC_LINK",
    //   description: "API Integration with AWS Fargate Service",
    //   integrationMethod: "ANY", // for GET and POST, use ANY
    //   integrationType: "HTTP_PROXY",
    //   integrationUri: sbApp.listener.listenerArn,
    //   payloadFormatVersion: "1.0",
    // });
    // sbApp.listener.loadBalancer.loadBalancerDnsName;

    // new CfnRoute(this, "Route", {
    //   apiId: apiGateway.httpApiId,
    //   routeKey: "ANY /{proxy+}",
    //   target: `integrations/${integration.ref}`,
    // });

    // Output
    //   new cdk.CfnOutput(this, "GithubReposApiUrl", {
    //     value: apiGateway.url!,
    //   })();
  }
}
