import { useState } from 'react'
import { emptyLine, suggestedTotal, toItems } from '../../components/lines'
import { ProductLines } from '../../components/ProductLines'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { channelNames, type Channel } from '../channels'
import { customersApi } from '../customers/api'
import { inventoryApi } from '../inventory/api'
import { money, today } from '../inventory/format'
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
  const [lines, setLines] = useState([emptyLine(0)])

  const { saving, error, submit } = useSubmit(
    () =>
      ordersApi.create({
        customerId: Number(customerId),
        occasion,
        channel,
        deliveryDate,
        total: total ? Number(total) : null,
        notes,
        items: toItems(lines),
      }),
    onDone,
  )

  if (loadError) return <p className="error" role="alert">{loadError}</p>
  if (!data) return <p>Cargando…</p>
  if (data.customers.length === 0) return <p>Primero registra un cliente en la sección Clientes.</p>
  if (data.products.length === 0) return <p>Primero registra productos en el inventario.</p>

  const suggested = suggestedTotal(lines, data.products)

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

      <ProductLines products={data.products} lines={lines} onChange={setLines} required />

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
