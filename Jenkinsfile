pipeline {
    agent any
    stages {
        stage('构建项目') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
    }
}
