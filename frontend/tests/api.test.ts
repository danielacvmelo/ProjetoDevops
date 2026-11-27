import assert from 'node:assert/strict'
import test from 'node:test'
import { request } from '../src/services/api.ts'

test('consulta a API e recebe os registros do servidor', async t => {
  t.mock.method(globalThis, 'fetch', async (url: string, options: RequestInit) => {
    assert.equal(url, '/api/products')
    assert.equal(options.headers && (options.headers as Record<string, string>).Accept, 'application/json')
    return Response.json([{ id: 'servidor-1', name: 'Hibisco' }])
  })
  assert.deepEqual(await request('/products'), [{ id: 'servidor-1', name: 'Hibisco' }])
})

test('envia POST e PUT em JSON e utiliza o identificador devolvido pela API', async t => {
  t.mock.method(globalThis, 'fetch', async (url: string, options: RequestInit) => {
    assert.equal((options.headers as Record<string, string>)['Content-Type'], 'application/json')
    assert.deepEqual(JSON.parse(options.body as string), { name: 'Camomila' })
    assert.equal(url, options.method === 'POST' ? '/api/products' : '/api/products/id-real')
    return Response.json({ id: 'id-real', name: 'Camomila' })
  })
  for (const method of ['POST', 'PUT']) {
    const result = await request<{ id: string }>(method === 'POST' ? '/products' : '/products/id-real', { method, body: JSON.stringify({ name: 'Camomila' }) })
    assert.equal(result.id, 'id-real')
  }
})

test('apresenta mensagem de duplicidade retornada pelo backend', async t => {
  t.mock.method(globalThis, 'fetch', async () => Response.json({ detail: 'Este fornecedor já está cadastrado.' }, { status: 409 }))
  await assert.rejects(request('/suppliers'), /Este fornecedor já está cadastrado/)
})

test('apresenta erros dos campos enviados pelo backend', async t => {
  t.mock.method(globalThis, 'fetch', async () => Response.json({ errors: { name: 'Informe o nome.', unit: 'Informe a unidade.' } }, { status: 400 }))
  await assert.rejects(request('/products'), /Informe o nome\. Informe a unidade\./)
})

test('trata indisponibilidade sem anunciar gravação bem-sucedida', async t => {
  t.mock.method(globalThis, 'fetch', async () => { throw new TypeError('Failed to fetch') })
  await assert.rejects(request('/products', { method: 'POST', body: '{}' }), /Não foi possível conectar à API/)
})

test('trata erro sem JSON e resposta de sucesso inválida', async t => {
  const fetch = t.mock.method(globalThis, 'fetch', async () => new Response('Indisponível', { status: 502 }))
  await assert.rejects(request('/products'), /HTTP 502/)
  fetch.mock.mockImplementation(async () => new Response('<html>Outra página</html>'))
  await assert.rejects(request('/products'), /resposta inválida/)
})

test('mantém cancelamento de uma consulta ao sair da tela', async t => {
  const controller = new AbortController()
  controller.abort()
  t.mock.method(globalThis, 'fetch', async () => { throw new DOMException('Cancelado', 'AbortError') })
  await assert.rejects(request('/products', { signal: controller.signal }), { name: 'AbortError' })
})
