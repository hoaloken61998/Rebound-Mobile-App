// src/app/customer-detail/customer-detail.component.ts
import { Component, OnInit, OnDestroy, Inject, LOCALE_ID } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule, DatePipe, formatCurrency, formatDate } from '@angular/common'; // Import formatDate
import { Database, ref, get, onValue, off, DataSnapshot } from '@angular/fire/database';

// Định nghĩa giao diện cho Customer (User)
export interface CustomerInterface {
  id?: string; // Firebase UID
  UserID?: number; // ID nội bộ của người dùng (quan trọng để lọc đơn hàng)
  Email?: string;
  FullName?: string;
  PhoneNumber?: string;
  DateOfBirth?: string;
  UserRanking?: string;
  RegistrationDate?: string;
  Sex?: string;
  Address?: {
    Street?: string;
    Ward?: string;
    District?: string;
    Province?: string;
  };
}

// Cập nhật giao diện cho OrderHistoryItem để khớp với cấu trúc Firebase 'Order'
export interface OrderHistoryItem {
  firebaseId?: string; // Key của đơn hàng trong Firebase (ví dụ: '0' hoặc ID ngẫu nhiên)
  DeliveryFee?: number;
  DiscountValue?: number;
  OrderDate?: string; // "YYYY-MM-DD HH:mm:ss.SSS" - CẦN CÓ TRƯỜNG NÀY
  OrderID?: number; // ID nội bộ của đơn hàng này - CẦN CÓ TRƯỜNG NÀY
  PaymentMethodID?: number;
  Status?: string; // Trạng thái đơn hàng - CẦN CÓ TRƯỜNG NÀY
  Subtotal?: number;
  TotalAmount?: number; // Tổng số tiền - CẦN CÓ TRƯỜNG NÀY
  UserID?: number; // Liên kết với User
  UserPromotion?: number;
}

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-detail.component.html',
  styleUrl: './customer-detail.component.css'
})
export class CustomerDetailComponent implements OnInit, OnDestroy {
  customer: CustomerInterface = {};
  orders: OrderHistoryItem[] = [];

  private customerFirebaseId: string | null = null;
  private ordersListenerCleanup: (() => void) | undefined;

  constructor(
    private route: ActivatedRoute,
    private db: Database,
    @Inject(LOCALE_ID) private locale: string // Inject LOCALE_ID cho formatCurrency
  ) {}

  ngOnInit(): void {
    this.customerFirebaseId = this.route.snapshot.paramMap.get('id');
    if (this.customerFirebaseId) {
      const userRef = ref(this.db, 'User/' + this.customerFirebaseId);
      get(userRef).then((snapshot: DataSnapshot) => {
        if (snapshot.exists()) {
          const rawCustomerData = snapshot.val();
          this.customer = { id: snapshot.key, ...rawCustomerData };

          if (this.customer.UserID) {
            this.fetchOrdersForCustomer(this.customer.UserID);
          } else {
            console.warn('Customer data does not contain UserID. Cannot fetch orders.');
            this.orders = [];
          }
          console.log('Customer (User) data loaded:', this.customer);
        } else {
          console.warn('Không tìm thấy khách hàng (người dùng) với ID:', this.customerFirebaseId);
          this.customer = {};
          this.orders = [];
        }
      }).catch((error) => {
        console.error('Lỗi khi lấy dữ liệu khách hàng (người dùng) từ Firebase:', error);
        this.customer = {};
        this.orders = [];
      });
    }
  }

  ngOnDestroy(): void {
    if (this.ordersListenerCleanup) {
      this.ordersListenerCleanup();
    }
  }

  private fetchOrdersForCustomer(customerInternalUserId: number): void {
    const ordersRef = ref(this.db, 'Order');

    this.ordersListenerCleanup = onValue(ordersRef, (snapshot: DataSnapshot) => {
      const allOrdersData = snapshot.val();
      const customerOrders: OrderHistoryItem[] = [];

      if (allOrdersData) {
        Object.keys(allOrdersData).forEach(key => {
          // Ép kiểu để đảm bảo các thuộc tính tồn tại cho Type Script
          const order = { firebaseId: key, ...allOrdersData[key] } as OrderHistoryItem;
          if (order.UserID === customerInternalUserId) {
            customerOrders.push(order);
          }
        });
        customerOrders.sort((a, b) => {
          const dateA = new Date(a.OrderDate || '').getTime();
          const dateB = new Date(b.OrderDate || '').getTime();
          return dateB - dateA;
        });
      }
      this.orders = customerOrders;
      console.log(`Orders for UserID ${customerInternalUserId} loaded and filtered:`, this.orders);
    }, (error) => {
      console.error('Lỗi khi tải và lọc đơn hàng cho khách hàng từ Firebase:', error);
      this.orders = [];
    });
  }

  // --- BỔ SUNG LẠI PHƯƠNG THỨC formatCurrency ---
  formatCurrency(value: number | undefined): string {
    if (value === undefined || isNaN(value)) {
      return 'N/A';
    }
    return formatCurrency(value, this.locale, '₫', 'VND', '1.0-0');
  }

  // --- BỔ SUNG LẠI PHƯƠNG THỨC formatDateTime ---
  formatDateTime(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const isoString = dateString.replace(' ', 'T').split('.')[0];
      const date = new Date(isoString);
      if (isNaN(date.getTime())) {
        throw new Error('Invalid date');
      }
      return formatDate(date, 'medium', this.locale);
    } catch (e) {
      console.error('Lỗi định dạng ngày giờ:', dateString, e);
      return 'Invalid Date';
    }
  }

  goBack(): void {
    window.history.back();
  }
}
