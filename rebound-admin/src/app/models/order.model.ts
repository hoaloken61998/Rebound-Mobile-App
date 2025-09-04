export interface OrderInterface {
    Order_ID: string;
    Order_Date: string;
    Order_Quality: string;
    Product_ID: string;  // Nếu bạn muốn giá là chuỗi (vì có dấu "đ")
    Customer_ID: string;
    Payment_ID: string;
    Order_Quantity: String;
    Payment_Method: string;
    Order_Total_Price: string;
    Delivery_Method: number;
    Order_Status: string;
    Order_Note: string;
    selected?: boolean;  // Thêm thuộc tính tuỳ chỉnh (tùy chọn)
  }