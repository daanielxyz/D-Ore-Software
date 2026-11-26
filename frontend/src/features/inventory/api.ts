export type Unit = 'STEM' | 'BUNCH' | 'ROLL' | 'PACKAGE' | 'UNIT'

export interface Product {
  id: number
  sku: string
  name: string
  category: string
  unit: Unit
  stock: number
  minStock: number
  unitCost: number
  unitPrice: number
  supplier: string
  active: boolean
  lowStock: boolean
  updatedAt: string
}

export async function listProducts(signal?: AbortSignal): Promise<Product[]> {
  const response = await fetch('/api/products', { signal })
  if (!response.ok) throw new Error('No se pudo cargar el inventario.')
  return (await response.json()) as Product[]
}
