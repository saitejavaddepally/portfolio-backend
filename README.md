# Portfolio Backend

A Spring Boot-based backend service for a portfolio builder platform that leverages AI to generate intelligent summaries and embeddings of user portfolios. This application facilitates portfolio management for developers and enables recruiters to discover talent through semantic search.

## 🎯 Project Overview

**Portfolio Backend** is a RESTful API built with Spring Boot that provides:

- **User Authentication & Authorization**: JWT-based authentication with role-based access control (Developer/Recruiter)
- **Portfolio Management**: Create, read, update, and delete portfolio information
- **AI-Powered Summaries**: Automatically generate professional summaries from portfolio data using OpenAI
- **Semantic Search**: Use vector embeddings to find similar portfolios based on skills and experience
- **Recruiter Tools**: Specialized endpoints for recruiters to discover and interact with developer portfolios
- **Real-time Chat**: Communication between recruiters and developers

## 🏗️ Architecture

### Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 21
- **Database**: MongoDB (NoSQL)
- **AI/ML**: 
  - Spring AI with OpenAI API (via OpenRouter)
  - Text embeddings for semantic search
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (for code generation)
  - Jackson (for JSON serialization)

### Project Structure

```
src/main/java/com/saiteja/portfolio_backend/
├── PortfolioBackendApplication.java      # Main Spring Boot application
├── TestController.java                    # Test endpoints
├── config/                                # Configuration classes
│   ├── AppConfig.java                    # General app configuration
│   └── SecurityConfig.java               # Spring Security setup
├── controller/                            # REST API endpoints
│   ├── PortfolioController.java          # Portfolio CRUD operations
│   ├── PublicPortfolioController.java    # Public portfolio endpoints
│   ├── AIController.java                 # AI-related endpoints
│   ├── RecruiterController.java          # Recruiter endpoints
│   ├── RecruiterChatController.java      # Chat functionality
│   ├── auth/
│   │   └── AuthController.java           # Authentication endpoints
│   └── recruiter/
│       └── RecruiterAuthController.java  # Recruiter auth endpoints
├── service/                               # Business logic
│   ├── AISummaryService.java             # AI summary generation
│   ├── AIService.java                    # General AI operations
│   ├── PortfolioService.java             # Portfolio operations
│   └── auth/recruiter/                   # Auth and recruiter services
├── model/                                 # MongoDB documents
│   ├── User.java                         # User entity
│   ├── Portfolio.java                    # Portfolio entity
│   ├── AISummary.java                    # AI summary entity
│   ├── ChatMessage.java                  # Chat message entity
│   └── PendingRegistration.java          # Pending registration entity
├── repository/                            # MongoDB repositories
│   ├── AISummaryRepository.java          # AI summary queries
│   └── ...
├── dto/                                   # Data Transfer Objects
│   ├── AuthResponse.java                 # Authentication response
│   ├── LoginRequest.java                 # Login credentials
│   ├── RegisterRequest.java              # Registration data
│   └── ...
├── exceptions/                            # Custom exceptions
└── security/                              # Security filters & utilities
    └── JwtTokenProvider.java             # JWT token handling
```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- MongoDB (local or cloud instance)
- OpenRouter API Key (for AI features)

### Environment Variables

Create a `.env` file or set these environment variables:

```bash
MONGO_URI=mongodb://localhost:27017/portfolio-dev
JWT_SECRET=your-secret-key-here
PORT=8080
RESEND_API_KEY=your-resend-api-key
OPENROUTER_API_KEY=your-openrouter-api-key
```

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd portfolio-backend\ 2
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or using Java:
   ```bash
   java -jar target/portfolio-backend-0.0.1-SNAPSHOT.jar
   ```

The application will start on `http://localhost:8080`

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh-token` - Refresh JWT token

### Portfolio Management
- `GET /api/portfolio` - Get user's portfolio
- `POST /api/portfolio` - Create portfolio
- `PUT /api/portfolio` - Update portfolio
- `DELETE /api/portfolio` - Delete portfolio
- `GET /api/portfolio/public/{portfolioId}` - Get public portfolio view

### AI Features
- `POST /api/ai/summary` - Generate AI summary for portfolio
- `POST /api/ai/chat` - Chat with AI about portfolio

### Recruiter Features
- `GET /api/recruiter/portfolios` - Search portfolios
- `GET /api/recruiter/portfolios/{id}` - View portfolio details
- `POST /api/recruiter/chat/{developerId}` - Send message to developer

## 🤖 Understanding Embeddings

### What Are Embeddings?

Embeddings are numerical representations of text that capture semantic meaning. They convert words, sentences, or documents into vectors of floating-point numbers that can be compared mathematically.

### How Embeddings Work in This Project

#### 1. **Embedding Generation Process**

When a portfolio summary is generated via the `AISummaryService`, the following happens:

```
Portfolio Data → AI Processing → Structured Summary → Embedding Text → Vector Embedding
```

#### 2. **Step-by-Step Breakdown**

**Step 1: Extract Relevant Information**
```
User's Portfolio Data
├── Professional Summary
├── Years of Experience
├── Core Skills
├── Work Experience
├── Projects
└── Education
```

**Step 2: Build Embedding Text**
The `buildEmbeddingText()` method in `AISummaryService` constructs a meaningful text representation:

```java
String embeddingText = """
Candidate Overview:
<professional summary>

Years of Experience: <years>

Core Skills: <skill1, skill2, skill3, ...>
""";
```

**Step 3: Convert to Numerical Vector**
The `generateEmbedding()` method uses the OpenAI text-embedding-3-small model:

```
Input Text: "Senior Java Developer with 5 years of experience in Spring Boot..."
       ↓
OpenAI Embedding Model (text-embedding-3-small)
       ↓
Vector Output: [0.0234, -0.156, 0.892, ..., 0.045]  (1536 dimensions)
```

#### 3. **Vector Dimensions**

- **Model**: `text-embedding-3-small`
- **Vector Size**: 1536 dimensions
- **Use Case**: Semantic similarity search

#### 4. **Why Are Embeddings Useful?**

| Use Case | Benefit |
|----------|---------|
| **Semantic Search** | Find similar portfolios based on skills/experience, not exact keyword matching |
| **Recruiter Matching** | Match job requirements with candidate profiles using vector similarity |
| **Skill Discovery** | Identify candidates with complementary or similar skill sets |
| **Portfolio Clustering** | Group portfolios by domain (Frontend, Backend, Full-stack, etc.) |

#### 5. **Example: Semantic Similarity**

```
Portfolio A: "Full-stack developer with React and Node.js expertise"
Embedding A: [0.023, -0.156, 0.892, ...]

Portfolio B: "Frontend specialist with JavaScript and React skills"
Embedding B: [0.021, -0.145, 0.901, ...]

Cosine Similarity: 0.95 (Very similar - both are JavaScript/React focused)
```

Even though Portfolio A and B use different wording, their embeddings are similar because they represent similar professional profiles.

#### 6. **Data Storage**

Embeddings are stored in MongoDB within the `AISummary` document:

```java
@Document(collection = "ai_summaries")
public class AISummary {
    private String id;
    private String userEmail;
    private String userId;
    private String model;
    private Map<String, Object> structuredSummary;  // Structured JSON from AI
    private String embeddingText;                    // Original text
    private List<Double> embedding;                  // Vector (1536 doubles)
    private Instant createdAt;
    private Instant updatedAt;
}
```

#### 7. **Asynchronous Processing**

The `@Async` annotation ensures embedding generation doesn't block the main request:

```java
@Async
public void generateAndSaveSummary(String userEmail, String userId, 
                                   Map<String, Object> portfolioData)
```

This allows:
- ✅ User receives immediate response
- ✅ AI processing happens in background
- ✅ Summary and embeddings are calculated asynchronously
- ✅ Better application performance

### 8. **Flow Diagram**

```
User Portfolio Saved
         ↓
generateAndSaveSummary() triggered
         ↓
    [ASYNC THREAD]
         ↓
Portfolio JSON serialized
         ↓
Send to OpenAI Chat Model
(with strict prompt for structured JSON)
         ↓
Parse structured response
(Professional Summary, Skills, Experience, etc.)
         ↓
Build embedding text from summary
         ↓
Send embedding text to OpenAI
(text-embedding-3-small model)
         ↓
Receive 1536-dimensional vector
         ↓
Store in MongoDB:
- Structured summary
- Embedding text
- Vector embedding
         ↓
Completed (User notified or via polling)
```

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Different permissions for Developers and Recruiters
- **Password Hashing**: Secure password storage
- **Email Verification**: OTP-based email verification (via Resend API)

## 📦 Key Dependencies

```xml
<!-- Spring Boot Core -->
<spring-boot-starter-web>
<spring-boot-starter-data-mongodb>
<spring-boot-starter-security>

<!-- AI & ML -->
<spring-ai-starter-model-openai>

<!-- JWT -->
<jjwt-api>
<jjwt-impl>
<jjwt-jackson>

<!-- Utilities -->
<lombok>

<!-- WebFlux for async operations -->
<spring-boot-starter-webflux>
```

## 🧪 Testing

Run tests with:

```bash
mvn test
```

Test files are located in `src/test/java/`

## 📝 Configuration

Key properties in `application.properties`:

```properties
# Database
spring.data.mongodb.uri=mongodb://localhost:27017/portfolio-dev

# JWT
jwt.secret=your-secret-key

# AI/OpenAI
spring.ai.openai.base-url=https://openrouter.ai/api
spring.ai.openai.chat.options.model=stepfun/step-3.5-flash:free
spring.ai.openai.embedding.options.model=text-embedding-3-small

# Server
server.port=8080
```

## 🔄 Asynchronous Processing

The application uses `@EnableAsync` to support non-blocking operations:

- AI summary generation runs asynchronously
- Long-running embeddings don't block API responses
- Improved user experience with faster API responses

## 🤝 Contributing

Contributions are welcome! Please ensure:

- Code follows Spring Boot best practices
- All tests pass
- New features include tests
- Documentation is updated

## 📄 License

This project is part of a portfolio platform. All rights reserved.

## 📧 Support

For issues or questions, please contact the development team.

---

**Last Updated**: February 2026

