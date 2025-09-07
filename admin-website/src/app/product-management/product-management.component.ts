// src/app/product-management/product-management.component.ts
import { Component, OnInit, OnDestroy, Inject, LOCALE_ID } from '@angular/core';
import { CommonModule, formatCurrency } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Database, ref, onValue, off, remove, DataSnapshot } from '@angular/fire/database';

// Cập nhật giao diện cho Product để khớp chính xác với Firebase Product node
export interface ProductInterface {
  id?: string; // ID từ Firebase Realtime Database key (ví dụ: '0' hoặc ID ngẫu nhiên)
  CategoryID?: number;
  ImageLink?: string; // URL của hình ảnh
  ProductDescription?: string;
  ProductID?: number; // ID nội bộ của sản phẩm
  ProductName?: string;
  ProductPrice?: string; // CHUỖI CÓ DẤU CHẤM, cần xử lý khi định dạng
  ProductStockQuantity?: number;
  StatusID?: number; // Trạng thái sản phẩm (ví dụ: 1 = Active, 2 = Inactive)
  selected?: boolean; // Để phục vụ chức năng chọn/bỏ chọn
}

// Giao diện cho ProductStatus từ Firebase
export interface ProductStatus {
  StatusID?: number;
  StatusName?: string;
}

// Giao diện cho Category từ Firebase
export interface Category {
  CategoryID?: number;
  CategoryName?: string;
}

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-management.component.html',
  styleUrls: ['./product-management.component.css']
})
export class ProductManagementComponent implements OnInit, OnDestroy {
  products: ProductInterface[] = [];
  searchTerm: string = '';
  selectedCategoryFilter: string = 'All'; // 'All' hoặc tên Category
  selectedStatusFilter: string = 'All'; // 'All' hoặc tên Status

  productStatuses: ProductStatus[] = []; // Dữ liệu trạng thái từ Firebase
  categories: Category[] = []; // Dữ liệu danh mục từ Firebase

  private productsListenerCleanup: (() => void) | undefined;
  private statusListenerCleanup: (() => void) | undefined;
  private categoryListenerCleanup: (() => void) | undefined;


  constructor(private router: Router, private db: Database, @Inject(LOCALE_ID) private locale: string) {}

  ngOnInit(): void {
    // Lắng nghe dữ liệu sản phẩm
    const productsRef = ref(this.db, 'Product');
    this.productsListenerCleanup = onValue(productsRef, (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      const loadedProducts: ProductInterface[] = [];
      if (data) {
        Object.keys(data).forEach(key => {
          loadedProducts.push({
            id: key,
            ...data[key],
            selected: false
          });
        });
      }
      this.products = loadedProducts;
      console.log('Products loaded from Firebase:', this.products);
    }, (error) => {
      console.error('Lỗi khi tải sản phẩm từ Firebase:', error);
    });

    // Lắng nghe dữ liệu trạng thái sản phẩm từ node "ProductStatus"
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
      console.log('Product Statuses loaded from Firebase:', this.productStatuses);
    }, (error) => {
      console.error('Lỗi khi tải trạng thái sản phẩm từ Firebase:', error);
    });

    // Lắng nghe dữ liệu danh mục từ node "Category"
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
      console.log('Categories loaded from Firebase:', this.categories);
    }, (error) => {
      console.error('Lỗi khi tải danh mục từ Firebase:', error);
    });
  }

  ngOnDestroy(): void {
    if (this.productsListenerCleanup) {
      this.productsListenerCleanup();
    }
    if (this.statusListenerCleanup) {
      this.statusListenerCleanup();
    }
    if (this.categoryListenerCleanup) {
      this.categoryListenerCleanup();
    }
  }

  // --- CẬP NHẬT PHƯƠNG THỨC formatPrice ---
  formatPrice(price: string | undefined): string {
    if (price === undefined) {
      return 'N/A';
    }
    const numericPrice = parseFloat(price.replace(/\./g, '').replace(',', '.'));

    if (isNaN(numericPrice)) {
      return 'N/A';
    }
    return formatCurrency(numericPrice, this.locale, '₫', 'VND', '1.0-0');
  }

  updateSearchQuery(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.searchTerm = inputElement.value.trim().toLowerCase();
  }

  get filteredProducts(): ProductInterface[] {
    let filtered = this.products;

    if (this.searchTerm) {
      const query = this.searchTerm;
      filtered = filtered.filter(product =>
        product.ProductName?.toLowerCase().includes(query) ||
        product.ProductDescription?.toLowerCase().includes(query) ||
        product.ProductID?.toString().toLowerCase().includes(query) ||
        this.getProductCategoryName(product.CategoryID).toLowerCase().includes(query) ||
        this.getProductStatusName(product.StatusID).toLowerCase().includes(query)
      );
    }

    if (this.selectedCategoryFilter && this.selectedCategoryFilter !== 'All') {
      filtered = filtered.filter(product =>
        this.getProductCategoryName(product.CategoryID).toLowerCase() === this.selectedCategoryFilter.toLowerCase()
      );
    }

    if (this.selectedStatusFilter && this.selectedStatusFilter !== 'All') {
      filtered = filtered.filter(product =>
        this.getProductStatusName(product.StatusID).toLowerCase() === this.selectedStatusFilter.toLowerCase()
      );
    }

    return filtered;
  }

  selectAllProducts(event: any): void {
    const isChecked = event.target.checked;
    this.products = this.products.map(product => ({ ...product, selected: isChecked }));
  }

  toggleProductSelection(product: ProductInterface): void {
    if (product && product.id) {
      this.products = this.products.map(p =>
        p.id === product.id ? { ...p, selected: !p.selected } : p
      );
    }
  }

  deleteProduct(productFirebaseId: string | undefined): void {
    if (!productFirebaseId) {
      console.error('Product Firebase ID is undefined, cannot delete.');
      return;
    }
    if (confirm('Bạn có chắc muốn xóa sản phẩm này không?')) {
      remove(ref(this.db, 'Product/' + productFirebaseId))
        .then(() => {
          console.log('Sản phẩm đã được xóa thành công từ Firebase:', productFirebaseId);
        })
        .catch((error) => {
          console.error('Lỗi khi xóa sản phẩm khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa sản phẩm.');
        });
    }
  }

  deleteSelectedProducts(): void {
    const selectedProductFirebaseIds = this.products
      .filter(product => product.selected && product.id)
      .map(product => product.id as string);

    if (selectedProductFirebaseIds.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm để xóa.');
      return;
    }

    if (confirm(`Bạn có chắc muốn xóa ${selectedProductFirebaseIds.length} sản phẩm đã chọn không?`)) {
      const deletePromises = selectedProductFirebaseIds.map(productFirebaseId =>
        remove(ref(this.db, 'Product/' + productFirebaseId))
      );

      Promise.all(deletePromises)
        .then(() => {
          console.log('Các sản phẩm đã chọn đã được xóa thành công từ Firebase.');
        })
        .catch((error) => {
          console.error('Lỗi khi xóa các sản phẩm đã chọn khỏi Firebase:', error);
          alert('Đã xảy ra lỗi khi xóa các sản phẩm đã chọn.');
        });
    }
  }

  editProduct(productFirebaseId: string | undefined): void {
    if (productFirebaseId) {
      this.router.navigate(['/edit-product', productFirebaseId]);
    } else {
      console.warn('Product ID is undefined, cannot edit.');
    }
  }

  // Phương thức trợ giúp để chuyển đổi StatusID thành tên trạng thái
  getProductStatusName(statusId: number | undefined): string {
    const status = this.productStatuses.find(s => s.StatusID === statusId);
    return status ? status.StatusName || 'Unknown' : 'N/A';
  }

  // Phương thức trợ giúp để chuyển đổi CategoryID thành tên Category
  getProductCategoryName(categoryId: number | undefined): string {
    const category = this.categories.find(c => c.CategoryID === categoryId);
    return category ? category.CategoryName || 'Unknown' : 'N/A';
  }
}
