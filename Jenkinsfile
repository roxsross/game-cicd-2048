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
            agent { docker { image 'node:16-alpine'; args '-u root:root' } }
            steps {
                script {
                    sh 'npm install'
                }
            }
        }

        stage('Security SAST') {
            parallel {
                def scanJob = { tool, image, args ->
                    stage(tool) {
                        agent { docker { image "${image}"; args "--entrypoint='' -u root -v ${WORKSPACE}:/src" } }
                        steps {
                            script {
                                sh "${tool} ${args}"
                                stash includes: "report_${tool.toLowerCase()}.json", name: "report_${tool.toLowerCase()}.json"
                            }
                        }
                    }
                }

                scanJob('Gitleaks-Scan', 'zricethezav/gitleaks', "detect --verbose --source . -f json -r /src/report_gitleaks.json")
                scanJob('NPMAudit-Scan', 'node:18-alpine', "npm audit --registry=https://registry.npmjs.org -audit-level=moderate --json > report_npmaudit.json || true")
                scanJob('Semgrep-Scan', 'returntocorp/semgrep', "ci --json --exclude=package-lock.json --output /src/report_semgrep.json --config auto --config p/ci || true")
                scanJob('Snyk-Code-Scan', 'snyk/snyk:node', "test --json --file=package.json --severity-threshold=high --print-deps --print-deps-uses --print-vulnerabilities --print-trace --print-all-environment --json-file-output=report_snyk.json")
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
            agent { docker { image 'aquasec/trivy:0.48.1'; args "--entrypoint='' -u root -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:/src" } }
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
                                echo "Subir Reportes"
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
                        echo "Paso de Despliegue"
                    '''
                }
            }
        }

        stage('Security DAST') {
            agent { docker { image 'ghcr.io/zaproxy/zaproxy:stable'; args '-u root -v ${WORKSPACE}:/zap/wrk/:rw' } }
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
                        echo "Notificar"
                    '''
                }
            }
        }
    }
}
