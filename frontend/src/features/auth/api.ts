import { http } from '../../http'

export const authApi = {
  me: () => http<{ name: string }>('/api/auth/me'),
  async login(username: string, password: string) {
    await http<void>('/api/auth/csrf')
    await http<void>('/api/auth/login', 'POST', new URLSearchParams({ username, password }))
  },
  async logout() {
    // El token cambia al iniciar sesión: se pide uno vigente antes de salir.
    await http<void>('/api/auth/csrf')
    await http<void>('/api/auth/logout', 'POST')
  },
}
