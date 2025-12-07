pipeline {
	agent any

	environment {
		DOCKER_HUB_USER = 'jalal2008z'
		DOCKER_CREDENTIALS_ID = 'dockerhub-login'
		GIT_REPO_URL = 'https://github.com/jalalgallaf/Libarary.git'
		GIT_BRANCH = 'main'
		GIT_CREDENTIALS_ID = 'github-token'

		// Ensure this matches the ID of your "Secret File" credential in Jenkins
		KUBECONFIG_ID = 'k8s-kubeconfig'
	}

	stages {
		stage('Checkout from GitHub') {
			steps {
				script {
					checkout([$class: 'GitSCM',
						branches: [[name: "*/${GIT_BRANCH}"]],
						userRemoteConfigs: [[url: GIT_REPO_URL, credentialsId: GIT_CREDENTIALS_ID]]
					])
				}
			}
		}

		stage('Build (Skip Tests)') {
			parallel {
				stage('Build Book Service') {
					steps { dir('book-service') { sh 'mvn clean install -DskipTests' } }
				}
				stage('Build Config Service') {
					steps { dir('config-service') { sh 'mvn clean install -DskipTests' } }
				}
				stage('Build Discovery Service') {
					steps { dir('discovery-service') { sh 'mvn clean install -DskipTests' } }
				}
				stage('Build Gateway Service') {
					steps { dir('gateway-service') { sh 'mvn clean install -DskipTests' } }
				}
			}
		}

		stage('Docker Build & Push') {
			parallel {
				stage('Process Book Service') {
					steps { processDockerImage('book-service') }
				}
				stage('Process Config Service') {
					steps { processDockerImage('config-service') }
				}
				stage('Process Discovery Service') {
					steps { processDockerImage('discovery-service') }
				}
				stage('Process Gateway Service') {
					steps { processDockerImage('gateway-service') }
				}
			}
		}

		stage('Pull Images') {
			steps {
				script {
					withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
						sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
						sh "docker pull ${DOCKER_HUB_USER}/book-service:latest"
						sh "docker pull ${DOCKER_HUB_USER}/config-service:latest"
						sh "docker pull ${DOCKER_HUB_USER}/discovery-service:latest"
						sh "docker pull ${DOCKER_HUB_USER}/gateway-service:latest"
						sh 'docker logout'
					}
				}
			}
		}

		stage('Deploy to K8s Local') {
			steps {
				script {
					// This step will now work because you installed the Kubernetes CLI plugin
					withKubeConfig([credentialsId: env.KUBECONFIG_ID]) {

						// Update images in YAML
						sh "sed -i 's|image: book-service|image: ${DOCKER_HUB_USER}/book-service|g' kuber.yaml"
						sh "sed -i 's|image: config-service|image: ${DOCKER_HUB_USER}/config-service|g' kuber.yaml"
						sh "sed -i 's|image: discovery-service|image: ${DOCKER_HUB_USER}/discovery-service|g' kuber.yaml"
						sh "sed -i 's|image: gateway-service|image: ${DOCKER_HUB_USER}/gateway-service|g' kuber.yaml"

						// Apply
						sh 'kubectl apply -f kuber.yaml'
					}
				}
			}
		}
	}
}

def processDockerImage(String serviceName) {
	dir(serviceName) {
		script {
			withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
				sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
				def imageName = "${DOCKER_HUB_USER}/${serviceName}"
				sh "docker build -t ${imageName}:latest ."
				sh "docker push ${imageName}:latest"
				sh 'docker logout'
			}
		}
	}
}