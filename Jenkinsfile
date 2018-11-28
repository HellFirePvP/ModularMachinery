pipeline {
  agent any
  environment {
    WEBHOOKURL = credentials('JenkinsDiscordWebhook')
  }
  stages {
    stage('Build') {
      steps {
        sh '''cp -a /var/lib/jenkins/buildMetadata/ModularMachinery/. .
rm -rf README.md
./gradlew build
cp -a ./build/libs/. .
rm -rf build gradle .gradle
find . ! -name \'*.jar\' -delete'''
      }
    }
    stage('Archive') {
      steps {
        archiveArtifacts '*.jar'
      }
    }
    stage('Notify') {
      when{
        branch 'master'
      }
      steps {
        discordSendHellFire link: env.BUILD_URL, result: currentBuild.currentResult, webhookURL: "${WEBHOOKURL}"
      }
    }
  }
}
