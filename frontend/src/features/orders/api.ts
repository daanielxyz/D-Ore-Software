import { http } from '../../http'
import type { Channel } from '../channels'
import type { Unit } from '../inventory/api'

export type OrderStatus = 'PENDING' | 'IN_DESIGN' | 'READY' | 'ON_THE_WAY' | 'DELIVERED' | 'CANCELLED'

export const statusNames: Record<OrderStatus, string> = {
  PENDING: 'Pendiente',
  IN_DESIGN: 'En diseño',
  READY: 'Listo',
  ON_THE_WAY: 'En camino',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
}

export interface NewOrder {
  customerId: number
  occasion: string
  channel: Channel
  deliveryDate: string
  total: number | null
  notes: string
  items: { productId: number; quantity: number }[]
}

export interface Order {
  id: number
  code: string
  customerName: string
  occasion: string
  deliveryDate: string
  status: OrderStatus
  total: number
  notes: string
  stockDeducted: boolean
  items: { productId: number; productName: string; unit: Unit; quantity: number }[]
}

export const ordersApi = {
  list: () => http<Order[]>('/api/orders'),
  create: (order: NewOrder) => http<Order>('/api/orders', 'POST', order),
  changeStatus: (id: number, status: OrderStatus) =>
    http<Order>(`/api/orders/${id}/status`, 'PUT', { status }),
}
