# CIC-MOB-APP

A futuristic cybersecurity community platform built as a native Android application for the Cyber Innovators Club (CIC).

CIC-MOB-APP combines:

- technical community management
- cybersecurity events
- CTF activities
- resource sharing
- announcements
- progression systems
- administrative operations

into a single immersive mobile ecosystem.

The application is designed with a cyber-operating-system aesthetic inspired by:

- Hack The Box
- Discord
- GitHub Dark
- Raycast
- VS Code
- Cyberpunk tactical interfaces

---

# 📋 Table of Contents

- [Overview](#-overview)
- [Project Status](#-project-status)
- [Core Features](#-core-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [UI & UX System](#-ui--ux-system)
- [Security Features](#-security-features)
- [Offline-First Architecture](#-offline-first-architecture)
- [Backend API](#-backend-api)
- [Core Data Models](#-core-data-models)
- [Project Structure](#-project-structure)
- [Installation](#-installation)
- [Development](#-development)
- [Build & Deployment](#-build--deployment)
- [Environment Variables](#-environment-variables)
- [Screenshots](#-screenshots)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [Troubleshooting](#-troubleshooting)
- [License](#-license)
- [Contact](#-contact)

---

# 🎯 Overview

CIC-MOB-APP is the official mobile platform of the Cyber Innovators Club (CIC).

The platform acts as a centralized operational hub for students, cybersecurity enthusiasts, developers, mentors, and administrators.

The application provides:

- event discovery and registration
- cybersecurity resource management
- announcements and updates
- technical community interaction
- profile progression systems
- administrator operational tools
- immersive cyber-themed interfaces

The goal is to transform a traditional student application into:

> a futuristic digital workspace for builders, hackers, and innovators.

---

# 📊 Project Status

| Component | Status |
|---|---|
| Android Application | Active Development |
| Backend API | Active Development |
| Authentication System | Implemented |
| Event Management | Implemented |
| Resource System | Implemented |
| Announcements System | Implemented |
| Admin Panel | Implemented |
| Offline Caching | Implemented |
| Discord OAuth2 | Implemented |
| Real-Time Features | In Progress |
| Push Notifications | Planned |

---

# ✨ Core Features

# Authentication & Session Management

- Email/password authentication
- User registration system
- Discord OAuth2 integration
- Secure JWT session handling
- Persistent auto-login
- Encrypted token storage
- Deep-link OAuth callback support

---

# Home Dashboard

- Welcome header with XP and level
- Live event carousel
- Announcements feed
- Quick access actions
- Animated cyber grid background
- Real-time operational feel

---

# Events System

- Event discovery interface
- Event detail pages
- Event registration/unregistration
- Difficulty indicators
- Seats remaining tracking
- Pull-to-refresh synchronization
- Banner image rendering

---

# Resources Hub

- Resource browsing system
- Full-text search
- Resource categorization
- Difficulty metadata
- Context-aware icons
- Documentation/video/lab support

---

# Announcements System

- Categorized announcements
- Priority indicators
- Pinned announcements
- Feed previews
- Administrative publishing system

---

# Profile System

- Dynamic avatars
- XP progression
- User levels
- Role system
- Administrator access control
- Cyber-themed profile presentation

---

# Administrator Panel

## Dashboard

- Live platform statistics
- Platform health monitoring
- Operational shortcuts
- Refresh synchronization

## Member Management

- User search/filtering
- Role management
- Member inspection
- Pull-to-refresh support

## Event Management

- Create/edit/delete events
- Event metadata management
- Capacity tracking
- Administrative controls

## Resource Management

- Resource publishing
- Resource editing
- Resource deletion
- Metadata management

## Announcement Management

- Create/edit/delete announcements
- Pinned state management
- Priority handling
- Administrative broadcasting

---

# Living UI System

- Persistent animated cyber grid
- Signal pulse rendering
- Ambient particle systems
- Dark-mode-first design
- Cyan/purple neon visual identity
- Motion-driven interactions
- Tactical operational atmosphere

---

# 🛠 Tech Stack

# Android Application

| Technology | Usage |
|---|---|
| Kotlin | Primary language |
| XML Layouts | UI rendering |
| ViewBinding | View binding system |
| MVVM | Architecture pattern |
| LiveData | Reactive UI updates |
| Navigation Component | Screen navigation |
| Room Database | Offline caching |
| Retrofit | REST API communication |
| OkHttp | Networking + interceptors |
| Glide | Image loading |
| Coroutines | Async operations |

---

# Backend

| Technology | Usage |
|---|---|
| Node.js | Backend runtime |
| Express.js | REST API server |
| JWT | Authentication |
| JSON Persistence | Mock database |
| Discord OAuth2 | External authentication |

---

# Development Tools

| Tool | Usage |
|---|---|
| Android Studio | Main IDE |
| Gradle | Build system |
| Git | Version control |
| Postman | API testing |
| Figma | UI/UX prototyping |

---

# 🏗 Architecture

# System Architecture

```text
┌──────────────────────────────┐
│      Android Application     │
│   Activities + Fragments     │
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│         ViewModels           │
│       MVVM State Layer       │
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│       Repository Layer       │
│ Retrofit + Room + Cache      │
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│         Mock REST API        │
│    Node.js + Persistent DB   │
└──────────────────────────────┘
````

---

# Architectural Principles

* offline-first architecture
* separation of concerns
* reactive UI updates
* scalable modular structure
* repository abstraction
* lifecycle-aware components

---

# 📡 UI & UX System

# Design Philosophy

The interface is designed to feel like:

> a cybersecurity operating system rather than a traditional mobile app.

---

# Visual Identity

## Primary Colors

| Token         | Value     |
| ------------- | --------- |
| Background    | `#0A0C10` |
| Cyan Accent   | `#00D1FF` |
| Purple Accent | `#8B5CF6` |
| Success Green | `#22C55E` |

---

# Interface Characteristics

* animated cyber grid backgrounds
* holographic-inspired surfaces
* futuristic operational dashboards
* smooth motion systems
* tactical visual hierarchy
* immersive dark mode
* ambient animated particles

---

# Motion System

* splash boot animations
* pulse transitions
* interactive feedback
* signal propagation effects
* ambient background rendering

---

# 🔐 Security Features

* AES256-GCM encrypted token storage
* JWT authentication
* OAuth2 Discord integration
* Role-based access control
* Protected administrator routes
* Secure session persistence
* Auth interceptors
* Deep-link validation
* Token refresh handling

---

# 📦 Offline-First Architecture

The application implements an offline-first architecture using Room Database caching.

Features include:

* cached events
* cached resources
* persistent user sessions
* local-first loading
* automatic synchronization
* offline availability

---

# 🌐 Backend API

# Base URL

```text
http://192.168.121.1:3000
```

---

# Endpoint Groups

| Endpoint Group   | Operations                       |
| ---------------- | -------------------------------- |
| `/auth`          | login, register, refresh, logout |
| `/users`         | list, get, patch                 |
| `/events`        | CRUD + registration              |
| `/resources`     | CRUD + filtering                 |
| `/announcements` | CRUD operations                  |
| `/admin/stats`   | dashboard statistics             |

---

# Built-in Image Functions

## avatarUrl(user)

Generates role-colored UI avatars.

## bannerUrl(event)

Generates deterministic event banners.

---

# Mock Data

Persistent demo data includes:

* 5 users
* 4 events
* 5 resources
* 3 announcements

All modifications survive server restarts.

---

# 🧩 Core Data Models

# User

```text
id
username
email
role
level
xp
avatar
```

---

# Event

```text
id
title
description
difficulty
date
location
capacity
participants
banner
```

---

# Resource

```text
id
title
type
category
difficulty
url
description
```

---

# Announcement

```text
id
title
body
type
priority
pinned
createdAt
```

---

# 📁 Project Structure

```text
CIC-MOB-APP/
│
├── app/
│   ├── data/
│   │   ├── api/
│   │   ├── database/
│   │   ├── models/
│   │   └── repositories/
│   │
│   ├── ui/
│   │   ├── auth/
│   │   ├── home/
│   │   ├── events/
│   │   ├── resources/
│   │   ├── announcements/
│   │   ├── profile/
│   │   ├── admin/
│   │   └── components/
│   │
│   ├── navigation/
│   ├── utils/
│   ├── theme/
│   └── MainActivity.kt
│
├── backend/
├── screenshots/
├── docs/
├── README.md
└── build.gradle
```

---

# 📦 Installation

# Prerequisites

* Android Studio
* JDK 17+
* Android SDK
* Node.js 18+
* Git

---

# Clone Repository

```bash
git clone https://github.com/fatehr15/CIC-MOB-APP.git
cd CIC-MOB-APP
```

---

# Backend Setup

```bash
cd backend
npm install
npm start
```

Server runs on:

```text
http://192.168.121.1:3000
```

---

# Android Setup

Open the project in Android Studio.

Then sync Gradle dependencies.

---

# 🚀 Development

# Debug Build

```bash
./gradlew assembleDebug
```

---

# Install Debug APK

```bash
./gradlew installDebug
```

---

# Release Build

```bash
./gradlew assembleRelease
```

---

# Run Unit Tests

```bash
./gradlew test
```

---

# Run Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

---

# 🌍 Environment Variables

Create a `.env` file inside the backend directory.

```env
PORT=3000
JWT_SECRET=your_secret
DISCORD_CLIENT_ID=your_client_id
DISCORD_CLIENT_SECRET=your_client_secret
DISCORD_REDIRECT_URI=cic://auth/callback
```

---

# 📸 Screenshots

# Planned Screenshots

* Authentication Screens
* Home Dashboard
* Events System
* Resources Hub
* Profile System
* Administrator Panel
* Animated Cyber Grid UI

---

# 🛣 Roadmap

# Planned Features

* WebSocket real-time synchronization
* Push notifications
* Integrated CTF platform
* Mentorship system
* AI assistant
* Team collaboration system
* Real-time activity feeds
* Advanced analytics dashboard
* Live event systems

---

# 🤝 Contributing

# Development Workflow

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push branch
5. Open pull request

---

# Commit Convention

```text
feat: add event registration system
fix: resolve token refresh issue
refactor: optimize repository layer
```

---

# Code Standards

* clean architecture principles
* meaningful commit messages
* modular components
* lifecycle-aware implementations
* consistent naming conventions

---

# 🐛 Troubleshooting

# Gradle Build Issues

```bash
./gradlew clean
```

---

# Android Studio Cache Problems

```text
File → Invalidate Caches → Restart
```

---

# Backend Dependency Issues

```bash
npm install
```

---

# Emulator Networking Issues

Use:

```text
10.0.2.2
```

instead of localhost when testing on Android Emulator.

---

# 📄 License

This project is currently under private development.

License definition will be added later.

---

# 📞 Contact

# Project Lead

Fateh

---

# Repository

[CIC-MOB-APP Repository](https://github.com/fatehr15/CIC-MOB-APP?utm_source=chatgpt.com)

---

# 🎯 Final Vision

CIC-MOB-APP is designed to become:

> the digital operational headquarters of the Cyber Innovators Club.

The platform aims to unify:

* cybersecurity learning
* technical collaboration
* events
* community interaction
* administrative operations

inside a single immersive cyber ecosystem.

---

# Final Identity Statement

> CIC-MOB-APP is not merely a mobile application.

> It is a futuristic cybersecurity workspace designed for builders, hackers, researchers, and future engineers.
```
