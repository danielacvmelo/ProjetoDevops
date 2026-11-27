export type Product = {
  id: string
  code: string
  name: string
  category: string
  unit: string
  purchasePrice: number
  salePrice: number
  stock: number
  minimumStock: number
  status: 'Ativo' | 'Inativo'
}

export type Supplier = { id: string; name: string; email: string; phone: string }

export const normalize = (value: string) => value.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR')

export function validateProduct(product: Product, products: Product[]) {
  if (!product.name.trim() || !product.unit.trim()) return 'Informe o nome e a unidade do produto.'
  if ([product.purchasePrice, product.salePrice, product.stock, product.minimumStock].some(value => !Number.isFinite(value) || value < 0)) return 'Preços e estoques devem ser números maiores ou iguais a zero.'
  if (product.code.trim() && products.some(item => item.id !== product.id && normalize(item.code) === normalize(product.code))) return 'Este código já está cadastrado em outro produto.'
  return ''
}

export function validateSupplier(supplier: Supplier, suppliers: Supplier[]) {
  if (!supplier.name.trim()) return 'Informe o nome do fornecedor.'
  if (suppliers.some(item => item.id !== supplier.id && normalize(item.name) === normalize(supplier.name))) return 'Este fornecedor já está cadastrado.'
  return ''
}

export const formatMoney = (value: number) => value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
