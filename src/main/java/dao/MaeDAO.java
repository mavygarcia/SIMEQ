package dao;

import factory.ConnectionFactory;
import modelo.Mae;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaeDAO {

    private final Connection connection;

    public MaeDAO() {
        this.connection = new ConnectionFactory().getConnection();
    }

    // Funcionalidade 1: Cadastro de Mães
    public void cadastrar(Mae mae) {
        String sql = "INSERT INTO mae (nome, telefone, endereco, data_aniversario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mae.getNome());
            stmt.setString(2, mae.getTelefone());
            stmt.setString(3, mae.getEndereco());
            // Converte LocalDate para java.sql.Date
            stmt.setDate(4, java.sql.Date.valueOf(mae.getDataAniversario()));

            stmt.execute();
            System.out.println("Mãe cadastrada com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar Mãe no banco de dados.", e);
        }
    }

    // Funcionalidade 5: Lista de Aniversariantes do Mês
    public List<Mae> listarAniversariantesDoMes(int mes) {
        String sql = "SELECT * FROM mae WHERE MONTH(data_aniversario) = ? ORDER BY DAY(data_aniversario)";
        List<Mae> aniversariantes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Mae mae = new Mae();
                    mae.setIdMae(rs.getInt("id_mae"));
                    mae.setNome(rs.getString("nome"));
                    mae.setTelefone(rs.getString("telefone"));
                    mae.setEndereco(rs.getString("endereco"));
                    // Converte java.sql.Date para LocalDate
                    mae.setDataAniversario(rs.getDate("data_aniversario").toLocalDate());
                    aniversariantes.add(mae);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar aniversariantes do mês.", e);
        }
        return aniversariantes;
    }

    // Método utilitário para buscar uma mãe pelo ID
    public Mae buscarPorId(int id) {
        String sql = "SELECT * FROM mae WHERE id_mae = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Mae mae = new Mae();
                    mae.setIdMae(rs.getInt("id_mae"));
                    mae.setNome(rs.getString("nome"));
                    mae.setTelefone(rs.getString("telefone"));
                    mae.setEndereco(rs.getString("endereco"));
                    mae.setDataAniversario(rs.getDate("data_aniversario").toLocalDate());
                    return mae;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Mãe por ID.", e);
        }
        return null;
    }

    // Método utilitário para listar todas as mães (Necessário para atribuições de serviço)
    public List<Mae> listarTodos() {
        String sql = "SELECT * FROM mae ORDER BY nome";
        List<Mae> maes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Mae mae = new Mae();
                mae.setIdMae(rs.getInt("id_mae"));
                mae.setNome(rs.getString("nome"));
                mae.setTelefone(rs.getString("telefone"));
                mae.setEndereco(rs.getString("endereco"));
                mae.setDataAniversario(rs.getDate("data_aniversario").toLocalDate());
                maes.add(mae);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todas as Mães.", e);
        }
        return maes;
    }
}