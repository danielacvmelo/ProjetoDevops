import assert from 'node:assert/strict'
import test from 'node:test'
import { normalize, validateProduct, validateSupplier } from '../src/domain/cadastros.ts'
import type { Product } from '../src/domain/cadastros.ts'

const product: Product = { id: '1', code: 'PRD-001', name: 'Hibisco', category: 'Chás', unit: 'kg', purchasePrice: 33, salePrice: 50, stock: 1.5, minimumStock: 0.5, status: 'Ativo' }

test('aceita produto válido, estoque fracionário e preços zero', () => {
  assert.equal(validateProduct(product, []), '')
  assert.equal(validateProduct({ ...product, salePrice: 0, purchasePrice: 0 }, []), '')
})

test('rejeita nome e unidade contendo apenas espaços', () => {
  assert.notEqual(validateProduct({ ...product, name: '   ' }, []), '')
  assert.notEqual(validateProduct({ ...product, unit: '   ' }, []), '')
})

test('rejeita preços e estoques negativos ou não numéricos', () => {
  for (const key of ['purchasePrice', 'salePrice', 'stock', 'minimumStock'] as const) {
    for (const value of [-1, NaN, Infinity]) assert.notEqual(validateProduct({ ...product, [key]: value }, []), '')
  }
})

test('impede código duplicado, permite editar o próprio produto e aceita código vazio', () => {
  assert.notEqual(validateProduct({ ...product, id: '2', code: ' prd-001 ' }, [product]), '')
  assert.equal(validateProduct({ ...product, name: 'Hibisco seco' }, [product]), '')
  assert.equal(validateProduct({ ...product, id: '2', code: '' }, [product]), '')
})

test('rejeita fornecedor sem nome ou duplicado ignorando acentos e espaços', () => {
  const supplier = { id: '1', name: 'Raízes Naturais', email: '', phone: '' }
  assert.equal(validateSupplier(supplier, []), '')
  assert.notEqual(validateSupplier({ ...supplier, name: '  ' }, []), '')
  assert.notEqual(validateSupplier({ ...supplier, id: '2', name: ' raizes naturais ' }, [supplier]), '')
})

test('normaliza busca com acentos, maiúsculas e espaços', () => {
  assert.equal(normalize('  CHÁS  '), 'chas')
  assert.ok(normalize(`${product.name} ${product.code} ${product.category}`).includes(normalize('chás')))
})
