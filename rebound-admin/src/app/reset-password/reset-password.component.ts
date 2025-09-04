import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms'; 

@Component({
  selector: 'app-reset-password',
  standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent {
  resetPasswordForm: FormGroup;
  successMessage: string = ''; // Thông báo khi đổi mật khẩu thành công
  errorMessage: string = '';   // Thông báo lỗi khi mật khẩu không trùng

  constructor(private fb: FormBuilder) {
    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    }, {
      validators: this.passwordMatchValidator
    });
  }
  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { passwordsDontMatch: true };
    }
    return null;
  }
  onSubmit() {
    if (this.resetPasswordForm.valid) {
      console.log('Password changed successfully:', this.resetPasswordForm.value);
      const newPassword = this.resetPasswordForm.value.newPassword;
      localStorage.setItem('userPassword', newPassword); 
      this.successMessage = 'Password changed successfully!';
      this.errorMessage = '';
    } else {
      console.log('Passwords do not match');
      this.errorMessage = 'Passwords do not match!';
      this.successMessage = '';
    }
  }
  get newPassword() {
    return this.resetPasswordForm.get('newPassword');
  }
  get confirmPassword() {
    return this.resetPasswordForm.get('confirmPassword');
  }
}