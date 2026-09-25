# AI Avatar Video Generation Backend

A Spring Boot backend for generating AI-powered promotional videos from a user prompt.

The application generates an AI script, creates scene-wise narration, generates avatar or normal scene videos, processes them using FFmpeg, and merges all scenes into a final video.

## Features

- JWT Authentication
- User and Project Management
- AI Script & Scene Generation
- Scene-wise Voice Generation
- AI Scene Video Generation
- Avatar/Lip-Sync Video Generation
- FFmpeg Audio/Video Processing
- Final Video Merging
- PostgreSQL Database
- Redis
- Docker Support
- REST APIs

## Video Generation Flow
```text
User Prompt
     ↓
AI Script & Scenes
     ↓
Scene Narration
     ↓
 ┌───────────────┬────────────────┐
 ↓               ↓
Normal Video    Avatar Video
(Pixazo)        (Sync Labs)
 └───────────────┴────────────────┘
                 ↓
              FFmpeg
                 ↓
            Final MP4





**Tech Stack**
Java 21
Spring Boot
Spring Security + JWT
Spring Data JPA / Hibernate
PostgreSQL
Redis
Docker
FFmpeg
Maven
Postman


**AI Services**
OpenRouter – AI script and scene generation
Cartesia – Scene narration/voice generation
Pixazo – AI scene video generation
Sync Labs – Avatar lip-sync video generation
FFmpeg – Video and audio processing


**Main APIs**

POST /auth/register
POST /auth/login

POST /api/projects
GET  /api/projects

POST /api/projects/{projectId}/video-generations

GET  /api/projects/{projectId}/video-generations/{videoGenerationId}/scenes

POST /api/projects/{projectId}/video-generations/{videoGenerationId}/scenes/{sceneId}/voice

POST /api/projects/{projectId}/video-generations/{videoGenerationId}/scenes/{sceneId}/video

POST /scenes/{sceneId}/avatar-video

POST /api/final-video/merge/{videoGenerationId}



**Testing**

The APIs can be tested using Postman.
**Basic testing flow:

Register
   ↓
Login → Get JWT Token
   ↓
Create Project
   ↓
Create Video Generation
   ↓
Generate Scenes
   ↓
Generate Scene Audio
   ↓
Generate Scene/Avatar Videos
   ↓
Merge Final Video
Database



****The main entities are:

User
  ↓
Project
  ↓
VideoGeneration
  ↓
VideoScene
Local Setup
Requirements
Java 21
Maven
Docker Desktop
FFmpeg


''''''Run Infrastructure
docker start avatar-postgres
docker start avatar-redis
Run Application
mvn spring-boot:run

Backend:
http://localhost:8081

**Security**
API keys and secrets must be provided through environment variables and must not be committed to GitHub.


''''Future Improvements
Cloud storage
Background video processing
Job queues
Video progress tracking
Cloud deployment
Automatic retries
User video history
