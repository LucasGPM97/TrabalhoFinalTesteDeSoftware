-- Drop existing objects to avoid conflicts
DROP PROCEDURE IF EXISTS ConsultarProdutos;
DROP PROCEDURE IF EXISTS carregar_produto;
DROP PROCEDURE IF EXISTS baixo_estoque;
DROP PROCEDURE IF EXISTS cadastrar_produto;
DROP PROCEDURE IF EXISTS excluir_produto;
DROP PROCEDURE IF EXISTS alterar_produto;
DROP PROCEDURE IF EXISTS entrada_produto;
DROP PROCEDURE IF EXISTS saida_produto;
DROP PROCEDURE IF EXISTS deletar_categoria;
DROP PROCEDURE IF EXISTS cadastrar_categoria;
DROP PROCEDURE IF EXISTS consultar_categoria;
DROP PROCEDURE IF EXISTS update_categoria;
DROP PROCEDURE IF EXISTS registrar_log_movimentacao;
DROP PROCEDURE IF EXISTS relatorio_vendas;

use testdb;

-- Tabelas
CREATE TABLE IF NOT EXISTS categorias (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          nome VARCHAR(100) NOT NULL,
    descricao TEXT
    );

CREATE TABLE IF NOT EXISTS produtos (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    quantidade_estoque INT NOT NULL,
    preco_compra DECIMAL(10, 2) NOT NULL,
    preco_venda DECIMAL(10, 2) NOT NULL,
    categoria_id INT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
    );

CREATE TABLE IF NOT EXISTS movimentacao_estoque (
                                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                                    produto_id INT,
                                                    produto_nome varchar(100) not null,
    operacao VARCHAR(20),
    quantidade INT,
    data TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    );

CREATE TABLE IF NOT EXISTS relatorio_vendas (
                                                id INT AUTO_INCREMENT PRIMARY KEY,
                                                produto_id INT,
                                                produto_nome VARCHAR(100) not null,
    preco_venda DECIMAL(10,2),
    quantidade INT,
    total_venda DECIMAL(10, 2),
    data TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
    );

-- Todas as suas procedures (sem triggers ainda)
DELIMITER $$

CREATE PROCEDURE carregar_produto()
BEGIN
SELECT p.id, p.nome, p.descricao, c.nome AS categoria,
       p.quantidade_estoque, p.preco_compra, p.preco_venda
FROM produtos p LEFT JOIN categorias c ON p.categoria_id = c.id;
END$$

CREATE PROCEDURE baixo_estoque()
BEGIN
SELECT p.id, p.nome, p.descricao, c.nome AS categoria,
       p.quantidade_estoque, p.preco_compra, p.preco_venda
FROM produtos p LEFT JOIN categorias c ON p.categoria_id = c.id
WHERE p.quantidade_estoque <= 20;
END$$

CREATE PROCEDURE cadastrar_produto(
    IN p_nome VARCHAR(100), IN p_descricao TEXT, IN p_qtde INT,
    IN p_preco_compra DECIMAL(10,2), IN p_preco_venda DECIMAL(10,2),
    IN p_categoria_id INT)
BEGIN
INSERT INTO produtos (nome, descricao, quantidade_estoque, preco_compra, preco_venda, categoria_id)
VALUES (p_nome, p_descricao, p_qtde, p_preco_compra, p_preco_venda, p_categoria_id);
END$$

CREATE PROCEDURE excluir_produto(IN p_id INT)
BEGIN
DELETE FROM produtos WHERE id = p_id;
END$$

CREATE PROCEDURE alterar_produto(
    IN p_id INT, IN p_nome VARCHAR(100), IN p_descricao VARCHAR(100),
    IN p_quantidade INT, IN p_preco_compra DECIMAL(10,2),
    IN p_preco_venda DECIMAL(10,2), IN p_categoria_id INT)
BEGIN
UPDATE produtos SET nome = p_nome, descricao = p_descricao,
                    quantidade_estoque = p_quantidade, preco_compra = p_preco_compra,
                    preco_venda = p_preco_venda, categoria_id = p_categoria_id
WHERE id = p_id;
END$$

CREATE PROCEDURE entrada_produto(IN p_id INT, IN p_quantidade INT)
BEGIN
UPDATE produtos SET quantidade_estoque = quantidade_estoque + p_quantidade WHERE id = p_id;
END$$

CREATE PROCEDURE saida_produto(IN p_id INT, IN p_quantidade INT)
BEGIN
UPDATE produtos SET quantidade_estoque = quantidade_estoque - p_quantidade WHERE id = p_id;
END$$

CREATE PROCEDURE deletar_categoria(IN p_id INT)
BEGIN
DELETE FROM categorias WHERE id = p_id;
END$$

CREATE PROCEDURE cadastrar_categoria(IN p_nome VARCHAR(100), IN p_descricao TEXT)
BEGIN
INSERT INTO categorias (nome, descricao) VALUES (p_nome, p_descricao);
END$$

CREATE PROCEDURE consultar_categoria(IN p_nome VARCHAR(100))
BEGIN
SELECT id, nome, descricao FROM categorias WHERE nome LIKE CONCAT('%', p_nome, '%');
END$$

CREATE PROCEDURE update_categoria(IN p_id INT, IN p_nome VARCHAR(100), IN p_descricao TEXT)
BEGIN
UPDATE categorias SET nome = p_nome, descricao = p_descricao WHERE id = p_id;
END$$

CREATE PROCEDURE registrar_log_movimentacao(
    IN p_id INT, IN p_nome_produto VARCHAR(255),
    IN p_operacao VARCHAR(10), IN p_quantidade INT)
BEGIN
INSERT INTO movimentacao_estoque (produto_id, produto_nome, operacao, quantidade)
VALUES (p_id, p_nome_produto, p_operacao, p_quantidade);
END$$

CREATE PROCEDURE ConsultarProdutos(
    IN p_nome VARCHAR(255), IN p_categoria VARCHAR(255),
    IN p_quantidade_min INT, IN p_quantidade_max INT)
BEGIN
SELECT p.* FROM produtos p
                    LEFT JOIN categorias c ON p.categoria_id = c.id
WHERE (p_nome IS NULL OR p.nome LIKE CONCAT('%', p_nome, '%'))
  AND (p_categoria IS NULL OR c.nome LIKE CONCAT('%', p_categoria, '%'))
  AND (p_quantidade_min IS NULL OR p.quantidade_estoque >= p_quantidade_min)
  AND (p_quantidade_max IS NULL OR p.quantidade_estoque <= p_quantidade_max)
ORDER BY p.id;
END$$

CREATE PROCEDURE relatorio_vendas(
    IN p_produto_id INT, IN p_produto_nome VARCHAR(100),
    IN p_preco_venda DECIMAL(10,2), IN p_quantidade INT,
    IN p_total_venda DECIMAL(10,2))
BEGIN
INSERT INTO relatorio_vendas (produto_id, produto_nome, preco_venda, quantidade, total_venda)
VALUES (p_produto_id, p_produto_nome, p_preco_venda, p_quantidade, p_total_venda);
END$$

DELIMITER ;