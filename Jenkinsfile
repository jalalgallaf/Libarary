pipeline {
	agent any

	// 1. THIS FIXES "mvn: not found"
	// Make sure the name 'Maven-3' matches exactly what is in:
	// Manage Jenkins -> Tools -> Maven Installations
	tools {
		maven 'Maven-3'
		jdk 'jdk-17' // Optional: Ensure you have a JDK configured with this name too, or remove this line
	}

	environment {
		// --- CONFIGURATION ---

		// 2. FIXED DOCKER CONFIGURATION
		// The username for tagging images (e.g. jalal2008z/book-service)
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
					// Empty string '' in withRegistry defaults to Docker Hub
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
						// Update kuber.yaml to point to the real Docker Hub images
						// Note: using | as delimiter in sed to avoid conflicts with / in the image name

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

// Helper function
def processDockerImage(String serviceName) {
	dir(serviceName) {
		script {
			// Using '' as the URL for standard Docker Hub
			docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
				// Image Name format: username/repo:tag
				def imageName = "${DOCKER_HUB_USER}/${serviceName}"

				// Build
				def app = docker.build("${imageName}:latest")

				// Push
				app.push()
			}
		}
	}
}