export type ExpenseCategory =
  | 'INVENTORY'
  | 'MATERIALS'
  | 'DELIVERY'
  | 'STAFF'
  | 'MARKETING'
  | 'OPERATIONS'

export const categoryNames: Record<ExpenseCategory, string> = {
  INVENTORY: 'Flor e inventario',
  MATERIALS: 'Materiales',
  DELIVERY: 'Domicilios',
  STAFF: 'Personal',
  MARKETING: 'Mercadeo',
  OPERATIONS: 'Operación',
}
