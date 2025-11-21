package factory;

import java.sql.Connection; // conexão SQL para JAVA
import java.sql.DriverManager; // dirver de conexão SQL para JAVA
import java.sql.SQLException; // classe para tratamento de exceções



public class ConnectionFactory {
    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/SIMEQ", "root", "Vitoria.1405");
        } catch (SQLException excecao) {
            throw new RuntimeException(excecao);
        }
    }
}