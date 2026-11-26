import { useState } from 'react'
import { useSubmit } from '../../components/useSubmit'
import { inventoryApi, type Product, type Unit } from './api'
import { today, unitNames } from './format'

interface Props {
  product: Product | null
  onDone: (message: string) => void
}

export function ProductForm({ product, onDone }: Props) {
  const [form, setForm] = useState({
    name: product?.name ?? '',
    category: product?.category ?? 'Flores',
    unit: product?.unit ?? ('STEM' as Unit),
    sku: product?.sku ?? '',
    supplier: product?.supplier ?? '',
    minStock: String(product?.minStock ?? 0),
    unitCost: String(product?.unitCost ?? 0),
    unitPrice: String(product?.unitPrice ?? 0),
    initialStock: '0',
    initialExpiresOn: '',
  })

  const set = (key: keyof typeof form) => (event: { target: { value: string } }) =>
    setForm((current) => ({ ...current, [key]: event.target.value }))

  const { saving, error, submit } = useSubmit(
    () => {
      const data = {
        name: form.name,
        category: form.category,
        unit: form.unit,
        sku: form.sku,
        supplier: form.supplier,
        minStock: Number(form.minStock),
        unitCost: Number(form.unitCost),
        unitPrice: Number(form.unitPrice),
      }
      return product
        ? inventoryApi.update(product.id, data)
        : inventoryApi.create({
            ...data,
            initialStock: Number(form.initialStock),
            initialExpiresOn: form.initialExpiresOn || null,
          })
    },
    () => onDone(product ? 'Producto actualizado.' : 'Producto creado.'),
  )

  const number = (label: string, key: keyof typeof form) => (
    <label>
      {label}
      <input type="number" min="0" step="1" required value={form[key]} onChange={set(key)} />
    </label>
  )

  return (
    <form className="form" onSubmit={submit}>
      <label className="wide">
        Nombre
        <input required maxLength={120} value={form.name} onChange={set('name')} />
      </label>
      <label>
        Categoría
        <input required maxLength={60} list="categories" value={form.category} onChange={set('category')} />
        <datalist id="categories">
          {['Flores', 'Follajes', 'Empaque', 'Insumos', 'Accesorios'].map((name) => (
            <option key={name} value={name} />
          ))}
        </datalist>
      </label>
      <label>
        Unidad
        <select value={form.unit} onChange={set('unit')}>
          {Object.entries(unitNames).map(([value, name]) => (
            <option key={value} value={value}>
              {name}
            </option>
          ))}
        </select>
      </label>
      <label>
        Código (opcional)
        <input maxLength={40} value={form.sku} onChange={set('sku')} />
      </label>
      <label>
        Proveedor
        <input maxLength={120} value={form.supplier} onChange={set('supplier')} />
      </label>
      {number('Costo (COP)', 'unitCost')}
      {number('Precio (COP)', 'unitPrice')}
      {number('Stock mínimo', 'minStock')}
      {!product && (
        <>
          {number('Stock inicial', 'initialStock')}
          <label>
            Vence (opcional)
            <input type="date" min={today()} value={form.initialExpiresOn} onChange={set('initialExpiresOn')} />
          </label>
        </>
      )}
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>
        {saving ? 'Guardando…' : 'Guardar'}
      </button>
    </form>
  )
}
