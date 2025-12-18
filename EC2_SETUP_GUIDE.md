# EC2 Complete Setup Guide

Complete step-by-step guide for setting up your AWS EC2 instance with Jenkins, Docker, Python, and deploying the Order Manager application.

---

## Prerequisites

- AWS Account
- EC2 Instance (t2.medium or larger recommended)
- Ubuntu 24.04 LTS
- SSH access (.pem key file)

---

## Step 1: Connect to EC2 Instance

```bash
# Update permissions on your key file
chmod 400 your-key.pem

# Connect via SSH
ssh -i your-key.pem ubuntu@<YOUR_EC2_PUBLIC_IP>
```

---

## Step 2: Create Swap Memory (5GB)

Swap prevents out-of-memory issues during builds.

```bash
# Create 5GB swap file
sudo dd if=/dev/zero of=/swapfile bs=1M count=5120

# Set correct permissions
sudo chmod 600 /swapfile

# Setup swap area
sudo mkswap /swapfile

# Enable swap immediately
sudo swapon /swapfile

# Make swap permanent (persists after reboot)
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify swap is active
swapon --show
free -h
```

**Expected output:**
```
NAME      TYPE SIZE USED PRIO
/swapfile file   5G   0B   -2
```

---

## Step 3: Update System & Install Basic Tools

```bash
# Update package list and upgrade installed packages
sudo apt update
sudo apt upgrade -y

# Install essential tools
sudo apt install -y git curl wget unzip lsof net-tools
```

---

## Step 4: Install Docker

```bash
# Install Docker
sudo apt install -y docker.io

# Enable Docker to start on boot
sudo systemctl enable docker

# Start Docker service
sudo systemctl start docker

# Verify Docker is running
sudo systemctl status docker

# Add ubuntu user to docker group (allows running docker without sudo)
sudo usermod -aG docker ubuntu

# Apply group changes (logout and login, or run):
newgrp docker

# Test Docker
docker --version
docker ps
```

---

## Step 5: Install Python 3.12

```bash
# Install Python 3.12 and virtual environment support
sudo apt install -y python3.12 python3.12-venv python3-pip

# Verify installation
python3.12 --version
```

---

## Step 6: Install Java 17 (Required for Jenkins)

```bash
# Install Java 17 and fontconfig
sudo apt install -y fontconfig openjdk-17-jre

# Verify installation
java -version
```

**Expected output:**
```
openjdk version "17.0.x"
```

---

## Step 7: Install Jenkins

### 7.1 Add Jenkins Repository

```bash
# Download Jenkins GPG key
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key

# Add Jenkins repository
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | \
  sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

# Update package list
sudo apt update
```

### 7.2 Install Jenkins

```bash
# Install Jenkins
sudo apt install -y jenkins
```

### 7.3 Configure Jenkins to Use Java 17

```bash
# Create systemd override directory
sudo mkdir -p /etc/systemd/system/jenkins.service.d

# Create override configuration
echo -e '[Service]\nEnvironment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"' | \
  sudo tee /etc/systemd/system/jenkins.service.d/override.conf

# Reload systemd configuration
sudo systemctl daemon-reload

# Reset any failed state
sudo systemctl reset-failed jenkins

# Start Jenkins
sudo systemctl start jenkins

# Enable Jenkins to start on boot
sudo systemctl enable jenkins

# Check Jenkins status
sudo systemctl status jenkins
```

### 7.4 Get Initial Admin Password

```bash
# Retrieve the password (save this!)
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

**Save this password** - you'll need it to unlock Jenkins!

### 7.5 Add Jenkins User to Docker Group

```bash
# Add jenkins to docker group
sudo usermod -aG docker jenkins

# Verify
groups jenkins

# Restart Jenkins to apply changes
sudo systemctl restart jenkins
```

---

## Step 8: Configure AWS Security Group

Open required ports in your EC2 Security Group:

1. Go to **AWS Console** → **EC2** → **Instances**
2. Select your instance
3. Click **Security** tab → Click **Security Group** link
4. Click **Edit inbound rules** → **Add rules**:

| Type       | Port  | Source    | Description           |
|------------|-------|-----------|-----------------------|
| SSH        | 22    | My IP     | SSH Access            |
| Custom TCP | 8080  | 0.0.0.0/0 | Jenkins Web Interface |
| Custom TCP | 8000  | 0.0.0.0/0 | Application Server    |

5. Click **Save rules**

---

## Step 9: Configure Git Identity

Set your Git user information for commits:

```bash
# Set your name
git config --global user.name "Your Name"

# Set your email (use the email you want to receive Jenkins notifications)
git config --global user.email "your.email@gmail.com"

# Verify configuration
git config --global --list | grep user
```

---

## Step 10: Clone Your Repository

```bash
# Navigate to home directory
cd /home/ubuntu

# Clone repository (replace with your repo URL)
git clone https://github.com/Mohibullah16/assign3Devops.git
cd assign3Devops
```

---

## Step 11: Manual Application Deployment (Optional)

If you want to test the app manually before Jenkins:

```bash
# Navigate to app directory
cd /home/ubuntu/assign3Devops/order_manager

# Create virtual environment
python3.12 -m venv venv

# Activate virtual environment
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Create .env file (add your credentials)
nano .env
```

Add these variables to `.env`:
```env
MONGO_URI=mongodb+srv://username:password@cluster.mongodb.net/database
TEST_MONGO_URI=mongodb+srv://username:password@cluster.mongodb.net/testdb
GROQ_API_KEY=gsk_your_api_key_here
SECRET_KEY=your-secret-key-here
```

Run the application:
```bash
# Start server
uvicorn app:app --host 0.0.0.0 --port 8000

# Or run in background
nohup uvicorn app:app --host 0.0.0.0 --port 8000 > app.log 2>&1 &
```

Access at: `http://<YOUR_EC2_PUBLIC_IP>:8000`

---

## Step 12: Access Jenkins Web Interface

1. Open browser: `http://<YOUR_EC2_PUBLIC_IP>:8080`
2. Paste the initial admin password
3. Click **Install suggested plugins**
4. Create admin user
5. Configure Jenkins URL (keep default)
6. Start using Jenkins!

---

## Next Steps

Now that EC2 and Jenkins are set up:

1. **Configure Jenkins Pipeline** - See [JENKINS_SETUP_GUIDE.md](JENKINS_SETUP_GUIDE.md)
2. **Add MongoDB Atlas** - See [MONGODB_ATLAS_SETUP.md](MONGODB_ATLAS_SETUP.md)
3. **Setup Webhooks** - Covered in Jenkins guide
4. **Configure Email Notifications** - Covered in Jenkins guide

---

## Useful Commands

### Check Service Status
```bash
# Jenkins
sudo systemctl status jenkins

# Docker
sudo systemctl status docker

# Application (if running manually)
ps aux | grep uvicorn
```

### View Logs
```bash
# Jenkins logs
sudo journalctl -u jenkins -f

# Application logs (if using nohup)
tail -f /home/ubuntu/assign3Devops/order_manager/app.log

# Docker logs
docker logs <container_id>
```

### Restart Services
```bash
# Jenkins
sudo systemctl restart jenkins

# Docker
sudo systemctl restart docker
```

### Check Open Ports
```bash
# Check if port is listening
sudo lsof -i :8080  # Jenkins
sudo lsof -i :8000  # Application
```

### Get Public IP
```bash
# Get your EC2 public IP
curl -s ifconfig.me
```

---

## Troubleshooting

### Jenkins Won't Start
```bash
# Check Java version
java -version

# Check Jenkins logs
sudo journalctl -u jenkins --since "5 minutes ago" --no-pager

# Verify Java path in override
cat /etc/systemd/system/jenkins.service.d/override.conf
```

### Docker Permission Denied
```bash
# Check docker group membership
groups ubuntu
groups jenkins

# Re-add to group if needed
sudo usermod -aG docker ubuntu
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Out of Memory
```bash
# Check memory usage
free -h

# Check swap
swapon --show

# If swap not active, re-enable
sudo swapon /swapfile
```

### Can't Access Jenkins from Browser
1. Check Security Group has port 8080 open
2. Check Jenkins is running: `sudo systemctl status jenkins`
3. Check firewall: `sudo ufw status` (should be inactive)
4. Verify public IP: `curl ifconfig.me`

---

## Summary Checklist

- ✅ EC2 instance running Ubuntu 24.04
- ✅ Swap memory created (5GB)
- ✅ System updated and essential tools installed
- ✅ Docker installed and running
- ✅ Python 3.12 installed
- ✅ Java 17 installed
- ✅ Jenkins installed and running on port 8080
- ✅ Jenkins configured to use Java 17
- ✅ Security Group: Ports 22, 8080, 8000 open
- ✅ Git configured with user name and email
- ✅ Repository cloned
- ✅ Users added to docker group

**EC2 setup complete! Proceed to Jenkins configuration.** 🚀

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
