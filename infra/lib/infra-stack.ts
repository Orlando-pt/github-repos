import * as cdk from "aws-cdk-lib";
import { HttpIntegration, RestApi } from "aws-cdk-lib/aws-apigateway";
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
      path: "/actuator/health",
      interval: cdk.Duration.seconds(30),
      timeout: cdk.Duration.seconds(10),
      healthyThresholdCount: 3,
    });

    new cdk.CfnOutput(this, "GithubReposAppUrl", {
      value: sbApp.listener.loadBalancer.loadBalancerDnsName,
    });

    // create Http Api Gateway
    const api = new RestApi(this, "GithubReposApi", {
      restApiName: "GithubReposApi",
    });

    const lbDns = sbApp.listener.loadBalancer.loadBalancerDnsName;
    api.root.addProxy({
      anyMethod: true,
      defaultIntegration: new HttpIntegration(`http://${lbDns}`, {
        proxy: true,
        httpMethod: "ANY",
      }),
    });

    const repository = api.root
      .addResource("repository")
      .addResource("{username}");

    const prefetch = repository.addResource("prefetch");
    prefetch.addMethod(
      "GET",
      new HttpIntegration(`http://${lbDns}/repository/Orlando-pt`)
    );

    const b = api.root.addResource("b").addProxy({
      defaultIntegration: new HttpIntegration(`http://${lbDns}/repository/`),
    });

    repository.addMethod(
      "GET",
      new HttpIntegration(`http://${lbDns}/repository/{username}`)
    );

    // Output
    new cdk.CfnOutput(this, "GithubReposApiUrl", {
      value: api.url!,
    });

    // Add Rest Api Gateway
    // const api = new RestApi(this, "GithubReposApi", {
    //   restApiName: "GithubReposApi",
    // });

    // // Add Resource
    // const githubRepos = api.root.addResource("/");

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
