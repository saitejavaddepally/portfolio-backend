# Postman Collection Guide - Portfolio Backend API

## 📚 Overview

This guide explains how to use the Postman collection for the Portfolio Backend API. The collection includes all endpoints organized in logical folders with pre-configured authentication and environment variables.

## 📥 Installation

### Step 1: Import the Collection
1. Open **Postman**
2. Click **Import** (top-left corner)
3. Select **Upload Files**
4. Choose `Portfolio-Backend.postman_collection.json`
5. Click **Import**

### Step 2: Import the Environment
1. Click the **Environment** dropdown (top-right)
2. Click **Import**
3. Choose `Portfolio-Backend-Environment.postman_environment.json`
4. Click **Import**

### Step 3: Select the Environment
1. Click the **Environment** dropdown (top-right)
2. Select **Portfolio Backend Environment**

## ⚙️ Configuration

### Update Base URL
If your API is running on a different host/port:
1. Click **Environments** (left sidebar)
2. Select **Portfolio Backend Environment**
3. Update `base_url` variable:
   - **Local**: `http://localhost:8080`
   - **Development**: `http://dev.example.com:8080`
   - **Production**: `https://api.example.com`

### Update Test Credentials
1. Go to **Environments** → **Portfolio Backend Environment**
2. Update these variables with your test account credentials:
   - `developer_email`
   - `developer_password`
   - `recruiter_email`
   - `recruiter_password`

## 🔐 Authentication Flow

### How JWT Tokens Work

The collection automatically manages JWT tokens:

1. **Register** (if new user):
   - POST `/auth/register`
   - Receive OTP via email
   - Verify OTP with `/auth/register/verify-otp`

2. **Login**:
   - POST `/auth/login`
   - Returns `accessToken` and `refreshToken`
   - **Test Script automatically saves tokens to environment**

3. **Authenticated Requests**:
   - All protected endpoints use `Authorization: Bearer {{access_token}}`
   - Token is automatically included in the header

4. **Token Refresh**:
   - When access token expires, use `/auth/refresh`
   - Provide `refreshToken` from login response
   - New access token is automatically saved

### Token Lifecycle

```
┌─────────────────────────────────────────────────────┐
│ 1. REGISTER or LOGIN                                │
│    - Credentials sent to server                     │
│    - Server returns accessToken & refreshToken      │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│ 2. MAKE REQUESTS                                    │
│    - Use accessToken in Authorization header        │
│    - Token stored in {{access_token}} variable      │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │ Token expires (typically      │
        │ 15-30 minutes)               │
        └────────┬─────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│ 3. REFRESH TOKEN                                    │
│    - Send refreshToken to /auth/refresh             │
│    - Get new accessToken                            │
│    - New token automatically saved                  │
└─────────────────────────────────────────────────────┘
```

## 📂 Collection Folder Structure

### 1. **Authentication**
Endpoints for user registration, login, and token management.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/register` | POST | Register new user (Developer or Recruiter) |
| `/auth/register/verify-otp` | POST | Verify OTP sent to email |
| `/auth/login` | POST | Login and get JWT tokens |
| `/auth/refresh` | POST | Refresh expired access token |

**Usage Order**:
1. Register → Receive OTP
2. Verify OTP
3. Login (or directly after verification)

### 2. **Portfolio Management**
CRUD operations for user portfolios. All requests require authentication.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/portfolio` | POST | Create or update portfolio |
| `/portfolio` | GET | Retrieve your portfolio |
| `/portfolio/publish` | POST | Publish portfolio and get public URL |

**Sample Workflow**:
1. Login as Developer
2. POST to `/portfolio` with portfolio data
3. GET from `/portfolio` to verify
4. POST to `/portfolio/publish` to make public

### 3. **Public Portfolios**
View published portfolios without authentication.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/public/{slug}` | GET | Get portfolio by public slug |

**Example**:
- Endpoint: `GET /public/john-doe-portfolio`
- No authentication required

### 4. **Recruiter - Professionals**
Browse developer profiles. Requires RECRUITER role.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/recruiter/professionals` | GET | List all professionals |
| `/recruiter/professionals/{id}` | GET | Get specific professional details |

**Usage**:
1. Login as Recruiter
2. GET `/recruiter/professionals` to browse all
3. GET `/recruiter/professionals/{id}` for details

### 5. **Recruiter - Search**
Semantic search to find candidates by skills.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/recruiter/search` | GET | Search candidates by skills/query |

**Query Parameters**:
- `query` (required): Search terms (e.g., "Java Spring Boot", "React JavaScript")

**Examples**:
- `/recruiter/search?query=Java+Spring+Boot`
- `/recruiter/search?query=Frontend+React`
- `/recruiter/search?query=DevOps+Kubernetes`

### 6. **Recruiter - Chat & AI**
AI-powered Q&A about candidates and chat history.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/ai/stream` | GET | Stream AI responses about candidate |
| `/ai/history` | GET | Get chat history between recruiter and candidate |

**Stream Endpoint**:
- Query Params: `candidateEmail`, `question`
- Response: Server-Sent Events (SSE) stream
- Real-time AI responses

**History Endpoint**:
- Query Params: `recruiterEmail`, `candidateEmail`
- Returns: Array of chat messages

## 🔑 Environment Variables

The collection uses these variables (all automatically managed):

| Variable | Type | Purpose |
|----------|------|---------|
| `base_url` | String | API server URL |
| `access_token` | String | JWT access token (auto-set after login) |
| `refresh_token` | String | JWT refresh token (auto-set after login) |
| `user_role` | String | User role - DEVELOPER or RECRUITER |
| `developer_email` | String | Test developer email |
| `developer_password` | String | Test developer password |
| `recruiter_email` | String | Test recruiter email |
| `recruiter_password` | String | Test recruiter password |
| `developer_id` | String | Developer ID for lookups |
| `portfolio_slug` | String | Published portfolio slug |

## 🚀 Common Workflows

### Workflow 1: Register and Create Portfolio (Developer)

```
1. Auth > Register - Developer
   ├─ Receives OTP via email
   
2. Auth > Verify Registration OTP
   ├─ Input OTP from email
   
3. Auth > Login - Developer
   ├─ Tokens automatically saved to environment
   
4. Portfolio Management > Create/Update Portfolio
   ├─ POST portfolio data
   
5. Portfolio Management > Get My Portfolio
   ├─ Verify portfolio saved
   
6. Portfolio Management > Publish Portfolio
   ├─ Get public URL
```

### Workflow 2: Recruiter Search and Chat

```
1. Auth > Login - Recruiter
   ├─ Tokens automatically saved
   
2. Recruiter - Professionals > Get All Professionals
   ├─ Browse all developers
   
3. Recruiter - Search > Search Candidates by Skills
   ├─ Search for specific skills (e.g., "Java Spring Boot")
   
4. Recruiter - Professionals > Get Professional Details
   ├─ Click on specific developer
   
5. Recruiter - Chat & AI > Stream Recruiter Chat
   ├─ Ask AI about candidate's expertise
   
6. Recruiter - Chat & AI > Get Chat History
   ├─ View previous conversations
```

### Workflow 3: View Public Portfolio

```
1. Public Portfolios > Get Public Portfolio by Slug
   ├─ No authentication needed
   ├─ Use portfolio slug from publish step
```

## 🧪 Testing Tips

### 1. Check Response Status
- ✅ **200 OK** - Request successful
- ✅ **201 Created** - Resource created
- ❌ **400 Bad Request** - Invalid request body
- ❌ **401 Unauthorized** - Missing or invalid token
- ❌ **403 Forbidden** - Insufficient permissions (e.g., RECRUITER-only endpoint)
- ❌ **404 Not Found** - Resource not found

### 2. Validate Token Expiry
If you get `401 Unauthorized`:
1. Go to **Auth > Refresh Token**
2. Send request with current `{{refresh_token}}`
3. New access token will be saved automatically

### 3. Role-Based Access
- **DEVELOPER**: Can create/update their own portfolio
- **RECRUITER**: Can browse professionals, search, and chat
- **Public**: Anyone can view published portfolios

### 4. Using Postman Tests
The collection includes test scripts that:
- Automatically save tokens after login
- Display console messages for debugging
- Update environment variables

View test results in the **Test Results** tab after sending a request.

## 🐛 Troubleshooting

### Issue: 401 Unauthorized on Protected Endpoint

**Cause**: Missing or expired access token

**Solution**:
1. Verify `access_token` variable is set
2. Use **Auth > Login** to refresh tokens
3. If still fails, use **Auth > Refresh Token** with `refresh_token`

### Issue: 403 Forbidden (Access Denied)

**Cause**: Wrong role for endpoint

**Solution**:
- Recruiter endpoints require `RECRUITER` role
- Developer endpoints require `DEVELOPER` role
- Check `user_role` variable in environment
- Login with correct account type

### Issue: Test Account Doesn't Exist

**Cause**: First time using Postman

**Solution**:
1. Go to **Auth > Register - Developer**
2. Click **Send**
3. Check email for OTP
4. Use **Auth > Verify Registration OTP** with OTP
5. Then **Auth > Login - Developer**

### Issue: Empty Response or Null

**Cause**: Portfolio not created yet

**Solution**:
1. Login as developer
2. Use **Portfolio Management > Create/Update Portfolio**
3. Ensure all required fields are populated
4. Then use **Get My Portfolio**

### Issue: OTP Not Received

**Check**:
- Email verification service is configured (RESEND_API_KEY)
- Email address is correct
- Check spam/junk folder

## 📊 Response Examples

### Successful Login Response
```json
{
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "DEVELOPER"
}
```

### Portfolio Response
```json
{
  "id": "507f1f77bcf86cd799439011",
  "email": "john@example.com",
  "userId": "507f1f77bcf86cd799439012",
  "published": true,
  "publicSlug": "john-doe-portfolio",
  "data": {
    "fullName": "John Doe",
    "headline": "Senior Full-Stack Developer",
    "skills": ["Java", "Spring Boot", "React"]
  }
}
```

### Search Response
```json
[
  {
    "_id": "507f1f77bcf86cd799439013",
    "email": "dev1@example.com",
    "fullName": "Jane Developer",
    "skills": ["Java", "Spring Boot"],
    "similarity_score": 0.95
  }
]
```

## 📝 Notes

- All timestamps are in UTC/ISO format
- Passwords should be at least 12 characters with special characters
- Portfolio slugs are automatically generated from names
- Embeddings are generated asynchronously - check back after a few seconds
- Chat streaming uses Server-Sent Events (SSE)

## 📞 Support

For issues with:
- **API responses** - Check server logs
- **Token problems** - Verify environment variables
- **Role-based access** - Confirm user role in login response
- **Postman setup** - Check environment is selected

---

**Last Updated**: February 2026
**API Version**: 1.0.0
**Postman Version**: Latest

