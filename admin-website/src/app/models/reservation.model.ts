export interface ReservationInterface {
    Reservation_ID: string;
    Customer_ID: string;
    Status: "Confirmed" | "Pending" | "Cancelled"; // Enum giống schema
    Appoinment_Time: string;
    Reservation_Location: string;
    Reservation_Note: string;
    Customer_Name: string;
    totalAmount?: number; // Dữ liệu có thể có hoặc không
    selected?: boolean; // Biến boolean để chọn khách hàng
  }
  