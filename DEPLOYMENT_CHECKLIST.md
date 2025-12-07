# Deployment & Submission Checklist

## Phase 1: Local Development ✓

### Application Setup
- [ ] Clone repository to local machine
- [ ] Create virtual environment
- [ ] Install dependencies from `order_manager/requirements.txt`
- [ ] Create MongoDB Atlas account and clusters
  - [ ] Production database: `order_management_db`
  - [ ] Test database: `order_management_test_db`
- [ ] Get Groq API key from console.groq.com
- [ ] Create `.env` file from `.env.example`
- [ ] Add MongoDB URIs to `.env`
- [ ] Add Groq API key to `.env`
- [ ] Add SECRET_KEY to `.env`
- [ ] Test run locally: `uvicorn app:app --reload`
- [ ] Verify app loads at http://localhost:8000
- [ ] Test adding an order manually
- [ ] Test OCR with sample invoice

### Test Suite Setup
- [ ] Install test dependencies from `tests/requirements.txt`
- [ ] Create sample invoice images in `tests/fixtures/`
- [ ] Set test environment variables
- [ ] Run tests locally: `pytest tests/ -v`
- [ ] Verify all 13 tests pass
- [ ] Fix any failing tests

### Docker Testing
- [ ] Install Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- [ ] Build test image: `docker build -f Dockerfile.test -t selenium-tests .`
- [ ] Run tests in container with app running locally
- [ ] Verify container can access localhost:8000
- [ ] Check test results are generated (results.xml, report.html)

## Phase 2: GitHub Setup ✓

### Repository Configuration
- [ ] Create GitHub repository (public or private)
- [ ] Initialize git in project directory
- [ ] Add `.gitignore` to prevent committing sensitive files
- [ ] Create `.env` file locally (DO NOT COMMIT)
- [ ] Commit all project files
- [ ] Push to GitHub repository
- [ ] Verify all files are present on GitHub
- [ ] Add instructor as collaborator (Settings → Collaborators)
- [ ] Test clone from another location

### GitHub Webhook (after EC2 setup)
- [ ] Go to repository Settings → Webhooks
- [ ] Add webhook with Jenkins URL
- [ ] Set Payload URL: `http://<ec2-ip>:8080/github-webhook/`
- [ ] Content type: application/json
- [ ] Select "Just the push event"
- [ ] Active: ✓
- [ ] Test webhook delivery

## Phase 3: AWS EC2 Setup ✓

### EC2 Instance Provisioning
- [ ] Launch EC2 instance (t2.micro or larger)
- [ ] OS: Ubuntu 22.04 LTS
- [ ] Configure Security Group:
  - [ ] Port 22 (SSH) from your IP
  - [ ] Port 8000 (App) from anywhere
  - [ ] Port 8080 (Jenkins) from anywhere
- [ ] Create or use existing key pair (ass3.pem)
- [ ] Download key pair if new
- [ ] Set key permissions: `chmod 400 ass3.pem` (Linux/Mac)
- [ ] Test SSH connection

### System Configuration
- [ ] SSH into EC2 instance
- [ ] Update system: `sudo apt update && sudo apt upgrade -y`
- [ ] Create 5GB swap memory:
  ```bash
  sudo dd if=/dev/zero of=/swapfile bs=1M count=5120
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  ```
- [ ] Verify swap: `swapon --show` and `free -h`
- [ ] Install Python 3.11: `sudo apt install -y python3.11 python3.11-venv python3-pip`
- [ ] Install Docker: `sudo apt install -y docker.io`
- [ ] Start Docker: `sudo systemctl enable docker && sudo systemctl start docker`
- [ ] Add ubuntu to docker group: `sudo usermod -aG docker ubuntu`
- [ ] Install Java: `sudo apt install -y openjdk-11-jdk`

### Application Deployment
- [ ] Clone repository: `git clone <your-repo> Assignment3`
- [ ] Navigate to app: `cd Assignment3/order_manager`
- [ ] Create virtual environment: `python3.11 -m venv venv`
- [ ] Activate venv: `source venv/bin/activate`
- [ ] Install dependencies: `pip install -r requirements.txt`
- [ ] Create `.env` file with production credentials
- [ ] Test run: `uvicorn app:app --host 0.0.0.0 --port 8000`
- [ ] Verify accessible from browser: `http://<ec2-ip>:8000`
- [ ] Stop test run (Ctrl+C)
- [ ] Create systemd service file (see EC2_SETUP_GUIDE.md)
- [ ] Enable service: `sudo systemctl enable order-manager`
- [ ] Start service: `sudo systemctl start order-manager`
- [ ] Check status: `sudo systemctl status order-manager`
- [ ] Verify app runs on reboot

## Phase 4: Jenkins Setup ✓

### Jenkins Installation
- [ ] Add Jenkins repository
- [ ] Install Jenkins
- [ ] Start Jenkins: `sudo systemctl enable jenkins && sudo systemctl start jenkins`
- [ ] Add jenkins to docker group: `sudo usermod -aG docker jenkins`
- [ ] Restart Jenkins: `sudo systemctl restart jenkins`
- [ ] Get initial password: `sudo cat /var/lib/jenkins/secrets/initialAdminPassword`
- [ ] Access Jenkins: `http://<ec2-ip>:8080`
- [ ] Complete setup wizard
- [ ] Install suggested plugins

### Jenkins Plugin Installation
- [ ] Manage Jenkins → Plugins → Available
- [ ] Install plugins:
  - [ ] GitHub Integration Plugin
  - [ ] Docker Pipeline Plugin
  - [ ] Email Extension Plugin
  - [ ] HTML Publisher Plugin
  - [ ] JUnit Plugin
- [ ] Restart Jenkins if required

### Gmail App Password Setup
- [ ] Enable 2FA on Gmail account
- [ ] Visit: https://myaccount.google.com/apppasswords
- [ ] Generate app password for "Mail"
- [ ] Save password securely

### Jenkins SMTP Configuration
- [ ] Manage Jenkins → Configure System
- [ ] Extended E-mail Notification:
  - [ ] SMTP server: `smtp.gmail.com`
  - [ ] SMTP Port: `587`
  - [ ] Use TLS: ✓
  - [ ] Credentials → Add → Username with password
    - [ ] Username: your Gmail address
    - [ ] Password: Gmail app password (not regular password)
  - [ ] Default recipients: instructor email
  - [ ] Test configuration by sending test email
- [ ] Save configuration

### Jenkins Credentials
- [ ] Manage Jenkins → Credentials → System → Global credentials
- [ ] Add Secret Text:
  - [ ] ID: `test-mongo-uri`
  - [ ] Secret: Your TEST_MONGO_URI value
  - [ ] Description: Test MongoDB Connection String
- [ ] Add Username with Password:
  - [ ] Username: GitHub username
  - [ ] Password: GitHub Personal Access Token
  - [ ] ID: `github-credentials`
  - [ ] Description: GitHub Access

### Create Pipeline Job
- [ ] New Item → Pipeline
- [ ] Name: `Order-Manager-Tests`
- [ ] Configure:
  - [ ] Description: Automated Selenium testing for Order Manager
  - [ ] GitHub project URL: Your repository URL
  - [ ] Build Triggers: ✓ GitHub hook trigger for GITScm polling
  - [ ] Pipeline:
    - [ ] Definition: Pipeline script from SCM
    - [ ] SCM: Git
    - [ ] Repository URL: Your GitHub repo URL
    - [ ] Credentials: Select github-credentials
    - [ ] Branch Specifier: `*/main`
    - [ ] Script Path: `Jenkinsfile`
- [ ] Save

## Phase 5: End-to-End Testing ✓

### Manual Pipeline Test
- [ ] Jenkins → Order-Manager-Tests → Build Now
- [ ] Monitor build console output
- [ ] Verify stages complete:
  - [ ] Checkout
  - [ ] Build Test Image
  - [ ] Run Selenium Tests
  - [ ] Publish Results
- [ ] Check test results page
- [ ] View HTML test report
- [ ] Verify email notification received

### GitHub Webhook Test
- [ ] Make a small change to README.md
- [ ] Commit and push to main branch
- [ ] Verify Jenkins build triggers automatically
- [ ] Check build completes successfully
- [ ] Confirm email sent

### Application Testing
- [ ] Open app in browser: `http://<ec2-ip>:8000`
- [ ] Verify KPIs display
- [ ] Add a test order manually
- [ ] Upload invoice image and test OCR
- [ ] Add multiple items to an order
- [ ] Edit an existing order
- [ ] Delete an order
- [ ] Test search functionality
- [ ] Verify orders persist after browser refresh

## Phase 6: Documentation ✓

### Screenshots Required
- [ ] Application homepage with sample orders
- [ ] Add order form with multiple items
- [ ] Edit order page
- [ ] MongoDB Atlas cluster dashboard
- [ ] Jenkins dashboard showing recent builds
- [ ] Jenkins build console output (successful build)
- [ ] JUnit test results graph
- [ ] HTML test report showing all tests passed
- [ ] Email notification in inbox (success & failure examples)
- [ ] GitHub repository page
- [ ] GitHub webhook settings
- [ ] EC2 instance details
- [ ] Security group configuration
- [ ] Terminal showing swap memory (`free -h`)

### Report Document Contents
- [ ] Title page with student details
- [ ] Table of contents
- [ ] Introduction
- [ ] Architecture diagram (GitHub → Jenkins → Docker → Email)
- [ ] Technology stack explanation
- [ ] MongoDB Atlas setup steps
- [ ] EC2 configuration (swap memory commands)
- [ ] Jenkins setup and configuration
- [ ] Gmail SMTP configuration
- [ ] Complete Jenkinsfile code with explanations
- [ ] Complete Dockerfile.test code with explanations
- [ ] List of all 13 test cases with descriptions
- [ ] Screenshots (labeled and captioned)
- [ ] Challenges faced and solutions
- [ ] Conclusion
- [ ] References

### Code Quality Check
- [ ] All code properly commented
- [ ] No hardcoded credentials in repository
- [ ] `.gitignore` prevents sensitive file commits
- [ ] README.md is comprehensive
- [ ] All dependencies listed in requirements.txt
- [ ] Code follows PEP 8 (Python style guide)
- [ ] No TODO or FIXME comments left in production code

## Phase 7: Final Submission ✓

### Pre-Submission Checklist
- [ ] All tests passing (13/13)
- [ ] Application accessible at public URL
- [ ] Jenkins pipeline working end-to-end
- [ ] Email notifications sending correctly
- [ ] Instructor added as GitHub collaborator
- [ ] GitHub repository is clean and organized
- [ ] All documentation complete
- [ ] Screenshots captured and organized
- [ ] Report proofread and formatted

### Submission Items
- [ ] Google Form filled with:
  - [ ] GitHub repository URL
  - [ ] Deployed application URL: `http://<ec2-ip>:8000`
  - [ ] Jenkins URL: `http://<ec2-ip>:8080`
  - [ ] Student information
- [ ] Report document uploaded to LMS/submitted via email
- [ ] Instructor confirmed as collaborator on GitHub

### Post-Submission
- [ ] Keep EC2 instance running until grading complete
- [ ] Monitor for instructor access to GitHub
- [ ] Be ready to provide credentials if needed (via secure channel)
- [ ] Respond promptly to any instructor queries

## Testing Credentials Template

**For Instructor Access (share via secure channel, not in code):**

```
Application URL: http://<ec2-ip>:8000
Jenkins URL: http://<ec2-ip>:8080

Jenkins Login:
- Username: [if created additional user]
- Password: [if created additional user]
- OR use GitHub credentials

GitHub Repository: <your-repo-url>
- Instructor should be added as collaborator

MongoDB Atlas:
- Read-only access can be provided if needed
- Connection string in .env file on EC2

EC2 Access:
- Available via SSH if needed for grading
- Key pair: ass3.pem (provide if requested)
```

## Troubleshooting Common Issues

### Application Issues
- [ ] Check service status: `sudo systemctl status order-manager`
- [ ] View logs: `sudo journalctl -u order-manager -f`
- [ ] Verify MongoDB connection
- [ ] Check .env file has correct credentials
- [ ] Ensure port 8000 is open in security group

### Jenkins Issues
- [ ] Verify Docker is running: `sudo systemctl status docker`
- [ ] Check jenkins in docker group: `groups jenkins`
- [ ] View Jenkins logs: `sudo journalctl -u jenkins -f`
- [ ] Verify GitHub webhook is configured
- [ ] Check credentials are correct

### Test Issues
- [ ] Ensure app is running on port 8000
- [ ] Check TEST_MONGO_URI is set in Jenkins credentials
- [ ] Verify Chrome/ChromeDriver versions match
- [ ] Check Docker has enough memory (--shm-size=2g)
- [ ] Review test logs in Jenkins console output

### Email Issues
- [ ] Verify Gmail app password (not regular password)
- [ ] Check TLS is enabled
- [ ] Test SMTP settings in Jenkins
- [ ] Verify recipient email is correct
- [ ] Check spam/junk folder

## Success Criteria

✅ **Application:**
- Accessible via public URL
- All features working (add, edit, delete, search)
- OCR processing functional
- Data persists in MongoDB

✅ **Tests:**
- All 13 Selenium tests passing
- Tests run in headless Chrome
- JUnit XML and HTML reports generated

✅ **Jenkins:**
- Pipeline triggers on GitHub push
- All stages complete successfully
- Test results published
- Email notifications sent

✅ **Documentation:**
- Comprehensive README
- Complete report with screenshots
- All code properly commented
- Setup guides available

✅ **Submission:**
- Google form submitted
- Instructor has access
- All URLs working
- Grading can proceed

---

**Estimated Time to Complete:**
- Local Development: 2-3 hours
- EC2 Setup: 1-2 hours
- Jenkins Configuration: 2-3 hours
- Testing & Documentation: 3-4 hours
- **Total: 8-12 hours**

**Good luck with your submission! 🚀**
