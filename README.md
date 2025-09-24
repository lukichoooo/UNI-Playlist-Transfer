# UNI Playlist Transfer

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.2.1-blue)
![License](https://img.shields.io/badge/license-SpringBoot_3.5.4-orange)

A full-stack web application for seamless playlist migration between multiple streaming platforms.  
This project provides a fast, secure, and reliable way to transfer your music playlists, ensuring your favorite tunes follow you wherever you go.

---

## 🚀 Live Demo
Experience the live application here: [UNI Playlist Transfer on Vercel](https://uni-playlist-transfer.vercel.app/)

---

## ✨ Key Features
- **Optimized Music-Matching Algorithm**: Uses fuzzy matching, ISRC codes, and duration comparisons for high transfer accuracy.  
- **Secure Authentication**: OAuth2 with encrypted token storage for maximum privacy.  
- **Scalable Backend Architecture**: Spring Boot, PostgreSQL, and Redis caching for efficient large-scale transfers.  
- **Containerized for Consistency**: Core services packaged with Docker for smooth development and deployment.  
- **Responsive Cross-Platform UI**: React + TypeScript frontend works seamlessly across devices.  
- **Thoroughly Tested**: Backend covered with JUnit and Mockito tests for reliability.  

---

## 🎥 App Preview
Main transfer process demo:  
![Playlist Transfer Demo 1](./assets/uni-gif-1.gif)

Additional preview (UI/feature demo):  
![Playlist Transfer Demo 2](./assets/uni-gif-2.gif)

---

## 🎶 Supported Streaming Platforms
- Spotify  
- YouTube  
- YouTube Music  
- SoundCloud  
- Deezer  
- Apple Music  

---

## 🛠️ Technologies Used

### Frontend
- **Framework**: React + TypeScript  
- **Build Tool**: Vite  
- **Routing**: React Router  
- **HTTP Client**: Axios  
- **Fuzzy Search**: Fuse.js  

### Backend
- **Framework**: Spring Boot  
- **Language**: Java 21  
- **Database**: PostgreSQL  
- **Caching**: Redis  
- **Authentication**: JWT (jjwt)  
- **Containerization**: Docker  

---

## 📦 Getting Started

### Prerequisites
- Java 21 (JVM)  
- Node.js & npm  
- PostgreSQL & Redis instances running  
- Configured `application.properties` file for backend  
- Configured `.env` file for frontend  

---

### Installation & Setup

#### Clone the repository
```bash
git clone https://github.com/lukichoooo/uni-playlist-transfer.git
```


Build and run the backend:
```bash
cd Backend/PlaylistConverter
mvn clean install
```

Install and run the frontend:
```bash
cd ../../Frontend/PlaylistConverter
npm install
npm run dev
```

Start the backend Spring Boot application from your IDE or command line.
```bash
cd Backend/PlaylistConverter
mvn clean install
```

### Contact

Email: khundadzeluka702@gmail.com

GitHub: lukichoooo
