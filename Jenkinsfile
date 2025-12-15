pipeline {
    agent any
    
    triggers {
        // Poll SCM every 5 minutes as fallback
        pollSCM('H/5 * * * *')
    }
    
    environment {
        // Load credentials
        // You MUST configure these in Jenkins > Manage Credentials
        MONGO_URI = credentials('mongo-uri')
        TEST_MONGO_URI = credentials('test-mongo-uri') 
        GROQ_API_KEY = credentials('groq-api-key')
        APP_URL = 'http://localhost:8000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }
        
        stage('Setup Application') {
            steps {
                echo 'Setting up Order Manager Application...'
                // Create virtual environment and install dependencies
                // This avoids 'externally-managed-environment' errors
                sh '''
                    cd order_manager
                    python3 -m venv venv
                    venv/bin/pip install -r requirements.txt
                '''
            }
        }
        
        stage('Start Application') {
            steps {
                echo 'Starting application in background...'
                // Start Uvicorn in background using nohup
                // Env vars are automatically available from the environment block
                script {
                    echo 'Stopping any existing application...'
                    // Try to kill process on port 8000 or using pid file
                    sh '''
                        if [ -f order_manager/app.pid ]; then
                            kill $(cat order_manager/app.pid) || true
                            rm order_manager/app.pid
                        else
                            # Fallback: kill anything on port 8000 (requires lsof or netstat or fuser)
                            # We will assume checking app.pid is sufficient for this pipeline flow
                            echo "No pid file found."
                        fi
                    '''
                    
                    echo 'Starting application...'
                    // Use JENKINS_NODE_COOKIE to prevent Jenkins from killing the process after build
                    withEnv(['JENKINS_NODE_COOKIE=dontKillMe']) {
                        sh '''
                            cd order_manager
                            # Start in background
                            nohup venv/bin/uvicorn app:app --host 0.0.0.0 --port 8000 > app.log 2>&1 &
                            echo $! > app.pid
                        '''
                    }
                }
            }
        }
        
        stage('Run Selenium Tests') {
            steps {
                script {
                    echo 'Running Selenium tests...'
                    // Wait for application to be ready
                    sleep 10
                    
                    // Verify app started by checking log for errors
                    sh '''
                        cd order_manager
                        cat app.log
                        if grep -q "Application startup complete" app.log; then
                            echo "App started successfully"
                        else
                            echo "Warning: App might not have started correctly. Checking logs..."
                        fi
                    '''
                    
                    // Use pre-built image as requested
                    // Use --network="host" allows container to access localhost:8000 on the host
                    // Jenkins automatically mounts the workspace, so we just need to cd into the correct dir
                    // We MUST set HOME to the workspace because the default home (/) is not writable for the jenkins user, causing Chrome to fail
                    docker.image('markhobson/maven-chrome:jdk-17').inside("--network='host' -e HOME=${env.WORKSPACE}") {
                        sh 'cd selenium-tests && mvn test'
                    }
                }
            }
        }
        
        stage('Publish Results') {
            steps {
                echo 'Archiving artifacts...'
                // Archive artifacts
                archiveArtifacts allowEmptyArchive: true, artifacts: 'selenium-tests/target/surefire-reports/**/*'
            }
        }
    }
    
    post {
        always {
            echo 'Publishing results (Application left running)...'
            
            // Publish TestNG/JUnit results ALWAYS
            junit allowEmptyResults: true, testResults: 'selenium-tests/target/surefire-reports/*.xml'
            
            // NOTE: We do NOT clean workspace or stop app so it remains deployable
            // cleanWs() 
        }
        
        success {
            echo 'Pipeline successfully executed!'
            emailext (
                subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build Status: SUCCESS</p>
                         <p>The build finished successfully.</p>
                         <p>Check the Test Report: <a href='${env.BUILD_URL}testReport'>${env.BUILD_URL}testReport</a></p>
                         <p>Console Output: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        
        failure {
            echo 'Pipeline failed!'
            emailext (
                subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build Status: FAILURE</p>
                         <p>The build failed. Please check the logs.</p>
                         <p>Check the Test Report: <a href='${env.BUILD_URL}testReport'>${env.BUILD_URL}testReport</a></p>
                         <p>Console Output: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        
        unstable {
            echo 'Pipeline is unstable!'
            emailext (
                subject: "UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build Status: UNSTABLE</p>
                         <p>Some tests may have failed.</p>
                         <p>Check the Test Report: <a href='${env.BUILD_URL}testReport'>${env.BUILD_URL}testReport</a></p>
                         <p>Console Output: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
    }
}
