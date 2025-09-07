// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { LoginComponent } from './log-in/log-in.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CustomerManagementComponent } from './customer-management/customer-management.component';
import { ProductManagementComponent } from './product-management/product-management.component';
import { OrderManagementComponent } from './order-management/order-management.component';
import { ReservationManagementComponent } from './reservation-management/reservation-management.component';
import { CustomerDetailComponent } from './customer-detail/customer-detail.component';
import { EditCustomerComponent } from './edit-customer/edit-customer.component';
import { AddProductComponent } from './add-product/add-product.component';
import { AddReservationComponent } from './add-reservation/add-reservation.component';
import { ReservationDetailComponent } from './reservation-detail/reservation-detail.component';
import { EditProductComponent } from './edit-product/edit-product.component';

// Import LogOutComponent
import { LogOutComponent } from './log-out/log-out.component';

// Import AuthGuard
import { AuthGuard } from './auth.guard';

// Import AppShellComponent (MỚI)
import { AppShellComponent } from './app-shell/app-shell.component';


export const routes: Routes = [
  // 1. Route cho trang đăng nhập (không bảo vệ)
  { path: 'log-in', component: LoginComponent },

  // 2. Route cho trang xác nhận đăng xuất
  { path: 'log-out', component: LogOutComponent },

  // 3. Đường dẫn gốc: CHUYỂN HƯỚNG MẶC ĐỊNH ĐẾN TRANG ĐĂNG NHẬP
  { path: '', redirectTo: '/log-in', pathMatch: 'full' }, // Khi vào /, sẽ chuyển hướng đến /log-in

  // 4. Đường dẫn chính của ứng dụng sau khi đăng nhập (ĐƯỢC BẢO VỆ bởi AuthGuard)
  // Component AppShellComponent sẽ là layout chung (sidebar, header)
  {
    path: '', // Đường dẫn trống này sẽ áp dụng cho tất cả các children, ví dụ: /dashboard, /customer-management
    component: AppShellComponent, // Component này sẽ chứa Header, Sidebar và router-outlet cho các trang con
    canActivate: [AuthGuard], // AuthGuard sẽ bảo vệ toàn bộ phần này
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'customer-management', component: CustomerManagementComponent },
      { path: 'customer-detail/:id', component: CustomerDetailComponent },
      { path: 'edit-customer/:id', component: EditCustomerComponent },
      { path: 'product-management', component: ProductManagementComponent },
      { path: 'add-product', component: AddProductComponent },
      { path: 'order-management', component: OrderManagementComponent },
      { path: 'reservation-management', component: ReservationManagementComponent },
      { path: 'add-reservation', component: AddReservationComponent },
      { path: 'reservation-detail/:id', component: ReservationDetailComponent },
      { path: 'edit-product/:id', component: EditProductComponent },
      // Các route khác của ứng dụng chính sẽ nằm ở đây
      { path: '**', redirectTo: 'dashboard' } // Bất kỳ đường dẫn con không khớp nào sẽ về dashboard
    ]
  },

  // 5. Wildcard route: Chuyển hướng bất kỳ đường dẫn không khớp nào (ngoài các route đã định nghĩa) về /log-in
  // Điều này bắt các trường hợp gõ sai URL mà không khớp với bất kỳ route nào khác
  { path: '**', redirectTo: '/log-in' }
];
