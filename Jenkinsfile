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
        // But for this exercise we'll keep it simple
//         stage('Integration Testing'){
//             steps{
//                 echo 'Testing integrations'
//             }
//         }
        // At this stage we could also use sonarqube to verify quality criteria
        // But for this exercise, laziness will do

        // Maybe generate swagger file to make a custom API Gateway
        stage('Deploy'){
            steps{
                sh 'cd infra && ls'
            }
        }
    }
}
