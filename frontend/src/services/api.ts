const baseUrl = (import.meta.env?.VITE_API_URL || '/api').replace(/\/$/, '')

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const timeout = AbortSignal.timeout(15000)
  let response: Response
  try {
    response = await fetch(`${baseUrl}${path}`, {
      ...options,
      headers: { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers },
      signal: options.signal ? AbortSignal.any([options.signal, timeout]) : timeout,
    })
  } catch (error) {
    if (options.signal?.aborted) throw error
    throw new Error(timeout.aborted ? 'A API demorou para responder. Tente novamente.' : 'Não foi possível conectar à API. Verifique se o backend está em execução.')
  }
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    const fields = data?.errors ? Object.values(data.errors).join(' ') : ''
    throw new Error(fields || data?.detail || `Não foi possível concluir a operação (HTTP ${response.status}).`)
  }
  if (data === null) throw new Error('A API retornou uma resposta inválida.')
  return data as T
}
