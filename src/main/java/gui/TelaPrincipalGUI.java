package gui;

import dao.MaeDAO;
import modelo.Mae;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

// Importa as novas classes de painel
// NOTA: Estas classes devem estar no pacote 'gui'
import gui.PainelCadastroEncontro;
import gui.PainelGerenciamentoEncontros;
import gui.PainelGeracaoRelatorio;


public class TelaPrincipalGUI extends Application {

    private final MaeDAO maeDAO = new MaeDAO();
    private BorderPane root = new BorderPane();
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("SIMEQ - Sistema de Encontros de Mães Que Oram pelos Filhos (JavaFX)");

        root.setTop(criarMenuBar());
        mostrarPainelInicial();

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // --- Estrutura de Navegação ---

    private void mostrarPainel(Node novoPainel) {
        VBox wrapper = new VBox(novoPainel);
        wrapper.setPadding(new Insets(20));
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle("-fx-background-color: white;");
        root.setCenter(wrapper);
    }

    // ====================================================================
    // DASHBOARD INICIAL (Grid 2x2 e Rosa Bebê)
    // ====================================================================
    private void mostrarPainelInicial() {
        // Título Central
        Label welcome = new Label("Painel de Controle SIMEQ");
        welcome.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        welcome.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));

        // Contêiner em GridPane para layout 2x2
        GridPane gridPane = new GridPane();
        gridPane.setHgap(40);
        gridPane.setVgap(40);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(40, 0, 40, 0));

        // Cria os cartões rosa para as funcionalidades
        Node cardCadastroMae = criarCartaoFuncionalidade(
                "1. Cadastro de Mães",
                "Registre novas mães participantes.",
                e -> mostrarPainel(criarPainelCadastroMae())
        );

        Node cardGerenciarEncontros = criarCartaoFuncionalidade(
                "2. Gerenciar Encontros",
                "Cadastrar, editar e excluir logicamente encontros.",
                e -> mostrarPainel(criarPainelGerenciamentoEncontros())
        );

        Node cardAniversariantes = criarCartaoFuncionalidade(
                "3. Aniversariantes do Mês",
                "Visualize a lista de aniversariantes.",
                e -> mostrarPainel(criarPainelAniversariantes())
        );

        Node cardRelatorio = criarCartaoFuncionalidade(
                "4. Gerar Relatórios",
                "Gere o resumo de encontros (.txt).",
                e -> mostrarPainel(criarPainelGeracaoRelatorio())
        );

        // Adiciona os cartões ao GridPane em layout 2x2
        gridPane.add(cardCadastroMae, 0, 0);       // Coluna 0, Linha 0
        gridPane.add(cardGerenciarEncontros, 1, 0); // Coluna 1, Linha 0
        gridPane.add(cardAniversariantes, 0, 1);   // Coluna 0, Linha 1
        gridPane.add(cardRelatorio, 1, 1);       // Coluna 1, Linha 1

        VBox painel = new VBox(40, welcome, gridPane);
        painel.setAlignment(Pos.TOP_CENTER);

        mostrarPainel(painel);
    }

    /**
     * Função auxiliar para criar os cartões retangulares na cor rosa bebê (Maior).
     * CORRIGIDO: Adiciona setWrapText(true) no título.
     */
    private Node criarCartaoFuncionalidade(String titulo, String descricao, javafx.event.EventHandler<javafx.event.ActionEvent> acao) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));
        lblTitulo.setWrapText(true); // <--- CORREÇÃO: Permite que o título quebre linha.
        lblTitulo.setMaxWidth(250);

        Label lblDescricao = new Label(descricao);
        lblDescricao.setWrapText(true);
        lblDescricao.setMaxWidth(250);
        lblDescricao.setTextFill(javafx.scene.paint.Color.BLACK);

        VBox content = new VBox(15, lblTitulo, lblDescricao);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        StackPane card = new StackPane(content);
        card.setPrefSize(300, 180); // Tamanho Maior

        // Estilo Rosa Bebê Retangular (CSS Inline)
        String style = "-fx-background-color: #FCE4EC; " +
                "-fx-border-color: #FF66B2; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15; " +
                "-fx-background-radius: 15;";
        card.setStyle(style);

        // Adiciona interatividade: clique no cartão
        card.setOnMouseClicked(e -> {
            Button tempButton = new Button();
            tempButton.setOnAction(acao);
            tempButton.fire();
        });

        // Efeito visual ao passar o mouse
        card.setOnMouseEntered(e -> card.setStyle(style + "-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        card.setOnMouseExited(e -> card.setStyle(style));

        return card;
    }

    // --- Menu Bar (COMPLETO) ---
    private MenuBar criarMenuBar() {
        // Menu Cadastros
        Menu menuCadastros = new Menu("Cadastros");

        MenuItem itemCadastroMae = new MenuItem("Cadastrar Mãe");
        itemCadastroMae.setOnAction(e -> mostrarPainel(criarPainelCadastroMae()));

        MenuItem itemCadastroEncontro = new MenuItem("Cadastrar Encontro e Serviços");
        itemCadastroEncontro.setOnAction(e -> mostrarPainel(criarPainelCadastroEncontro()));

        MenuItem itemGerenciarEncontros = new MenuItem("Gerenciar/Editar Encontros");
        itemGerenciarEncontros.setOnAction(e -> mostrarPainel(criarPainelGerenciamentoEncontros()));

        menuCadastros.getItems().addAll(itemCadastroMae, itemCadastroEncontro, itemGerenciarEncontros);

        // Menu Consultas
        Menu menuConsultas = new Menu("Consultas");
        MenuItem itemAniversariantes = new MenuItem("Aniversariantes do Mês");
        itemAniversariantes.setOnAction(e -> mostrarPainel(criarPainelAniversariantes()));
        menuConsultas.getItems().add(itemAniversariantes);

        // Menu Relatórios
        Menu menuRelatorios = new Menu("Relatórios");
        MenuItem itemGerarRelatorio = new MenuItem("Gerar Relatório (.txt)");
        itemGerarRelatorio.setOnAction(e -> mostrarPainel(criarPainelGeracaoRelatorio()));
        menuRelatorios.getItems().add(itemGerarRelatorio);

        // Menu Principal
        Menu menuHome = new Menu("Início");
        MenuItem itemHome = new MenuItem("Dashboard Principal");
        itemHome.setOnAction(e -> mostrarPainelInicial());
        menuHome.getItems().add(itemHome);

        return new MenuBar(menuHome, menuCadastros, menuConsultas, menuRelatorios);
    }

    // ====================================================================
    // Implementação dos Painéis
    // ====================================================================

    // 1. PAINEL DE CADASTRO DE MÃES (MANTIDO)
    private GridPane criarPainelCadastroMae() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label titulo = new Label("Cadastro de Mães");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        grid.add(titulo, 0, 0, 2, 1);

        TextField txtNome = new TextField();
        TextField txtTelefone = new TextField();
        TextField txtEndereco = new TextField();
        TextField txtAniversario = new TextField();
        txtAniversario.setPromptText("DD/MM/AAAA");

        grid.add(new Label("Nome:"), 0, 1);
        grid.add(txtNome, 1, 1);

        grid.add(new Label("Telefone:"), 0, 2);
        grid.add(txtTelefone, 1, 2);

        grid.add(new Label("Endereço:"), 0, 3);
        grid.add(txtEndereco, 1, 3);

        grid.add(new Label("Data Aniversário (DD/MM/AAAA):"), 0, 4);
        grid.add(txtAniversario, 1, 4);

        Button btnCadastrar = new Button("Cadastrar");
        HBox hbBtn = new HBox(10, btnCadastrar);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        grid.add(hbBtn, 1, 5);

        // Lógica de Cadastro
        btnCadastrar.setOnAction(e -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dataAniversario = LocalDate.parse(txtAniversario.getText().trim(), formatter);

                Mae mae = new Mae(
                        txtNome.getText(),
                        txtTelefone.getText(),
                        txtEndereco.getText(),
                        dataAniversario
                );

                maeDAO.cadastrar(mae);
                new Alert(Alert.AlertType.INFORMATION, "Mãe cadastrada com sucesso!").showAndWait();

                // Limpa os campos
                txtNome.clear();
                txtTelefone.clear();
                txtEndereco.clear();
                txtAniversario.clear();

            } catch (DateTimeParseException ex) {
                new Alert(Alert.AlertType.ERROR, "Formato de data inválido. Use DD/MM/AAAA.").showAndWait();
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.ERROR, "Erro ao cadastrar: " + ex.getMessage()).showAndWait();
            }
        });

        return grid;
    }

    // 2. PAINEL DE ANIVERSARIANTES (MANTIDO)
    private VBox criarPainelAniversariantes() {
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Aniversariantes do Mês");
        titulo.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        titulo.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));

        // Exibe o Mês Atual por extenso
        String nomeMes = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        Label mesLabel = new Label("Mês Atual: " + nomeMes.toUpperCase());
        mesLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
        mesLabel.setTextFill(javafx.scene.paint.Color.DARKGREY);

        int mesAtual = LocalDate.now().getMonthValue();
        // Funcionalidade 5: Lista de Aniversariantes do Mês
        List<Mae> listaMaes = maeDAO.listarAniversariantesDoMes(mesAtual);
        ObservableList<Mae> aniversariantes = FXCollections.observableArrayList(listaMaes);

        TableView<Mae> tabela = new TableView<>();
        tabela.setItems(aniversariantes);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuração das Colunas
        TableColumn<Mae, String> nomeCol = new TableColumn<>("Nome da Mãe");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        nomeCol.setMinWidth(250);

        TableColumn<Mae, String> telefoneCol = new TableColumn<>("Telefone");
        telefoneCol.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        telefoneCol.setMinWidth(150);

        // Coluna de Aniversário (mostra a data completa DD/MM/AAAA)
        TableColumn<Mae, LocalDate> aniversarioCol = new TableColumn<>("Data de Aniversário");
        aniversarioCol.setCellValueFactory(new PropertyValueFactory<>("dataAniversario"));
        aniversarioCol.setMinWidth(150);

        // Formata a data para o formato completo DD/MM/AAAA
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        aniversarioCol.setCellFactory(column -> new TableCell<Mae, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        tabela.getColumns().addAll(nomeCol, telefoneCol, aniversarioCol);

        if (aniversariantes.isEmpty()) {
            tabela.setPlaceholder(new Label("Nenhuma aniversariante encontrada para este mês."));
        }

        vbox.getChildren().addAll(titulo, mesLabel, tabela);
        return vbox;
    }

    // 3. PAINEL DE CADASTRO DE ENCONTROS (Chama a classe PainelCadastroEncontro)
    private VBox criarPainelCadastroEncontro() {
        return new PainelCadastroEncontro();
    }

    // 4. PAINEL DE GERENCIAMENTO DE ENCONTROS (Chama a classe PainelGerenciamentoEncontros)
    private VBox criarPainelGerenciamentoEncontros() {
        // Passa a referência para a classe principal se o painel precisar dela
        return new PainelGerenciamentoEncontros(this);
    }

    // 5. PAINEL DE GERAÇÃO DE RELATÓRIOS (Chama a classe PainelGeracaoRelatorio)
    private VBox criarPainelGeracaoRelatorio() {
        // Passa a referência da Stage principal para abrir o FileChooser
        return new PainelGeracaoRelatorio(primaryStage);
    }
}