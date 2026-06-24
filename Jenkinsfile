pipeline {
    agent any

    tools {
        // Name must match a JDK 21 installation configured in
        // Manage Jenkins → Tools → JDK installations
        jdk 'jdk-21'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages { 

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests -q'
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