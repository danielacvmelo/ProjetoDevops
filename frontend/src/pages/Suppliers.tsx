import { useState } from 'react'
import type { FormEvent } from 'react'
import Field from '../components/Field'
import { normalize, validateSupplier } from '../domain/cadastros'
import type { Supplier } from '../domain/cadastros'
import { useApiList } from '../hooks/useApiList'

const emptySupplier: Supplier = { id: '', name: '', email: '', phone: '' }

export default function Suppliers() {
  const { items, save, loading, error: loadError, reload } = useApiList<Supplier>('/suppliers')
  const [saving, setSaving] = useState(false)
  const [query, setQuery] = useState('')
  const [draft, setDraft] = useState<Supplier | null>(null)
  const [selected, setSelected] = useState<Supplier | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const filtered = items.filter(item => normalize(item.name).includes(normalize(query)))

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!draft || saving) return
    const supplier = { id: '', name: draft.name.trim(), email: draft.email.trim(), phone: draft.phone.trim() }
    const validation = validateSupplier(supplier, items)
    setError(validation)
    if (validation) return
    setSaving(true)
    try {
      await save(supplier)
      setDraft(null)
      setQuery('')
      setMessage('Fornecedor cadastrado.')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Não foi possível salvar o fornecedor.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <div className="page-heading"><div><p className="breadcrumb">Cadastros</p><h1>Fornecedores</h1><p>Consulte os fornecedores e seus dados de contato.</p></div><button className="primary" type="button" disabled={loading || saving || !!loadError} onClick={() => { setDraft({ ...emptySupplier }); setSelected(null); setError(''); setMessage('') }}>Novo fornecedor</button></div>
      {loadError && <div className="load-error"><p className="error" role="alert">{loadError}</p><button type="button" onClick={reload}>Tentar novamente</button></div>}
      <p className="feedback" role="status">{message}</p>
      {draft && <section className="panel editor" aria-labelledby="supplier-form-title">
        <h2 id="supplier-form-title">Novo fornecedor</h2>
        <form onSubmit={submit}>
          <fieldset className="fields" disabled={saving}>
            <Field label="Nome *" maxLength={160} required autoFocus value={draft.name} onChange={e => setDraft({ ...draft, name: e.target.value })} />
            <Field label="E-mail" maxLength={255} type="email" value={draft.email} onChange={e => setDraft({ ...draft, email: e.target.value })} />
            <Field label="Telefone" maxLength={40} type="tel" value={draft.phone} onChange={e => setDraft({ ...draft, phone: e.target.value })} />
          </fieldset>
          {error && <p className="error" role="alert">{error}</p>}
          <div className="form-actions"><button type="button" disabled={saving} onClick={() => setDraft(null)}>Cancelar</button><button className="primary" type="submit" disabled={saving}>{saving ? 'Salvando...' : 'Salvar fornecedor'}</button></div>
        </form>
      </section>}
      {selected && <section className="panel editor" aria-labelledby="supplier-details-title">
        <h2 id="supplier-details-title">Detalhes do fornecedor</h2>
        <dl><dt>Nome</dt><dd>{selected.name}</dd><dt>E-mail</dt><dd>{selected.email || 'Não informado'}</dd><dt>Telefone</dt><dd>{selected.phone || 'Não informado'}</dd></dl>
        <button type="button" onClick={() => setSelected(null)}>Fechar detalhes</button>
      </section>}
      <section className="panel" aria-label="Fornecedores cadastrados" aria-busy={loading}>
        <div className="toolbar"><Field label="Buscar por nome" type="search" placeholder="Buscar fornecedor" value={query} onChange={e => setQuery(e.target.value)} /><span>{filtered.length} fornecedor(es)</span></div>
        <div className="table-container"><table>
          <thead><tr><th scope="col">Fornecedor</th><th scope="col">E-mail</th><th scope="col">Telefone</th><th scope="col">Ações</th></tr></thead>
          <tbody>{filtered.map(supplier => <tr key={supplier.id}><td><strong>{supplier.name}</strong></td><td>{supplier.email || 'Não informado'}</td><td>{supplier.phone || 'Não informado'}</td><td><button className="text-button" type="button" disabled={saving} aria-label={`Ver detalhes de ${supplier.name}`} onClick={() => { setSelected(supplier); setDraft(null); setMessage('') }}>Ver detalhes</button></td></tr>)}</tbody>
        </table></div>
        {loading && <p className="empty" role="status">Carregando fornecedores...</p>}
        {!loading && !loadError && !filtered.length && <p className="empty">{items.length ? 'Nenhum fornecedor encontrado para esta busca.' : 'Nenhum fornecedor cadastrado. Clique em Novo fornecedor para começar.'}</p>}
      </section>
    </>
  )
}
