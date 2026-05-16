package model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    private Categoria categoriaValida;

    @BeforeEach
    void setUp() {
        categoriaValida = new Categoria(1, "Categoria Teste", "Categoria de Teste");
    }

    // CA04.1
    // O sistema deve permitir a inserção de nome, descrição, quantidade,
    // preço de compra, preço de venda e selecionar uma categoria

    @Test
    @DisplayName("CT01-C01 - Produto válido")
    void deveAceitarProdutoComTodosCamposValidos() {
        Produto produto = new Produto(
                1,
                "Produto",
                "Produto Teste",
                10,
                2000.0,
                2500.0,
                categoriaValida
        );

        assertAll("Verificando todos os campos",
                () -> assertEquals(1, produto.getId()),
                () -> assertEquals("Produto", produto.getNome()),
                () -> assertEquals("Produto Teste", produto.getDescricao()),
                () -> assertEquals(10, produto.getQuantidade()),
                () -> assertEquals(2000.0, produto.getPrecoCompra()),
                () -> assertEquals(2500.0, produto.getPrecoVenda()),
                () -> assertEquals(categoriaValida, produto.getCategoria())
        );
    }


    // CA04.2
    // Nome, quantidade, preço de compra, preço de venda e categoria são campos obrigatórios

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("CT01-C02/03 - Deve rejeitar nome nulo ou vazio")
    void deveRejeitarNomeInvalido(String nome) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Produto(1, nome, "Descrição", 10, 100.0, 150.0, categoriaValida);
        });
    }


    @Test
    @DisplayName("CT01-C04 - Categoria nula")
    void deveRejeitarCategoriaNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Produto(1, "Produto", "Descrição", 10, 100.0, 150.0, null);
        });
    }

    // CA04.3
    // A quantidade deve ser um número inteiro positivo

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -999})
    @DisplayName("CT01-C05 - Ouantidade inválida")
    void deveRejeitarQuantidadeNegativaOuZero(int quantidadeInvalida) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Produto(1, "Notebook", "Descrição", quantidadeInvalida, 2000.0, 2500.0, categoriaValida);
        });
    }

    // CA04.4
    // Os preços de compra e venda devem ser valores numéricos positivos

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -999.9})
    @DisplayName("CT01-C06 Preço compra inválido")
    void deveRejeitarPrecoCompraNegativoOuZero(double precoInvalido) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Produto(1, "Mouse", "Mouse Gamer", 10, precoInvalido, 150.0, categoriaValida);
        });
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -999.00})
    @DisplayName("CT01-C07 Preço venda inválido")
    void deveRejeitarPrecoVendaNegativoOuZero(double precoInvalido) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Produto(1, "Teclado", "Teclado Mecânico", 5, 100.0, precoInvalido, categoriaValida);
        });
    }
}