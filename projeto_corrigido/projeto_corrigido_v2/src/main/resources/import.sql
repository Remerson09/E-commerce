-- 1. ROLES
INSERT INTO role (id, nome) VALUES (1,'ROLE_ADMIN');
INSERT INTO role (id, nome) VALUES (2,'ROLE_USER');

-- 2. PESSOAS (Necessário para os usuários e vendas abaixo)
-- Inserindo o João Silva (ID 1) e a ACME (ID 2)
INSERT INTO pessoas (id, email, telefone, tipo_pessoa, nome, cpf) VALUES (1, 'joao@email.com', '11987654321', 'F', 'Joao Silva', '12345678901');
INSERT INTO pessoas (id, email, telefone, tipo_pessoa, razao_social, cnpj) VALUES (2, 'contato@acme.com', '2133334444', 'J', 'ACME Corporation', '98765432000199');

-- 3. USUÁRIOS (Login: admin / joao | Senha: 123)
-- VINCULANDO O USUÁRIO À PESSOA PELO pessoa_id (conforme mapeamento OneToOne em Usuario)
INSERT INTO usuario (id, login, password, pessoa_id) VALUES (1, 'admin', '$2a$12$uS6AKt6H9daz2gaTVpg8G.xcg9swDH5CV5rDwJKEYzvdSX32vr7hS', 2);
INSERT INTO usuario (id, login, password, pessoa_id) VALUES (2, 'joao', '$2a$12$S8mbuDfX1DrG.Rnl.OW/v.MSgndeLgaJRp2iSCoADy8AKqL25xNoC', 1);

-- 4. ASSOCIAÇÃO DE ROLES
INSERT INTO usuario_roles (usuarios_id, roles_id) VALUES (1, 1);
INSERT INTO usuario_roles (usuarios_id, roles_id) VALUES (2, 2);

-- 5. PRODUTOS
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (1, 'Café Torrado', 25.00, 'https://images.tcdn.com.br/img/img_prod/1303842/cafe_torrado_e_moido_500gr_torra_media_77_2_dd6c41c3b45515dafc757784acce0c19.jpg');
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (2, 'Água Voss', 5.00, 'https://images.tcdn.com.br/img/img_prod/1054850/180_agua_voss_norueguesa_com_gas_vidro_800ml_1075_1_8cfbf69dad867977c8de0e50a059ec49.png');
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (3, 'Cápsula de Espresso', 32.90, 'https://www.cafefacil.com.br/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/1/3/1363_0_1.jpg');
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (4, 'Biscoito Amanteigado', 12.50, 'https://mercantilnovaera.vtexassets.com/arquivos/ids/207196/Biscoito-Amanteigado-Fortaleza-Tradicional-Embalagem-330.jpg?v=638203819927170000');
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (5, 'Chocolate Amargo 70%', 18.00, 'https://img.megaboxatacado.com.br/produto/1000X1000/2023621_alind.jpg');
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (6, 'Chá Verde Orgânico', 15.00, 'https://m.media-amazon.com/images/I/517x9TRfkhL._AC_UF894,1000_QL80_.jpg');
INSERT INTO produtos (id, descricao, valor, imagem_url) VALUES (7, 'Caneca de Cerâmica', 45.00, 'https://portobrasil.vtexassets.com/arquivos/ids/179001/11314109201---CANECA-ORGANICO-LITCHI---02.jpg?v=638219123923230000');

-- 6. VENDAS
INSERT INTO vendas (id, cliente_id, data_venda, descricao) VALUES (1, 1, NOW(), 'Venda inicial para Joao Silva');
INSERT INTO vendas (id, cliente_id, data_venda, descricao) VALUES (2, 2, NOW(), 'Venda inicial para ACME');

-- 7. ITENS DE VENDA
INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario) VALUES (1, 1, 5, 25.00);
INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario) VALUES (2, 2, 10, 5.00);

-- 8. REINICIA SEQUENCES (Ajustado para o próximo ID disponível)
ALTER TABLE produtos ALTER COLUMN id RESTART WITH 100;
ALTER TABLE pessoas ALTER COLUMN id RESTART WITH 100;
ALTER TABLE vendas ALTER COLUMN id RESTART WITH 100;
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 100;
ALTER TABLE role ALTER COLUMN id RESTART WITH 100;