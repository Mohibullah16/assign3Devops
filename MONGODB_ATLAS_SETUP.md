# MongoDB Atlas Setup Guide

## Step 1: Create MongoDB Atlas Account

1. Go to https://www.mongodb.com/cloud/atlas/register
2. Sign up with:
   - Email address
   - Or use Google/GitHub account
3. Verify your email if required

## Step 2: Create a Free Cluster

1. After logging in, click **"Build a Database"** or **"Create"**
2. Choose **FREE** tier (M0 Sandbox)
   - 512 MB storage
   - Shared RAM
   - No credit card required
3. Select a provider and region:
   - **Provider:** AWS (recommended for your EC2)
   - **Region:** Choose closest to your EC2 (e.g., us-east-1, eu-west-1)
4. Cluster Name: Leave default or name it `OrderManagement`
5. Click **"Create"**
6. Wait 1-3 minutes for cluster creation

## Step 3: Create Database User

1. Click **"Security"** → **"Database Access"** (left sidebar)
2. Click **"Add New Database User"**
3. Choose **"Password"** authentication
4. Enter:
   - **Username:** `orderuser`
   - **Password:** Click **"Autogenerate Secure Password"** and **COPY IT**
   - Or create your own strong password
5. **Database User Privileges:** Select **"Read and write to any database"**
6. Click **"Add User"**

**⚠️ SAVE THIS PASSWORD - You'll need it for connection strings!**

## Step 4: Configure Network Access

1. Click **"Security"** → **"Network Access"** (left sidebar)
2. Click **"Add IP Address"**
3. Choose one option:
   - **Option A (For Testing):** Click **"Allow Access from Anywhere"**
     - This adds `0.0.0.0/0`
     - ⚠️ Not recommended for production
   - **Option B (More Secure):** Add your EC2 public IP
     - Enter your EC2 instance public IP
     - Description: `EC2 Instance`
4. Click **"Confirm"**

## Step 5: Create Databases

1. Click **"Database"** (left sidebar)
2. Click **"Browse Collections"** on your cluster
3. Click **"Add My Own Data"**
4. Create first database:
   - **Database name:** `order_management_db`
   - **Collection name:** `orders`
   - Click **"Create"**
5. Click **"Create Database"** again (the + button)
6. Create second database:
   - **Database name:** `order_management_test_db`
   - **Collection name:** `orders`
   - Click **"Create"**

## Step 6: Get Connection Strings

1. Click **"Database"** (left sidebar)
2. Click **"Connect"** button on your cluster
3. Click **"Connect your application"**
4. Select:
   - **Driver:** Python
   - **Version:** 3.12 or later
5. Copy the connection string - it looks like:
   ```
   mongodb+srv://orderuser:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```

## Step 7: Create Your Connection Strings

Replace `<password>` with your actual password from Step 3.

**Production Database (MONGO_URI):**
```
mongodb+srv://orderuser:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/order_management_db?retryWrites=true&w=majority
```

**Test Database (TEST_MONGO_URI):**
```
mongodb+srv://orderuser:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/order_management_test_db?retryWrites=true&w=majority
```

**⚠️ Important:**
- Replace `YOUR_PASSWORD` with your actual database user password
- Replace `cluster0.xxxxx.mongodb.net` with your actual cluster hostname
- Note the database name at the end: `order_management_db` vs `order_management_test_db`

## Step 8: Test Connection (Optional)

You can test the connection from your EC2 instance:

```bash
# Install mongosh (MongoDB Shell)
wget -qO - https://www.mongodb.org/static/pgp/server-7.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt update
sudo apt install -y mongodb-mongosh

# Test connection (replace with your connection string)
mongosh "mongodb+srv://orderuser:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/order_management_db"

# If successful, you'll see:
# Current Mongosh Log ID: xxxxx
# Connecting to: mongodb+srv://...
# Using MongoDB: x.x.x
# Using Mongosh: x.x.x

# Type 'exit' to quit
```

## Summary

You should now have:
- ✅ MongoDB Atlas free tier cluster
- ✅ Database user: `orderuser` with password
- ✅ Network access configured (0.0.0.0/0 or EC2 IP)
- ✅ Two databases: `order_management_db` and `order_management_test_db`
- ✅ Two connection strings ready

## Next Steps

1. **Save your connection strings securely**
2. **Get Groq API key** (next step)
3. **Push code to GitHub**
4. **Deploy application on EC2**
5. **Configure Jenkins**

---

## Quick Reference

**MongoDB Atlas Dashboard:** https://cloud.mongodb.com

**Your Connection Strings Format:**
```bash
# Production
MONGO_URI=mongodb+srv://orderuser:PASSWORD@cluster0.xxxxx.mongodb.net/order_management_db?retryWrites=true&w=majority

# Testing
TEST_MONGO_URI=mongodb+srv://orderuser:PASSWORD@cluster0.xxxxx.mongodb.net/order_management_test_db?retryWrites=true&w=majority
```

**Need to find your cluster hostname?**
- MongoDB Atlas → Database → Connect → Connect your application
- Copy the full string and look for the `@cluster0.xxxxx.mongodb.net` part
