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
        stage('Integration Testing'){
            steps{
                echo 'Testing integrations'
            }
        }
        // At this stage we could also use sonarqube to verify quality criteria
        // But for this exercise we'll keep it simple
        stage('Deploy'){
            steps{
                sh 'cd infra && ls'
            }
        }
    }
}
