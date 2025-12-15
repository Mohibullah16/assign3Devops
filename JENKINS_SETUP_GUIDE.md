# How to Create Your Jenkins Pipeline

You currently have a **Jenkinsfile** in your repository. This file is the *blueprint* for your pipeline. To actually *run* it, you need to create a "Pipeline" job on your Jenkins Server (AWS EC2).

## 1. Prerequisites (On Jenkins Server)
- **Jenkins Installed**: Ensure Jenkins is running on your AWS EC2 instance.
- **Docker Installed**: Jenkins needs permission to run Docker commands (`sudo usermod -aG docker jenkins`).
- **Plugins**: Ensure the "Docker Pipeline" and "Git" plugins are installed.

## 2. Configure Credentials
Your pipeline requires secrets to run the application.
1. Go to **Dashboard > Manage Jenkins > Credentials > System > Global credentials**.
2. Click **+ Add Credentials**.
3. Add the following **Secret Text** credentials:
   - **Kind**: Secret Text
   - **ID**: `mongo-uri`
   - **Secret**: Your MongoDB Connection String (e.g., `mongodb+srv://...`)
   
   - **ID**: `groq-api-key`
   - **Secret**: Your Groq API Key
   
   - **ID**: `test-mongo-uri`
   - **Secret**: Your Test Database Connection String

## 3. Create the Pipeline Job
1. Go to **Dashboard > New Item**.
2. Enter a name (e.g., `OrderManager-Pipeline`).
3. Select **Pipeline** and click **OK**.
4. Scroll to the **Pipeline** section.
   - **Definition**: Select `Pipeline script from SCM`.
   - **SCM**: Select `Git`.
   - **Repository URL**: `https://github.com/Mohibullah16/assign3Devops.git`
   - **Branch Specifier**: `*/main`
   - **Script Path**: `Jenkinsfile` (Default)
5. Click **Save**.

## 4. Run the Pipeline
1. Click **Build Now** on the left menu.
2. Click the Build Number (e.g., `#1`) under "Build History".
3. Click **Console Output** to watch the progress.
4. If successful, you will see `Finished: SUCCESS`.

## 5. Troubleshooting
- **Permission Denied**: If Docker fails, restart Jenkins after adding it to the docker group (`sudo systemctl restart jenkins`).
- **App Startup Failed**: Check the "Start Application" stage logs.

## 6. Configure Email Notifications (SMTP)
To allow Jenkins to send emails to the collaborator who pushed the code, you must configure the SMTP server.

1. Go to **Manage Jenkins** > **System**.
2. Scroll down to **Extended E-mail Notification**.
   - **SMTP Server**: `smtp.gmail.com` (for Gmail)
   - **Default User E-mail Suffix**: `@gmail.com`
   - **Use SMTP Authentication**: Checked.
     - **User Name**: Your Gmail address.
     - **Password**: Your **App Password** (Not your login password. Generate one in Google Account > Security > 2-Step Verification > App passwords).
   - **Use SSL**: Checked.
   - **SMTP Port**: `465`.
3. Scroll down to **E-mail Notification** (standard plugin) and configure it similarly as a fallback.
4. Click **Save**.

**Note regarding "Collaborator"**:
- Jenkins identifies the "collaborator" based on the Git commit email.
- Ensure your local git config (`git config user.email`) matches the email you want to receive notifications on.
- The pipeline uses `RequesterRecipientProvider` and `DevelopersRecipientProvider` to find the user who triggered the build or committed the code.
