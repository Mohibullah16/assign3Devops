# Order Manager - Quick Start Guide

## For Local Development

### 1. Setup Environment

```bash
# Install Python 3.11 and create virtual environment
python3.11 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Install dependencies
pip install -r order_manager/requirements.txt

# Create .env file
cp order_manager/.env.example order_manager/.env
# Edit .env with your credentials
```

### 2. Get Required API Keys

**MongoDB Atlas:**
1. Visit https://www.mongodb.com/cloud/atlas
2. Create free cluster
3. Create database user
4. Whitelist IP: 0.0.0.0/0
5. Get connection string

**Groq API:**
1. Visit https://console.groq.com
2. Create account
3. Generate API key
4. Copy key (starts with gsk_)

### 3. Run Application

```bash
cd order_manager
uvicorn app:app --reload
```

Visit: http://localhost:8000

### 4. Run Tests

```bash
# Install test dependencies
pip install -r tests/requirements.txt

# Set environment
export APP_URL=http://localhost:8000
export TEST_MONGO_URI=<your-test-db-uri>
export TEST_MODE=true

# Run tests
pytest tests/ -v
```

## For EC2 Deployment

### Quick Commands

```bash
# SSH to EC2
ssh -i ass3.pem ubuntu@<ec2-ip>

# Setup swap (5GB)
sudo dd if=/dev/zero of=/swapfile bs=1M count=5120
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Install everything
sudo apt update
sudo apt install -y python3.11 python3.11-venv python3-pip docker.io openjdk-11-jdk

# Clone and setup app
git clone <your-repo> Assignment3
cd Assignment3/order_manager
python3.11 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Create .env and start
nano .env  # Add your keys
uvicorn app:app --host 0.0.0.0 --port 8000
```

See `EC2_SETUP_GUIDE.md` for complete instructions.

## Jenkins Pipeline

### Prerequisites
1. Jenkins installed on EC2
2. Docker plugin installed
3. GitHub webhook configured
4. Gmail SMTP configured

### Trigger Build
```bash
git add .
git commit -m "Your changes"
git push origin main
# Jenkins automatically triggers
```

## Project URLs

- **Application:** http://<ec2-ip>:8000
- **Jenkins:** http://<ec2-ip>:8080
- **GitHub:** <your-repo-url>

## Common Issues

**MongoDB Connection Failed:**
- Check IP whitelist in Atlas
- Verify connection string format
- Ensure database user has permissions

**OCR Not Working:**
- Verify Groq API key is correct
- Check image format (JPG/PNG)
- Review API usage limits

**Tests Failing:**
- Ensure app is running on port 8000
- Check TEST_MONGO_URI is set
- Verify Chrome/ChromeDriver versions match

**Jenkins Build Fails:**
- Check Docker is running: `sudo systemctl status docker`
- Verify jenkins user in docker group: `groups jenkins`
- Review build console output

## Support

For detailed documentation:
- `README.md` - Complete project documentation
- `EC2_SETUP_GUIDE.md` - EC2 deployment steps
- Assignment instructions PDF

## Assignment Submission

**Before submitting:**
- [ ] App deployed and accessible on EC2
- [ ] All 13 tests passing
- [ ] Jenkins pipeline working
- [ ] Email notifications configured
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
