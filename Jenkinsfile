library identifier: 'pipeline-library', changelog: false

configuration {
  jdk = 'openjdk8'
  slack = 'webcam-capture-library'
  email = false
  release = [slack: 'product-releases', name: 'Webcam-capture']
}

// The node label
def label = 'ecs-small'

buildProject {
  node(label) {
    stage('commit') {
      checkout scm
      test {
        mvn 'clean package'
      }
    }

    stage('acceptance') {
      test {
        mvn 'install -DjenkinsAcceptance=true'
      }
    }

    if (branch('master')) {
      stage('nexus') {
        mvn 'deploy -DjenkinsDeploy=true'
      }
    }

    sonarStage() {
      mvnSonarQube()
    }

    // Here, we consider the build a success.
    notification 'success'
  }

  if (branch('master')) {
    releaseProject { releaseVersion, developmentVersion ->
      node(label) {
        checkout scm
        mvnRelease args: '-DskipTests', tag: "v$releaseVersion"
      }
    }
  }
}