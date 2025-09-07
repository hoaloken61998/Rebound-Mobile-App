import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

interface Product {
  name: string;
  category: string;
  status: string;
  price: string;
  stock: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private productsSource = new BehaviorSubject<Product[]>([]);
  products$ = this.productsSource.asObservable();

  constructor() { }

  // Add a new product
  addProduct(product: Product) {
    const currentProducts = this.productsSource.value;
    this.productsSource.next([...currentProducts, product]); // Adds new product to the list
  }

  // Optional: Method to get all products
  getProducts() {
    return this.productsSource.asObservable();
  }

  // Optional: Method to delete a product (if needed)
  deleteProduct(productName: string) {
    const currentProducts = this.productsSource.value.filter(product => product.name !== productName);
    this.productsSource.next(currentProducts);
  }
}
