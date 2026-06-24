pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-21-alpine'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    stages {

        stage('Build') {
            steps {
                sh './mvnw package -DskipTests -q'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/crm-customer-*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test -q'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}