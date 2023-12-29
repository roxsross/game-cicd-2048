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
                stage('Gitleaks-Scan') {
                    agent {
                        docker {
                            image 'zricethezav/gitleaks'
                            args '--entrypoint="" -u root -v ${WORKSPACE}:/src'
                        }
                    }                    
                    steps {
                        script {
                            sh "gitleaks detect --verbose --source . -f json -r /src/report_gitleaks.json"
                            stash includes: 'report_gitleaks.json', name: 'report_gitleaks.json'
                        }
                    }
                }
                stage('Retire-Scan') {
                    agent {
                        docker {
                            image 'node:16-alpine'
                            args '-u root:root -v ${WORKSPACE}:/src'
                        }
                    }                    
                    steps {
                        script {
                            sh "npm install -g retire"
                            sh "retire --outputformat json --outputpath /src/report_retire.json"
                            stash includes: 'report_retire.json', name: 'report_retire.json'
                        }
                    }
                }
                stage('Semgrep-Scan') {
                    agent {
                        docker {
                            image 'returntocorp/semgrep'
                            args '-u root:root -v ${WORKSPACE}:/src'
                        }
                    }                     
                    steps {
                        script {
                            sh "semgrep ci --json --exclude=package-lock.json --output /src/report_semgrep.json --config auto --config p/ci || true"
                            stash includes: 'report_semgrep.json', name: 'report_semgrep.json'
                        }
                    }
                }    
                stage('Njscan-Scan') {
                    agent {
                        docker {
                            image 'python:3.8-alpine'
                            args '-u root:root -v ${WORKSPACE}:/src'
                        }
                    }                     
                    steps {
                        script {
                            sh ''' 
                                pip3 install --upgrade njsscan >/dev/null
                                njsscan --exit-warning -o /src/report_njsscan.json /src    
                            '''
                            stash includes: 'report_njsscan.json', name: 'report_njsscan.json'
                        }
                    }
                }   
                stage('Horusec-Scan') {
                    agent {
                        docker {
                            image 'public.ecr.aws/roxsross/horusec:v2.9.0'
                            args '-u root:root -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:/src'
                        }
                    }                     
                    steps {
                        script {
                            sh ''' 
                                horusec start -p /src -P "$(pwd)/src" -e="true" -o="json" -O=src/report_horusec.json || true
                            '''
                            stash includes: 'report_horusec.json', name: 'report_horusec.json'
                        }
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
                    stash includes: 'report_trivy.json', name: 'report_trivy.json'
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
            agent {
                docker {
                    image 'ghcr.io/zaproxy/zaproxy:stable'
                    args '-u root -v ${WORKSPACE}:/zap/wrk/:rw'
                }
            }            
            steps {
                script {
                    sh '''
                        zap-full-scan.py -t https://roxs.295devops.com -g gen.conf -r testreport.html
                    '''
                    stash includes: 'testreport.html', name: 'testreport.html'
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
