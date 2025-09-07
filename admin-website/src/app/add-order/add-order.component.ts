import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-order',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './add-order.component.html',
  styleUrl: './add-order.component.css'
})
export class AddOrderComponent {
  total = 0;

  addProduct() {
    const search = (document.getElementById("product-search") as HTMLInputElement).value;
    if (search) {
      const price = Math.floor(Math.random() * 1000) + 100;
      const table = document.getElementById("product-list") as HTMLTableElement;
      const row = `<tr>
                    <td>${search}</td>
                    <td><input type='number' value='1' min='1' (change)='updateTotal($event, ${price})'></td>
                    <td>${price}</td>
                    <td>${price}</td>
                    <td><button class='btn-remove' (click)='removeProduct($event)'>–</button></td>
                  </tr>`;
      table.innerHTML += row;
      this.calculateTotal();
      (document.getElementById("product-search") as HTMLInputElement).value = "";
    }
  }

  updateTotal(event: any, price: number) {
    const quantity = parseInt(event.target.value) || 1;
    event.target.parentElement.nextElementSibling.innerText = price * quantity;
    this.calculateTotal();
  }

  removeProduct(event: any) {
    const row = event.target.parentElement.parentElement;
    row.remove();
    this.calculateTotal();
  }

  calculateTotal() {
    this.total = Array.from(document.querySelectorAll("#product-list tr")).reduce((sum: number, row: any) => {
      return sum + parseInt(row.children[3].innerText);
    }, 0);
    document.getElementById("total")!.innerText = this.total.toString();
  }

  clearForm() {
    document.querySelectorAll("input, textarea").forEach((input: any) => input.value = "");
    document.getElementById("product-list")!.innerHTML = "";
    document.getElementById("total")!.innerText = "0";
  }

  confirmOrder() {
    alert("Order Confirmed!");
    this.clearForm();
  }

  goBack() {
    window.history.back();
  }
}

