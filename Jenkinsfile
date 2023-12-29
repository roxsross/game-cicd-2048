pipeline {
    agent any

    environment {
        DOCKER_HUB_LOGIN = credentials('docker-hub')
        VERSION = sh(script: 'jq --raw-output .version package.json', returnStdout: true).trim()
        REPO = sh(script: 'basename `git rev-parse --show-toplevel`', returnStdout: true).trim()
        REGISTRY = "roxsross12"
    }

    stages {
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
        stage('Security SAST') {
            parallel {
                stage('Secrets-SCAN') {
                    agent {
                        docker {
                            image 'zricethezav/gitleaks'
                            args '--entrypoint="" -u root -v ${WORKSPACE}:/src'
                        }
                    }                    
                    steps {
                        script {
                            sh "gitleaks detect --verbose --source . -f json -r /src/report_gitleaks.json"
                        }
                    }
                }
                stage('SONAR-SCAN') {
                    steps {
                        echo 'Esperando a que termine el Docker Build...'
                    }
                }
                stage('AUDITH-SCAN') {
                    steps {
                        echo 'Esperando a que termine el Docker Build...'
                    }
                }                
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    sh "docker build -t $REGISTRY/$REPO:$VERSION ."
                }
            }
        }

        stage('Trivy Scan') {
            agent {
                docker {
                    image 'aquasec/trivy:0.48.1'
                    args '--entrypoint="" -u root -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:/src'
                }
            }
            steps {
                script {
                    sh "trivy image --format json --output /src/report_trivy.json $REGISTRY/$REPO:$VERSION"
                }
            }
        }
        stage('Docker Push') {
            steps {
                script {
                    sh '''
                        docker login -u $DOCKER_HUB_LOGIN_USR -p $DOCKER_HUB_LOGIN_PSW
                        docker push $REGISTRY/$REPO:$VERSION
                    '''
                }
            }
        } 
        stage('Deploy') {
            steps {
                script {
                    sh '''
                        echo "prueba"
                    '''
                }
            }
        } 
        stage('Security DAST') {
            steps {
                script {
                    sh '''
                        echo "prueba"
                    '''
                }
            }
        }  
        stage('Notify') {
            steps {
                script {
                    sh '''
                        echo "prueba"
                    '''
                }
            }
        }                  
    }
}
