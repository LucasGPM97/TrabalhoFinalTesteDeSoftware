use testdb;

DROP TRIGGER IF EXISTS movimentacao_estoque_trigger;
DROP TRIGGER IF EXISTS relatorio_vendas_trigger;
DROP TRIGGER IF EXISTS verificar_estoque_baixo;

DELIMITER $$

CREATE TRIGGER movimentacao_estoque_trigger
    AFTER UPDATE ON produtos
    FOR EACH ROW
BEGIN
    DECLARE operacao VARCHAR(10);
    DECLARE quantidade INT;
    DECLARE nome_produto VARCHAR(100);

    IF OLD.quantidade_estoque <> NEW.quantidade_estoque THEN
        SET nome_produto = NEW.nome;
        IF NEW.quantidade_estoque > OLD.quantidade_estoque THEN
            SET operacao = 'Entrada';
            SET quantidade = NEW.quantidade_estoque - OLD.quantidade_estoque;
    ELSE
            SET operacao = 'Saida';
            SET quantidade = OLD.quantidade_estoque - NEW.quantidade_estoque;
END IF;
CALL registrar_log_movimentacao(NEW.id, nome_produto, operacao, quantidade);
END IF;
END$$

CREATE TRIGGER relatorio_vendas_trigger
    AFTER UPDATE ON produtos
    FOR EACH ROW
BEGIN
    DECLARE valor_total DECIMAL(10,2);
    DECLARE quantidade INT;
    IF OLD.quantidade_estoque <> NEW.quantidade_estoque THEN
        IF NEW.quantidade_estoque < OLD.quantidade_estoque THEN
            SET quantidade = OLD.quantidade_estoque - NEW.quantidade_estoque;
            SET valor_total = quantidade * NEW.preco_venda;
    CALL relatorio_vendas(NEW.id, NEW.nome, NEW.preco_venda, quantidade, valor_total);
END IF;
END IF;
END$$

CREATE TRIGGER verificar_estoque_baixo
    AFTER UPDATE ON produtos
    FOR EACH ROW
BEGIN
    DECLARE mensagem VARCHAR(255);
    IF OLD.quantidade_estoque <> NEW.quantidade_estoque THEN
        IF NEW.quantidade_estoque < OLD.quantidade_estoque THEN
            IF NEW.quantidade_estoque < 0 THEN
                SET mensagem = CONCAT('Aviso: Estoque baixo para o produto ', NEW.nome);
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = mensagem;
END IF;
END IF;
END IF;
END$$

DELIMITER ;