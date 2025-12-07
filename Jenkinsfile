pipeline {
	agent any

	tools {
		// We keep Maven, but we removed the 'jdk' line causing the error.
		// Make sure you have configured 'Maven-3' in Manage Jenkins -> Tools
		maven 'Maven-3'
	}

	environment {
		// --- CONFIGURATION ---
		DOCKER_HUB_USER = 'jalal2008z'
		DOCKER_CREDENTIALS_ID = 'dockerhub-login'

		// Git Configuration
		GIT_REPO_URL = 'https://github.com/jalalgallaf/Libarary.git'
		GIT_BRANCH = 'main'
		GIT_CREDENTIALS_ID = 'github-token'

		// Kubeconfig for local deployment
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

		stage('Build & Test') {
			parallel {
				stage('Build Book Service') {
					steps {
						dir('book-service') {
							sh 'mvn clean install'
						}
					}
				}
				stage('Build Config Service') {
					steps {
						dir('config-service') {
							sh 'mvn clean install'
						}
					}
				}
				stage('Build Discovery Service') {
					steps {
						dir('discovery-service') {
							sh 'mvn clean install'
						}
					}
				}
				stage('Build Gateway Service') {
					steps {
						dir('gateway-service') {
							sh 'mvn clean install'
						}
					}
				}
			}
		}

		stage('Docker Build & Push') {
			parallel {
				stage('Process Book Service') {
					steps {
						processDockerImage('book-service')
					}
				}
				stage('Process Config Service') {
					steps {
						processDockerImage('config-service')
					}
				}
				stage('Process Discovery Service') {
					steps {
						processDockerImage('discovery-service')
					}
				}
				stage('Process Gateway Service') {
					steps {
						processDockerImage('gateway-service')
					}
				}
			}
		}

		stage('Pull Images') {
			steps {
				script {
					docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
						sh "docker pull ${DOCKER_HUB_USER}/book-service:latest"
						sh "docker pull ${DOCKER_HUB_USER}/config-service:latest"
						sh "docker pull ${DOCKER_HUB_USER}/discovery-service:latest"
						sh "docker pull ${DOCKER_HUB_USER}/gateway-service:latest"
					}
				}
			}
		}

		stage('Deploy to K8s Local') {
			steps {
				script {
					withKubeConfig([credentialsId: env.KUBECONFIG_ID]) {
						sh "sed -i 's|image: book-service|image: ${DOCKER_HUB_USER}/book-service|g' kuber.yaml"
						sh "sed -i 's|image: config-service|image: ${DOCKER_HUB_USER}/config-service|g' kuber.yaml"
						sh "sed -i 's|image: discovery-service|image: ${DOCKER_HUB_USER}/discovery-service|g' kuber.yaml"
						sh "sed -i 's|image: gateway-service|image: ${DOCKER_HUB_USER}/gateway-service|g' kuber.yaml"

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
			docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
				def imageName = "${DOCKER_HUB_USER}/${serviceName}"
				def app = docker.build("${imageName}:latest")
				app.push()
			}
		}
	}
}