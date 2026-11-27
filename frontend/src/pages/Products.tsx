import { useState } from 'react'
import type { FormEvent } from 'react'
import Field from '../components/Field'
import { formatMoney, normalize, validateProduct } from '../domain/cadastros'
import type { Product } from '../domain/cadastros'
import { useApiList } from '../hooks/useApiList'

const emptyProduct: Product = { id: '', code: '', name: '', category: '', unit: '', purchasePrice: 0, salePrice: 0, stock: 0, minimumStock: 0, status: 'Ativo' }

export default function Products() {
  const { items, save, loading, error: loadError, reload } = useApiList<Product>('/products')
  const [saving, setSaving] = useState(false)
  const [query, setQuery] = useState('')
  const [draft, setDraft] = useState<Product | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const filtered = items.filter(item => normalize(`${item.name} ${item.code} ${item.category}`).includes(normalize(query)))

  function open(product: Product) {
    setDraft({ ...product })
    setError('')
    setMessage('')
  }

  function change<K extends keyof Product>(key: K, value: Product[K]) {
    if (draft) setDraft({ ...draft, [key]: value })
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!draft || saving) return
    const product = { ...draft, code: draft.code.trim(), name: draft.name.trim(), category: draft.category.trim(), unit: draft.unit.trim() }
    const validation = validateProduct(product, items)
    setError(validation)
    if (validation) return
    setSaving(true)
    try {
      await save(product)
      setDraft(null)
      setQuery('')
      setMessage(draft.id ? 'Produto atualizado.' : 'Produto cadastrado.')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Não foi possível salvar o produto.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <div className="page-heading"><div><p className="breadcrumb">Cadastros</p><h1>Produtos</h1><p>Organize os produtos, preços e estoques da loja.</p></div><button className="primary" type="button" disabled={loading || saving || !!loadError} onClick={() => open(emptyProduct)}>Novo produto</button></div>
      {loadError && <div className="load-error"><p className="error" role="alert">{loadError}</p><button type="button" onClick={reload}>Tentar novamente</button></div>}
      <p className="feedback" role="status">{message}</p>
      {draft && <section className="panel editor" aria-labelledby="product-form-title">
        <h2 id="product-form-title">{draft.id ? 'Editar produto' : 'Novo produto'}</h2>
        <form onSubmit={submit}>
          <fieldset className="fields" disabled={saving}>
            <Field label="Código" maxLength={80} value={draft.code} onChange={e => change('code', e.target.value)} />
            <Field label="Nome *" maxLength={160} required autoFocus value={draft.name} onChange={e => change('name', e.target.value)} />
            <Field label="Categoria" maxLength={100} value={draft.category} onChange={e => change('category', e.target.value)} />
            <Field label="Unidade *" maxLength={30} required placeholder="Ex.: kg, g, un" value={draft.unit} onChange={e => change('unit', e.target.value)} />
            <Field label="Preço de compra (R$)" type="number" required min="0" step="0.01" value={Number.isNaN(draft.purchasePrice) ? '' : draft.purchasePrice} onChange={e => change('purchasePrice', e.target.valueAsNumber)} />
            <Field label="Preço de venda (R$) *" type="number" required min="0" step="0.01" value={Number.isNaN(draft.salePrice) ? '' : draft.salePrice} onChange={e => change('salePrice', e.target.valueAsNumber)} />
            <Field label="Estoque atual" type="number" required min="0" step="0.001" value={Number.isNaN(draft.stock) ? '' : draft.stock} onChange={e => change('stock', e.target.valueAsNumber)} />
            <Field label="Estoque mínimo" type="number" required min="0" step="0.001" value={Number.isNaN(draft.minimumStock) ? '' : draft.minimumStock} onChange={e => change('minimumStock', e.target.valueAsNumber)} />
            <label className="field">Status<select value={draft.status} onChange={e => change('status', e.target.value as Product['status'])}><option>Ativo</option><option>Inativo</option></select></label>
          </fieldset>
          {error && <p className="error" role="alert">{error}</p>}
          <div className="form-actions"><button type="button" disabled={saving} onClick={() => setDraft(null)}>Cancelar</button><button className="primary" type="submit" disabled={saving}>{saving ? 'Salvando...' : 'Salvar produto'}</button></div>
        </form>
      </section>}
      <section className="panel" aria-label="Produtos cadastrados" aria-busy={loading}>
        <div className="toolbar"><Field label="Buscar por nome, código ou categoria" type="search" placeholder="Buscar produto" value={query} onChange={e => setQuery(e.target.value)} /><span>{filtered.length} produto(s)</span></div>
        <div className="table-container"><table>
          <thead><tr><th scope="col">Produto</th><th scope="col">Unidade</th><th scope="col">Venda</th><th scope="col">Estoque</th><th scope="col">Status</th><th scope="col">Ações</th></tr></thead>
          <tbody>{filtered.map(product => <tr key={product.id}>
            <td><strong>{product.name}</strong><small>{[product.code, product.category].filter(Boolean).join(' · ') || 'Sem categoria'}</small></td>
            <td>{product.unit}</td><td>{formatMoney(product.salePrice)}</td><td>{product.stock.toLocaleString('pt-BR')}</td><td><span className={`status ${product.status === 'Inativo' ? 'inactive' : ''}`}>{product.status}</span></td>
            <td><button className="text-button" type="button" disabled={saving || loading || !!loadError} aria-label={`Editar ${product.name}`} onClick={() => open(product)}>Editar</button></td>
          </tr>)}</tbody>
        </table></div>
        {loading && <p className="empty" role="status">Carregando produtos...</p>}
        {!loading && !loadError && !filtered.length && <p className="empty">{items.length ? 'Nenhum produto encontrado para esta busca.' : 'Nenhum produto cadastrado. Clique em Novo produto para começar.'}</p>}
      </section>
    </>
  )
}
