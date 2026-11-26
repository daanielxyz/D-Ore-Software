import type { MovementType, Product, Unit } from './api'

export const unitNames: Record<Unit, string> = {
  STEM: 'tallos',
  BUNCH: 'ramos',
  ROLL: 'rollos',
  PACKAGE: 'paquetes',
  UNIT: 'unidades',
}

export const movementNames: Record<MovementType, string> = {
  IN: 'Entrada',
  OUT: 'Salida',
  WASTE: 'Merma',
  ADJUSTMENT: 'Conteo',
}

export const money = (value: number) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(value)

/** Fecha de hoy en Cali como AAAA-MM-DD. */
export const today = () =>
  new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Bogota' }).format(new Date())

/** Una fecha sin hora se muestra tal cual, sin correrla de zona. */
export const day = (isoDate: string) =>
  new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', timeZone: 'UTC' }).format(
    new Date(isoDate),
  )

export const dateTime = (instant: string) =>
  new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'America/Bogota',
  }).format(new Date(instant))

export const SOON_DAYS = 3

export function expiresSoon(product: Product): boolean {
  if (!product.nextExpiry) return false
  const days = (Date.parse(product.nextExpiry) - Date.parse(today())) / 86_400_000
  return days <= SOON_DAYS
}
