# Spring Boot on AWS Elastic Beanstalk

A Spring Boot web application deployed on AWS Elastic Beanstalk with DynamoDB integration and automated CI/CD via GitHub Actions.

---

## Application Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | App info, version, deployment confirmation |
| `/health` | GET | Health check (used by Elastic Beanstalk) |
| `/data` | GET | Read visitor records from DynamoDB |
| `/data?id=yourname` | POST | Write a visitor record to DynamoDB |

---

## Architecture

```
GitHub Push
    ↓
GitHub Actions
    ↓ mvn package → JAR
    ↓ upload JAR → S3
    ↓ create EB version
    ↓ deploy to environment
Elastic Beanstalk (public URL)
    ↓
DynamoDB (visitor records)
```

---

## AWS Setup (One-Time)

### 1. Create S3 Bucket
```bash
aws s3 mb s3://your-eb-deployments-bucket --region us-east-1
```

### 2. Create DynamoDB Table
```bash
aws dynamodb create-table \
  --table-name eb-app-visitors \
  --attribute-definitions AttributeName=visitorId,AttributeType=S \
  --key-schema AttributeName=visitorId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

### 3. Create IAM User for GitHub Actions
Create an IAM user with these policies:
- `AWSElasticBeanstalkFullAccess`
- `AmazonS3FullAccess`
- `AmazonDynamoDBFullAccess`

Generate access keys for this user.

### 4. Create Elastic Beanstalk Application
- Platform: **Java 17 running on 64bit Amazon Linux 2023**
- Upload the first JAR manually from `target/spring-eb-app.jar`

### 5. Set Elastic Beanstalk Environment Variables
In the EB Console → Configuration → Software → Environment properties:

| Key | Value |
|-----|-------|
| `AWS_REGION` | `us-east-1` |
| `DYNAMODB_TABLE` | `eb-app-visitors` |

### 6. Add GitHub Secrets
In your GitHub repo → Settings → Secrets → Actions:

| Secret | Value |
|--------|-------|
| `AWS_ACCESS_KEY_ID` | From IAM user |
| `AWS_SECRET_ACCESS_KEY` | From IAM user |
| `AWS_REGION` | `us-east-1` |
| `S3_BUCKET` | `your-eb-deployments-bucket` |
| `EB_APP_NAME` | Your EB application name |
| `EB_ENV_NAME` | Your EB environment name |

---

## Local Build

```bash
# Build the JAR
mvn package -DskipTests

# The JAR will be at:
target/spring-eb-app.jar
```

---

## Triggering a Deployment

Any push to `main` automatically deploys:
```bash
git add .
git commit -m "Update version"
git push origin main
```

Watch the deployment in **GitHub → Actions tab**.

---

## Tech Stack

- **Java 17** (Amazon Corretto)
- **Spring Boot 3.2**
- **Maven**
- **AWS Elastic Beanstalk**
- **Amazon DynamoDB**
- **Amazon S3**
- **GitHub Actions**
