// security.groovy
def installDependencies() {
    agent { docker { image 'node:16-alpine'; args "-u root:root" } }
    steps {
        script {
            sh 'npm install'
        }
    }
}

def gitleaksScan() {
    agent { docker { image 'zricethezav/gitleaks'; args "--entrypoint='' -u root -v ${WORKSPACE}:/src" } }
    steps {
        script {
            sh "gitleaks detect --verbose --source . -f json -r /src/report_gitleaks.json"
            stash includes: 'report_gitleaks.json', name: 'report_gitleaks.json'
        }
    }
}

def npmauditScan() {
    agent { docker { image 'node:16-alpine'; args '-u root:root -v ${WORKSPACE}:/src' } }
    steps {
        script {
            sh "npm install -g retire"
            sh "npm audit --registry=https://registry.npmjs.org -audit-level=moderate --json > report_npmaudit.json || true"
            stash includes: 'report_npmaudit.json', name: 'report_npmaudit.json'
        }
    }
}
