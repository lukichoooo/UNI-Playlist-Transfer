# UNI Playlist Transfer

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.1.0-blue)
![License](https://img.shields.io/badge/license-SpringBoot_3.5.4-orange)

A full-stack web application designed for seamless playlist migration between multiple streaming platforms. This project provides a fast, secure, and reliable way to transfer your music playlists between different streaming services, ensuring your favorite tunes follow you wherever you go.

---

## **Key Features**

- **Optimized Music-Matching Algorithm:** The backend features a custom algorithm designed to improve playlist transfer success rates while reducing processing time. It uses fuzzy matching, ISRC codes, and duration comparisons to find the best match for each track.

- **Secure Authentication:** The system uses OAuth2 for secure, token-based authentication with streaming platforms. All sensitive tokens are encrypted before being stored, enhancing user privacy.

- **Scalable Backend Architecture:** The backend is built with Spring Boot, PostgreSQL, and Redis caching, engineered to handle large-scale playlist transfers efficiently and reliably.

- **Containerized Development:** Docker is used to containerize core services like Redis, streamlining the development environment setup and deployment process for a consistent experience.

- **Cross-Platform UI:** The frontend, developed with React and TypeScript, provides a consistent and responsive user experience on both web and mobile devices.

- **Unit Tested:** The backend includes comprehensive JUnit and Mockito tests to ensure the reliability and security of the system.

---

## **Supported Streaming Platforms**

The project supports transferring playlists between the following platforms:

- Spotify
- YouTube
- YouTube Music
- SoundCloud
- Deezer
- Apple Music

---

## **Built With**

### Frontend
- **React:** A JavaScript library for building user interfaces.
- **TypeScript:** A typed superset of JavaScript that compiles to plain JavaScript.
- **Vite:** A build tool that aims to provide a faster and leaner development experience for modern web projects.
- **React Router:** For declarative routing in a React application.
- **Axios:** A promise-based HTTP client for the browser and node.js.
- **Fuse.js:** A powerful, lightweight fuzzy-search library for JavaScript.

### Backend
- **Java:** The core programming language, version 21.
- **Spring Boot:** A framework to simplify the bootstrapping and development of new Spring applications.
- **PostgreSQL:** A powerful, open-source relational database.
- **Redis:** An in-memory data structure store used for caching and state management.
- **JWT:** The backend uses jjwt for JSON Web Token creation and validation.
- **Docker:** For containerization of the application and its dependencies.

---

## **Getting Started**

To get a copy of the project up and running on your local machine for development and testing purposes, follow these steps.

### **Prerequisites**
- Java 21 (JVM)
- Node.js & npm
- PostgreSQL & Redis
- Backend `application-properties` file configured
- Frontend `.env` file configured

---

## **Contact**

- **Email:** khundadzeluka702@gmail.com  
- **GitHub:** [lukichoooo](https://github.com/lukichoooo)

