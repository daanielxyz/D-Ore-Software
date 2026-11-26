import { useCallback, useState } from 'react'
import { Modal } from '../../components/Modal'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { http } from '../../http'
import { day, money, today } from '../inventory/format'
import { categoryNames, type ExpenseCategory } from './categories'

interface Expense {
  id: number
  description: string
  amount: number
  category: ExpenseCategory
  expenseDate: string
}

interface FinanceMonth {
  month: string
  income: number
  spent: number
  expenses: Expense[]
}

export function FinancePage() {
  const [month, setMonth] = useState(today().slice(0, 7))
  const load = useCallback(() => http<FinanceMonth>(`/api/finance?month=${month}`), [month])
  const { data, error, reload } = useLoad(load)
  const [creating, setCreating] = useState(false)
  const [failure, setFailure] = useState('')

  async function remove(expense: Expense) {
    if (!window.confirm(`¿Eliminar el gasto "${expense.description}"?`)) return
    setFailure('')
    try {
      await http<void>(`/api/finance/expenses/${expense.id}`, 'DELETE')
      await reload()
    } catch (reason) {
      setFailure(reason instanceof Error ? reason.message : 'Error inesperado.')
    }
  }

  return (
    <>
      <div className="page-head">
        <h1>Finanzas</h1>
        <button className="primary" onClick={() => setCreating(true)}>+ Registrar gasto</button>
      </div>
      <div className="toolbar">
        <label className="month">
          Mes
          <input type="month" max={today().slice(0, 7)} value={month} onChange={(e) => e.target.value && setMonth(e.target.value)} />
        </label>
      </div>
      {(error || failure) && <p className="error" role="alert">{error || failure}</p>}
      {!data ? (
        !error && <p>Cargando…</p>
      ) : (
        <>
          <div className="cards">
            <div className="panel stat"><strong>{money(data.income)}</strong><span>Ingresos (ventas)</span></div>
            <div className="panel stat"><strong>{money(data.spent)}</strong><span>Gastos</span></div>
            <div className="panel stat">
              <strong className={data.income < data.spent ? 'error' : ''}>{money(data.income - data.spent)}</strong>
              <span>Balance registrado</span>
            </div>
          </div>
          <p className="muted">Solo cuenta lo anotado: las compras de inventario no se registran solas como gasto.</p>
          <div className="panel scroll">
            {data.expenses.length === 0 ? (
              <p className="muted">No hay gastos en este mes.</p>
            ) : (
              <table>
                <thead>
                  <tr><th>Concepto</th><th>Categoría</th><th>Fecha</th><th>Valor</th><th /></tr>
                </thead>
                <tbody>
                  {data.expenses.map((expense) => (
                    <tr key={expense.id}>
                      <td>{expense.description}</td>
                      <td>{categoryNames[expense.category]}</td>
                      <td>{day(expense.expenseDate)}</td>
                      <td>{money(expense.amount)}</td>
                      <td><button className="danger" onClick={() => void remove(expense)}>Eliminar</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
      {creating && (
        <Modal title="Registrar gasto" onClose={() => setCreating(false)}>
          <ExpenseForm
            onDone={() => {
              setCreating(false)
              void reload()
            }}
          />
        </Modal>
      )}
    </>
  )
}

function ExpenseForm({ onDone }: { onDone: () => void }) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState<ExpenseCategory>('INVENTORY')
  const [expenseDate, setExpenseDate] = useState(today())
  const { saving, error, submit } = useSubmit(
    () => http<void>('/api/finance/expenses', 'POST', { description, amount: Number(amount), category, expenseDate }),
    onDone,
  )

  return (
    <form className="form" onSubmit={submit}>
      <label className="wide">Concepto<input required maxLength={200} value={description} onChange={(e) => setDescription(e.target.value)} /></label>
      <label>Valor (COP)<input type="number" required min="1" step="1" value={amount} onChange={(e) => setAmount(e.target.value)} /></label>
      <label>
        Categoría
        <select value={category} onChange={(e) => setCategory(e.target.value as ExpenseCategory)}>
          {Object.entries(categoryNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label className="wide">Fecha<input type="date" required max={today()} value={expenseDate} onChange={(e) => setExpenseDate(e.target.value)} /></label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>Registrar</button>
    </form>
  )
}
