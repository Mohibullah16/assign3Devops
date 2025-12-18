# Order Manager - Quick Start Guide

This guide provides quick commands for common tasks. For detailed explanations, see the full setup guides.

---

## 📚 Full Documentation

- **[EC2_SETUP_GUIDE.md](EC2_SETUP_GUIDE.md)** - Complete EC2 setup with Jenkins & Docker
- **[JENKINS_SETUP_GUIDE.md](JENKINS_SETUP_GUIDE.md)** - Jenkins configuration, webhooks, email
- **[MONGODB_ATLAS_SETUP.md](MONGODB_ATLAS_SETUP.md)** - MongoDB setup and configuration

---

## 🚀 Quick EC2 Setup (One Command Block)

```bash
# === Update System ===
sudo apt update && sudo apt upgrade -y

# === Create 5GB Swap ===
sudo dd if=/dev/zero of=/swapfile bs=1M count=5120 && \
sudo chmod 600 /swapfile && \
sudo mkswap /swapfile && \
sudo swapon /swapfile && \
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# === Install Docker, Python, Java ===
sudo apt install -y git curl docker.io python3.12 python3.12-venv python3-pip fontconfig openjdk-17-jre

# === Enable Docker ===
sudo systemctl enable docker && sudo systemctl start docker && sudo usermod -aG docker ubuntu

# === Install Jenkins ===
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key && \
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null && \
sudo apt update && sudo apt install -y jenkins

# === Configure Jenkins Java ===
sudo mkdir -p /etc/systemd/system/jenkins.service.d && \
echo -e '[Service]\nEnvironment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"' | sudo tee /etc/systemd/system/jenkins.service.d/override.conf && \
sudo systemctl daemon-reload && sudo systemctl reset-failed jenkins && sudo systemctl start jenkins && sudo systemctl enable jenkins

# === Add Jenkins to Docker Group ===
sudo usermod -aG docker jenkins && sudo systemctl restart jenkins

# === Get Jenkins Password ===
echo "=== Jenkins Initial Password ===" && sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# === Configure Git ===
git config --global user.name "YourName"
git config --global user.email "your.email@gmail.com"

# === Clone Repository ===
cd /home/ubuntu && git clone https://github.com/Mohibullah16/assign3Devops.git

echo "✅ Setup complete! Access Jenkins at: http://$(curl -s ifconfig.me):8080"
```

**After running, configure:**
1. AWS Security Group: Open ports 8080 (Jenkins) and 8000 (App)
2. Access Jenkins web interface
3. Complete Jenkins setup wizard

---

## 🔐 Required API Keys

### MongoDB Atlas
1. Visit: https://cloud.mongodb.com
2. Create free cluster (M0)
3. Database Access → Add user (username/password)
4. Network Access → Add IP: `0.0.0.0/0`
5. Get connection string: `mongodb+srv://...`

### Groq API Key
1. Visit: https://console.groq.com
2. Sign up/Login
3. API Keys → Create API Key
4. Copy key (starts with `gsk_`)

---

## ⚙️ Jenkins Configuration (After Installation)

### 1. Access Jenkins
```
http://<YOUR_EC2_IP>:8080
```

Paste initial password shown in terminal, install suggested plugins.

### 2. Add Credentials
**Manage Jenkins → Credentials → Global → Add Credentials**

Add three "Secret text" credentials:
- **ID**: `mongo-uri` → **Secret**: Your MongoDB connection string
- **ID**: `test-mongo-uri` → **Secret**: Test database connection string  
- **ID**: `groq-api-key` → **Secret**: Your Groq API key (gsk_...)

### 3. Create Pipeline Job
1. **New Item** → Enter name: `OrderManager-Pipeline` → **Pipeline** → **OK**
2. **Pipeline** section:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/Mohibullah16/assign3Devops.git`
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`
3. **Save**

### 4. Configure Email (Gmail)

**Get App Password:**
- Go to: https://myaccount.google.com/apppasswords
- Generate password for "Jenkins"

**Manage Jenkins → System:**

**Extended E-mail Notification:**
- SMTP server: `smtp.gmail.com`
- Advanced → Username: `your.email@gmail.com` 
- Password: [App Password]
- Use SSL: ✅, Port: `465`

**E-mail Notification:** (same settings as above)

### 5. Setup GitHub Webhook

**GitHub Repository → Settings → Webhooks → Add webhook:**
- **Payload URL**: `http://<YOUR_EC2_IP>:8080/github-webhook/` (⚠️ trailing slash!)
- **Content type**: `application/json`
- **Events**: Just the push event
- **Active**: ✅

---

## 🔄 Daily Workflow

### Make Changes and Deploy
```bash
# Make code changes
git add .
git commit -m "Your message"
git push origin main

# Jenkins automatically builds and deploys!
```

### Check Status
```bash
# Jenkins status
sudo systemctl status jenkins

# Application status
curl http://localhost:8000

# Docker status
docker ps

# View application logs
cat /var/lib/jenkins/workspace/OrderManager-Pipeline/order_manager/app.log
```

---

## 🌐 Access URLs

After successful deployment:

- **Application**: `http://<YOUR_EC2_IP>:8000`
- **Jenkins**: `http://<YOUR_EC2_IP>:8080`
- **GitHub**: `https://github.com/Mohibullah16/assign3Devops`

---

## 🐛 Quick Troubleshooting

### Jenkins Won't Start
```bash
# Check Java version
java -version  # Should be 17

# Check logs
sudo journalctl -u jenkins --since "5 min ago" --no-pager

# Restart
sudo systemctl restart jenkins
```

### Docker Permission Denied
```bash
# Add to group
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Verify
groups jenkins
```

### Email Not Working
- Use Gmail **App Password**, not regular password
- Check SMTP: `smtp.gmail.com:465` with SSL
- Test: Manage Jenkins → System → Extended E-mail → Test configuration

### Webhook Not Triggering
- Verify URL: `http://<IP>:8080/github-webhook/` (trailing slash!)
- Check port 8080 is open in Security Group
- GitHub → Settings → Webhooks → Check Recent Deliveries

### Application Not Accessible
```bash
# Check if running
ps aux | grep uvicorn

# Check port
sudo lsof -i :8000

# Restart via Jenkins
# Go to Jenkins → Build Now
```

---

## 📋 Pre-Submission Checklist

Before submitting your assignment:

- ✅ EC2 instance running and accessible
- ✅ Swap memory configured (5GB)
- ✅ Jenkins installed and running
- ✅ Docker permissions configured
- ✅ MongoDB Atlas database created
- ✅ Jenkins credentials added (mongo-uri, test-mongo-uri, groq-api-key)
- ✅ Jenkins pipeline job created
- ✅ GitHub webhook configured
- ✅ Gmail SMTP configured
- ✅ Git identity configured (name & email)
- ✅ Security Group ports open (22, 8080, 8000)
- ✅ Application accessible at `http://<EC2_IP>:8000`
- ✅ All tests passing in Jenkins
- ✅ Email notifications working
- ✅ Professor added as collaborator

---

## 📖 Local Development (Optional)

If you want to run locally before EC2 deployment:

```bash
# Clone repository
git clone https://github.com/Mohibullah16/assign3Devops.git
cd assign3Devops/order_manager

# Create virtual environment
python3.12 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Create .env file
nano .env
```

Add to `.env`:
```env
MONGO_URI=mongodb+srv://...
TEST_MONGO_URI=mongodb+srv://...
GROQ_API_KEY=gsk_...
SECRET_KEY=your-secret-key
```

```bash
# Run application
uvicorn app:app --reload

# Access at: http://localhost:8000
```

---

## 🆘 Need Help?

1. Check the detailed guides:
   - [EC2_SETUP_GUIDE.md](EC2_SETUP_GUIDE.md)
   - [JENKINS_SETUP_GUIDE.md](JENKINS_SETUP_GUIDE.md)
   
2. Check logs:
   - Jenkins: `sudo journalctl -u jenkins -f`
   - Application: `cat order_manager/app.log`

3. Verify services:
   ```bash
   sudo systemctl status jenkins
   sudo systemctl status docker
   ```

**Good luck! 🚀**
- [ ] Screenshots captured
- [ ] Instructor added as GitHub collaborator
- [ ] Report document completed
- [ ] Google form submitted

**Required Screenshots:**
1. Running application with sample orders
2. Jenkins dashboard with successful build
3. Test results page (JUnit report)
4. HTML test report
5. Email notification received
6. MongoDB Atlas cluster
7. GitHub repository with commits

Good luck! 🚀
