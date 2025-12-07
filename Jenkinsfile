pipeline {
    agent any

    environment {
        // --- CONFIGURATION ---
        DOCKER_REGISTRY = 'https://hub.docker.com/repositories/jalal2008z'  // e.g., 'docker.io/myuser' or 'myregistry.com'
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
                            sh 'mvn clean install' // Runs unit & integration tests
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
                    // Verify we can pull the images we just pushed
                    // This satisfies the requirement "then pull the image from repository"
                    // Also useful to ensure the local K8s node has the latest image if imagePullPolicy is Always/IfNotPresent
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        sh "docker pull ${DOCKER_REGISTRY}/book-service:latest"
                        sh "docker pull ${DOCKER_REGISTRY}/config-service:latest"
                        sh "docker pull ${DOCKER_REGISTRY}/discovery-service:latest"
                        sh "docker pull ${DOCKER_REGISTRY}/gateway-service:latest"
                    }
                }
            }
        }

        stage('Deploy to K8s Local') {
            steps {
                script {
                    withKubeConfig([credentialsId: env.KUBECONFIG_ID]) {
                        // We need to update the image names in kuber.yaml to point to the registry
                        // We'll use sed to do this on the fly so we don't commit the change
                        // Also ensure imagePullPolicy allows pulling (we set it to IfNotPresent in the file, which is good)
                        
                        sh "sed -i.bak 's|image: book-service|image: ${DOCKER_REGISTRY}/book-service|g' kuber.yaml"
                        sh "sed -i.bak 's|image: config-service|image: ${DOCKER_REGISTRY}/config-service|g' kuber.yaml"
                        sh "sed -i.bak 's|image: discovery-service|image: ${DOCKER_REGISTRY}/discovery-service|g' kuber.yaml"
                        sh "sed -i.bak 's|image: gateway-service|image: ${DOCKER_REGISTRY}/gateway-service|g' kuber.yaml"

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
            docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                // Build
                def app = docker.build("${DOCKER_REGISTRY}/${serviceName}:latest")
                // Push
                app.push()
                // app.push("${env.BUILD_NUMBER}") // Optional version tag
            }
        }
    }
}