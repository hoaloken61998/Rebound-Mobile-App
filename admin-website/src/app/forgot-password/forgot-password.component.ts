
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {ReactiveFormsModule} from '@angular/forms'
import {RouterModule} from '@angular/router'



@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  emailValid: boolean = true; // Biến để kiểm tra tính hợp lệ của email

  // Cố định email cần kiểm tra
  fixedEmail: string = 'reboundpiercing.official@gmail.com';

  constructor(private fb: FormBuilder) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  // Kiểm tra email có đúng không
  checkEmailValidity() {
    const email = this.forgotPasswordForm.get('email')?.value;
    if (email && email !== this.fixedEmail) {
      this.emailValid = false;
    } else {
      this.emailValid = true;
    }
  }

  onSubmit() {
    this.checkEmailValidity(); // Kiểm tra email trước khi gửi

    if (this.forgotPasswordForm.valid && this.emailValid) {
      console.log('Forgot Password data:', this.forgotPasswordForm.value);
      // Logic gửi yêu cầu xác nhận quên mật khẩu tại đây
    } else {
      console.log('Invalid email!');
    }
  }
}