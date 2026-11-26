import type { AudioData } from './api'

const MAX_BYTES = 6_000_000

/** Los navegadores y las apps nombran distinto el mismo formato. */
function mimeType(raw: string): string {
  const base = raw.split(';')[0].trim()
  const aliases: Record<string, string> = {
    'audio/mp4': 'audio/m4a',
    'audio/x-m4a': 'audio/m4a',
    'audio/x-wav': 'audio/wav',
    'video/webm': 'audio/webm',
    'video/ogg': 'audio/ogg',
  }
  return aliases[base] ?? base
}

export function toAudioData(blob: Blob): Promise<AudioData> {
  if (blob.size > MAX_BYTES) {
    return Promise.reject(new Error('El audio es muy largo. Graba una nota más corta.'))
  }
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = () => reject(new Error('No se pudo leer el audio.'))
    reader.onload = () =>
      resolve({
        mimeType: mimeType(blob.type),
        data: String(reader.result).split(',')[1],
      })
    reader.readAsDataURL(blob)
  })
}
