pipeline {
    agent any
    environment {
        DOCKER_HUB_LOGIN = credentials('docker-hub')
        VERSION = sh(script: 'jq --raw-output .version package.json', returnStdout: true).trim()
        REPO= sh(script: 'basename `git rev-parse --show-toplevel`', returnStdout: true).trim()
    }
    stages { //
        stage('Install Dependencies') {
            agent {
                docker {
                    image 'node:16-alpine'
                    args '-u root:root'
                }
            }
            steps {
                script {
                    sh 'npm install'
                }
            }
        }
        stage('Build') {
            parallel {
                stage('Docker Build') {
                    steps {
                        script {
                            sh 'echo "REPO= ${REPO}"'
                            sh 'docker build -t prueba .'
                        }
                    }
                }
                stage('Trivy Scan') {
                    agent {
                        docker {
                            image 'aquasec/trivy:0.48.1'
                            args '-u root -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:/src'
                            entryPoint ''
                        }
                    }
                    steps {
                        script {
                            sh 'trivy image --format json --output /src/report_trivy.json python:alpine'
                            sh 'ls -lrt'
                        }
                    }
                }
            }
        } //
    }
}
