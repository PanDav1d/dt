def branch = 'master'
def scmUrl = 'https://gitea.englert.xyz/frank/doctag.git'
def devServer = '116.202.109.213'
def devServerPort = '8080'

node {

    stage('checkout git') {
        git branch: branch, credentialsId: 'f7860fcf-a15d-4130-b9ab-b286676577f4', url: scmUrl
    }

    stage('build') {
        sh 'echo "$BUILD_NUMBER" > docsrv/src/main/resources/version.txt'
        sh './gradlew docsrv:fatJar'
    }

    stage('Confirm deploy') {
        input "Click to deploy..."
    }

    stage('deploy dev'){
        sshagent(['berry3-ssh']) {
            sh "scp docsrv/build/libs/docsrv-0.1.0.jar root@${devServer}:/home/pi/docsrv.jar"
            sh "ssh pi@${devServer} sudo service docsrv restart"
        }
    }
}