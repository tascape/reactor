pipeline {
    agent {
        docker {
            image 'tascape/reactor-builder' 
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
