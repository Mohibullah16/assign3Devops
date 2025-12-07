pipeline {
    agent any
    
    triggers {
        // Poll SCM every 5 minutes as fallback
        pollSCM('H/5 * * * *')
    }
    
    environment {
        // Load test MongoDB URI from Jenkins credentials
        TEST_MONGO_URI = credentials('test-mongo-uri')
        APP_URL = 'http://localhost:8000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }
        
        stage('Build Test Image') {
            steps {
                echo 'Building Docker test image...'
                script {
                    docker.build("selenium-tests:${env.BUILD_ID}", "-f Dockerfile.test .")
                }
            }
        }
        
        stage('Run Selenium Tests') {
            steps {
                echo 'Running Selenium tests in Docker container...'
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        try {
                            // Run tests with network host to access app on localhost:8000
                            sh """
                                docker run --rm \
                                    --shm-size=2g \
                                    --network=host \
                                    -e APP_URL=${APP_URL} \
                                    -e TEST_MONGO_URI=${TEST_MONGO_URI} \
                                    -v \$(pwd)/test-results:/tests \
                                    selenium-tests:${env.BUILD_ID}
                            """
                        } catch (Exception e) {
                            echo "Tests failed, but continuing to publish results..."
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }
            }
        }
        
        stage('Publish Results') {
            steps {
                echo 'Publishing test results...'
                
                // Publish JUnit test results
                junit allowEmptyResults: true, testResults: 'test-results/results.xml'
                
                // Publish HTML report
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'test-results',
                    reportFiles: 'report.html',
                    reportName: 'Selenium Test Report',
                    reportTitles: 'Order Manager Tests'
                ])
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            
            // Publish test results even if tests failed
            junit allowEmptyResults: true, testResults: 'test-results/results.xml'
            
            // Clean up old Docker images
            sh 'docker image prune -f'
        }
        
        success {
            echo 'Tests passed successfully!'
            emailext(
                subject: "✅ Tests Passed: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Success</h2>
                    <p>All Selenium tests passed successfully!</p>
                    
                    <h3>Build Information</h3>
                    <ul>
                        <li><strong>Job:</strong> ${env.JOB_NAME}</li>
                        <li><strong>Build Number:</strong> ${env.BUILD_NUMBER}</li>
                        <li><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
                    </ul>
                    
                    <h3>Test Results</h3>
                    <p>View detailed test report: <a href="${env.BUILD_URL}Selenium_20Test_20Report/">Test Report</a></p>
                    
                    <p><em>Automated message from Jenkins</em></p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}',
                recipientProviders: [developers(), requestor()]
            )
        }
        
        failure {
            echo 'Tests failed!'
            emailext(
                subject: "❌ Tests Failed: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Failed</h2>
                    <p>Some Selenium tests failed. Please review and fix the issues.</p>
                    
                    <h3>Build Information</h3>
                    <ul>
                        <li><strong>Job:</strong> ${env.JOB_NAME}</li>
                        <li><strong>Build Number:</strong> ${env.BUILD_NUMBER}</li>
                        <li><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
                        <li><strong>Console Output:</strong> <a href="${env.BUILD_URL}console">View Console</a></li>
                    </ul>
                    
                    <h3>Test Results</h3>
                    <p>View detailed test report: <a href="${env.BUILD_URL}Selenium_20Test_20Report/">Test Report</a></p>
                    
                    <p><strong>Action Required:</strong> Please check the console output and test report for details.</p>
                    
                    <p><em>Automated message from Jenkins</em></p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}',
                recipientProviders: [developers(), requestor(), culprits()]
            )
        }
        
        unstable {
            echo 'Build is unstable (some tests failed)'
            emailext(
                subject: "⚠️ Tests Unstable: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Unstable</h2>
                    <p>Some tests failed but build completed. Review required.</p>
                    
                    <h3>Build Information</h3>
                    <ul>
                        <li><strong>Job:</strong> ${env.JOB_NAME}</li>
                        <li><strong>Build Number:</strong> ${env.BUILD_NUMBER}</li>
                        <li><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
                    </ul>
                    
                    <h3>Test Results</h3>
                    <p>View detailed test report: <a href="${env.BUILD_URL}Selenium_20Test_20Report/">Test Report</a></p>
                    
                    <p><em>Automated message from Jenkins</em></p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}',
                recipientProviders: [developers(), requestor()]
            )
        }
    }
}
