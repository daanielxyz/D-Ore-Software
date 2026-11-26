import { useState } from 'react'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { channelNames, type Channel } from '../channels'
import { customersApi } from '../customers/api'
import { inventoryApi } from '../inventory/api'
import { money, today, unitNames } from '../inventory/format'
import { ordersApi } from './api'

const loadOptions = async () => {
  const [customers, products] = await Promise.all([customersApi.list(), inventoryApi.list(false)])
  return { customers, products }
}

export function OrderForm({ onDone }: { onDone: () => void }) {
  const { data, error: loadError } = useLoad(loadOptions)
  const [customerId, setCustomerId] = useState('')
  const [occasion, setOccasion] = useState('Cumpleaños')
  const [channel, setChannel] = useState<Channel>('WHATSAPP')
  const [deliveryDate, setDeliveryDate] = useState(today())
  const [total, setTotal] = useState('')
  const [notes, setNotes] = useState('')
  const [lines, setLines] = useState([{ key: 0, productId: '', quantity: '1' }])

  const { saving, error, submit } = useSubmit(
    () =>
      ordersApi.create({
        customerId: Number(customerId),
        occasion,
        channel,
        deliveryDate,
        total: total ? Number(total) : null,
        notes,
        items: lines.map((l) => ({ productId: Number(l.productId), quantity: Number(l.quantity) })),
      }),
    onDone,
  )

  if (loadError) return <p className="error" role="alert">{loadError}</p>
  if (!data) return <p>Cargando…</p>
  if (data.customers.length === 0) return <p>Primero registra un cliente en la sección Clientes.</p>
  if (data.products.length === 0) return <p>Primero registra productos en el inventario.</p>

  const suggested = lines.reduce((sum, line) => {
    const product = data.products.find((p) => p.id === Number(line.productId))
    return sum + (product?.unitPrice ?? 0) * Number(line.quantity)
  }, 0)

  const change = (key: number, field: 'productId' | 'quantity', value: string) =>
    setLines((current) => current.map((l) => (l.key === key ? { ...l, [field]: value } : l)))

  return (
    <form className="form" onSubmit={submit}>
      <label className="wide">
        Cliente
        <select required value={customerId} onChange={(e) => setCustomerId(e.target.value)}>
          <option value="">Elige…</option>
          {data.customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </label>
      <label>
        Ocasión
        <input required maxLength={60} list="occasions" value={occasion} onChange={(e) => setOccasion(e.target.value)} />
        <datalist id="occasions">
          {['Cumpleaños', 'Aniversario', 'Boda', 'Grado', 'Condolencias', 'Corporativo'].map((o) => <option key={o} value={o} />)}
        </datalist>
      </label>
      <label>
        Canal
        <select value={channel} onChange={(e) => setChannel(e.target.value as Channel)}>
          {Object.entries(channelNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label className="wide">
        Fecha de entrega
        <input type="date" required min={today()} value={deliveryDate} onChange={(e) => setDeliveryDate(e.target.value)} />
      </label>

      <fieldset className="wide">
        <legend>Flores y materiales</legend>
        {lines.map((line) => (
          <div className="line" key={line.key}>
            <select required aria-label="Producto" value={line.productId} onChange={(e) => change(line.key, 'productId', e.target.value)}>
              <option value="">Elige un producto…</option>
              {data.products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.stock - p.expiredStock} {unitNames[p.unit]})
                </option>
              ))}
            </select>
            <input type="number" required min="1" step="1" aria-label="Cantidad" value={line.quantity} onChange={(e) => change(line.key, 'quantity', e.target.value)} />
            <button type="button" aria-label="Quitar línea" disabled={lines.length === 1} onClick={() => setLines((c) => c.filter((l) => l.key !== line.key))}>✕</button>
          </div>
        ))}
        <button type="button" onClick={() => setLines((c) => [...c, { key: c[c.length - 1].key + 1, productId: '', quantity: '1' }])}>
          + Agregar producto
        </button>
      </fieldset>

      <label className="wide">
        Total a cobrar (COP). Vacío usa la suma: {money(suggested)}
        <input type="number" min="0" step="1" value={total} onChange={(e) => setTotal(e.target.value)} />
      </label>
      <label className="wide">Notas<input maxLength={300} value={notes} onChange={(e) => setNotes(e.target.value)} /></label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>Crear pedido</button>
    </form>
  )
}
