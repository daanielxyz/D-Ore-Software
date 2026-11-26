import { http } from '../../http'
import type { Channel } from '../channels'

export interface NewSale {
  description: string
  amount: number | null
  channel: Channel
  saleDate: string
  customerId: number | null
  items: { productId: number; quantity: number }[]
}

export interface Sale {
  id: number
  description: string
  amount: number
  channel: Channel
  saleDate: string
  customerName: string | null
  orderId: number | null
}

export const salesApi = {
  list: () => http<Sale[]>('/api/sales'),
  register: (sale: NewSale) => http<void>('/api/sales', 'POST', sale),
}
