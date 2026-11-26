import { http } from '../../http'

export interface CustomerData {
  name: string
  phone: string
  email: string
  notes: string
}

export interface Customer extends CustomerData {
  id: number
  ordersCount: number
  totalSpent: number
}

export const customersApi = {
  list: () => http<Customer[]>('/api/customers'),
  create: (data: CustomerData) => http<Customer>('/api/customers', 'POST', data),
  update: (id: number, data: CustomerData) => http<Customer>(`/api/customers/${id}`, 'PUT', data),
}
