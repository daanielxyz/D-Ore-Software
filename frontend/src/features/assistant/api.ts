import { http } from '../../http'

export interface Turn {
  fromUser: boolean
  text: string
}

export interface AudioData {
  mimeType: string
  data: string
}

export interface Proposal {
  id: string
  actions: { summary: string }[]
}

export interface Reply {
  message: string
  heard: string | null
  proposal: Proposal | null
}

export const assistantApi = {
  send: (text: string, audio: AudioData | null, history: Turn[]) =>
    http<Reply>('/api/assistant/messages', 'POST', { text, audio, history }),
  confirm: (id: string) => http<void>(`/api/assistant/actions/${id}/confirm`, 'POST'),
  cancel: (id: string) => http<void>(`/api/assistant/actions/${id}`, 'DELETE'),
}
