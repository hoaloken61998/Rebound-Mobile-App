// src/app/order-management/order-management.component.ts
import { Component, OnInit, OnDestroy, Inject, LOCALE_ID } from '@angular/core';
import { CommonModule, formatCurrency, formatDate } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms'; // Thêm FormsModule cho [(ngModel)]
// Import các module và hàm từ Firebase Realtime Database
import { Database, ref, onValue, off, remove, DataSnapshot } from '@angular/fire/database';
import { Subscription } from 'rxjs'; // Để quản lý việc hủy đăng ký lắng nghe Firebase (nếu cần)

// Cập nhật giao diện cho Order để khớp chính xác với Firebase Order node
export interface OrderInterface {
  id?: string; // ID từ Firebase Realtime Database key (ví dụ: '0' hoặc ID ngẫu nhiên)
  DeliveryFee?: number;
  DiscountValue?: number;
  OrderDate?: string; // Chuỗi ngày tháng từ Firebase: "YYYY-MM-DD HH:mm:ss.SSS"
  OrderID?: number; // ID nội bộ của đơn hàng
  PaymentMethodID?: number;
  Status?: string; // Trạng thái đơn hàng
  Subtotal?: number;
  TotalAmount?: number;
  UserID?: number; // ID của người dùng đặt hàng
  UserPromotion?: number;
  selected?: boolean; // Để phục vụ chức năng chọn/bỏ chọn
}

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // Thêm FormsModule
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css']
})
export class OrderManagementComponent implements OnInit, OnDestroy {
  orders: OrderInterface[] = [];
  searchTerm: string = '';
  selectedStatusFilter: string = 'All'; // Biến mới để lưu trạng thái lọc, mặc định là 'All'
  private ordersListenerCleanup: (() => void) | undefined;

  constructor(private router: Router, private db: Database, @Inject(LOCALE_ID) private locale: string) {}

  ngOnInit(): void {
    const ordersRef = ref(this.db, 'Order');

    this.ordersListenerCleanup = onValue(ordersRef, (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      const loadedOrders: OrderInterface[] = [];
      if (data) {
        Object.keys(data).forEach(key => {
          loadedOrders.push({
            id: key,
            ...data[key],
            selected: false
          });
        });
        loadedOrders.sort((a, b) => {
          const dateA = new Date(a.OrderDate || '').getTime();
          const dateB = new Date(b.OrderDate || '').getTime();
          return dateB - dateA;
        });
      }
      this.orders = loadedOrders;
      console.log('Đơn hàng đã tải từ Firebase:', this.orders);
    }, (error) => {
      console.error('Lỗi khi tải đơn hàng từ Firebase:', error);
    });
  }

  ngOnDestroy(): void {
    if (this.ordersListenerCleanup) {
      this.ordersListenerCleanup();
    }
  }

  formatDateTime(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const isoString = dateString.replace(' ', 'T').split('.')[0];
      const date = new Date(isoString);
      if (isNaN(date.getTime())) {
        throw new Error('Ngày không hợp lệ');
      }
      return formatDate(date, 'medium', this.locale);
    } catch (e) {
      console.error('Lỗi định dạng ngày giờ:', dateString, e);
      return 'Ngày không hợp lệ';
    }
  }

  deleteOrder(orderFirebaseId: string | undefined): void {
    if (!orderFirebaseId) {
      console.error('ID Firebase của đơn hàng không xác định, không thể xóa.');
      return;
    }
    if (confirm('Bạn có chắc muốn xóa đơn hàng này không?')) {
      remove(ref(this.db, 'Order/' + orderFirebaseId))
        .then(() => {
          console.log('Đơn hàng đã được xóa thành công từ Firebase:', orderFirebaseId);
        })
        .catch((error) => {
          console.error('Lỗi khi xóa đơn hàng khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa đơn hàng.');
        });
    }
  }

  deleteSelectedOrders(): void {
    const selectedOrderFirebaseIds = this.orders
      .filter(order => order.selected && order.id)
      .map(order => order.id as string);

    if (selectedOrderFirebaseIds.length === 0) {
      alert('Vui lòng chọn ít nhất một đơn hàng để xóa.');
      return;
    }

    if (confirm(`Bạn có chắc muốn xóa ${selectedOrderFirebaseIds.length} đơn hàng đã chọn không?`)) {
      const deletePromises = selectedOrderFirebaseIds.map(orderFirebaseId =>
        remove(ref(this.db, 'Order/' + orderFirebaseId))
      );

      Promise.all(deletePromises)
        .then(() => {
          console.log('Các đơn hàng đã chọn đã được xóa thành công từ Firebase.');
        })
        .catch((error) => {
          console.error('Lỗi khi xóa các đơn hàng đã chọn khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa các đơn hàng đã chọn.');
        });
    }
  }

  formatPrice(price: any): string {
    if (typeof price !== 'number') {
      price = parseFloat(price);
      if (isNaN(price)) {
        return 'N/A';
      }
    }
    return formatCurrency(price, this.locale, '₫', 'VND', '1.0-0');
  }

  updateSearchQuery(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.searchTerm = inputElement.value.trim().toLowerCase();
  }

  // Phương thức để lọc orders dựa trên searchTerm VÀ selectedStatusFilter
  get filteredOrders(): OrderInterface[] {
    let filtered = this.orders;

    // Lọc theo tìm kiếm
    if (this.searchTerm) {
      const query = this.searchTerm;
      filtered = filtered.filter(order =>
        order.OrderID?.toString().toLowerCase().includes(query) ||
        order.OrderDate?.toLowerCase().includes(query) ||
        order.UserID?.toString().toLowerCase().includes(query) ||
        order.Status?.toLowerCase().includes(query)
      );
    }

    // Lọc theo trạng thái
    if (this.selectedStatusFilter && this.selectedStatusFilter !== 'All') {
      filtered = filtered.filter(order =>
        order.Status?.toLowerCase() === this.selectedStatusFilter.toLowerCase()
      );
    }

    return filtered;
  }

  selectAllOrders(event: any) {
    const isChecked = event.target.checked;
    this.orders = this.orders.map(order => ({ ...order, selected: isChecked }));
  }

  toggleOrderSelection(orderFirebaseId: string | undefined): void {
    if (!orderFirebaseId) {
      return;
    }
    this.orders = this.orders.map(order =>
      order.id === orderFirebaseId ? { ...order, selected: !order.selected } : order
    );
  }
}
