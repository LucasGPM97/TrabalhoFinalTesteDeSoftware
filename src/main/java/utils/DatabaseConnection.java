package utils;

import java.sql.*;

public class DatabaseConnection {

    private static  String url = "jdbc:mysql://localhost:3306/testdb";  // URL do banco de dados
    private static  String user = "test";  // Usuário do banco de dados - inserir
    private static  String password = "test";  // Senha do banco de dados - inserir
    private static Connection conexao;

    private DatabaseConnection(){

    }

    // Método para conectar ao banco de dados
    public static Connection getConnection() {
        if (conexao == null) {
            try {
                conexao = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Mensagem de erro: " + e.getMessage());
                throw new RuntimeException("Erro ao conectar ao banco de dados", e);
            }
        }
        return conexao;
    }

    //  setters para testes (Testcontainers)
    public static void setUrl(String url) {
        DatabaseConnection.url = url;
        conexao = null; // força nova conexão
    }

    public static void setUser(String user) {
        DatabaseConnection.user = user;
        conexao = null;
    }

    public static void setPassword(String password) {
        DatabaseConnection.password = password;
        conexao = null;
    }

    // Método para fechar a conexão
    public static void closeConnection() {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}