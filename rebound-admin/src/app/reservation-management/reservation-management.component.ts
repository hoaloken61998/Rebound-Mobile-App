// src/app/reservation-management/reservation-management.component.ts
import { Component, OnInit, OnDestroy, Inject, LOCALE_ID, signal } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Database, ref, onValue, off, update, remove, DataSnapshot } from '@angular/fire/database';

// Cập nhật giao diện ReservationInterface để khớp chính xác với Firebase BookingSchedule
export interface ReservationInterface {
  id?: string; // Firebase key (chẳng hạn như '0' trong ví dụ của bạn, hoặc ID tự động tạo)
  BookingID?: number; // Thay vì Reservation_ID
  BookingTime?: string; // Chuỗi ngày giờ từ Firebase, thay vì Appoinment_Time
  LocationID?: number;
  ServiceID?: number;
  Status?: 'Confirmed' | 'Cancelled' | 'Pending' | string; // Cho phép các giá trị khác hoặc là string
  UserID?: number; // Thay vì Customer_Name (bạn sẽ cần tìm tên khách hàng từ UserID nếu muốn hiển thị tên)
  selected?: boolean; // Để phục vụ chức năng chọn/bỏ chọn
  // Nếu bạn muốn hiển thị Customer_Name hoặc Service_Type, bạn sẽ cần lấy dữ liệu từ các node khác (User, Service)
  // hoặc đảm bảo dữ liệu này được nhúng vào BookingSchedule trên Firebase.
  // Nếu không, bạn chỉ có thể hiển thị UserID và ServiceID.
}

@Component({
  selector: 'app-reservation-management', // Selector đúng
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './reservation-management.component.html',
  styleUrls: ['./reservation-management.component.css']
})
export class ReservationManagementComponent implements OnInit, OnDestroy { // Đảm bảo tên class là ReservationManagementComponent
  reservations = signal<ReservationInterface[]>([]);
  searchQuery = signal('');

  constructor(
    @Inject(LOCALE_ID) private locale: string,
    private router: Router,
    private db: Database
  ) { }

  ngOnInit(): void {
    const reservationsRef = ref(this.db, 'BookingSchedule');

    onValue(reservationsRef, (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      const loadedReservations: ReservationInterface[] = [];
      if (data) {
        // Firebase Realtime Database có thể trả về mảng nếu các key là số liên tiếp
        // hoặc object nếu các key là string (ID ngẫu nhiên)
        // Chúng ta xử lý cả hai trường hợp bằng cách lặp qua các keys
        Object.keys(data).forEach(key => {
          loadedReservations.push({
            id: key, // Lưu key từ Firebase làm ID
            ...data[key],
            selected: false
          });
        });
      }
      this.reservations.set(loadedReservations);
      console.log('Reservations loaded from Firebase:', this.reservations());
    }, (error) => {
      console.error('Lỗi khi tải đặt chỗ từ Firebase:', error);
    });
  }

  ngOnDestroy(): void {
    // off(ref(this.db, 'BookingSchedule')); // Hủy lắng nghe khi component bị hủy
  }

  formatDate(dateString: string | undefined): string { // Chuyển từ Date sang string | undefined
    if (!dateString) return 'N/A';
    try {
      // Firebase lưu BookingTime là "YYYY-MM-DD HH:mm:ss"
      // Cần tạo đối tượng Date từ chuỗi này
      const date = new Date(dateString.replace(' ', 'T')); // Thay ' ' bằng 'T' để Date parser hiểu đúng định dạng ISO
      if (isNaN(date.getTime())) { // Kiểm tra ngày hợp lệ
          return 'Invalid Date';
      }
      return formatDate(date, 'medium', this.locale); // 'medium' sẽ hiển thị cả ngày và giờ
    } catch (e) {
      console.error('Lỗi định dạng ngày:', dateString, e);
      return 'Invalid Date';
    }
  }


  updateSearchQuery(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.searchQuery.set(inputElement.value.trim().toLowerCase());
  }

  get filteredReservations(): ReservationInterface[] {
    const query = this.searchQuery();
    if (!query) {
      return this.reservations();
    }
    return this.reservations().filter(reservation =>
      reservation.id?.toLowerCase().includes(query) ||
      reservation.BookingID?.toString().includes(query) || // Tìm kiếm theo BookingID
      reservation.Status?.toLowerCase().includes(query) ||
      reservation.BookingTime?.toLowerCase().includes(query) || // Tìm kiếm theo BookingTime
      reservation.UserID?.toString().includes(query) // Tìm kiếm theo UserID
      // Thêm các trường khác nếu bạn muốn
    );
  }

  async toggleStatus(reservation: ReservationInterface): Promise<void> {
    if (!reservation.id) {
      console.error('Reservation ID is undefined, cannot update status.');
      return;
    }

    // Đảo ngược trạng thái
    let newStatus: string;
    if (reservation.Status === 'Confirmed') {
        newStatus = 'Cancelled';
    } else if (reservation.Status === 'Cancelled') {
        newStatus = 'Confirmed';
    } else {
        newStatus = 'Confirmed'; // Mặc định chuyển sang Confirmed nếu trạng thái khác
    }

    const reservationRef = ref(this.db, `BookingSchedule/${reservation.id}`);

    try {
      await update(reservationRef, { Status: newStatus });
      console.log(`Reservation ${reservation.id} status updated to ${newStatus} in Firebase.`);
      // Signal sẽ được cập nhật tự động bởi onValue listener
    } catch (error) {
      console.error('Lỗi khi cập nhật trạng thái đặt chỗ trong Firebase:', error);
      alert('Đã xảy ra lỗi khi cập nhật trạng thái đặt chỗ.');
    }
  }

  editReservation(reservation: ReservationInterface): void {
    if (reservation.id) {
      this.router.navigate(['/reservation-detail', reservation.id]);
    } else {
      console.warn('Reservation ID is undefined, cannot edit.');
    }
  }

  selectAllReservations(event: any): void {
    const isChecked = event.target.checked;
    this.reservations.set(this.reservations().map(reservation => ({ ...reservation, selected: isChecked })));
  }

  toggleReservationSelection(reservation: ReservationInterface): void {
    if (reservation && reservation.id) {
      this.reservations.set(this.reservations().map(r =>
        r.id === reservation.id ? { ...r, selected: !r.selected } : r
      ));
    }
  }

  deleteReservation(reservationId: string | undefined): void {
    if (!reservationId) {
      console.error('Reservation ID is undefined, cannot delete.');
      return;
    }
    if (confirm('Bạn có chắc muốn xóa đặt chỗ này không?')) {
      remove(ref(this.db, 'BookingSchedule/' + reservationId))
        .then(() => {
          console.log('Đặt chỗ đã được xóa thành công từ Firebase:', reservationId);
        })
        .catch((error) => {
          console.error('Lỗi khi xóa đặt chỗ khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa đặt chỗ.');
        });
    }
  }

  deleteSelectedReservations(): void {
    const selectedReservationIds = this.reservations()
      .filter(reservation => reservation.selected && reservation.id)
      .map(reservation => reservation.id as string);

    if (selectedReservationIds.length === 0) {
      alert('Vui lòng chọn ít nhất một đặt chỗ để xóa.');
      return;
    }

    if (confirm(`Bạn có chắc muốn xóa ${selectedReservationIds.length} đặt chỗ đã chọn không?`)) {
      const deletePromises = selectedReservationIds.map(reservationId =>
        remove(ref(this.db, 'BookingSchedule/' + reservationId))
      );

      Promise.all(deletePromises)
        .then(() => {
          console.log('Các đặt chỗ đã chọn đã được xóa thành công từ Firebase.');
        })
        .catch((error) => {
          console.error('Lỗi khi xóa các đặt chỗ đã chọn khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa các đặt chỗ đã chọn.');
        });
    }
  }
}
