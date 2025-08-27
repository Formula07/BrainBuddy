## ✨ About the Project
**BrainBuddy** is a platform designed to help students collaborate and connect.  
It allows users to register, match with peers, and share knowledge efficiently.  

Whether you're preparing for exams or brainstorming projects, BrainBuddy is here to connect you with the right study buddy.  

---

## 🔑 Features
✔️ User Registration & Authentication  
✔️ Profile Management  
✔️ Smart Matching System (Find your study buddy!)  
✔️ Real-time Notifications *(planned)*  
✔️ MySQL-powered Data Persistence  
✔️ RESTful API for extensibility  

---

## 🛠️ Tech Stack
- **Backend**: Spring Boot (Java)  
- **Database**: MySQL  
- **Build Tool**: Maven  
- **Authentication**: Spring Security *(planned / optional)*  
- **Deployment**: Docker *(optional)*  

---

## 🏗️ Architecture

<img width="6367" height="4439" alt="NotebookLM Mind Map (1)" src="https://github.com/user-attachments/assets/34339f1d-356e-4857-863a-85bc52ec385b" />

## 🗄️ Database Schema
Example schema (simplified):

**users**  
- id (PK)  
- name  
- email  
- password  

**matches**  
- id (PK)  
- user1_id (FK → users.id)  
- user2_id (FK → users.id)  
- status

## 📡 API Endpoints

| Method | Endpoint          | Description              | Request Body (Example) |
|--------|-------------------|--------------------------|-------------------------|
| **POST** | `/users/register` | Register a new user       | `{ "name": "kartikey", "email": "kartikey@example.com", "password": "12345" }` |
| **POST** | `/users/login`    | User login               | `{ "email": "ansh@example.com", "password": "12345" }` |
| **GET**  | `/users/{id}`     | Fetch user details       | — |
| **GET**  | `/matches/{id}`   | Get matches for a user   | — |
| **POST** | `/matches/create` | Create a new match       | `{ "user1Id": 1, "user2Id": 2, "status": "pending" }` |

## 🤝 Contributing

- Contributions are welcome!
- Fork the repo
- Create a new branch (feature/my-feature)
- Commit changes
- Push to your branch
- Create a Pull Request 🎉
