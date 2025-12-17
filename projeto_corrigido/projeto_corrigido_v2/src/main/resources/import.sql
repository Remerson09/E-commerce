-- 1. Insere Pessoas com IDs fixos (OK)
INSERT INTO pessoas (id, email, telefone, tipo_pessoa, nome, cpf, cnpj) VALUES (1, 'joao@email.com', '11987654321', 'F', 'Joao Silva', '12345678901', NULL);
INSERT INTO pessoas (id, email, telefone, tipo_pessoa, razao_social, cnpj, nome, cpf) VALUES (2, 'contato@acme.com', '2133334444', 'J', 'ACME Corporation', '98765432000199', NULL, NULL);

-- 2. Insere Produtos (necessário para o ItemVenda)
INSERT INTO produtos (id, descricao, valor) VALUES (1, 'Café Torrado', 25.00);
INSERT INTO produtos (id, descricao, valor) VALUES (2, 'Água Mineral', 5.00);

-- 3. Insere Vendas (usando o nome da tabela CORRETO e SEM a coluna 'valor')
INSERT INTO vendas (id, cliente_id, data_venda, descricao) VALUES (1, 1, NOW(), 'Venda de produto para Joao Silva');
INSERT INTO vendas (id, cliente_id, data_venda, descricao) VALUES (2, 2, NOW(), 'Venda de consultoria para ACME');

-- 4. Insere Itens de Venda para as vendas acima
INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario) VALUES (1, 1, 5, 25.00); -- Total Venda 1: 125.00
INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario) VALUES (2, 2, 10, 5.00); -- Total Venda 2: 50.00

-- 5. CORREÇÃO DA SEQUÊNCIA (Obrigatória após inserções manuais)
-- Reinicia o contador para o próximo ID livre (ID mais alto inserido + 1)
ALTER TABLE pessoas ALTER COLUMN id RESTART WITH 3;
ALTER TABLE produtos ALTER COLUMN id RESTART WITH 3;
ALTER TABLE vendas ALTER COLUMN id RESTART WITH 3;
ALTER TABLE itens_venda ALTER COLUMN id RESTART WITH 3;