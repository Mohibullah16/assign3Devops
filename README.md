# Order Manager - DevOps Assignment 3

A FastAPI-based Order Management System with OCR capabilities, MongoDB Atlas integration, and automated Selenium testing via Jenkins CI/CD pipeline.

## Features

- 📦 **Order Management**: Create, read, update, and delete orders with multiple line items
- 🤖 **OCR Integration**: Automatic invoice data extraction using Groq Vision API
- 📊 **Analytics Dashboard**: Real-time KPIs (Total Spend, Order Count, Average Order Value)
- 🔍 **Search Functionality**: Filter orders by invoice number or customer name
- 🎨 **Modern UI**: Clean, responsive interface using Pico.css
- ☁️ **Cloud Database**: MongoDB Atlas for scalable data storage
- 🧪 **Automated Testing**: 13 Selenium test cases with headless Chrome
- 🚀 **CI/CD Pipeline**: Jenkins automation with Docker containerization

## Tech Stack

- **Backend**: FastAPI (Python 3.11)
- **Database**: MongoDB Atlas
- **OCR**: Groq API (llama-3.2-11b-vision-preview)
- **Frontend**: HTML5, Pico.css, Vanilla JavaScript
- **Testing**: Selenium WebDriver, pytest
- **CI/CD**: Jenkins, Docker
- **Deployment**: AWS EC2, Gunicorn/Uvicorn

## Project Structure

```
Assignment3/
├── order_manager/
│   ├── static/
│   │   └── uploads/          # Invoice images storage
│   ├── templates/
│   │   ├── index.html        # Main page with order list
│   │   └── edit.html         # Edit order page
│   ├── app.py                # FastAPI application
│   ├── requirements.txt      # Python dependencies
│   └── .env.example          # Environment variables template
├── tests/
│   ├── test_order_manager.py # 13 Selenium test cases
│   └── requirements.txt      # Test dependencies
├── Dockerfile.test           # Docker image for running tests
├── Jenkinsfile              # CI/CD pipeline configuration
└── README.md

```

## Setup Instructions

### 1. Prerequisites

- Python 3.11+
- MongoDB Atlas account (free tier)
- Groq API key ([Get here](https://console.groq.com/keys))
- Docker (for testing)
- Jenkins (for CI/CD)

### 2. Local Development Setup

```bash
# Clone repository
git clone <repository-url>
cd Assignment3

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r order_manager/requirements.txt

# Configure environment variables
cp order_manager/.env.example order_manager/.env
# Edit .env with your MongoDB URI and Groq API key

# Run application
cd order_manager
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

Access the application at `http://localhost:8000`

### 3. MongoDB Atlas Setup

1. Create free cluster at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create two databases:
   - `order_management_db` (production)
   - `order_management_test_db` (testing)
3. Add database user with read/write permissions
4. Whitelist IP: `0.0.0.0/0` (for testing) or specific IPs
5. Copy connection string to `.env` file

### 4. Running Tests Locally

```bash
# Install test dependencies
pip install -r tests/requirements.txt

# Set environment variables
export APP_URL=http://localhost:8000
export TEST_MONGO_URI=<your-test-mongodb-uri>
export TEST_MODE=true

# Run tests
pytest tests/test_order_manager.py -v -s
```

### 5. Docker Test Environment

```bash
# Build test image
docker build -f Dockerfile.test -t selenium-tests:latest .

# Run tests in container
docker run --rm \
  --shm-size=2g \
  --network=host \
  -e APP_URL=http://localhost:8000 \
  -e TEST_MONGO_URI=<your-test-mongodb-uri> \
  selenium-tests:latest
```

## AWS EC2 Deployment

### 1. EC2 Instance Setup

```bash
# SSH into EC2
ssh -i ass3.pem ubuntu@<ec2-public-ip>

# Create 5GB swap memory
sudo dd if=/dev/zero of=/swapfile bs=1M count=5120
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Install dependencies
sudo apt update
sudo apt install -y python3.11 python3.11-venv python3-pip docker.io
sudo systemctl enable docker
sudo systemctl start docker

# Add user to docker group
sudo usermod -aG docker ubuntu
```

### 2. Deploy Application

```bash
# Clone repository
git clone <repository-url>
cd Assignment3/order_manager

# Create virtual environment
python3.11 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Configure .env file
nano .env  # Add your credentials

# Create systemd service
sudo nano /etc/systemd/system/order-manager.service
```

**Service file content:**
```ini
[Unit]
Description=Order Manager FastAPI App
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/Assignment3/order_manager
Environment="PATH=/home/ubuntu/Assignment3/order_manager/venv/bin"
ExecStart=/home/ubuntu/Assignment3/order_manager/venv/bin/uvicorn app:app --host 0.0.0.0 --port 8000 --workers 2
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable order-manager
sudo systemctl start order-manager
sudo systemctl status order-manager
```

### 3. Security Group Configuration

Open these ports in AWS EC2 Security Group:
- **22** (SSH)
- **8000** (Application)
- **8080** (Jenkins)

## Jenkins CI/CD Setup

### 1. Install Jenkins on EC2

```bash
# Install Java
sudo apt install -y openjdk-11-jdk

# Add Jenkins repository
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo tee \
  /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

# Install Jenkins
sudo apt update
sudo apt install -y jenkins

# Add jenkins user to docker group
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 2. Configure Jenkins

1. Access Jenkins at `http://<ec2-ip>:8080`
2. Install suggested plugins + additional:
   - GitHub Integration Plugin
   - Docker Pipeline Plugin
   - Email Extension Plugin
   - HTML Publisher Plugin
   - JUnit Plugin

3. **Configure Email Extension:**
   - Manage Jenkins → Configure System → Extended E-mail Notification
   - SMTP server: `smtp.gmail.com`
   - SMTP Port: `587`
   - Use TLS: ✓
   - Credentials: Add Gmail + App Password
   - Default recipients: `<instructor-email>`

4. **Create Gmail App Password:**
   - Enable 2FA on Gmail account
   - Go to https://myaccount.google.com/apppasswords
   - Generate app password for "Mail"
   - Use this password in Jenkins credentials

5. **Add Credentials:**
   - Manage Jenkins → Credentials
   - Add Secret Text: `test-mongo-uri` (MongoDB connection string)
   - Add Username/Password: GitHub account

### 3. Create Jenkins Pipeline Job

1. New Item → Pipeline → Name: "Order-Manager-Tests"
2. Configure:
   - **General**: GitHub project URL
   - **Build Triggers**: ✓ GitHub hook trigger for GITScm polling
   - **Pipeline**: 
     - Definition: Pipeline script from SCM
     - SCM: Git
     - Repository URL: `<your-github-repo>`
     - Credentials: (GitHub credentials)
     - Branch: `*/main`
     - Script Path: `Jenkinsfile`

### 4. Configure GitHub Webhook

1. Go to GitHub repository → Settings → Webhooks
2. Add webhook:
   - Payload URL: `http://<ec2-ip>:8080/github-webhook/`
   - Content type: `application/json`
   - Events: Just the push event
   - Active: ✓

## Test Cases

The project includes 13 comprehensive Selenium test cases:

1. **Homepage loads** - Verify correct title
2. **KPIs display** - Check all 3 KPI cards present
3. **Add valid order** - Complete order submission
4. **Empty description validation** - Required field check
5. **Negative price validation** - Min value validation
6. **Zero quantity validation** - Min value validation
7. **Delete order** - Remove order functionality
8. **Edit order loads data** - Pre-fill form correctly
9. **Update order** - Save changes functionality
10. **Search functionality** - Filter by invoice/customer
11. **Invoice upload indicator** - OCR loading message
12. **Orders table display** - Table rendering
13. **Multiple items** - Add multiple line items

All tests run in headless Chrome mode for CI/CD compatibility.

## API Endpoints

- `GET /` - Homepage with order list and KPIs
- `POST /add_order` - Create new order
- `GET /edit/{id}` - Get edit form for order
- `POST /update/{id}` - Update existing order
- `GET /delete/{id}` - Delete order
- `GET /search?q=<query>` - Search orders
- `POST /process_ocr` - Process invoice image with OCR

## Environment Variables

```env
# MongoDB Atlas
MONGO_URI=mongodb+srv://user:pass@cluster.mongodb.net/
TEST_MONGO_URI=mongodb+srv://user:pass@cluster.mongodb.net/

# Groq API
GROQ_API_KEY=gsk_your_key_here

# Application
SECRET_KEY=your-secret-key
TEST_MODE=false
```

## Troubleshooting

### Chrome/ChromeDriver Issues
- Ensure Chrome and ChromeDriver versions match
- Use `--shm-size=2g` flag for Docker to prevent crashes
- Check ChromeDriver is in PATH

### MongoDB Connection
- Verify IP whitelist includes EC2 IP or `0.0.0.0/0`
- Check username/password in connection string
- Ensure database user has read/write permissions

### Jenkins Email
- Gmail requires app-specific password (not regular password)
- Enable "Less secure app access" or use OAuth
- Check SMTP settings and TLS configuration

### EC2 Memory Issues
- Verify swap memory is active: `swapon --show`
- Monitor with: `free -h`
- Reduce Uvicorn workers if needed

## License

This project is created for educational purposes as part of DevOps Assignment 3.

## Contributors

- **Student Name**: [Your Name]
- **Course**: DevOps
- **Semester**: 7
- **Institution**: Higher Education Commission

## Submission Checklist

- [ ] GitHub repository created with instructor as collaborator
- [ ] Application deployed on EC2 and accessible
- [ ] All 13 Selenium tests passing
- [ ] Jenkins pipeline configured and working
- [ ] GitHub webhook triggering builds
- [ ] Email notifications working
- [ ] Screenshots captured (Jenkins, tests, email)
- [ ] Report document completed
- [ ] Google form submitted with URLs
# Test webhook
# Test webhook
