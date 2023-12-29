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
                stage('NPMAudit-Scan') {
                    agent {
                        docker {
                            image 'node:16-alpine'
                            args '-u root:root -v ${WORKSPACE}:/src'
                        }
                    }                    
                    steps {
                        script {
                            sh "npm audit --registry=https://registry.npmjs.org -audit-level=moderate --json > report_npmaudit.json || true"
                            stash includes: 'report_npmaudit.json', name: 'report_npmaudit.json'
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
                stage('Snyk-Code-Scan'){
                    agent {
                        docker {
                            image 'snyk/snyk:node'
                            args '--entrypoint="" -e SNYK_TOKEN=$SNYK_CREDENTIALS -u root:root -v ${WORKSPACE}:/src'
                        }
                    }                     
                    steps {
                        script {
                            sh "snyk test --json --file=package.json --severity-threshold=high --print-deps --print-deps-uses --print-vulnerabilities --print-trace --print-all-environment --json-file-output=report_snyk.json"
                            stash includes: 'report_snyk.json', name: 'report_snyk.json'
                        }
                    }
                }
                stage('Horusec-Scan') {                   
                    steps {
                        script {
                            sh ''' 
                                docker run --rm \
                                    -v /var/run/docker.sock:/var/run/docker.sock \
                                    -v $(pwd):/src \
                                    horuszup/horusec-cli:v2.9.0-beta.3 \
                                    horusec start \
                                    -p /src \
                                    -P "$(pwd)/src" \
                                    -e="true" \
                                    -o="json" \
                                    -O=src/report_horusec.json || true
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

        stage('Trivy-Scan') {
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
        stage('Hub&Report') {
                    parallel {        
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
                    stage('Upload Reports') {
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
                        zap-full-scan.py -t https://roxs.295devops.com -g gen.conf -r testreport.html || true
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
