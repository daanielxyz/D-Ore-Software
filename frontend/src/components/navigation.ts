import type { IconName } from './icons'

export const navGroups = [
  {
    label: 'Taller',
    items: [
      { id: 'resumen', title: 'Resumen', description: 'Lo urgente del día.' },
      { id: 'asistente', title: 'Asistente', description: 'Actualiza el inventario escribiendo o hablando.' },
      { id: 'pedidos', title: 'Pedidos', description: 'Entregas y estado de cada arreglo.' },
      { id: 'inventario', title: 'Inventario', description: 'Existencias, lotes y vencimientos.' },
    ],
  },
  {
    label: 'Negocio',
    items: [
      { id: 'clientes', title: 'Clientes', description: 'Contactos y compras acumuladas.' },
      { id: 'ventas', title: 'Ventas', description: 'Ventas de mostrador y pedidos entregados.' },
      { id: 'finanzas', title: 'Finanzas', description: 'Gastos y balance del mes.' },
      { id: 'analitica', title: 'Analítica', description: 'Qué se vende y cuánto se pierde.' },
    ],
  },
  {
    label: 'Marca',
    items: [
      { id: 'contenido', title: 'Contenido', description: 'Calendario de publicaciones.' },
      { id: 'escucha', title: 'Social listening', description: 'Menciones anotadas por el equipo.' },
    ],
  },
] as const satisfies readonly { label: string; items: readonly { id: IconName; title: string; description: string }[] }[]

export type PageId = (typeof navGroups)[number]['items'][number]['id']
