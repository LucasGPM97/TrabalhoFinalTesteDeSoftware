package utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadoresTest {

    private Validadores validadores;

    @BeforeEach
    void setUp() {
        validadores = new Validadores();
    }

    // CA04.1
    // O sistema deve permitir a inserção de nome, descrição, quantidade, preço de compra,
    // preço de venda e selecionar uma categoria

    @Test
    @DisplayName("CT02-C01 Produto válido")
    void deveAceitarProdutoComTodosCamposValidos() {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class)) {
            boolean resultado = validadores.validarProduto(
                    "Produto",
                    "Produto Teste",
                    "10",
                    "2500.0",
                    "2000.0",
                    "CategoriaTeste"
            );

            assertTrue(resultado, "Validador deveria retornar true");
            mockedJOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), anyString()), never());
        }
    }

    // CA04.2
    // Nome, quantidade, preço de compra, preço de venda e categoria são campos obrigatórios

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("CT02-C02/C03 Nome vazio ou nulo")
    void deveRejeitarNomeVazioOuNulo(String nome) {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class)) {
            boolean resultado = validadores.validarProduto(
                    nome,
                    "Produto Teste",
                    "10",
                    "2500.0",
                    "2000.0",
                    "CategoriaTeste"
            );

            assertFalse(resultado, "Validador deveria rejeitar o nome " + nome);
        }
    }


    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("CT02-C04 - Categoria vazia ou nula")
    void deveRejeitarCategoriaVaziaOuNula(String categoria) {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class)) {
            boolean resultado = validadores.validarProduto(
                    "Produto",
                    "Produto Teste",
                    "10",
                    "2500.0",
                    "2000.0",
                    categoria
            );

            assertFalse(resultado, "Validador deveria rejeitar a categoria " + categoria );
        }
    }


    // CA04.3
    // A quantidade deve ser um número inteiro positivo


    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-999", "a", "10.4", "10,6"})
    @DisplayName("CT02-C05 - Quantidade inválida")
    void deveRejeitarQuantidadeInvalida(String quantidadeInvalida) {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class)) {
            boolean resultado = validadores.validarProduto(
                    "Produto",
                    "Produto Teste",
                    quantidadeInvalida,
                    "2500.0",
                    "2000.0",
                    "CategoriaTeste"
            );

            assertFalse(resultado,"Validador deveria rejeitar a quantidade " + quantidadeInvalida );
        }
    }

    // CA04.4
    // Os preços de compra e venda devem ser valores numéricos positivos

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-999.99", "a"})
    @DisplayName("CT02-C06 - Preço compra inválido")
    void deveRejeitarPrecoCompraInvalido(String precoInvalido) {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class)) {
            boolean resultado = validadores.validarProduto(
                    "Produto",
                    "Produto Teste",
                    "10",
                    "2500.0",
                    precoInvalido,
                    "CategoriaTeste"
            );

            assertFalse(resultado,"Validador deveria rejeitar o valor " + precoInvalido );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-999.99", "a"})
    @DisplayName("CT02-C07 - Preço venda inválido")
    void deveRejeitarPrecoVendaNegativoOuZero(String precoInvalido) {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class)) {
            boolean resultado = validadores.validarProduto(
                    "Produto",
                    "Produto Teste",
                    "5",
                    precoInvalido,
                    "2000.0",
                    "CategoriaTeste"
            );

            assertFalse(resultado,"Validador deveria rejeitar o valor " + precoInvalido );
        }
    }

}