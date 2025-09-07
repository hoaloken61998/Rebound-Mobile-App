import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LogInComponent } from './log-in/log-in.component';
import { LogOutComponent } from './log-out/log-out.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './auth.guard';
import { SideBarComponent } from './side-bar/side-bar.component';
import { ReservationManagementComponent } from './reservation-management/reservation-management.component';
import { ProductManagementComponent } from './product-management/product-management.component';
import { OrderManagementComponent } from './order-management/order-management.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { OrderDetailComponent } from './order-detail/order-detail.component';
import { EditProductComponent } from './edit-product/edit-product.component';
import { ReservationDetailComponent } from './reservation-detail/reservation-detail.component';
import { CustomerManagementComponent } from './customer-management/customer-management.component';
import { CustomerDetailComponent } from './customer-detail/customer-detail.component';


const routes: Routes = [

  { path: 'log-out', component: LogOutComponent },
  { path: 'log-in', component: LogInComponent },
  { path: '', redirectTo: '/log-in', pathMatch: 'full' }, 
  { path: 'side-bar', component: SideBarComponent },
  { path: 'reservation-management', component: ReservationManagementComponent },
  { path: 'product-management', component: ProductManagementComponent },
  { path: 'order-management', component: OrderManagementComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'order-detail', component: OrderDetailComponent },
  { path: 'edit-product', component: EditProductComponent },
  { path: 'reservation-detail', component: ReservationDetailComponent },
  { path: 'customer-management', component: CustomerManagementComponent },
  { path: 'customer-detail/:id', component: CustomerDetailComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}