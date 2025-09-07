export interface CustomerInterface {
  UserID: number;
  FullName: string;
  Email: string;
  PhoneNumber: string;
  DateOfBirth: string;
  Sex: "Male" | "Female" | "Other";
  RegistrationDate: string;
  UserRanking: "Bạc" | "Vàng" | "Bạch Kim" | "Kim Cương";
  totalOrders?: number;
  selected?: boolean;
}