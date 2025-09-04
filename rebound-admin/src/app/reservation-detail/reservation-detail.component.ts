// src/app/reservation-detail/reservation-detail.component.ts
import { Component, OnInit, OnDestroy, Inject, LOCALE_ID } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { Database, ref, get, DataSnapshot } from '@angular/fire/database';

// Interfaces to match Firebase structure
// Interface for main reservation data from "BookingSchedule" node
export interface BookingScheduleData {
  id?: string; // Firebase key
  BookingID?: number;
  BookingTime?: string; // "YYYY-MM-DD HH:mm:ss"
  LocationID?: number; // Corresponds to BranchID in Branch node
  ServiceID?: number;
  Status?: string;
  UserID?: number;
  SpecialRequests?: string;
}

// Interface for user data from "User" node
export interface UserData {
  id?: string; // Firebase key (UID)
  UserID?: number; // Internal ID
  FullName?: string;
  PhoneNumber?: string;
  Email?: string;
  DateOfBirth?: string; // "YYYY-MM-DD"
}

// Interface for service data from "Service" node
export interface ServiceData {
  id?: string; // Firebase key
  ServiceID?: number;
  ServiceType?: string; // Changed from Service_Name to ServiceType
}

// Interface for branch data from "Branch" node
export interface BranchData { // Changed from LocationData to BranchData
  id?: string; // Firebase key
  BranchID?: number; // Corresponds to LocationID in BookingSchedule
  District?: string; // Used to build location name
  Street?: string; // Used to build location name
  // ... other fields if needed, like Details
}

@Component({
  selector: 'reservation-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reservation-detail.component.html',
  styleUrls: ['./reservation-detail.component.css']
})
export class ReservationDetailComponent implements OnInit, OnDestroy {
  reservationData: BookingScheduleData = {};
  customerData: UserData = {};
  serviceData: ServiceData = {};
  branchData: BranchData = {}; // Changed from locationData to branchData
  isLoading: boolean = true;
  private reservationFirebaseId: string | null = null;
  private allUsers: UserData[] = [];
  private allServices: ServiceData[] = [];
  private allBranches: BranchData[] = []; // Changed from allLocations to allBranches

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private db: Database,
    @Inject(LOCALE_ID) private locale: string
  ) {}

  ngOnInit(): void {
    this.reservationFirebaseId = this.route.snapshot.paramMap.get('id');

    if (this.reservationFirebaseId) {
      this.loadAllReferenceData().then(() => {
        this.loadReservationData(this.reservationFirebaseId!);
      }).catch(error => {
        console.error('Error loading reference data:', error);
        this.isLoading = false;
      });
    } else {
      console.warn('Reservation ID not provided in URL.');
      this.isLoading = false;
    }
  }

  ngOnDestroy(): void {
    // No specific cleanup needed for Firebase auth listeners in this setup
  }

  private async loadAllReferenceData(): Promise<void> {
    try {
      const userSnapshot = await get(ref(this.db, 'User'));
      if (userSnapshot.exists()) {
        this.allUsers = Object.values(userSnapshot.val());
      }

      const serviceSnapshot = await get(ref(this.db, 'Service'));
      if (serviceSnapshot.exists()) {
        this.allServices = Object.values(serviceSnapshot.val());
      }

      const branchSnapshot = await get(ref(this.db, 'Branch')); // Changed from 'Location' to 'Branch'
      if (branchSnapshot.exists()) {
        this.allBranches = Object.values(branchSnapshot.val()); // Changed from allLocations to allBranches
      }
      console.log('Reference data loaded:', { users: this.allUsers, services: this.allServices, branches: this.allBranches });
    } catch (error) {
      console.error('Failed to load reference data:', error);
      throw error;
    }
  }

  private async loadReservationData(id: string): Promise<void> {
    this.isLoading = true;
    try {
      const snapshot = await get(ref(this.db, 'BookingSchedule/' + id));
      if (snapshot.exists()) {
        this.reservationData = { id: snapshot.key, ...snapshot.val() };
        console.log('BookingSchedule loaded:', this.reservationData);

        if (this.reservationData.UserID !== undefined) {
          this.customerData = this.allUsers.find(u => u.UserID === this.reservationData.UserID) || {};
          console.log('Customer found:', this.customerData);
        }

        if (this.reservationData.ServiceID !== undefined) {
          this.serviceData = this.allServices.find(s => s.ServiceID === this.reservationData.ServiceID) || {};
          console.log('Service found:', this.serviceData);
        }

        // Changed from LocationID to BranchID and used allBranches
        if (this.reservationData.LocationID !== undefined) {
          this.branchData = this.allBranches.find(b => b.BranchID === this.reservationData.LocationID) || {};
          console.log('Branch found:', this.branchData);
        }

      } else {
        console.warn('No reservation found with ID:', id);
        this.reservationData = {};
      }
    } catch (error) {
      console.error('Error loading reservation data:', error);
      this.reservationData = {};
    } finally {
      this.isLoading = false;
    }
  }

  formatBookingTime(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const isoString = dateString.replace(' ', 'T');
      const date = new Date(isoString);
      if (isNaN(date.getTime())) {
        throw new Error('Invalid date');
      }
      return formatDate(date, 'yyyy-MM-dd HH:mm', this.locale);
    } catch (e) {
      console.error('Error formatting date/time:', dateString, e);
      return 'Invalid Date';
    }
  }

  formatDateOfBirth(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        throw new Error('Invalid date');
      }
      return formatDate(date, 'mediumDate', this.locale);
    } catch (e) {
      console.error('Error formatting date of birth:', dateString, e);
      return 'Invalid Date';
    }
  }

  // New helper to construct branch name
  getBranchName(branch: BranchData | undefined): string {
    if (!branch) return 'N/A';
    const street = branch.Street || '';
    const district = branch.District || '';
    // Combine Street and District, add a comma if both exist
    if (street && district) {
      return `${street}, ${district}`;
    }
    return street || district || 'N/A'; // Return whichever exists, or 'N/A'
  }

  goBack(): void {
    this.location.back();
  }
}
