pipeline {
    agent {
        docker {
            image 'maven:3.8.2-openjdk-17-slim' 
            args '-v /home/user/.m2:/root/.m2 -u root:root' 
        }
    }

    stages {
        stage('Build') {
            steps {
                sh 'apt update && apt install -y iputils-ping'
                sh 'mvn clean package'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
    }
}
