// src/app/edit-customer/edit-customer.component.ts
import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { Location, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Database, ref, get, update, DataSnapshot } from '@angular/fire/database';

// Import các hàm từ Firebase Authentication để thay đổi mật khẩu (nếu cần)
// import { Auth, updatePassword, getAuth } from '@angular/fire/auth'; // Uncomment nếu bạn muốn thêm tính năng đổi mật khẩu

// Định nghĩa giao diện CustomerInterface, bao gồm AvatarURL
export interface CustomerInterface {
  id?: string; // Firebase UID
  UserID?: number;
  Email?: string; // Sẽ cho phép chỉnh sửa
  FullName?: string;
  PhoneNumber?: string; // Sẽ cho phép chỉnh sửa
  DateOfBirth?: string;
  UserRanking?: string; // Sẽ cho phép chỉnh sửa (tương ứng với 'Status' trong HTML)
  RegistrationDate?: string;
  Sex?: string;
  Address?: {
    Street?: string;
    Ward?: string;
    District?: string;
    Province?: string;
  };
  Description?: string;
  AvatarURL?: string; // Thêm trường AvatarURL
}

@Component({
  selector: 'app-edit-customer',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './edit-customer.component.html',
  styleUrl: './edit-customer.component.css',
  encapsulation: ViewEncapsulation.None,
})
export class EditCustomerComponent implements OnInit, OnDestroy {
  customer: CustomerInterface = {}; // Đối tượng customer sẽ chứa dữ liệu tải về
  originalCustomer: CustomerInterface = {}; // Lưu trữ dữ liệu gốc để giữ nguyên các trường không sửa
  customerId: string | null = null; // Để lưu Firebase UID của khách hàng
  isLoading: boolean = true; // Biến trạng thái tải dữ liệu

  constructor(
    private location: Location,
    private route: ActivatedRoute,
    private db: Database,
    // private auth: Auth // Uncomment nếu bạn muốn thêm tính năng đổi mật khẩu
  ) {}

  ngOnInit(): void {
    this.customerId = this.route.snapshot.paramMap.get('id');
    if (this.customerId) {
      this.loadCustomerData(this.customerId);
    } else {
      console.warn('Customer ID not provided in URL.');
      this.isLoading = false;
    }
  }

  ngOnDestroy(): void {
    // Không cần cleanup listener đặc biệt ở đây vì chúng ta dùng `get` (one-time fetch)
  }

  private loadCustomerData(id: string): void {
    this.isLoading = true;
    get(ref(this.db, 'User/' + id))
      .then((snapshot: DataSnapshot) => {
        if (snapshot.exists()) {
          const rawCustomerData = snapshot.val();
          this.customer = { id: snapshot.key, ...rawCustomerData };
          this.originalCustomer = { ...this.customer }; // Lưu bản sao của dữ liệu gốc

          // Xử lý định dạng ngày sinh nếu cần (ví dụ: chuyển đổi từ "YYYY-MM-DD HH:mm:ss" sang "YYYY-MM-DD" cho input type="date")
          if (this.customer.DateOfBirth && typeof this.customer.DateOfBirth === 'string') {
            this.customer.DateOfBirth = this.customer.DateOfBirth.split(' ')[0];
          }

          console.log('Customer data loaded for editing:', this.customer);
        } else {
          console.warn('No customer found with ID:', id);
          this.customer = {};
          this.originalCustomer = {};
        }
      })
      .catch((error) => {
        console.error('Error loading customer data:', error);
        this.customer = {};
        this.originalCustomer = {};
      })
      .finally(() => {
        this.isLoading = false;
      });
  }

  goBack(): void {
    this.location.back();
  }

  // Phương thức Clear chỉ đặt lại các trường có thể chỉnh sửa về rỗng
  clear(): void {
    // Các trường chỉ đọc sẽ giữ nguyên giá trị từ originalCustomer
    this.customer.Email = '';
    this.customer.PhoneNumber = '';
    this.customer.UserRanking = 'Member'; // Đặt lại về giá trị mặc định cho Status/UserRanking
    this.customer.Description = ''; // Description cũng là trường có thể chỉnh sửa
    // Không reset các trường như FullName, UserID, Address, DOB, Sex, AvatarURL
    console.log('Editable form fields cleared.');
  }

  // Phương thức lưu thay đổi vào Firebase
  async save(): Promise<void> {
    if (!this.customerId) {
      console.error('Cannot save: Customer ID (Firebase UID) is missing.');
      alert('Không thể lưu: ID khách hàng bị thiếu.');
      return;
    }

    // Chỉ gửi các trường có thể chỉnh sửa đến Firebase
    const dataToUpdate: Partial<CustomerInterface> = {
      Email: this.customer.Email,
      PhoneNumber: this.customer.PhoneNumber,
      UserRanking: this.customer.UserRanking,
      Description: this.customer.Description,
      // Nếu DateOfBirth cũng có thể chỉnh sửa, hãy thêm nó vào đây
      DateOfBirth: this.customer.DateOfBirth, // Giả sử DOB cũng có thể sửa
      Sex: this.customer.Sex, // Giả sử Sex cũng có thể sửa
      // Nếu bạn muốn cho phép sửa địa chỉ, hãy thêm các trường địa chỉ vào đây:
      // Address: this.customer.Address
    };

    // Ví dụ về cách thay đổi mật khẩu (KHÔNG NÊN LÀM TRỰC TIẾP TẠI ĐÂY TRONG REALTIME DB UPDATE)
    // Nếu bạn muốn cho phép admin đổi mật khẩu người dùng, bạn cần dùng Cloud Functions
    // hoặc một luồng an toàn khác để gọi updatePassword từ Admin SDK.
    // Nếu bạn muốn người dùng tự đổi mật khẩu, họ sẽ làm trên giao diện riêng của họ.
    // LƯU Ý: KHÔNG THÊM TRƯỜNG 'password' VÀO dataToUpdate NÀY.

    try {
      // Cập nhật dữ liệu vào node User trong Realtime Database
      await update(ref(this.db, 'User/' + this.customerId), dataToUpdate);
      console.log('Customer information saved successfully:', dataToUpdate);
      alert('Thông tin khách hàng đã được lưu thành công!');
      this.location.back(); // Quay lại trang trước sau khi lưu
    } catch (error) {
      console.error('Error saving customer information:', error);
      alert('Đã xảy ra lỗi khi lưu thông tin khách hàng.');
    }
  }
}
