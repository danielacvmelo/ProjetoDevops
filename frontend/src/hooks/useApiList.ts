import { useCallback, useEffect, useState } from 'react'
import { request } from '../services/api'

export function useApiList<T extends { id: string }>(path: string) {
  const [items, setItems] = useState<T[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setError('')
    request<T[]>(path, { signal: controller.signal })
      .then(data => { if (!controller.signal.aborted) setItems(data) })
      .catch((error: Error) => { if (!controller.signal.aborted) setError(error.message) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [path, attempt])

  async function save(item: T) {
    const { id, ...body } = item
    const saved = await request<T>(id ? `${path}/${id}` : path, {
      method: id ? 'PUT' : 'POST', body: JSON.stringify(body),
    })
    setItems(current => id ? current.map(existing => existing.id === id ? saved : existing) : [...current, saved])
  }

  const reload = useCallback(() => setAttempt(current => current + 1), [])
  return { items, loading, error, save, reload }
}
