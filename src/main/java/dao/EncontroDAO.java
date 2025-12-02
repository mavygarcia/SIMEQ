package dao;

import factory.ConnectionFactory;
import modelo.Encontro;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EncontroDAO {

    private final Connection connection;

    public EncontroDAO() {
        this.connection = new ConnectionFactory().getConnection();
    }

    // Funcionalidade 2: Cadastro de Encontros
    public int cadastrar(Encontro encontro) {
        String sql = "INSERT INTO encontro (data_encontro, status_realizado) VALUES (?, ?)";
        // Retorna o ID gerado (RETURN_GENERATED_KEYS)
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Converte LocalDate para java.sql.Date
            stmt.setDate(1, java.sql.Date.valueOf(encontro.getDataEncontro()));
            stmt.setBoolean(2, encontro.isRealizado());

            stmt.execute();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Retorna o ID do encontro recém-cadastrado
                }
            }
            return -1; // Caso não consiga obter a chave
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar Encontro.", e);
        }
    }

    // Funcionalidade 4: Edição de Encontros (apenas para encontros futuros)
    public void editar(Encontro encontro) {
        String sql = "UPDATE encontro SET data_encontro = ?, status_realizado = ? WHERE id_encontro = ?";

        // Verifica se a data é futura antes de editar (Lógica de Negócio)
        if (encontro.getDataEncontro().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Não é permitido editar encontros passados.");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(encontro.getDataEncontro()));
            stmt.setBoolean(2, encontro.isRealizado());
            stmt.setInt(3, encontro.getIdEncontro());
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao editar Encontro.", e);
        }
    }

    // Funcionalidade 4: Exclusão Lógica (para encontros que já deveriam ter ocorrido)
    public void excluirLogicamente(int idEncontro) {
        // Encontra o encontro para verificar a data
        Encontro encontro = buscarPorId(idEncontro);

        if (encontro == null) {
            throw new IllegalArgumentException("Encontro não encontrado.");
        }

        // Se a data já passou, faz a exclusão lógica (status_realizado = FALSE)
        if (encontro.getDataEncontro().isBefore(LocalDate.now())) {
            String sql = "UPDATE encontro SET status_realizado = FALSE WHERE id_encontro = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, idEncontro);
                stmt.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao excluir logicamente o Encontro.", e);
            }
        } else {
            // Se o encontro é futuro, remove do BD (Exclusão Física)
            removerDoBanco(idEncontro);
        }
    }

    // Método auxiliar para exclusão física de encontros futuros (se a data ainda não tiver ocorrido)
    private void removerDoBanco(int idEncontro) {
        try {
            // CORREÇÃO CRÍTICA: Primeiramente, exclua os registros em ENCONTRO_SERVICO
            // para evitar a exceção de Foreign Key ao tentar excluir o ENCONTRO.
            String sqlDeleteServicos = "DELETE FROM encontro_servico WHERE id_encontro = ?";
            try (PreparedStatement stmtServicos = connection.prepareStatement(sqlDeleteServicos)) {
                stmtServicos.setInt(1, idEncontro);
                stmtServicos.execute();
            }

            // Em seguida, exclua o registro principal na tabela ENCONTRO
            String sqlDeleteEncontro = "DELETE FROM encontro WHERE id_encontro = ?";
            try (PreparedStatement stmtEncontro = connection.prepareStatement(sqlDeleteEncontro)) {
                stmtEncontro.setInt(1, idEncontro);
                stmtEncontro.execute();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover Encontro (Falha na exclusão dos registros relacionados).", e);
        }
    }

    public Encontro buscarPorId(int id) {
        String sql = "SELECT * FROM encontro WHERE id_encontro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Encontro encontro = new Encontro();
                    encontro.setIdEncontro(rs.getInt("id_encontro"));
                    // Converte java.sql.Date para LocalDate
                    encontro.setDataEncontro(rs.getDate("data_encontro").toLocalDate());
                    encontro.setRealizado(rs.getBoolean("status_realizado"));
                    return encontro;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Encontro por ID.", e);
        }
        return null;
    }

    public List<Encontro> listarTodos() {
        String sql = "SELECT * FROM encontro ORDER BY data_encontro DESC";
        List<Encontro> encontros = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Encontro encontro = new Encontro();
                encontro.setIdEncontro(rs.getInt("id_encontro"));
                encontro.setDataEncontro(rs.getDate("data_encontro").toLocalDate());
                encontro.setRealizado(rs.getBoolean("status_realizado"));
                encontros.add(encontro);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todos os Encontros.", e);
        }
        return encontros;
    }
}