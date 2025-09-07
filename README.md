# Rebound-Mobile-App  

## 📄 Academic Background  
This project is based on our research paper:  

**“Enhancing Sustainable Consumption in Fashion Accessories through AR-Based Virtual Try-On Systems in Vietnam”**  
Accepted at **SEBL 2025 Conference**.  

👉 [Full Paper (PDF)](docs/SEBL2025_Paper.pdf)  

---

## 🌍 Overview  
The growth of e-commerce in Vietnam has made fashion accessories more accessible, but it also causes challenges in **visualization, purchase confidence, and sustainability**. Shoppers often cannot preview how products fit, leading to unnecessary consumption and waste.  

This project introduces a **mobile-based AR Virtual Try-On (VTO) system** that integrates **Augmented Reality (AR)** and **Machine Learning (ML)** to improve the shopping experience, reduce product waste, and promote sustainable consumption.  

The system enables users to:  
- 👓 Virtually try on jewelry in **real time** using **MediaPipe** (landmark detection) and **Google Filament** (3D rendering).  
- 🤖 Receive **personalized recommendations** via a hybrid recommender system (Collaborative + Content-Based Filtering).  
- 🛍️ Browse, purchase, and book piercing services.  
- 🛠️ Manage orders, products, and users via an **admin website**.  

---

## ✨ Features  

### 🔑 Authentication  
- Email/Google login and registration  
- OTP-based password reset  
- Multi-factor authentication for admins  

### 🛒 Shopping & Services  
- Product browsing (category, price, material filters)  
- Wishlist and cart functionality  
- Checkout with **Cash on Delivery, Credit/Debit Card, or Bank Transfer (QR code)**  
- Service booking with scheduling and reminders  

### 🕶️ AR Virtual Try-On  
- Real-time jewelry try-on with MediaPipe  
- Photorealistic rendering with Google Filament  
- Camera switching, snapshot, and sharing  

### 🖥️ Admin Website  
- Product & inventory management  
- Customer account and booking management  
- Order tracking & validation  

---

## 🏗️ Tech Stack  

- **Frontend (Mobile App):** Android Studio (Java/Kotlin), ARCore, MediaPipe, Google Filament  
- **Frontend (Admin Website):** Angular, TypeScript, HTML, CSS  
- **Backend:** Firebase (Authentication, Firestore, Cloud Functions, Analytics)  
- **Database:** Firestore + SQLite (local caching)  
- **Machine Learning:** Python, Scikit-learn, TensorFlow  
- **Design Tools:** Figma (UI/UX), Draw.io (UML, ERD, BPMN)  
- **Version Control:** GitHub  

---

## 📂 Repository Structure  

