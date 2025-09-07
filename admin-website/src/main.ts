// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config'; // <-- QUAN TRỌNG: Import appConfig

// Các import provideRouter, provideFirebaseApp, initializeApp, getFirestore, etc.,
// sẽ không cần ở đây nữa vì chúng được cấu hình trong app.config.ts

bootstrapApplication(AppComponent, appConfig) // <-- QUAN TRỌNG: Sử dụng appConfig
  .catch((err) => console.error(err));
