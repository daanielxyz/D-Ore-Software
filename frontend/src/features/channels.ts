export type Channel = 'STORE' | 'WHATSAPP' | 'INSTAGRAM' | 'WEB' | 'OTHER'

export const channelNames: Record<Channel, string> = {
  STORE: 'Tienda',
  WHATSAPP: 'WhatsApp',
  INSTAGRAM: 'Instagram',
  WEB: 'Web',
  OTHER: 'Otro',
}
