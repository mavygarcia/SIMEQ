package gui;

import dao.EncontroDAO;
import dao.EncontroServicoDAO;
import dao.MaeDAO;
import dao.ServicoDAO;
import modelo.Encontro;
import modelo.EncontroServico;
import modelo.Mae;
import modelo.Servico;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ADICIONAR IMPORTAÇÃO DO HBOX:
import javafx.scene.layout.HBox;

public class PainelCadastroEncontro extends VBox {

    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final MaeDAO maeDAO = new MaeDAO();
    private final EncontroDAO encontroDAO = new EncontroDAO();
    private final EncontroServicoDAO esDAO = new EncontroServicoDAO();

    // Mapas para armazenar a Mãe e a Descrição para cada serviço
    private final Map<Integer, ComboBox<Mae>> comboMaes = new HashMap<>();
    private final Map<Integer, TextField> camposDescricao = new HashMap<>();

    public PainelCadastroEncontro() {
        super(15);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(20));

        Label titulo = new Label("Cadastro de Novo Encontro e Atribuição de Serviços");
        titulo.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        titulo.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));

        // 1. Campo de Data
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPromptText("Selecione a Data do Encontro");

        // HBox agora está resolvido
        HBox dataBox = new HBox(10, new Label("Data:"), datePicker);
        dataBox.setAlignment(Pos.CENTER);

        // 2. Seção de Atribuição de Serviços
        GridPane servicosGrid = criarGridServicos();

        // 3. Botão de Cadastro
        Button btnCadastrar = new Button("Cadastrar Encontro e Serviços");
        btnCadastrar.setStyle("-fx-font-size: 16px; -fx-background-color: #FFC0CB;"); // Estilo rosa

        // Lógica principal de Cadastro
        btnCadastrar.setOnAction(e -> {
            LocalDate data = datePicker.getValue();
            if (data == null) {
                new Alert(Alert.AlertType.ERROR, "Selecione a data do encontro.").showAndWait();
                return;
            }
            if (data.isBefore(LocalDate.now())) {
                new Alert(Alert.AlertType.ERROR, "Não é permitido cadastrar encontros em datas passadas.").showAndWait();
                return;
            }

            cadastrarEncontroCompleto(data);
        });

        this.getChildren().addAll(titulo, dataBox, new Separator(), new Label("Atribuição de Responsáveis:"), new ScrollPane(servicosGrid), btnCadastrar);
    }

    // Constrói a grade 2xN com todos os serviços e ComboBoxes
    private GridPane criarGridServicos() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        List<Servico> servicos = servicoDAO.listar();
        // CORREÇÃO: Usa listarTodos() para obter a lista completa de mães para atribuição de serviço
        List<Mae> maes = maeDAO.listarTodos();

        ObservableList<Mae> obsMaes = FXCollections.observableArrayList(maes);

        int row = 0;
        for (Servico servico : servicos) {
            Label lblServico = new Label(servico.getNome());
            lblServico.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            // ComboBox para selecionar a Mãe Responsável
            ComboBox<Mae> comboMae = new ComboBox<>(obsMaes);
            comboMae.setPromptText("Selecione a Mãe...");
            // Exibe o nome da mãe
            comboMae.setConverter(new javafx.util.StringConverter<Mae>() {
                @Override
                public String toString(Mae mae) {
                    return mae != null ? mae.getNome() : "";
                }
                @Override
                public Mae fromString(String string) { return null; }
            });

            // Campo de descrição
            TextField txtDescricao = new TextField();
            txtDescricao.setPromptText("Descrição breve da atividade (opcional)");

            // Armazena os componentes no mapa pelo ID do Serviço
            comboMaes.put(servico.getIdServico(), comboMae);
            camposDescricao.put(servico.getIdServico(), txtDescricao);

            grid.add(lblServico, 0, row);
            grid.add(comboMae, 1, row);
            grid.add(txtDescricao, 2, row);

            row++;
        }
        return grid;
    }

    // Salva o Encontro e as 12 atribuições
    private void cadastrarEncontroCompleto(LocalDate data) {
        try {
            // 1. Cadastrar o Encontro
            Encontro novoEncontro = new Encontro(data);
            int idEncontro = encontroDAO.cadastrar(novoEncontro);

            if (idEncontro == -1) {
                throw new RuntimeException("Erro ao obter o ID do encontro.");
            }
            novoEncontro.setIdEncontro(idEncontro);

            // 2. Listar serviços fixos e salvar as atribuições
            List<Servico> servicos = servicoDAO.listar();

            for (Servico servico : servicos) {
                Mae maeResponsavel = comboMaes.get(servico.getIdServico()).getValue();
                String descricao = camposDescricao.get(servico.getIdServico()).getText();

                EncontroServico es = new EncontroServico();
                es.setEncontro(novoEncontro);
                es.setServico(servico);
                es.setMaeResponsavel(maeResponsavel);
                es.setDescricaoAtividade(descricao);

                esDAO.registrarServicoEmEncontro(es);
            }

            new Alert(Alert.AlertType.INFORMATION, "Encontro e serviços cadastrados com sucesso!").showAndWait();

        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, "Falha no cadastro: " + e.getMessage()).showAndWait();
        }
    }
}