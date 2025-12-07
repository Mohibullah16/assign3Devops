# EC2 Configuration Commands

## 1. Create 5GB Swap Memory

```bash
# Create swap file
sudo dd if=/dev/zero of=/swapfile bs=1M count=5120

# Set permissions
sudo chmod 600 /swapfile

# Setup swap area
sudo mkswap /swapfile

# Enable swap
  sudo swapon /swapfile

# Make swap permanent (add to fstab)
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify swap is active
swapon --show
free -h
```

## 2. Install Required Software

```bash
# Update system
sudo apt update
sudo apt upgrade -y

# Install Python 3.11
sudo apt install -y python3.11 python3.11-venv python3-pip

# Install Docker
sudo apt install -y docker.io
sudo systemctl enable docker
sudo systemctl start docker

# Install Java for Jenkins
sudo apt install -y openjdk-11-jdk

# Add users to docker group
sudo usermod -aG docker ubuntu
sudo usermod -aG docker jenkins
```

## 3. Install Jenkins

```bash
# Add Jenkins repository key
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key

# Add Jenkins repository
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

# Update and install Jenkins
sudo apt update
sudo apt install -y jenkins

# Start Jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins

# Verify Jenkins is running
sudo systemctl status jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

## 4. Deploy Order Manager Application

```bash
# Clone repository
cd /home/ubuntu
git clone https://github.com/Mohibullah16/assign3Devops.git Assignment3
cd Assignment3/order_manager

# Create virtual environment
python3.11 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Create .env file
nano .env
# Add your environment variables:
# MONGO_URI=mongodb+srv://...
# TEST_MONGO_URI=mongodb+srv://...
# GROQ_API_KEY=gsk_...
# SECRET_KEY=your-secret-key

# Test run
uvicorn app:app --host 0.0.0.0 --port 8000
```

## 5. Create Systemd Service for Application

```bash
# Create service file
sudo nano /etc/systemd/system/order-manager.service
```

**Paste this content:**
```ini
[Unit]
Description=Order Manager FastAPI Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/Assignment3/order_manager
Environment="PATH=/home/ubuntu/Assignment3/order_manager/venv/bin"
ExecStart=/home/ubuntu/Assignment3/order_manager/venv/bin/uvicorn app:app --host 0.0.0.0 --port 8000 --workers 2
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
```

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable and start service
sudo systemctl enable order-manager
sudo systemctl start order-manager

# Check status
sudo systemctl status order-manager

# View logs
sudo journalctl -u order-manager -f
```

## 6. Configure Firewall (if ufw is enabled)

```bash
# Allow SSH
sudo ufw allow 22/tcp

# Allow HTTP for app
sudo ufw allow 8000/tcp

# Allow Jenkins
sudo ufw allow 8080/tcp

# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

## 7. MongoDB Atlas Setup

1. Go to https://www.mongodb.com/cloud/atlas
2. Create free tier cluster (M0)
3. Create database user:
   - Username: `orderuser`
   - Password: Generate strong password
4. Network Access:
   - Add IP: `0.0.0.0/0` (for testing) or your EC2 IP
5. Create databases:
   - `order_management_db`
   - `order_management_test_db`
6. Get connection string from "Connect" → "Connect your application"
7. Replace `<password>` and `<dbname>` in connection string

## 8. Groq API Setup

1. Go to https://console.groq.com
2. Sign up / Log in
3. Generate API key from Keys section
4. Copy key (starts with `gsk_...`)

## 9. Jenkins Configuration

**Access Jenkins:**
```bash
# Get initial password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# Access at: http://<ec2-public-ip>:8080
```

**Install Plugins:**
- GitHub Integration
- Docker Pipeline
- Email Extension Plugin
- HTML Publisher
- JUnit Plugin

**Configure Email Extension:**
- Manage Jenkins → Configure System
- Extended E-mail Notification:
  - SMTP server: `smtp.gmail.com`
  - SMTP Port: `587`
  - Use TLS: ✓
  - Credentials: Add Gmail + App Password
  
**Gmail App Password:**
1. Enable 2FA on Gmail
2. Visit: https://myaccount.google.com/apppasswords
3. Generate password for "Mail"
4. Use in Jenkins credentials

**Add Credentials:**
- Manage Jenkins → Credentials → Global
- Add Secret Text:
  - ID: `test-mongo-uri`
  - Secret: Your TEST_MONGO_URI value
- Add Username/Password:
  - Username: GitHub username
  - Password: GitHub Personal Access Token

## 10. Create Jenkins Pipeline Job

1. New Item → Pipeline
2. Name: `Order-Manager-Tests`
3. Configure:
   - **Build Triggers:** ✓ GitHub hook trigger for GITScm polling
   - **Pipeline:**
     - Definition: Pipeline script from SCM
     - SCM: Git
     - Repository URL: Your GitHub repo URL
     - Credentials: GitHub credentials
     - Branch: `*/main`
     - Script Path: `Jenkinsfile`
4. Save

## 11. GitHub Webhook Setup

1. Go to GitHub repository → Settings → Webhooks
2. Add webhook:
   - Payload URL: `http://<ec2-public-ip>:8080/github-webhook/`
   - Content type: `application/json`
   - SSL verification: Disable (if using HTTP)
   - Events: Just the push event
   - Active: ✓
3. Save

## 12. Test the Pipeline

```bash
# Make a test commit
cd /home/ubuntu/Assignment3
echo "# Test commit" >> README.md
git add .
git commit -m "Test Jenkins pipeline"
git push origin main

# Check Jenkins dashboard
# Pipeline should trigger automatically
```

## 13. Monitoring Commands

```bash
# Check application status
sudo systemctl status order-manager

# View application logs
sudo journalctl -u order-manager -f

# Check Jenkins status
sudo systemctl status jenkins

# Check Docker containers
docker ps -a

# Check disk space
df -h

# Check memory usage
free -h

# Check swap usage
swapon --show

# View Docker logs
docker logs <container-id>
```

## 14. Troubleshooting

**Application won't start:**
```bash
# Check logs
sudo journalctl -u order-manager -n 50

# Test manually
cd /home/ubuntu/Assignment3/order_manager
source venv/bin/activate
uvicorn app:app --host 0.0.0.0 --port 8000
```

**Jenkins can't access Docker:**
```bash
# Verify jenkins user in docker group
groups jenkins

# If not, add it
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

**Tests failing:**
```bash
# Check if app is running
curl http://localhost:8000

# Check Docker network
docker network ls

# Run tests manually
docker run --rm --network=host -e APP_URL=http://localhost:8000 selenium-tests:latest
```

**Email not sending:**
- Check Gmail app password is correct
- Verify SMTP settings in Jenkins
- Check Jenkins system logs: Manage Jenkins → System Log

## 15. Security Best Practices

```bash
# Keep system updated
sudo apt update && sudo apt upgrade -y

# Configure SSH key authentication only
sudo nano /etc/ssh/sshd_config
# Set: PasswordAuthentication no
sudo systemctl restart sshd

# Regular backups of .env and database
# Store credentials in Jenkins credentials store, not in code
# Use environment-specific .env files
# Regularly rotate API keys and passwords
```
