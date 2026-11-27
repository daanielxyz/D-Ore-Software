import { useState } from 'react'
import { useSubmit } from '../../components/useSubmit'
import { SessionExpired } from '../../http'
import { authApi } from './api'
import { LoginBackground } from './LoginBackground'

export function LoginPage({ onLogin }: { onLogin: () => void }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const { saving, error, submit } = useSubmit(async () => {
    try {
      await authApi.login(username, password)
    } catch (reason) {
      if (reason instanceof SessionExpired) throw new Error('Usuario o clave incorrectos.', { cause: reason })
      throw reason
    }
  }, onLogin)

  return (
    <div className="login with-video">
      <LoginBackground />
      <form className="panel" onSubmit={submit}>
        <h1>
          <img src="/logo.webp" alt="D'Orée Floristería" width={640} height={531} />
        </h1>
        <label>
          Usuario
          <input required autoComplete="username" value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>
        <label>
          Clave
          <input type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>
        {error && <p className="error" role="alert">{error}</p>}
        <button className="primary" disabled={saving}>{saving ? 'Entrando…' : 'Entrar'}</button>
      </form>
    </div>
  )
}
