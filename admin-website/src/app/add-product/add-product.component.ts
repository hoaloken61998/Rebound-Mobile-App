// src/app/add-product/add-product.component.ts
import { Component, OnInit, OnDestroy, ViewEncapsulation, Inject, LOCALE_ID } from '@angular/core';
import { CommonModule, formatCurrency } from '@angular/common';
import { Location } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Quan trọng: Thêm FormsModule
import { Database, ref, push, set, onValue, off, get, DataSnapshot } from '@angular/fire/database'; // Import Firebase modules

// Interfaces để khớp với dữ liệu Firebase Product, Category, ProductStatus
export interface NewProductInterface {
  CategoryID?: number;
  ImageLink?: string; // URL của hình ảnh
  ProductDescription?: string;
  ProductID?: number; // ID nội bộ, sẽ được tạo tự động
  ProductName?: string;
  ProductPrice?: string; // Giữ là CHUỖI CÓ DẤM CHẤM như Firebase
  ProductStockQuantity?: number;
  StatusID?: number; // Trạng thái sản phẩm
  // Thêm các trường khác nếu có (ví dụ: Origin, Customize)
  Origin?: string; // Thêm Origin nếu có
  Customize?: string; // Thêm Customize nếu có
}

export interface Category {
  CategoryID?: number;
  CategoryName?: string;
}

export interface ProductStatus {
  StatusID?: number;
  StatusName?: string;
}

@Component({
  selector: 'app-add-product', // Đổi từ 'add-product' thành 'app-add-product' cho nhất quán
  standalone: true,
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.css'],
  imports: [CommonModule, FormsModule], // Thêm FormsModule
  encapsulation: ViewEncapsulation.None,
})
export class AddProductComponent implements OnInit, OnDestroy {
  // Khởi tạo đối tượng sản phẩm mới với các giá trị mặc định/rỗng
  newProduct: NewProductInterface = {
    ProductName: '',
    ImageLink: 'https://placehold.co/150x150/eeeeee/black?text=Product+Image', // Placeholder mặc định
    CategoryID: undefined, // undefined để người dùng chọn
    StatusID: undefined, // undefined để người dùng chọn
    ProductDescription: '',
    ProductPrice: '',
    ProductStockQuantity: undefined,
    Origin: '',
    Customize: ''
  };

  categories: Category[] = [];
  productStatuses: ProductStatus[] = [];
  
  // Để quản lý lắng nghe Firebase
  private categoryListenerCleanup: (() => void) | undefined;
  private statusListenerCleanup: (() => void) | undefined;

  constructor(
    private location: Location,
    private db: Database, // Inject Firebase Database
    @Inject(LOCALE_ID) private locale: string // Inject LOCALE_ID cho formatCurrency
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProductStatuses();
  }

  ngOnDestroy(): void {
    if (this.categoryListenerCleanup) {
      this.categoryListenerCleanup();
    }
    if (this.statusListenerCleanup) {
      this.statusListenerCleanup();
    }
  }

  // Tải danh sách Categories từ Firebase
  private loadCategories(): void {
    const categoryRef = ref(this.db, 'Category');
    this.categoryListenerCleanup = onValue(categoryRef, (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      const loadedCategories: Category[] = [];
      if (data) {
        Object.keys(data).forEach(key => {
          if (data[key] && data[key].CategoryID && data[key].CategoryName) {
            loadedCategories.push(data[key]);
          }
        });
      }
      this.categories = loadedCategories;
      // Chọn category đầu tiên làm mặc định nếu có và chưa được chọn
      if (this.categories.length > 0 && this.newProduct.CategoryID === undefined) {
        this.newProduct.CategoryID = this.categories[0].CategoryID;
      }
      console.log('Categories loaded:', this.categories);
    }, (error) => {
      console.error('Error loading categories:', error);
    });
  }

  // Tải danh sách Product Statuses từ Firebase
  private loadProductStatuses(): void {
    const productStatusRef = ref(this.db, 'ProductStatus');
    this.statusListenerCleanup = onValue(productStatusRef, (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      const loadedStatuses: ProductStatus[] = [];
      if (data) {
        Object.keys(data).forEach(key => {
          if (data[key] && data[key].StatusID && data[key].StatusName) {
            loadedStatuses.push(data[key]);
          }
        });
      }
      this.productStatuses = loadedStatuses;
      // Chọn status đầu tiên làm mặc định nếu có và chưa được chọn
      if (this.productStatuses.length > 0 && this.newProduct.StatusID === undefined) {
        this.newProduct.StatusID = this.productStatuses[0].StatusID;
      }
      console.log('Product Statuses loaded:', this.productStatuses);
    }, (error) => {
      console.error('Error loading product statuses:', error);
    });
  }


  // Xử lý khi chọn file hình ảnh (chỉ lấy URL tạm thời)
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      // Đây chỉ là cách hiển thị ảnh cục bộ tạm thời.
      // Để tải lên Firebase Storage hoặc dịch vụ khác, bạn cần tích hợp API.
      const reader = new FileReader();
      reader.onload = () => {
        this.newProduct.ImageLink = reader.result as string;
      };
      reader.readAsDataURL(file);
      console.log('Selected file:', file.name);
      alert('Tải ảnh lên Firebase Storage cần tích hợp thêm API.');
    }
  }

  // Định dạng giá khi người dùng nhập
  formatPriceInput(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input) {
      // Loại bỏ tất cả ký tự không phải số và dấu chấm, chỉ giữ lại số
      let value = input.value.replace(/\D/g, "");
      // Định dạng lại thành chuỗi có dấu chấm phân cách hàng nghìn (ví dụ: 4.500.000)
      input.value = new Intl.NumberFormat('vi-VN').format(Number(value));
      // Lưu giá trị SỐ CHUỖI VÀO MODEL (không có dấu chấm)
      this.newProduct.ProductPrice = value;
    }
  }

  // Phương thức để định dạng giá trị từ model cho hiển thị (nếu cần)
  displayFormattedPrice(price: string | undefined): string {
    if (price === undefined) {
      return '0 ₫';
    }
    const numericPrice = parseFloat(price.replace(/\./g, '').replace(',', '.'));
    if (isNaN(numericPrice)) {
      return '0 ₫';
    }
    return formatCurrency(numericPrice, this.locale, '₫', 'VND', '1.0-0');
  }

  async onSubmit(event: Event): Promise<void> {
    event.preventDefault(); // Ngăn chặn hành vi submit mặc định của form

    // Kiểm tra các trường bắt buộc
    if (!this.newProduct.ProductName || !this.newProduct.ProductPrice || this.newProduct.ProductStockQuantity === undefined || this.newProduct.CategoryID === undefined || this.newProduct.StatusID === undefined) {
      alert('Vui lòng điền đầy đủ các thông tin bắt buộc: Product Name, Price, Stock, Category, Status.');
      return;
    }

    // Chuyển đổi ProductPrice từ chuỗi số đã được làm sạch sang định dạng Firebase mong muốn
    // Firebase của bạn lưu "4.500.000" (dùng dấu chấm cho hàng ngàn)
    // new Intl.NumberFormat('vi-VN').format() sẽ tạo ra định dạng này.
    const priceToSave = new Intl.NumberFormat('vi-VN').format(parseFloat(this.newProduct.ProductPrice || '0'));
    this.newProduct.ProductPrice = priceToSave; // Cập nhật lại ProductPrice với định dạng có dấu chấm

    try {
      // Tìm ProductID lớn nhất hiện có và tăng lên 1
      const productSnapshot = await get(ref(this.db, 'Product'));
      let maxProductId = 0;
      if (productSnapshot.exists()) {
        productSnapshot.forEach(childSnapshot => {
          const productData = childSnapshot.val();
          if (productData.ProductID && productData.ProductID > maxProductId) {
            maxProductId = productData.ProductID;
          }
        });
      }
      this.newProduct.ProductID = maxProductId + 1;

      // Tạo một key mới cho sản phẩm trong Firebase
      const newProductRef = push(ref(this.db, 'Product'));
      // Lưu sản phẩm vào Firebase Realtime Database
      await set(newProductRef, this.newProduct);

      alert('Sản phẩm đã được thêm thành công!');
      this.goBack(); // Quay lại trang quản lý sản phẩm
    } catch (error) {
      console.error('Lỗi khi thêm sản phẩm:', error);
      alert('Đã xảy ra lỗi khi thêm sản phẩm.');
    }
  }

  // Đặt lại form về trạng thái ban đầu
  clearForm(): void {
    this.newProduct = {
      ProductName: '',
      ImageLink: 'https://placehold.co/150x150/eeeeee/black?text=Product+Image',
      CategoryID: this.categories.length > 0 ? this.categories[0].CategoryID : undefined, // Đặt lại về mặc định
      StatusID: this.productStatuses.length > 0 ? this.productStatuses[0].StatusID : undefined, // Đặt lại về mặc định
      ProductDescription: '',
      ProductPrice: '',
      ProductStockQuantity: undefined,
      Origin: '',
      Customize: ''
    };
  }

  goBack(): void {
    this.location.back();
  }
}
