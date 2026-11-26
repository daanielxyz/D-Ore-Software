import type { Product } from '../features/inventory/api'
import { unitNames } from '../features/inventory/format'
import { emptyLine, type Line } from './lines'

interface Props {
  products: Product[]
  lines: Line[]
  onChange: (lines: Line[]) => void
  /** Si es verdadero, no se puede dejar la lista vacía. */
  required?: boolean
}

export function ProductLines({ products, lines, onChange, required = false }: Props) {
  const set = (key: number, field: 'productId' | 'quantity', value: string) =>
    onChange(lines.map((l) => (l.key === key ? { ...l, [field]: value } : l)))

  return (
    <fieldset className="wide">
      <legend>Flores y materiales</legend>
      {lines.map((line) => (
        <div className="line" key={line.key}>
          <select required aria-label="Producto" value={line.productId} onChange={(e) => set(line.key, 'productId', e.target.value)}>
            <option value="">Elige un producto…</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} ({p.stock - p.expiredStock} {unitNames[p.unit]})
              </option>
            ))}
          </select>
          <input type="number" required min="1" step="1" aria-label="Cantidad" value={line.quantity} onChange={(e) => set(line.key, 'quantity', e.target.value)} />
          <button type="button" aria-label="Quitar línea" disabled={required && lines.length === 1} onClick={() => onChange(lines.filter((l) => l.key !== line.key))}>
            ✕
          </button>
        </div>
      ))}
      <button type="button" onClick={() => onChange([...lines, emptyLine(Math.max(-1, ...lines.map((l) => l.key)) + 1)])}>
        + Agregar producto
      </button>
    </fieldset>
  )
}
