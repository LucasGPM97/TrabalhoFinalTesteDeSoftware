package e2e;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.jupiter.api.*;
import view.TelaInicialView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.finder.JOptionPaneFinder.findOptionPane;
import static org.junit.jupiter.api.Assertions.*;

class FluxoPrincipalTest {

    private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/testdb";
    private static final String TEST_DB_USER = "test";
    private static final String TEST_DB_PASSWORD = "test";

    private static final String RELATORIOS_DIR = System.getProperty("user.home");
    private static final Timeout TIMEOUT = Timeout.timeout(10000);

    private FrameFixture janelaPrincipal;
    private static long timestamp;


    @BeforeAll
    static void setUpBanco() throws SQLException {
        timestamp = System.currentTimeMillis();
        limparBancoCompleto();
        resetarSequencias();
    }

    private static void limparBancoCompleto() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DELETE FROM movimentacao_estoque");
            stmt.execute("DELETE FROM relatorio_vendas");
            stmt.execute("DELETE FROM produtos");
            stmt.execute("DELETE FROM categorias");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private static void resetarSequencias() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE categorias AUTO_INCREMENT = 1");
            stmt.execute("ALTER TABLE produtos AUTO_INCREMENT = 1");
        }
    }

    private void limparDadosTeste(String... nomesCategorias) throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Deletar produtos
            for (String nomeCategoria : nomesCategorias) {
                // Produtos que pertencem a esta categoria
                stmt.execute("DELETE FROM produtos WHERE categoria_id IN (SELECT id FROM categorias WHERE nome = '" + nomeCategoria + "')");
            }

            // Deletar categorias
            for (String nomeCategoria : nomesCategorias) {
                stmt.execute("DELETE FROM categorias WHERE nome = '" + nomeCategoria + "'");
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    @BeforeEach
    void setUp() {
        TelaInicialView frame = GuiActionRunner.execute(TelaInicialView::new);
        janelaPrincipal = new FrameFixture(frame);
        janelaPrincipal.show();
        Pause.pause(1000);
    }

    @AfterEach
    void tearDown() {
        if (janelaPrincipal != null) {
            janelaPrincipal.cleanUp();
        }
    }

    // =========================
    // TESTES
    // =========================

    @Test
    @DisplayName("Teste E2E - Fluxo Principal")
    void fluxoPrincipal() throws Exception {
        String nomeCategoria = "Fluxo Principal_" + timestamp;
        String nomeProduto = "Produto Fictício_" + timestamp;

        try {
            // 1. CRIAR CATEGORIA
            janelaPrincipal.button("btnCategorias").click();
            Pause.pause(1500);

            FrameFixture janelaCategoria = findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
                @Override
                protected boolean isMatching(JFrame frame) {
                    return frame.isShowing() && frame.getTitle().equals("Gerenciar Categorias");
                }
            }).withTimeout(TIMEOUT.duration()).using(janelaPrincipal.robot());

            janelaCategoria.textBox("txtNomeCategoria").enterText(nomeCategoria);
            janelaCategoria.textBox("txtDescricaoCategoria").enterText("Categoria para relatório");
            janelaCategoria.button("btnSalvarCategoria").click();

            confirmarDialogoSim();
            fecharDialogoOk();

            janelaCategoria.close();
            Pause.pause(1000);


            // 2. CRIAR PRODUTO
            janelaPrincipal.button("btnProdutos").click();
            Pause.pause(1500);

            FrameFixture janelaProduto = findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
                @Override
                protected boolean isMatching(JFrame frame) {
                    return frame.isShowing() && frame.getTitle().equals("Gerenciar Produtos");
                }
            }).withTimeout(TIMEOUT.duration()).using(janelaPrincipal.robot());

            janelaProduto.textBox("txtNomeProduto").enterText(nomeProduto);
            janelaProduto.textBox("txtDescricaoProduto").enterText("Produto para teste");
            janelaProduto.textBox("txtQuantidadeProduto").enterText("10");
            janelaProduto.textBox("txtPrecoCompra").enterText("50");
            janelaProduto.textBox("txtPrecoVenda").enterText("100");

            selecionarItemPorNome(janelaProduto.comboBox("cmbCategoria"), nomeCategoria);

            janelaProduto.button("btnSalvar").click();
            confirmarDialogoSim();
            fecharDialogoOk();

            janelaProduto.close();
            Pause.pause(1000);


            // 3. FAZER MOVIMENTAÇÃO DE ENTRADA
            janelaPrincipal.button("btnMovimentacao").click();
            Pause.pause(1500);

            FrameFixture janelaMovimentacao = findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
                @Override
                protected boolean isMatching(JFrame frame) {
                    return frame.getTitle().equals("Movimentar Estoque") && frame.isShowing();
                }
            }).withTimeout(TIMEOUT.duration()).using(janelaPrincipal.robot());

            janelaMovimentacao.button("btnListar").click();
            Pause.pause(2000);

            janelaMovimentacao.table("tableProdutos").selectRows(0);
            Pause.pause(500);

            janelaMovimentacao.comboBox("cmbOperacao").selectItem("Entrada");
            Pause.pause(300);
            janelaMovimentacao.textBox("txtQuantidade").enterText("5");
            janelaMovimentacao.button("btnRealizarOperacao").click();

            confirmarDialogoSim();
            fecharDialogoOk();

            janelaMovimentacao.close();
            Pause.pause(1000);

            // 4. GERAR RELATÓRIO
            janelaPrincipal.button("btnRelatorios").click();
            Pause.pause(1500);

            JOptionPaneFixture opcaoPane = findOptionPane()
                    .withTimeout(TIMEOUT.duration())
                    .using(janelaPrincipal.robot());

            opcaoPane.comboBox().selectItem("Relatorio de Movimentação de Estoque");
            Pause.pause(500);
            opcaoPane.okButton().click();

            // 4.1 INTERAGIR COM JFileChooser USANDO ROBOT
            Pause.pause(2000);

            Robot robot = new Robot();

            // Limpar qualquer texto existente (Ctrl+A + Delete)
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            Pause.pause(100);

            robot.keyPress(KeyEvent.VK_DELETE);
            robot.keyRelease(KeyEvent.VK_DELETE);
            Pause.pause(100);

            // Digitar o nome do arquivo
            String fileName = "relatorio_movimentacao_estoque.pdf";
            for (char c : fileName.toCharArray()) {
                robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
                robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(c));
                Pause.pause(20);
            }

            Pause.pause(500);

            // Pressionar Enter para salvar
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            Pause.pause(2000);

            // Se aparecer diálogo de confirmação de substituição (perguntar se quer sobrescrever)
            try {
                JOptionPaneFixture confirmPane = findOptionPane()
                        .withTimeout(Timeout.timeout(3000).duration())
                        .using(janelaPrincipal.robot());
                confirmPane.yesButton().click();
                Pause.pause(1000);
            } catch (Exception e) {
                // Não havia diálogo de confirmação
                System.out.println("Nenhum diálogo de confirmação apareceu");
            }

            // Aguardar o arquivo ser salvo
            Pause.pause(3000);


            // 5. VALIDAR
            File relatorioFile = encontrarUltimoRelatorio();

            assertNotNull(relatorioFile, "Relatório não foi gerado");
            assertTrue(relatorioFile.exists(), "Arquivo de relatório não existe em: " + relatorioFile.getAbsolutePath());
            assertTrue(relatorioFile.length() > 0, "Relatório está vazio");


            // Fechar visualização do PDF
            Desktop.getDesktop().open(relatorioFile);
            Pause.pause(2000);
            fecharVisualizadorPDF();

        } finally {
            limparDadosTeste(nomeCategoria);
            limparRelatoriosGerados();
        }
    }


    // MÉTODOS AUXILIARES

    private void confirmarDialogoSim() {
        Pause.pause(1000);
        try {
            JOptionPaneFixture opcaoPane = findOptionPane()
                    .withTimeout(TIMEOUT.duration())
                    .using(janelaPrincipal.robot());
            opcaoPane.yesButton().click();
            Pause.pause(500);
        } catch (Exception e) {

        }
    }

    private void fecharDialogoOk() {
        Pause.pause(1000);
        try {
            JOptionPaneFixture opcaoPane = findOptionPane()
                    .withTimeout(TIMEOUT.duration())
                    .using(janelaPrincipal.robot());
            opcaoPane.okButton().click();
            Pause.pause(500);
        } catch (Exception e) {
        }
    }

    private void selecionarItemPorNome(JComboBoxFixture comboBox, String nomeBuscado) {
        Pause.pause(500);

        String[] itens = comboBox.contents();
        for (int i = 0; i < itens.length; i++) {
            if (itens[i].contains(nomeBuscado)) {
                comboBox.selectItem(i);
                Pause.pause(300);
                return;
            }
        }
        throw new AssertionError("Item não encontrado no comboBox: " + nomeBuscado);
    }


    // MÉTODOS AUXILIARES - RELATÓRIOS

    private File encontrarUltimoRelatorio() {
        File homeDir = new File(RELATORIOS_DIR);
        File[] matches = homeDir.listFiles((dir, name) ->
                name.matches("relatorio[-_]movimentacao.*\\.pdf"));

        if (matches != null && matches.length > 0) {
            File ultimo = matches[0];
            for (File f : matches) {
                if (f.lastModified() > ultimo.lastModified()) {
                    ultimo = f;
                }
            }
            return ultimo;
        }
        return null;
    }

    private void limparRelatoriosGerados() {
        File homeDir = new File(RELATORIOS_DIR);
        File[] matches = homeDir.listFiles((dir, name) ->
                name.matches("relatorio[-_]movimentacao.*\\.pdf"));

        if (matches != null) {
            for (File f : matches) {
                f.delete();
            }
        }
    }

    private void fecharVisualizadorPDF() throws AWTException {
        try {
            Robot robot = new Robot();
            Pause.pause(1000);
            robot.keyPress(java.awt.event.KeyEvent.VK_ALT);
            robot.keyPress(java.awt.event.KeyEvent.VK_F4);
            robot.keyRelease(java.awt.event.KeyEvent.VK_F4);
            robot.keyRelease(java.awt.event.KeyEvent.VK_ALT);
        } catch (AWTException e) {
        }
    }
}