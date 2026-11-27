import { useState } from 'react'

export function useStoredList<T>(key: string) {
  const [initial] = useState(() => {
    try {
      const value: unknown = JSON.parse(localStorage.getItem(key) ?? '[]')
      if (!Array.isArray(value)) throw new Error()
      return { items: value as T[], error: '' }
    } catch {
      return { items: [] as T[], error: 'Não foi possível ler os cadastros deste navegador. Os dados salvos não foram alterados.' }
    }
  })
  const [items, setItems] = useState(initial.items)
  const [error, setError] = useState(initial.error)

  function save(next: T[]) {
    if (initial.error) return false
    try {
      localStorage.setItem(key, JSON.stringify(next))
      setItems(next)
      setError('')
      return true
    } catch {
      setError('Não foi possível salvar neste navegador. Verifique o espaço e a permissão de armazenamento e tente novamente.')
      return false
    }
  }

  return { items, save, error }
}
