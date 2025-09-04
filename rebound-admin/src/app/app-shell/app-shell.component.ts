// src/app/app-shell/app-shell.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router'; // Dành cho router-outlet của nội dung chính

import { HeaderComponent } from '../header/header.component'; // Import Header
import { SideBarComponent } from '../side-bar/side-bar.component'; // Import SideBar

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, SideBarComponent], // Import các component con
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.css']
})
export class AppShellComponent {
  // Component này không cần nhiều logic, chủ yếu là để cấu trúc layout
  constructor() {
    console.log('AppShellComponent initialized: This is the authenticated layout.');
  }
}
