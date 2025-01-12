# Fullstack Java Project

## Ian Notermans (3AONA)
Change the name and Class in the title above

## Folder structure

- Readme.md
- _architecture_: this folder contains documentation regarding the architecture of your system.
- `docker-compose.yml` : to start the backend (starts all microservices)
- _backend-java_: contains microservices written in java
- _demo-artifacts_: contains images, files, etc that are useful for demo purposes.
- _frontend-web_: contains the Angular webclient

Each folder contains its own specific `.gitignore` file.  
**:warning: complete these files asap, so you don't litter your repository with binary build artifacts!**

## How to setup and run this application

   1. Frontend (Angular)

Install Dependencies: Navigate to the Angular project directory and run:

    npm install

Build the Angular Application:

    ng build

Start Frontend and Databases in Docker: From the root directory (where docker-compose.yml is located), run:

    docker-compose up --build

2. Backend (Spring Boot Microservices)

Start the microservices manually in this order:

    Config Service: Ensure it uses the native profile.
    Discovery Service
    Gateway Service
    Messaging Service
    Comment Service
    Review Service
    Post Service
