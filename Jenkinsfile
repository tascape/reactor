pipeline {
    agent {
        docker {
            image 'maven:3.6.3-openjdk-14' 
            args '-v /home/user/.m2:/root/.m2' 
        }
    }

    stages {
        stage('Build') {
            steps {
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
