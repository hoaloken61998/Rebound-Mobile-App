import { Component, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'add-reservation',
  standalone: true,
  imports:[FormsModule],
  templateUrl: './add-reservation.component.html',
  styleUrls: ['./add-reservation.component.css'],
  encapsulation: ViewEncapsulation.None // Cho CSS global
})
export class AddReservationComponent {
  reservation = {
    customerName: '',
    phoneNumber: '',
    email: '',
    dob: '',
    appointmentDate: '',
    serviceType: 'Piercing Service',
    reservationLocation: 'Rebound Cao Ba Quat - Ha Noi',
    jewelryType: 'Ear Piercing Jewelry',
    specialRequests: ''
  };

  clear(): void {
    this.reservation = {
      customerName: '',
      phoneNumber: '',
      email: '',
      dob: '',
      appointmentDate: '',
      serviceType: 'Piercing Service',
      reservationLocation: 'Rebound Cao Ba Quat - Ha Noi',
      jewelryType: 'Ear Piercing Jewelry',
      specialRequests: ''
    };
  }

  goBack() {
    window.history.back(); 
  }

  setReservation() {
    alert("Reservation Successful!");
  }
 }