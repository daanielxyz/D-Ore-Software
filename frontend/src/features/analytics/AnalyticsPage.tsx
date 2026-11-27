import { useCallback, useState } from 'react'
import { useLoad } from '../../components/useLoad'
import { http } from '../../http'
import { channelNames, type Channel } from '../channels'
import { categoryNames, type ExpenseCategory } from '../finance/categories'
import type { Unit } from '../inventory/api'
import { money, unitNames } from '../inventory/format'
import { BarList, DayColumns, type DaySales } from './charts'

interface Analytics {
  salesByDay: DaySales[]
  byChannel: { channel: Channel; total: number; count: number }[]
  topProducts: { name: string; unit: Unit; quantity: number }[]
  waste: { name: string; unit: Unit; quantity: number; cost: number }[]
  expensesByCategory: { category: ExpenseCategory; total: number }[]
}

export function AnalyticsPage() {
  const [days, setDays] = useState(14)
  const load = useCallback(() => http<Analytics>(`/api/analytics?days=${days}`), [days])
  const { data, error } = useLoad(load)

  const total = data?.salesByDay.reduce((sum, d) => sum + d.total, 0) ?? 0
  const count = data?.salesByDay.reduce((sum, d) => sum + d.count, 0) ?? 0
  const lost = data?.waste.reduce((sum, w) => sum + w.cost, 0) ?? 0

  return (
    <>
      <div className="page-head">
        <h1>Analítica</h1>
        <label className="month">
          Periodo
          <select value={days} onChange={(e) => setDays(Number(e.target.value))}>
            <option value={7}>Últimos 7 días</option>
            <option value={14}>Últimos 14 días</option>
            <option value={30}>Últimos 30 días</option>
          </select>
        </label>
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      {!data ? (
        !error && <p>Cargando…</p>
      ) : (
        <>
          <div className="cards">
            <div className="panel stat"><strong>{money(total)}</strong><span>Vendido</span></div>
            <div className="panel stat"><strong>{count}</strong><span>Ventas</span></div>
            <div className="panel stat"><strong>{money(count ? Math.round(total / count) : 0)}</strong><span>Venta promedio</span></div>
            <div className="panel stat"><strong>{money(lost)}</strong><span>Perdido en mermas (al costo)</span></div>
          </div>
          <DayColumns days={data.salesByDay} />
          <div className="columns">
            <BarList
              title="Ventas por canal"
              empty="Sin ventas en el periodo."
              items={data.byChannel.map((c) => ({ label: channelNames[c.channel], value: c.total, text: money(c.total) }))}
            />
            <BarList
              title="Lo que más sale"
              empty="Sin productos vendidos en el periodo."
              items={data.topProducts.map((p) => ({ label: p.name, value: p.quantity, text: `${p.quantity} ${unitNames[p.unit]}` }))}
            />
            <BarList
              title="Mermas"
              empty="Sin mermas en el periodo."
              items={data.waste.map((w) => ({ label: w.name, value: w.cost, text: `${w.quantity} ${unitNames[w.unit]} · ${money(w.cost)}` }))}
            />
            <BarList
              title="Gastos por categoría"
              empty="Sin gastos en el periodo."
              items={data.expensesByCategory.map((c) => ({ label: categoryNames[c.category], value: c.total, text: money(c.total) }))}
            />
          </div>
        </>
      )}
    </>
  )
}
