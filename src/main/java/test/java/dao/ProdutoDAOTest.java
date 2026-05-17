package dao;

import model.Categoria;
import model.Produto;
import org.junit.jupiter.api.*;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoDAOTest {

    private static ProdutoDAO produtoDAO;
    private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/testdb";
    private static final String TEST_DB_USER = "test";
    private static final String TEST_DB_PASSWORD = "test";

    private static Categoria categoriaTeste;
    private static final int CATEGORIA_ID = 1;
    private static final String CATEGORIA_NOME = "Categoria Teste";
    private static final String CATEGORIA_DESCRICAO = "Categoria para testes de integração";

    @BeforeAll
    static void setUpClass() throws Exception {
        // Configurar conexão do banco de testes
        DatabaseConnection.setUrl(TEST_DB_URL);
        DatabaseConnection.setUser(TEST_DB_USER);
        DatabaseConnection.setPassword(TEST_DB_PASSWORD);

        produtoDAO = new ProdutoDAO();

        // Criar categoria base para todos os testes
        categoriaTeste = new Categoria(CATEGORIA_ID, CATEGORIA_NOME, CATEGORIA_DESCRICAO);
        criarCategoriaNoBanco(categoriaTeste);
    }

    @BeforeEach
    void setUp() throws Exception {
        // Limpar tabelas antes de cada teste (ordem correta respeitando FK)
        limparTabelas();
    }

    @AfterAll
    static void tearDownClass() throws Exception {
        // Limpar tudo ao final dos testes
        limparTabelas();

        // Remover categoria criada
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM categorias WHERE id = " + CATEGORIA_ID);
        }

        DatabaseConnection.closeConnection();
    }


    // CA04.1
    // O sistema deve permitir a inserção de nome, descrição, quantidade, preço de compra,
    // preço de venda e selecionar uma categoria

    // CA04.5
    // Após o cadastro, o novo produto deve ser exibido na lista de produtos


    @Test
    @DisplayName("CT03-C01 Persistencia de Produto")
    void deveInserirProdutoComTodosCampos() throws Exception {

        Produto produto = new Produto(0, "Produto Valido", "Esse produto esta correto", 21, 150.0, 299.90, categoriaTeste);

        boolean inserido = produtoDAO.inserir(produto);
        List<Produto> lista = produtoDAO.listar();

        assertTrue(inserido);
        assertEquals(1, lista.size());

        Produto salvo = lista.get(0);
        assertAll("Verificar produto inserido",
                () -> assertEquals("Produto Valido", salvo.getNome()),
                () -> assertEquals("Esse produto esta correto", salvo.getDescricao()),
                () -> assertEquals(21, salvo.getQuantidade()),
                () -> assertEquals(150.0, salvo.getPrecoCompra()),
                () -> assertEquals(299.90, salvo.getPrecoVenda()),
                () -> assertEquals(categoriaTeste.getId(), salvo.getCategoria().getId())
        );
    }

    @Test
    @DisplayName("CT03-C02 Persistência de produto inválido")
    void naoDeveInserirProdutoComCategoriaInexistente() throws Exception {

        Categoria categoriaInexistente =
                new Categoria(999, "Inexistente", "Categoria não cadastrada");

        Produto produtoInvalido =
                new Produto(
                        0,
                        "Produto Inválido",
                        "Desc",
                        10,
                        10.0,
                        20.0,
                        categoriaInexistente
                );


        boolean inserido = produtoDAO.inserir(produtoInvalido);

        List<Produto> lista = produtoDAO.listar();

        assertFalse(inserido);

        assertEquals(0, lista.size());
    }

    // CA04.5
    // Após o cadastro, o novo produto deve ser exibido na lista de produtos

    @Test
    @DisplayName("CT03-C03 - Consulta após inserção")
    void produtoCadastradoDeveAparecerNaLista() throws Exception {

        Produto produto = criarProdutoTeste("Produto Consulta", 10, 2000.0, 2500.0);

        boolean inserido = produtoDAO.inserir(produto);
        List<Produto> lista = produtoDAO.listar();

        assertTrue(inserido, "Produto deveria ser inserido");
        assertEquals(1, lista.size(), "Lista deveria conter 1 produto");
        assertEquals("Produto Consulta", lista.get(0).getNome());
    }

    // MÉTODOS AUXILIARES

    private static void criarCategoriaNoBanco(Categoria categoria) throws SQLException {
        String sql = String.format(
                "INSERT INTO categorias (id, nome, descricao) VALUES (%d, '%s', '%s') " +
                        "ON DUPLICATE KEY UPDATE nome = '%s', descricao = '%s'",
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getNome(),
                categoria.getDescricao()
        );

        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void limparTabelas() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Desabilitar verificação de chaves estrangeiras
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Limpar tabelas na ordem correta
            stmt.execute("DELETE FROM movimentacao_estoque");
            stmt.execute("DELETE FROM relatorio_vendas");
            stmt.execute("DELETE FROM produtos");

            // Reabilitar verificação de chaves estrangeiras
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private Produto criarProdutoTeste(String nome, int quantidade, double precoCompra, double precoVenda) {
        return new Produto(0, nome, "Descrição do produto " + nome, quantidade, precoCompra, precoVenda, categoriaTeste);
    }

    private Produto criarEInserirProduto(String nome, int quantidade, double precoCompra, double precoVenda) throws SQLException {
        Produto produto = criarProdutoTeste(nome, quantidade, precoCompra, precoVenda);
        boolean inserido = produtoDAO.inserir(produto);
        assertTrue(inserido, "Produto deveria ter sido inserido com sucesso");
        return produto;
    }


}