pipeline{
    agent any

    environment{
        GITHUB_TOKEN            = credentials('githubToken')
        AWS_ACCESS_KEY_ID       = credentials('awsAccessKey')
        AWS_SECRET_ACCESS_KEY   = credentials('awsSecretKey')
        AWS_DEFAULT_REGION      = 'eu-central-1'
    }

    tools {
        nodejs "node-20"
    }

    stages{
        stage('Build'){
            steps{
                sh './gradlew build -x test'
            }
        }
        stage('Unit Testing'){
            steps{
                sh './gradlew test'
            }
        }
        // Normally we would have a stage for integration testing

        // At this stage we could also use sonarqube to verify quality criteria

        // Maybe generate swagger file to make a custom API Gateway
        stage('Deploy'){
            steps{
                dir('infra'){
                    sh 'npm install'
                    sh 'npx cdk deploy --require-approval never'
                }
            }
        }
    }
}
