// src/app/customer-management/customer-management.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router'; // Import Router để điều hướng
import { Database, ref, onValue, off, remove, DataSnapshot } from '@angular/fire/database';

// Định nghĩa giao diện cho User (Customer)
// Đảm bảo UserInterface này khớp với cấu trúc dữ liệu của bạn trên Firebase Realtime Database
export interface UserInterface {
  id?: string; // Đây là Firebase UID (key của object trong node User)
  UserID?: number; // ID nội bộ của người dùng (từ database của bạn)
  Email?: string;
  FullName?: string; // Tên đầy đủ của người dùng
  PhoneNumber?: string;
  Address?: string;
  DateOfBirth?: string;
  RegistrationDate?: string;
  selected?: boolean; // Để phục vụ chức năng chọn/bỏ chọn
  // Thêm các trường khác nếu có trong cấu trúc dữ liệu User của bạn, nhưng chỉ hiển thị chính ở đây
}

@Component({
  selector: 'app-customer-management',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './customer-management.component.html',
  styleUrl: './customer-management.component.css'
})
export class CustomerManagementComponent implements OnInit, OnDestroy {
  customers: UserInterface[] = []; // Kiểu dữ liệu là UserInterface
  searchTerm: string = '';

  // Khởi tạo các thuộc tính để tránh lỗi 'Property does not exist'
  // (Bạn sẽ cần logic cụ thể để điền dữ liệu vào các mảng này nếu muốn sử dụng chúng)
  topTierUsers: UserInterface[] = [];
  recentUsers: UserInterface[] = [];

  private dbListenerCleanup: (() => void) | undefined; // Để lưu hàm hủy đăng ký lắng nghe Firebase

  // Inject Database và Router
  constructor(private db: Database, private router: Router) {}

  ngOnInit(): void {
    const usersRef = ref(this.db, 'User'); // Sử dụng 'User' theo cấu trúc DB của bạn

    // onValue sẽ tự động cập nhật customers mỗi khi dữ liệu thay đổi trên Firebase
    this.dbListenerCleanup = onValue(usersRef, (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      const loadedCustomers: UserInterface[] = [];
      if (data) {
        Object.keys(data).forEach(key => {
          loadedCustomers.push({
            id: key, // LƯU KEY CỦA FIREBASE LÀM ID (UID)
            ...data[key],
            selected: false // Khởi tạo selected là false
          });
        });
      }
      this.customers = loadedCustomers;
      console.log('Customers (Users) loaded from Firebase:', this.customers);

      // Cập nhật logic cho topTierUsers và recentUsers nếu có
      // Ví dụ:
      // this.topTierUsers = this.customers.filter(c => /* thêm điều kiện cho top tier */);
      // this.recentUsers = this.customers.sort((a, b) => {
      //   const dateA = new Date(a.RegistrationDate || '').getTime();
      //   const dateB = new Date(b.RegistrationDate || '').getTime();
      //   return dateB - dateA;
      // }).slice(0, 5);
    }, (error) => {
      console.error('Lỗi khi tải khách hàng (người dùng) từ Firebase:', error);
    });
  }

  ngOnDestroy(): void {
    // Hủy đăng ký lắng nghe khi component bị hủy
    if (this.dbListenerCleanup) {
      this.dbListenerCleanup(); // Gọi hàm cleanup để hủy lắng nghe
    }
  }

  // Phương thức để cập nhật truy vấn tìm kiếm từ input
  updateSearchQuery(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.searchTerm = inputElement.value.trim().toLowerCase();
  }

  // Phương thức để lọc customers dựa trên searchTerm
  get filteredCustomers(): UserInterface[] {
    if (!this.searchTerm) {
      return this.customers;
    }
    const lowerCaseSearchTerm = this.searchTerm.toLowerCase();
    return this.customers.filter(customer =>
      // Tìm kiếm theo các trường hiển thị trong bảng
      customer.UserID?.toString().toLowerCase().includes(lowerCaseSearchTerm) ||
      customer.Email?.toLowerCase().includes(lowerCaseSearchTerm) ||
      customer.FullName?.toLowerCase().includes(lowerCaseSearchTerm) ||
      customer.PhoneNumber?.toLowerCase().includes(lowerCaseSearchTerm)
      // Không tìm kiếm theo Address, DateOfBirth, RegistrationDate ở đây, chỉ trên trang detail
    );
  }

  // Chọn hoặc bỏ chọn tất cả các khách hàng
  selectAllCustomers(event: any) {
    const isChecked = event.target.checked;
    this.customers = this.customers.map(customer => ({ ...customer, selected: isChecked }));
  }

  // Chọn hoặc bỏ chọn một khách hàng riêng biệt
  toggleCustomerSelection(customer: UserInterface): void {
    if (customer && customer.id) {
      this.customers = this.customers.map(c =>
        c.id === customer.id ? { ...c, selected: !c.selected } : c
      );
    }
  }

  // Điều hướng đến trang chi tiết/chỉnh sửa khách hàng
  // Truyền Firebase UID (customer.id) để trang chi tiết có thể tải dữ liệu
  editCustomer(customerId: string | undefined): void {
    if (customerId) {
      this.router.navigate(['/customer-detail', customerId]);
    } else {
      console.warn('Customer ID is undefined, cannot edit.');
    }
  }

  // Xóa một khách hàng khỏi Firebase Realtime Database
  // SỬ DỤNG customer.id (Firebase UID) để xóa đúng bản ghi
  deleteCustomer(customerId: string | undefined): void {
    if (!customerId) {
      console.error('Customer ID (Firebase UID) is undefined, cannot delete.');
      return;
    }
    if (confirm('Bạn có chắc muốn xóa khách hàng này không?')) {
      remove(ref(this.db, 'User/' + customerId)) // Sử dụng 'User/' và Firebase UID
        .then(() => {
          console.log('Khách hàng đã được xóa thành công từ Firebase:', customerId);
          // Dữ liệu sẽ tự động cập nhật trong this.customers do onValue listener
        })
        .catch((error) => {
          console.error('Lỗi khi xóa khách hàng khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa khách hàng.');
        });
    }
  }

  // Xóa các khách hàng đã chọn khỏi Firebase Realtime Database
  deleteSelectedCustomers(): void {
    const selectedCustomerIds = this.customers
      .filter(customer => customer.selected && customer.id) // Lọc những khách hàng được chọn có Firebase UID
      .map(customer => customer.id as string);

    if (selectedCustomerIds.length === 0) {
      alert('Vui lòng chọn ít nhất một khách hàng để xóa.');
      return;
    }

    if (confirm(`Bạn có chắc muốn xóa ${selectedCustomerIds.length} khách hàng đã chọn không?`)) {
      const deletePromises = selectedCustomerIds.map(customerId =>
        remove(ref(this.db, 'User/' + customerId)) // Xóa bằng Firebase UID
      );

      Promise.all(deletePromises)
        .then(() => {
          console.log('Các khách hàng đã chọn đã được xóa thành công từ Firebase.');
          // Dữ liệu sẽ tự động cập nhật do onValue listener
        })
        .catch((error) => {
          console.error('Lỗi khi xóa các khách hàng đã chọn khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa các khách hàng đã chọn.');
        });
    }
  }
}
