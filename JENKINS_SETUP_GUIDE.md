# Jenkins Setup Guide - Complete Installation & Configuration

This guide covers **everything** from installing Jenkins to configuring webhooks and email notifications.

---

## Part 1: Install Jenkins on Ubuntu/EC2

### Step 1: Install Java 17 (Required for Jenkins)

```bash
# Install Java 17 and fontconfig
sudo apt update
sudo apt install fontconfig openjdk-17-jre -y

# Verify installation
java -version
# Should show: openjdk version "17.0.x"
```

### Step 2: Install Jenkins

```bash
# Add Jenkins repository key
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key

# Add Jenkins repository
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | \
  sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

# Update and install Jenkins
sudo apt update
sudo apt install jenkins -y
```

### Step 3: Configure Jenkins to Use Java 17

```bash
# Create systemd override directory
sudo mkdir -p /etc/systemd/system/jenkins.service.d

# Create override configuration
echo -e '[Service]\nEnvironment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"' | \
  sudo tee /etc/systemd/system/jenkins.service.d/override.conf

# Reload systemd and start Jenkins
sudo systemctl daemon-reload
sudo systemctl reset-failed jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Check status
sudo systemctl status jenkins
```

### Step 4: Get Initial Admin Password

```bash
# Retrieve the initial password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

**Save this password** - you'll need it for initial setup!

### Step 5: Configure Docker Permissions for Jenkins

```bash
# Add jenkins user to docker group
sudo usermod -aG docker jenkins

# Verify
groups jenkins
# Should show: jenkins : jenkins docker

# Restart Jenkins to apply changes
sudo systemctl restart jenkins
```

### Step 6: Open Required Ports in AWS Security Group

1. Go to **AWS Console** → **EC2** → **Instances**
2. Select your instance
3. Click **Security** tab → Click on **Security Group** link
4. Click **Edit inbound rules** → **Add rule**:

| Type       | Port Range | Source    | Description           |
|------------|------------|-----------|-----------------------|
| Custom TCP | 8080       | 0.0.0.0/0 | Jenkins Web Interface |
| Custom TCP | 8000       | 0.0.0.0/0 | Application Server    |

5. Click **Save rules**

---

## Part 2: Initial Jenkins Configuration

### Step 1: Access Jenkins Web Interface

1. Open browser: `http://<YOUR_EC2_PUBLIC_IP>:8080`
2. Paste the initial admin password from earlier
3. Click **Install suggested plugins**
4. Create your first admin user
5. Keep the default Jenkins URL
6. Click **Start using Jenkins**

### Step 2: Install Required Plugins

1. Go to **Manage Jenkins** → **Plugins** → **Available plugins**
2. Search and install:
   - **Docker Pipeline** (if not already installed)
   - **Email Extension Plugin** (if not already installed)
   - **Git** (if not already installed)
3. Restart Jenkins if prompted

---

## Part 3: Configure Jenkins Credentials

Your pipeline needs MongoDB and Groq API credentials.

1. Go to **Dashboard** → **Manage Jenkins** → **Credentials**
2. Click **System** → **Global credentials (unrestricted)**
3. Click **+ Add Credentials**

Add these **three** credentials:

### Credential 1: MongoDB URI
- **Kind**: Secret text
- **Scope**: Global
- **Secret**: `mongodb+srv://username:password@cluster.mongodb.net/database`
- **ID**: `mongo-uri`
- **Description**: MongoDB Connection String
- Click **Create**

### Credential 2: Test MongoDB URI
- **Kind**: Secret text
- **Scope**: Global
- **Secret**: Your test database URI
- **ID**: `test-mongo-uri`
- **Description**: Test Database Connection
- Click **Create**

### Credential 3: Groq API Key
- **Kind**: Secret text
- **Scope**: Global
- **Secret**: `gsk_...` (your Groq API key)
- **ID**: `groq-api-key`
- **Description**: Groq API Key for OCR
- Click **Create**

---

## Part 4: Create Jenkins Pipeline Job

### Step 1: Create New Pipeline

1. Go to **Dashboard** → **New Item**
2. Enter name: `OrderManager-Pipeline`
3. Select **Pipeline**
4. Click **OK**

### Step 2: Configure Pipeline

Scroll down to the **Pipeline** section:

- **Definition**: `Pipeline script from SCM`
- **SCM**: `Git`
- **Repository URL**: `https://github.com/Mohibullah16/assign3Devops.git`
- **Credentials**: None (public repository)
- **Branch Specifier**: `*/main`
- **Script Path**: `Jenkinsfile`

### Step 3: Enable Build Triggers (Optional Polling)

Check **Poll SCM** if you want fallback polling:
- **Schedule**: `H/5 * * * *` (every 5 minutes)

**Note**: We'll set up webhooks for instant triggers later.

Click **Save**.

---

## Part 5: Configure Email Notifications (Gmail SMTP)

### Step 1: Get Gmail App Password

1. Go to https://myaccount.google.com/apppasswords
2. Sign in with your Gmail account
3. Select **App**: Mail, **Device**: Other (Custom name) → Enter "Jenkins"
4. Click **Generate**
5. **Copy the 16-character password** (no spaces)

### Step 2: Configure Jenkins Email Settings

1. Go to **Manage Jenkins** → **System**
2. Scroll to **Extended E-mail Notification**

**Configure as follows:**

- **SMTP server**: `smtp.gmail.com`
- **Default user E-mail suffix**: `@gmail.com`
- Click **Advanced...**
  - **Use SMTP Authentication**: ✅ Checked
  - **User Name**: Your full Gmail address (e.g., `mohibazhar16@gmail.com`)
  - **Password**: Paste the 16-character App Password
  - **Use SSL**: ✅ Checked
  - **SMTP Port**: `465`
  - **Charset**: `UTF-8`
  - **Default Content Type**: `text/html`

3. Scroll to **E-mail Notification** section (standard plugin)

**Configure similarly:**
- **SMTP server**: `smtp.gmail.com`
- Click **Advanced...**
  - **Use SMTP Authentication**: ✅ Checked
  - **User Name**: Your Gmail address
  - **Password**: App Password
  - **Use SSL**: ✅ Checked
  - **SMTP Port**: `465`

4. **Test Configuration** (Optional):
   - Check **Test configuration by sending test e-mail**
   - Enter your email
   - Click **Test configuration**
   - You should receive a test email

5. Click **Save**

---

## Part 6: Configure Git for Commit Emails

Jenkins sends emails based on Git commit author. Configure your Git identity:

```bash
# Set your name and email globally
git config --global user.name "YourName"
git config --global user.email "your.email@gmail.com"

# Verify
git config --global --list | grep user
```

**Important**: Use the **same email** you configured in Jenkins SMTP settings!

When you commit and push code, Jenkins will send build notifications to this email.

---

## Part 7: Setup GitHub Webhook (Instant Triggers)

### Step 1: Configure Webhook in GitHub

1. Go to your repository: https://github.com/Mohibullah16/assign3Devops
2. Click **Settings** (repository settings, not account)
3. Click **Webhooks** (left sidebar)
4. Click **Add webhook**

**Configure webhook:**

- **Payload URL**: `http://<YOUR_EC2_PUBLIC_IP>:8080/github-webhook/`
  - ⚠️ **Must end with trailing slash** `/github-webhook/`
  - Example: `http://13.60.219.62:8080/github-webhook/`
- **Content type**: `application/json`
- **Secret**: Leave blank (or add for extra security)
- **Which events would you like to trigger this webhook?**
  - Select **Just the push event**
- **Active**: ✅ Checked

Click **Add webhook**

### Step 2: Verify Webhook

1. After adding, GitHub will send a test ping
2. Click on your webhook in the list
3. Click **Recent Deliveries** tab
4. You should see a delivery with a **green checkmark** ✅
5. If you see a **red X**, check:
   - Jenkins is running: `sudo systemctl status jenkins`
   - Port 8080 is open in Security Group
   - URL ends with `/github-webhook/`

---

## Part 8: Run Your First Build

### Option 1: Manual Trigger
1. Go to Jenkins Dashboard
2. Click on your pipeline: `OrderManager-Pipeline`
3. Click **Build Now**
4. Watch the build progress

### Option 2: Automatic Trigger (via Webhook)
1. Make any change to your repository
2. Commit and push:
   ```bash
   git add .
   git commit -m "Test Jenkins webhook"
   git push origin main
   ```
3. Jenkins should **automatically** start building within seconds!

### View Build Results

1. Click the build number (e.g., `#1`)
2. Click **Console Output** to see logs
3. Check **Test Results** for test reports
4. You should receive an **email notification** when build completes!

---

## Part 9: Add Collaborators (Professor)

To send build notifications to your professor:

1. Go to your repository: https://github.com/Mohibullah16/assign3Devops
2. Click **Settings** → **Collaborators**
3. Click **Add people**
4. Enter professor's GitHub username or email
5. Select access level (e.g., **Write**)
6. Click **Add [username] to this repository**

Your professor will receive:
- An invitation email to join the repository
- Build notifications when they push code (after accepting invitation)

---

## Part 10: Accessing Your Deployed Application

After a successful build, your application stays running!

**Access URL**: `http://<YOUR_EC2_PUBLIC_IP>:8000`

Example: `http://13.60.219.62:8000`

---

## Troubleshooting

### Jenkins Won't Start
```bash
# Check logs
sudo journalctl -u jenkins --since "5 minutes ago" --no-pager

# Common issue: Wrong Java version
sudo systemctl status jenkins
# Should show Java 17

# Fix Java path if needed
sudo systemctl edit jenkins
# Add: Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
```

### Docker Permission Denied
```bash
# Ensure jenkins is in docker group
groups jenkins

# If not, add it
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Email Not Sending
- Verify Gmail App Password (not regular password)
- Check SMTP settings: `smtp.gmail.com:465` with SSL
- Test with "Test configuration by sending test e-mail"
- Check Jenkins system log: **Manage Jenkins** → **System Log**

### Webhook Not Triggering
- Verify URL ends with `/github-webhook/`
- Check port 8080 is accessible from internet
- Review webhook delivery in GitHub: Settings → Webhooks → Recent Deliveries

### Build Fails on Tests
```bash
# Check app is running
curl http://localhost:8000

# Check logs
cat /var/lib/jenkins/workspace/OrderManager-Pipeline/order_manager/app.log
```

---

## Summary Checklist

Before running your pipeline, ensure:

- ✅ Jenkins installed and running (Java 17)
- ✅ Docker permissions configured (`jenkins` in `docker` group)
- ✅ Security Group: Ports 8080 and 8000 open
- ✅ Jenkins credentials configured (mongo-uri, test-mongo-uri, groq-api-key)
- ✅ Gmail SMTP configured with App Password
- ✅ Git user.email matches your notification email
- ✅ GitHub webhook configured
- ✅ Pipeline job created from SCM

**You're all set! Push code and watch Jenkins work!** 🚀

