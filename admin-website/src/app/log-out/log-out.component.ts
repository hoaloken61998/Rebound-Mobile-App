// src/app/log-out/log-out.component.ts
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common'; // Import CommonModule for general directives
import { Auth, signOut } from '@angular/fire/auth'; // Import Firebase Auth and signOut

@Component({
  selector: 'app-logout', // Selector for the component
  standalone: true,
  imports: [RouterModule, CommonModule], // Add CommonModule
  templateUrl: './log-out.component.html',
  styleUrls: ['./log-out.component.css']
})
export class LogOutComponent {
  constructor(private router: Router, private auth: Auth) {} // Inject Auth service

  // When 'Yes' is clicked, sign out and navigate to Login
  async onYesClick(): Promise<void> {
    try {
      await signOut(this.auth); // Perform Firebase signOut
      console.log('User signed out successfully.');
      localStorage.removeItem('isLoggedIn'); // Remove the login flag from localStorage
      this.router.navigate(['/log-in']); // Navigate to the login page
    } catch (error) {
      console.error('Error during logout:', error);
      // Optionally, show an error message to the user
      alert('An error occurred during logout. Please try again.');
      // Still navigate to login or dashboard, depending on desired UX
      this.router.navigate(['/dashboard']); // Navigate to dashboard if logout failed but user is still technically "logged in" by localStorage
    }
  }

  // When 'No' is clicked, navigate back to Dashboard
  onNoClick(): void {
    this.router.navigate(['/dashboard']);
  }
}
