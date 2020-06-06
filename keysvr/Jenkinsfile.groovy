def branch = 'master'
def scmUrl = 'https://gitea.englert.xyz/frank/doctag.git'
def devServer = '192.168.178.36'
def devServerPort = '8080'

node {

    stage('checkout git') {
        git branch: branch, credentialsId: 'f7860fcf-a15d-4130-b9ab-b286676577f4', url: scmUrl
    }

    stage('build') {
        sh './gradlew keysvr:fatJar'
    }

    stage('Confirm deploy') {
        input "Click to deploy..."
    }

    stage('deploy dev'){
        sshagent(['berry3-ssh']) {
            sh "scp keysrv/build/libs/keysrv-0.1.0.jar pi@${devServer}:/home/pi/keysrv.jar"
            sh "ssh pi@${devServer} sudo service keysrv restart"
        }
    }
}