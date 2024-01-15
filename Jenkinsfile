pipeline{
    agent any

    environment{
        GITHUB_TOKEN = credentials('githubToken')
    }

    stages{
        stage('Build'){
            steps{
                 sh 'echo $GITHUB_TOKEN'
                sh './gradlew build'
            }
        }
        stage('Test'){
            steps{
                echo 'Testing...'
            }
        }
        stage('Deploy'){
            steps{
                echo 'Deploying...'
            }
        }
    }
}
