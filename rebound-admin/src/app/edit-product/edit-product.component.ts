import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Location } from '@angular/common';
import { FormsModule } from '@angular/forms'; 

@Component({
  selector: 'app-edit-product',
  standalone: true,
  imports: [RouterModule,FormsModule],
  templateUrl: './edit-product.component.html',
  styleUrl: './edit-product.component.css'
})
export class EditProductComponent {
  product = {
    name: 'Sample Product',
    category: 'Jewelry',
    price: 1500,
    stock: 20,
    description: 'This is a sample product description.',
  };

  imagePreview: string = 'sample-product.jpg';  // Default image

  constructor(private location: Location) {}

  ngOnInit(): void {}

  // Preview the selected image
  previewImage(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  // Go back to the previous page
  goBack(): void {
    this.location.back();
  }

  // Save the product information (you can add more logic for saving here)
  saveProduct(): void {
    alert('Product details saved!');
    // Add your save logic (e.g., send data to the backend)
  }
}
