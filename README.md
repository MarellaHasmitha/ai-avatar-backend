
# AI Avatar Video Generation Backend

A Java Spring Boot REST API backend for generating AI-powered promotional videos from user prompts.

The application integrates multiple AI services to generate scripts, create scene-wise narration, produce AI-generated videos or avatar lip-sync videos, and combine individual scenes into a final video using FFmpeg.

It also provides JWT-based authentication, user and project management, and PostgreSQL data persistence.

## Features

- **Authentication:** User registration, login and JWT-based authorization.
- **Project Management:** Create, retrieve, update and delete projects.
- **AI Script Generation:** Generate scripts and scene-wise content from user prompts.
- **Voice Generation:** Generate narration for individual video scenes.
- **AI Video Generation:** Generate videos for individual scenes.
- **Avatar Integration:** Generate avatar-based lip-sync videos.
- **Video Processing:** Process and merge audio and video using FFmpeg.
- **Database:** Persist application data using PostgreSQL.
- **API Documentation:** Interactive API documentation using Swagger / OpenAPI.

## Video Generation Workflow

```text
             User Prompt
                  |
                  v
          AI Script Generation
                  |
                  v
          Scene-wise Generation
                  |
                  v
           Voice Generation
                  |
                  v
          Scene Audio Creation
                  |
                  v
          Video Generation
                  |
          +-------+-------+
          |               |
          v               v
      AI Scene Video   Avatar Video
          |            (Lip-Sync)
          |               |
          +-------+-------+
                  |
                  v
           FFmpeg Processing
                  |
                  v
           Final MP4 Video
```

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Backend Framework | Spring Boot |
| Security | Spring Security, JWT |
| ORM | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Cache / Infrastructure | Redis |
| Build Tool | Maven |
| Containerization | Docker |
| API Documentation | Swagger / OpenAPI |
| API Testing | Postman |
| Media Processing | FFmpeg |

## AI Service Integrations

The application integrates the following third-party services:

| Service | Purpose |
|---|---|
| OpenRouter | AI script and scene generation |
| Cartesia | Text-to-speech and voice generation |
| Pixazo | AI scene video generation |
| Sync Labs | Avatar lip-sync video generation |
| FFmpeg | Audio/video processing and merging |

### API Usage and Limitations

This project uses **free-tier APIs and services** for its AI integrations.

- OpenRouter is used for AI-powered script generation.
- Cartesia is used for voice generation.
- Pixazo is used for AI video generation.
- Sync Labs is used for avatar lip-sync generation.

These services have usage quotas and rate limits depending on their respective free-tier plans.

The AI integrations were previously tested successfully. However, some generation features may temporarily be unavailable when the free API quotas are exhausted.

The core backend features, including authentication, JWT authorization and project management, can be tested independently of these external API limits.

## API Documentation

The backend exposes REST APIs for authentication, project management and video generation.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Register a new user |
| POST | `/api/auth/login` | Authenticate and obtain JWT |

### Project Management

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/projects` | Create a project |
| GET | `/api/projects` | Retrieve projects |
| GET | `/api/projects/{projectId}` | Retrieve project details |
| PUT | `/api/projects/{projectId}` | Update a project |
| DELETE | `/api/projects/{projectId}` | Delete a project |

### Video Generation

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/projects/{projectId}/video-generations` | Create a video generation |
| GET | `/api/projects/{projectId}/video-generations/{videoGenerationId}/scenes` | Retrieve video scenes |
| POST | `/api/projects/{projectId}/video-generations/{videoGenerationId}/scenes/{sceneId}/voice` | Generate scene narration |
| POST | `/api/projects/{projectId}/video-generations/{videoGenerationId}/scenes/{sceneId}/video` | Generate scene video |
| POST | `/scenes/{sceneId}/avatar-video` | Generate avatar lip-sync video |
| POST | `/api/final-video/merge/{videoGenerationId}` | Merge scenes into final video |

**Authentication:** Protected endpoints require a valid JWT Bearer token.

```http
Authorization: Bearer <your-jwt-token>
```

For complete request parameters, response formats and endpoint testing, refer to the Swagger documentation.

## Database Structure

The main entities represent the application's user and video generation workflow.

```text
User
 |
 +---- Project
          |
          +---- VideoGeneration
                    |
                    +---- VideoScene
```

PostgreSQL is used for persistent data storage.

## Running Locally

### Prerequisites

- Java 21
- Maven
- Docker Desktop
- PostgreSQL
- Redis
- FFmpeg

### 1. Clone the Repository

```bash
git clone https://github.com/MarellaHasmitha/ai-avatar-backend/
cd <YOUR_PROJECT_FOLDER>
```

### 2. Configure Environment Variables

Configure the required credentials and API keys through environment variables.

The configuration includes:

- PostgreSQL connection details
- Redis connection details
- JWT secret
- OpenRouter API key
- Cartesia API key
- Pixazo API key
- Sync Labs API key

Use the environment variable names defined in the application configuration.

**Never commit API keys, passwords or other sensitive credentials to GitHub.**

### 3. Start PostgreSQL and Redis

If the Docker containers have already been created:

```bash
docker start avatar-postgres
docker start avatar-redis
```

Verify that the containers are running:

```bash
docker ps
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Alternatively, build the application:

```bash
mvn clean package
```

### 5. Access the Application

| Resource | URL |
|---|---|
| Backend | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |

## Testing

The APIs can be tested using Swagger UI or Postman.

Recommended testing sequence:

1. Register a user.
2. Log in and obtain a JWT token.
3. Authorize protected endpoints using the token.
4. Create and manage projects.
5. Create a video generation.
6. Generate scenes and narration.
7. Generate scene or avatar videos.
8. Merge the generated scenes into a final video.

Core authentication, protected endpoints and project CRUD operations have been tested in the deployed environment.

AI generation workflows were also tested previously, subject to third-party API availability and quotas.

## Deployment

The backend is deployed on Vercel.

- **Backend:** https://ai-avatar-backend-eta.vercel.app/
- **Swagger UI:** https://ai-avatar-backend-eta.vercel.app/swagger-ui/index.html
- **OpenAPI Specification:** https://ai-avatar-backend-eta.vercel.app/v3/api-docs

## Security

- Passwords are hashed using BCrypt.
- JWT authentication protects private endpoints.
- Stateless authentication is implemented using Spring Security.
- Sensitive credentials are managed through environment variables.

## Future Enhancements

- Cloud storage for generated videos
- Asynchronous video processing
- Background job queues
- Video generation progress tracking
- Automatic retry mechanisms
- Video history and download management

---

**Author:** Hasmitha Marella

**Technologies:** Java | Spring Boot | PostgreSQL | JWT | REST APIs | AI Integrations
