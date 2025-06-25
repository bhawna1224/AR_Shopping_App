# 🛋️ AR Furniture Shop

**AR Furniture Shop** is an Android application that enables users to browse and preview furniture in their own space using augmented reality. With shopping features like wishlist and cart management, plus secure Firebase Authentication, users can personalize and streamline their experience.

## ✨ Features

- 🔐 **Firebase Authentication** (Sign up, login, logout)
- 🛒 Add furniture items to **Cart** and **Wishlist**
- 🔍 **Filter furniture** by type (e.g., chair, table, sofa)
- 📱 **View in AR** using your phone's camera
- 🎨 Clean and responsive UI

## 🚀 Tech Stack

- **Language**: Java
- **AR**: ARCore / Sceneform (or your AR SDK)
- **Backend**: Firebase (Authentication, optionally Firestore/Storage)
- **UI**: Android XML layouts
- **IDE**: Android Studio

## 🛠️ Getting Started

### Prerequisites

- Android Studio installed
- ARCore-supported device
- Firebase project with `google-services.json` file

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/bhawna1224/ar_shopping_app.git
#### 2. Open the Project
- Launch **Android Studio**
- Select **Open an existing project**
- Navigate to the cloned folder

#### 3. Configure Firebase
- Go to [Firebase Console](https://console.firebase.google.com/)
- Create a new project
- Add an Android app to it
- Download the `google-services.json` file and place it inside the `/app` directory
- Enable **Email/Password Authentication**

#### 4. Amazon S3 Setup
- Upload your 3D models (`.glb`, `.gltf`, or `.sfb`) to an Amazon S3 bucket
- Update the app’s model-loading logic with your S3 base URL or use signed URLs
- Ensure models are publicly accessible or served securely via API

#### 5. Run the App
- Use a **physical device** that supports **ARCore**
- Click the **Run** button in Android Studio
---

### 🛠 Features

- 🔐 Secure login and logout with **Firebase Authentication**
- 🛒 Add/remove items from **Cart** and **Wishlist**
- 🔍 **Filter** furniture items by type (e.g., Chair, Sofa, Table)
- 📱 **View furniture in AR** using **ARCore**
- ☁️ Fetch 3D models from **Amazon S3**
- 🖼️ Intuitive and clean user interface

---

### 💡 Notes

- Your device **must support ARCore** to run the AR viewer
- You can expand the furniture catalog by adding new entries and corresponding 3D models
- Keep credentials and access keys secure and out of source code
