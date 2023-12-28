pipeline {
    agent any

    stages { //
        stage('Install Dependencies') {
            agent {
                docker {
                    image 'node:16-alpine'
                    args '-u root'
                }
            }
            steps {
                script {
                    // Instalar las dependencias
                    sh 'npm install'
                }
            }
        }
    }
}
