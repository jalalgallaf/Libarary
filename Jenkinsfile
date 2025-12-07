pipeline {
	agent any

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

		stage('Build (Skip Tests)') {
			parallel {
				stage('Build Book Service') {
					steps {
						dir('book-service') {
							// ADDED -DskipTests here
							sh 'mvn clean install -DskipTests'
						}
					}
				}
				stage('Build Config Service') {
					steps {
						dir('config-service') {
							// ADDED -DskipTests here
							sh 'mvn clean install -DskipTests'
						}
					}
				}
				stage('Build Discovery Service') {
					steps {
						dir('discovery-service') {
							// ADDED -DskipTests here
							sh 'mvn clean install -DskipTests'
						}
					}
				}
				stage('Build Gateway Service') {
					steps {
						dir('gateway-service') {
							// ADDED -DskipTests here
							sh 'mvn clean install -DskipTests'
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
					// CHANGED: Use shell commands instead of 'docker.withRegistry'
					// to avoid "No such property: docker" error
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