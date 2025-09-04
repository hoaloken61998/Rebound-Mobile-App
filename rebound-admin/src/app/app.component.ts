// src/app/app.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router'; // Chỉ cần RouterOutlet
// Các import Firebase Auth và Subscription/filter không còn cần ở đây.
// import { Auth, signOut } from '@angular/fire/auth';
// import { Subscription, filter } from 'rxjs';

// HeaderComponent và SideBarComponent sẽ được import trong AppShellComponent
// import { SideBarComponent } from './side-bar/side-bar.component';
// import { HeaderComponent } from './header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet], // Chỉ import CommonModule và RouterOutlet
  template: `<router-outlet></router-outlet>`, // Template chỉ chứa router-outlet
  styleUrls: ['./app.component.css'] // Giữ lại style nếu có các style global ảnh hưởng đến body/html
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'rebound-admin-app';

  // Không cần các thuộc tính liên quan đến trạng thái đăng nhập hoặc theo dõi route nữa
  // isLoggedIn: boolean = false;
  // private routerEventsSubscription: Subscription | undefined;
  // currentRoute: string = '';

  constructor(private router: Router) {
    // Không cần lắng nghe router events ở đây nữa
  }

  ngOnInit(): void {
    // Không cần logic ở đây nữa, router sẽ xử lý mọi thứ
    console.log('AppComponent initialized.');
  }

  ngOnDestroy(): void {
    // Không cần cleanup gì nữa
  }

  // Phương thức isLoginPage không còn cần thiết ở đây vì layout đã được tách biệt
  // isLoginPage(): boolean {
  //   return this.currentRoute === '/log-in';
  // }
}
