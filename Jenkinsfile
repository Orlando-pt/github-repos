pipeline{
    agent any

    environment{
        GITHUB_TOKEN = credentials('githubToken')
    }

    stages{
        stage('Build'){
            steps{
                sh './gradlew build'
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
                sh 'cd infra && npm run deploy'
            }
        }
    }
}
