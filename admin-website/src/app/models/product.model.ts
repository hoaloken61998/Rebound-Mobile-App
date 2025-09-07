export interface ProductInterface {
    Product_ID: string;
    Product_Name: string;
    Product_Image: string;
    Product_Price: string;  // Nếu bạn muốn giá là chuỗi (vì có dấu "đ")
    Product_Description: string;
    Product_Category: string;
    Product_Status: string;
    Product_Origin: string;
    Product_Stock: number;
    Product_Customize?: string;
    selected?: boolean;  // Thêm thuộc tính tuỳ chỉnh (tùy chọn)
  }