# Rebound-Mobile-App  

## Overview  
This project presents the development of a mobile e-commerce application that integrates **Augmented Reality (AR)** and **Machine Learning (ML)** to provide a **Virtual Try-On (VTO)** experience for fashion accessories, specifically piercing jewelry.  

The system enables users to:  
- Virtually try on accessories in real time using **MediaPipe** (landmark detection) and **Google Filament** (3D rendering).  
- Receive personalized product recommendations via a **hybrid recommender system** (Collaborative Filtering + Content-Based Filtering).  
- Browse, purchase, and book piercing services through a **mobile app**.  
- Manage orders, products, and users via an **admin website**.  

The solution addresses key challenges in online shopping such as lack of visualization, low purchase confidence, and high product return rates, while promoting sustainability by reducing unnecessary consumption.  

---

## Features  

### Authentication  
- Register and log in with email or Google OAuth  
- Secure OTP-based password reset  
- Multi-factor authentication for administrators  

### Shopping and Services  
- Browse products by category, price, and material  
- Add products to cart and wishlist  
- Checkout with multiple payment options (Card, Cash on Delivery, Bank Transfer)  
- Book piercing services with real-time scheduling  

### AR Virtual Try-On  
- Real-time jewelry placement using **MediaPipe**  
- 3D rendering with **Google Filament**  
- Capture snapshots and share results  

### Admin Website  
- Manage products, categories, and inventory  
- Manage customer accounts and bookings  
- Order validation and tracking  

---

## Tech Stack  

- **Frontend (Mobile App):** Android Studio (Java/Kotlin), MediaPipe, Google Filament  
- **Frontend (Admin Website):** Angular 
- **Backend:** Firebase (Authentication, Firestore Database, Cloud Functions, Analytics)  
- **Design Tools:** Figma (UI/UX), Draw.io (UML, ERD, BPMN)
- **Version Control:** GitHub

## Results

- The AR virtual try-on was tested on multiple Android devices.
- The hybrid recommender system improved product relevance and user engagement.
- User interface and experience were validated through Figma prototypes.
- The system enhanced purchase satisfaction and reduced hesitation in online shopping.
