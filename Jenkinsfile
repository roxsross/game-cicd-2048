def securityScript = load 'security.groovy'

pipeline {
    agent any

    environment {
        DOCKER_HUB_LOGIN = credentials('docker-hub')
        VERSION = sh(script: 'jq --raw-output .version package.json', returnStdout: true).trim()
        REPO = sh(script: 'basename `git rev-parse --show-toplevel`', returnStdout: true).trim()
        REGISTRY = "roxsross12"
        SNYK_CREDENTIALS = credentials('snyk-token')
    }

    stages {
        stage('Install Dependencies') {
            steps {
                script {
                    securityScript.installDependencies()
                }
            }
        }             
    }
}
