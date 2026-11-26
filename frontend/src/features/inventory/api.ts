export type Unit = 'STEM' | 'BUNCH' | 'ROLL' | 'PACKAGE' | 'UNIT'
export type MovementType = 'IN' | 'OUT' | 'WASTE' | 'ADJUSTMENT'

export interface ProductData {
  name: string
  category: string
  unit: Unit
  sku: string
  minStock: number
  unitCost: number
  unitPrice: number
  supplier: string
}

export interface NewProduct extends ProductData {
  initialStock: number
  initialExpiresOn: string | null
}

export interface Product extends ProductData {
  id: number
  stock: number
  active: boolean
  lowStock: boolean
  updatedAt: string
  expiredStock: number
  nextExpiry: string | null
}

export interface NewMovement {
  type: MovementType
  quantity: number
  reason: string
  expiresOn: string | null
  lotId: number | null
}

export interface Movement {
  id: number
  type: MovementType
  delta: number
  stockAfter: number
  reason: string
  source: 'MANUAL' | 'ASSISTANT'
  createdAt: string
}

export interface Lot {
  id: number
  quantity: number
  expiresOn: string | null
  expired: boolean
}

async function request<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  let response: Response
  try {
    response = await fetch(`/api/products${path}`, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new Error('No hay conexión con el servidor.')
  }
  if (!response.ok) {
    // El backend explica las reglas de negocio en "detail" (404 y 409).
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null
    const explained = response.status === 404 || response.status === 409
    throw new Error(
      explained && problem?.detail ? problem.detail : 'No se pudo completar. Revisa los datos.',
    )
  }
  return (response.status === 204 ? undefined : await response.json()) as T
}

export const inventoryApi = {
  list: (includeInactive: boolean) => request<Product[]>(`?includeInactive=${includeInactive}`),
  create: (product: NewProduct) => request<Product>('', 'POST', product),
  update: (id: number, data: ProductData) => request<Product>(`/${id}`, 'PUT', data),
  deactivate: (id: number) => request<void>(`/${id}`, 'DELETE'),
  movements: (id: number) => request<Movement[]>(`/${id}/movements`),
  record: (id: number, movement: NewMovement) =>
    request<Movement>(`/${id}/movements`, 'POST', movement),
  lots: (id: number) => request<Lot[]>(`/${id}/lots`),
}
