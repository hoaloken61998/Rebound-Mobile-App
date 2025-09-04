// src/app/side-bar/side-bar.component.ts
import { Component, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-side-bar',
  standalone: true,
  imports: [RouterModule, CommonModule, ],
  templateUrl: './side-bar.component.html',
  styleUrls: ['./side-bar.component.css'],
  encapsulation: ViewEncapsulation.None // For global CSS
})
export class SideBarComponent {
  constructor(private router: Router) {}

  // Navigate to routes within the app shell
  navigateToDashboard() {
    this.router.navigate(['/dashboard']); // Now direct path from root, handled by app.routes children
  }

  navigateToCustomer() {
    this.router.navigate(['/customer-management']);
  }

  navigateToProduct() {
    this.router.navigate(['/product-management']);
  }

  navigateToReservation() {
    this.router.navigate(['/reservation-management']);
  }

  navigateToOrder() {
    this.router.navigate(['/order-management']);
  }

  // Changed to navigate to the logout confirmation page
  navigateToLogout(): void {
    console.log('Navigating to logout confirmation page.');
    this.router.navigate(['/log-out']);
  }
}
