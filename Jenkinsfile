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
                'Docker Build': {
                    steps {
                        script {
                            sh 'docker build -t $REPO:$VERSION .'
                        }
                    }
                }
                'Trivy Scan': {
                    agent {
                        docker {
                            image 'aquasec/trivy:0.48.1'
                            args '--entrypoint="" -u root -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:/src'
                        }
                    }
                    steps {
                        script {
                            build 'Docker Build'
                            sh 'trivy image --format json --output /src/report_trivy.json $REPO:$VERSION'
                        }
                    }
                }
            }
        } //
    }
}
