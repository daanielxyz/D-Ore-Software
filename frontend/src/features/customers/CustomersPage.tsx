import { useState } from 'react'
import { Modal } from '../../components/Modal'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { money } from '../inventory/format'
import { customersApi, type Customer } from './api'

export function CustomersPage() {
  const { data, error, reload } = useLoad(customersApi.list)
  const [editing, setEditing] = useState<Customer | 'new' | null>(null)
  const [query, setQuery] = useState('')

  if (error) return <p className="error" role="alert">{error}</p>
  if (!data) return <p>Cargando…</p>

  const term = query.trim().toLowerCase()
  const visible = data.filter((c) => `${c.name} ${c.phone} ${c.email}`.toLowerCase().includes(term))

  return (
    <>
      <div className="page-head">
        <h1>Clientes</h1>
        <button className="primary" onClick={() => setEditing('new')}>+ Nuevo cliente</button>
      </div>
      <div className="toolbar">
        <input type="search" placeholder="Buscar cliente" aria-label="Buscar cliente" value={query} onChange={(e) => setQuery(e.target.value)} />
      </div>
      <div className="panel scroll">
        {visible.length === 0 ? (
          <p className="muted">No hay clientes para mostrar.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Cliente</th><th>Contacto</th><th>Pedidos</th><th>Compras</th><th>Acciones</th></tr>
            </thead>
            <tbody>
              {visible.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.name}<small>{customer.notes}</small></td>
                  <td>{customer.phone || '—'}<small>{customer.email}</small></td>
                  <td>{customer.ordersCount}</td>
                  <td>{money(customer.totalSpent)}</td>
                  <td><button onClick={() => setEditing(customer)}>Editar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      {editing && (
        <Modal title={editing === 'new' ? 'Nuevo cliente' : 'Editar cliente'} onClose={() => setEditing(null)}>
          <CustomerForm
            customer={editing === 'new' ? null : editing}
            onDone={() => {
              setEditing(null)
              void reload()
            }}
          />
        </Modal>
      )}
    </>
  )
}

function CustomerForm({ customer, onDone }: { customer: Customer | null; onDone: () => void }) {
  const [form, setForm] = useState({
    name: customer?.name ?? '',
    phone: customer?.phone ?? '',
    email: customer?.email ?? '',
    notes: customer?.notes ?? '',
  })
  const set = (key: keyof typeof form) => (event: { target: { value: string } }) =>
    setForm((current) => ({ ...current, [key]: event.target.value }))
  const { saving, error, submit } = useSubmit(
    () => (customer ? customersApi.update(customer.id, form) : customersApi.create(form)),
    onDone,
  )

  return (
    <form className="form" onSubmit={submit}>
      <label className="wide">Nombre<input required maxLength={120} value={form.name} onChange={set('name')} /></label>
      <label>Teléfono<input type="tel" maxLength={40} value={form.phone} onChange={set('phone')} /></label>
      <label>Correo<input type="email" maxLength={120} value={form.email} onChange={set('email')} /></label>
      <label className="wide">Notas<input maxLength={300} value={form.notes} onChange={set('notes')} /></label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>Guardar</button>
    </form>
  )
}
