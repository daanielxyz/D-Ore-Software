/** Única puerta hacia el backend: agrega el token CSRF y traduce los errores. */
export class SessionExpired extends Error {}

function csrfToken(): string {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : ''
}

export async function http<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = {}
  if (method !== 'GET') headers['X-XSRF-TOKEN'] = csrfToken()
  const form = body instanceof URLSearchParams
  if (body && !form) headers['Content-Type'] = 'application/json'

  let response: Response
  try {
    response = await fetch(path, {
      method,
      headers,
      body: form ? body : body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new Error('No hay conexión con el servidor.')
  }
  if (response.status === 401) {
    window.dispatchEvent(new Event('session-expired'))
    throw new SessionExpired('Tu sesión terminó. Vuelve a entrar.')
  }
  if (!response.ok) {
    // El backend explica las reglas de negocio en "detail" (404 y 409).
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null
    const explained = response.status === 404 || response.status === 409
    throw new Error(
      explained && problem?.detail ? problem.detail : 'No se pudo completar. Revisa los datos.',
    )
  }
  // Algunas respuestas (201, 204) no traen contenido.
  const content = await response.text()
  return (content ? JSON.parse(content) : undefined) as T
}
