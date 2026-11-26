import type { Product } from '../features/inventory/api'

export interface Line {
  key: number
  productId: string
  quantity: string
}

export const emptyLine = (key: number): Line => ({ key, productId: '', quantity: '1' })

export const toItems = (lines: Line[]) =>
  lines.map((l) => ({ productId: Number(l.productId), quantity: Number(l.quantity) }))

export const suggestedTotal = (lines: Line[], products: Product[]) =>
  lines.reduce((sum, line) => {
    const product = products.find((p) => p.id === Number(line.productId))
    return sum + (product?.unitPrice ?? 0) * Number(line.quantity)
  }, 0)
