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
                // Install Python dependencies
                // Warning: This assumes pip is available on the agent
                sh '''
                    cd order_manager
                    pip install -r requirements.txt
                '''
            }
        }
        
        stage('Start Application') {
            steps {
                echo 'Starting application in background...'
                // Start Uvicorn in background using nohup
                // Env vars are automatically available from the environment block
                sh '''
                    cd order_manager
                    
                    nohup uvicorn app:app --host 0.0.0.0 --port 8000 > app.log 2>&1 &
                    echo $! > app.pid
                    sleep 10  # Wait for app to startup
                    
                    # Verify app started by checking log for errors
                    cat app.log
                    if grep -q "Application startup complete" app.log; then
                        echo "App started successfully"
                    else
                        echo "Warning: App might not have started correctly. Checking logs..."
                    fi
                '''
            }
        }
        
        stage('Run Selenium Tests') {
            steps {
                echo 'Running Selenium tests using Docker...'
                script {
                    // Use pre-built image as requested
                    // Mount the selenium-tests directory into the container
                    // Use --network="host" allows container to access localhost:8000 on the host
                    docker.image('markhobson/maven-chrome:jdk-17').inside('--network="host" -v $WORKSPACE/selenium-tests:/usr/src/app -w /usr/src/app') {
                        // Environment variables inside container
                        withEnv(["APP_URL=${APP_URL}", "TEST_MONGO_URI=${TEST_MONGO_URI}"]) {
                            sh 'mvn test'
                        }
                    }
                }
            }
        }
        
        stage('Publish Results') {
            steps {
                echo 'Publishing test results...'
                
                // Publish TestNG/JUnit results
                junit allowEmptyResults: true, testResults: 'selenium-tests/target/surefire-reports/*.xml'
                
                // Archive artifacts
                archiveArtifacts allowEmptyArchive: true, artifacts: 'selenium-tests/target/surefire-reports/**/*'
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            // Stop the application
            sh '''
                if [ -f order_manager/app.pid ]; then
                    kill $(cat order_manager/app.pid) || true
                    rm order_manager/app.pid
                fi
            '''
            
            cleanWs()
        }
        
        success {
            echo 'Pipeline successfully executed!'
        }
        
        failure {
            echo 'Pipeline failed!'
        }
    }
}
