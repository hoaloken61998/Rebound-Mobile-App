// src/app/login/log-in.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

// Import Firebase Authentication modules
import { Auth, getAuth, signInWithEmailAndPassword, setPersistence, browserLocalPersistence, browserSessionPersistence, sendPasswordResetEmail, User, signOut } from '@angular/fire/auth';
import { initializeApp } from 'firebase/app';

declare const __firebase_config: string;
declare const __initial_auth_token: string;


@Component({
  selector: 'app-log-in',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './log-in.component.html',
  styleUrls: ['./log-in.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  private auth: Auth;

  constructor(private fb: FormBuilder, private router: Router) {
    const firebaseConfig = JSON.parse(typeof __firebase_config !== 'undefined' ? __firebase_config : '{}');
    const app = initializeApp(firebaseConfig);
    this.auth = getAuth(app);

    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false],
    });
  }

  ngOnInit(): void {
    // Không cần logic điều hướng trong ngOnInit nữa, AuthGuard và Router sẽ xử lý.
    console.log('Login Component: ngOnInit. Ready for login form.');
  }

  ngOnDestroy(): void {
    // No specific cleanup needed
  }

  async onSubmit(): Promise<void> {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.loginForm.valid) {
      const { email, password, rememberMe } = this.loginForm.value;

      try {
        await setPersistence(this.auth, rememberMe ? browserLocalPersistence : browserSessionPersistence);
        const userCredential = await signInWithEmailAndPassword(this.auth, email, password);
        const user = userCredential.user;
        console.log('Login successful:', user.email ?? 'No Email');

        this.successMessage = 'Login successful!';
        
        // IMPORTANT: SET THE FLAG IN LOCALSTORAGE ON SUCCESSFUL LOGIN
        localStorage.setItem('isLoggedIn', 'true');

        setTimeout(() => {
          // Navigate to the dashboard route within the protected app shell
          this.router.navigate(['/dashboard']); // <-- Changed target route
        }, 1500);

      } catch (error: any) {
        console.error('Login error:', error);
        switch (error.code) {
          case 'auth/invalid-credential':
            this.errorMessage = 'Invalid email or password. Please try again.';
            break;
          case 'auth/user-disabled':
            this.errorMessage = 'Your account has been disabled.';
            break;
          case 'auth/invalid-email':
            this.errorMessage = 'Invalid email address.';
            break;
          case 'auth/too-many-requests':
            this.errorMessage = 'Too many login attempts. Please try again later.';
            break;
          default:
            this.errorMessage = 'An unknown error occurred. Please try again.';
        }
        // IMPORTANT: REMOVE THE FLAG FROM LOCALSTORAGE ON FAILED LOGIN
        localStorage.removeItem('isLoggedIn');
      }
    } else {
      this.errorMessage = 'Please fill in all required fields correctly.';
      // IMPORTANT: REMOVE THE FLAG FROM LOCALSTORAGE IF FORM IS INVALID
      localStorage.removeItem('isLoggedIn');
    }
  }

  async forgotPassword(): Promise<void> {
    this.errorMessage = null;
    this.successMessage = null;
    const email = this.loginForm.get('email')?.value;

    if (!email || !this.loginForm.get('email')?.valid) {
      this.errorMessage = 'Please enter a valid email address to reset your password.';
      return;
    }

    try {
      await sendPasswordResetEmail(this.auth, email);
      this.successMessage = 'Password reset email sent. Please check your inbox.';
    } catch (error: any) {
      console.error('Forgot password error:', error);
      switch (error.code) {
        case 'auth/user-not-found':
        case 'auth/invalid-email':
          this.errorMessage = 'No user found with this email. Please check the email address.';
          break;
        case 'auth/too-many-requests':
          this.errorMessage = 'Too many requests. Please try again later.';
          break;
        default:
          this.errorMessage = 'An error occurred while sending the password reset email. Please try again.';
      }
    }
  }

  // This logout function should ideally only be called by the login page itself if user is already logged in
  // but somehow landed here. For actual logout, the sidebar's logout button will trigger /log-out route.
  async logout(): Promise<void> {
    try {
      await signOut(this.auth);
      console.log('Successfully logged out.');
      localStorage.removeItem('isLoggedIn');
      this.router.navigate(['/log-in']);
    } catch (error) {
      console.error('Error logging out:', error);
      alert('An error occurred during logout.');
    }
  }
}
