package com.xanpan.incident.ui;

import com.xanpan.incident.model.Impact;
import com.xanpan.incident.model.Incident;
import com.xanpan.incident.model.IncidentState;
import com.xanpan.incident.model.Priority;
import com.xanpan.incident.model.Urgency;
import com.xanpan.incident.repository.DatabaseConfig;
import com.xanpan.incident.repository.PostgresIncidentRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Main desktop interface for the HelpDesk Flow application.
 */
public final class IncidentFrame extends JFrame {

    private static final Color NAVY = new Color(18, 31, 53);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color CYAN = new Color(8, 145, 178);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color AMBER = new Color(217, 119, 6);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color MUTED = new Color(71, 85, 105);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IncidentController controller;
    private final IncidentTableModel tableModel = new IncidentTableModel();

    private final JTextField titleField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(5, 20);
    private final JTextField categoryField = new JTextField();
    private final JComboBox<Impact> impactCombo = new JComboBox<>(Impact.values());
    private final JComboBox<Urgency> urgencyCombo = new JComboBox<>(Urgency.values());

    private final JTextField searchField = new JTextField();
    private final JComboBox<IncidentController.Scope> scopeCombo =
            new JComboBox<>(IncidentController.Scope.values());
    private final JComboBox<Object> stateFilter = new JComboBox<>();
    private final JComboBox<Object> priorityFilter = new JComboBox<>();
    private final JTable incidentTable = new JTable(tableModel);
    private final JTextArea detailArea = new JTextArea();

    private final JLabel totalValue = metricValueLabel();
    private final JLabel openValue = metricValueLabel();
    private final JLabel closedValue = metricValueLabel();
    private final JLabel throughputValue = metricValueLabel();
    private final JLabel leadTimeValue = metricValueLabel();
    private final JLabel prioritySummary = new JLabel();
    private final JLabel selectedTitle = new JLabel("Seleccione una incidencia");
    private final JLabel statusLabel = new JLabel("Listo");

    private final JButton advanceButton = actionButton("Avanzar estado", BLUE);
    private final JButton expediteButton = actionButton("Marcar EXPEDITE", AMBER);
    private final JButton completeButton = actionButton("Finalizar con solucion", GREEN);

    public IncidentFrame(IncidentController controller) {
        super("HelpDesk Flow - Sistema de Incidencias");
        this.controller = controller;

        configureWindow();
        setContentPane(createContent());
        wireActions();
        refreshData();
    }

    public static void launch() {
        configureLookAndFeel();
        SwingUtilities.invokeLater(() ->
                new IncidentFrame(new IncidentController(
                        new PostgresIncidentRepository(DatabaseConfig.fromEnvironment())
                )).setVisible(true)
        );
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1160, 720));
        setSize(1320, 820);
        setLocationRelativeTo(null);
    }

    private JComponent createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createHeader(), BorderLayout.NORTH);

        JPanel workspace = new JPanel(new BorderLayout(14, 0));
        workspace.setBackground(BACKGROUND);
        workspace.setBorder(new EmptyBorder(16, 16, 12, 16));
        workspace.add(createRegistrationPanel(), BorderLayout.WEST);
        workspace.add(createIncidentWorkspace(), BorderLayout.CENTER);
        workspace.add(createActionsPanel(), BorderLayout.EAST);
        root.add(workspace, BorderLayout.CENTER);

        statusLabel.setBorder(new EmptyBorder(8, 18, 10, 18));
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        root.add(statusLabel, BorderLayout.SOUTH);
        return root;
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("HelpDesk Flow");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitle = new JLabel("Gestion Xanpan de incidencias y soporte");
        subtitle.setForeground(new Color(186, 230, 253));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));
        identity.add(title);
        identity.add(Box.createVerticalStrut(3));
        identity.add(subtitle);
        header.add(identity, BorderLayout.WEST);

        JPanel metrics = new JPanel();
        metrics.setOpaque(false);
        metrics.setLayout(new BoxLayout(metrics, BoxLayout.X_AXIS));
        metrics.add(metricCard("TOTAL", totalValue, BLUE));
        metrics.add(Box.createHorizontalStrut(8));
        metrics.add(metricCard("ABIERTAS", openValue, CYAN));
        metrics.add(Box.createHorizontalStrut(8));
        metrics.add(metricCard("CERRADAS", closedValue, GREEN));
        metrics.add(Box.createHorizontalStrut(8));
        metrics.add(metricCard("THROUGHPUT", throughputValue, AMBER));
        metrics.add(Box.createHorizontalStrut(8));
        metrics.add(metricCard("LEAD TIME", leadTimeValue, new Color(124, 58, 237)));
        header.add(metrics, BorderLayout.EAST);
        return header;
    }

    private JComponent metricCard(String caption, JLabel value, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 45, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                new EmptyBorder(8, 12, 8, 14)
        ));
        card.setPreferredSize(new Dimension(112, 58));

        JLabel label = new JLabel(caption);
        label.setForeground(new Color(148, 163, 184));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        value.setForeground(Color.WHITE);
        card.add(label);
        card.add(Box.createVerticalStrut(2));
        card.add(value);
        return card;
    }

    private JComponent createRegistrationPanel() {
        JPanel panel = cardPanel();
        panel.setPreferredSize(new Dimension(310, 600));
        panel.setLayout(new BorderLayout());
        panel.add(sectionHeader(
                "Nueva incidencia",
                "Registre los datos; la prioridad se calcula automaticamente."
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 16, 14, 16));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 6, 0);

        int row = 0;
        row = addFormField(form, constraints, row, "Titulo", titleField);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(260, 110));
        row = addFormField(form, constraints, row, "Descripcion", descriptionScroll);
        row = addFormField(form, constraints, row, "Categoria", categoryField);
        row = addFormField(form, constraints, row, "Impacto", impactCombo);
        row = addFormField(form, constraints, row, "Urgencia", urgencyCombo);

        JButton registerButton = actionButton("Registrar incidencia", BLUE);
        constraints.gridy = row++;
        constraints.insets = new Insets(12, 0, 7, 0);
        form.add(registerButton, constraints);
        registerButton.addActionListener(event -> registerIncident());

        JButton exampleButton = secondaryButton("Cargar datos de ejemplo");
        constraints.gridy = row;
        constraints.insets = new Insets(0, 0, 0, 0);
        form.add(exampleButton, constraints);
        exampleButton.addActionListener(event -> loadExampleData());

        constraints.gridy = row + 1;
        constraints.weighty = 1;
        form.add(Box.createVerticalGlue(), constraints);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createIncidentWorkspace() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createFilterBar(), BorderLayout.NORTH);

        configureTable();
        JScrollPane tableScroll = new JScrollPane(incidentTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setRows(6);
        detailArea.setBackground(new Color(248, 250, 252));
        detailArea.setForeground(new Color(30, 41, 59));
        detailArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                "Detalle de la incidencia"
        ));

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tableScroll,
                detailScroll
        );
        split.setResizeWeight(0.72);
        split.setDividerSize(7);
        split.setBorder(BorderFactory.createEmptyBorder());
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createFilterBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sectionHeader(
                "Incidencias",
                "Seleccione una fila para administrar su flujo."
        ), BorderLayout.NORTH);

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        filters.setBorder(new EmptyBorder(8, 14, 12, 14));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.weightx = 1;
        searchField.putClientProperty("JTextField.placeholderText", "Buscar...");
        filters.add(searchField, constraints);

        constraints.gridx++;
        constraints.weightx = 0;
        filters.add(scopeCombo, constraints);

        stateFilter.setModel(filterModel(
                "Todos los estados",
                IncidentState.values()
        ));
        constraints.gridx++;
        filters.add(stateFilter, constraints);

        priorityFilter.setModel(filterModel(
                "Todas las prioridades",
                Priority.values()
        ));
        constraints.gridx++;
        constraints.insets = new Insets(0, 0, 0, 0);
        filters.add(priorityFilter, constraints);
        wrapper.add(filters, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent createActionsPanel() {
        JPanel panel = cardPanel();
        panel.setPreferredSize(new Dimension(280, 600));
        panel.setLayout(new BorderLayout());
        panel.add(sectionHeader(
                "Gestion del flujo",
                "Las reglas se validan en los servicios de dominio."
        ), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(14, 16, 14, 16));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        selectedTitle.setFont(selectedTitle.getFont().deriveFont(Font.BOLD, 15f));
        selectedTitle.setForeground(NAVY);
        selectedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(selectedTitle);
        body.add(Box.createVerticalStrut(14));

        advanceButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        expediteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        completeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(advanceButton);
        body.add(Box.createVerticalStrut(8));
        body.add(expediteButton);
        body.add(Box.createVerticalStrut(8));
        body.add(completeButton);
        body.add(Box.createVerticalStrut(20));

        JLabel flowTitle = new JLabel("Flujo permitido");
        flowTitle.setFont(flowTitle.getFont().deriveFont(Font.BOLD, 12f));
        flowTitle.setForeground(MUTED);
        flowTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(flowTitle);
        body.add(Box.createVerticalStrut(7));

        JLabel flow = new JLabel(
                "<html>REGISTRADA<br>↓<br>LISTA<br>↓<br>"
                        + "EN DESARROLLO<br>↓<br>EN VALIDACION<br>↓<br>"
                        + "<b>FINALIZADA</b></html>"
        );
        flow.setForeground(new Color(51, 65, 85));
        flow.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(flow);
        body.add(Box.createVerticalStrut(20));

        prioritySummary.setVerticalAlignment(SwingConstants.TOP);
        prioritySummary.setForeground(MUTED);
        prioritySummary.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(prioritySummary);
        body.add(Box.createVerticalGlue());

        JLabel explanation = new JLabel(
                "<html><b>Para explicar:</b><br>"
                        + "la ventana coordina acciones, pero prioridad, "
                        + "transiciones y EXPEDITE se deciden en el dominio.</html>"
        );
        explanation.setForeground(MUTED);
        explanation.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(explanation);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private void configureTable() {
        incidentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        incidentTable.setRowHeight(32);
        incidentTable.setShowVerticalLines(false);
        incidentTable.setGridColor(new Color(226, 232, 240));
        incidentTable.setIntercellSpacing(new Dimension(0, 1));
        incidentTable.getTableHeader().setFont(
                incidentTable.getTableHeader().getFont().deriveFont(Font.BOLD, 12f)
        );
        incidentTable.getTableHeader().setBackground(new Color(226, 232, 240));
        incidentTable.getTableHeader().setForeground(new Color(51, 65, 85));
        incidentTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        incidentTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        incidentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        incidentTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        incidentTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        incidentTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        incidentTable.getColumnModel().getColumn(3).setCellRenderer(
                new PriorityRenderer()
        );
    }

    private void wireActions() {
        DocumentListener searchListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshData();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshData();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshData();
            }
        };
        searchField.getDocument().addDocumentListener(searchListener);
        scopeCombo.addActionListener(event -> refreshData());
        stateFilter.addActionListener(event -> refreshData());
        priorityFilter.addActionListener(event -> refreshData());
        incidentTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedIncident();
            }
        });

        advanceButton.addActionListener(event -> selectedIncident().ifPresent(
                incident -> execute(
                        () -> controller.advanceIncident(incident.getId()),
                        "Estado actualizado correctamente"
                )
        ));
        expediteButton.addActionListener(event -> selectedIncident().ifPresent(
                incident -> execute(
                        () -> controller.markExpedited(incident.getId()),
                        "Incidencia marcada como EXPEDITE"
                )
        ));
        completeButton.addActionListener(event -> completeSelectedIncident());
    }

    private void registerIncident() {
        execute(
                () -> controller.registerIncident(
                        titleField.getText(),
                        descriptionArea.getText(),
                        (Impact) impactCombo.getSelectedItem(),
                        (Urgency) urgencyCombo.getSelectedItem(),
                        categoryField.getText()
                ),
                "Incidencia registrada correctamente"
        );
        if (!statusLabel.getText().startsWith("Error")) {
            clearRegistrationForm();
        }
    }

    private void loadExampleData() {
        if (controller.metrics().total() > 0) {
            showInformation("Los datos de ejemplo solo se cargan cuando la lista esta vacia.");
            return;
        }
        try {
            Incident payment = controller.registerIncident(
                    "Servicio de pagos no disponible",
                    "Los usuarios no pueden completar sus pagos.",
                    Impact.ALTO,
                    Urgency.ALTA,
                    "Software"
            );
            controller.markExpedited(payment.getId());
            controller.advanceIncident(payment.getId());
            controller.advanceIncident(payment.getId());

            Incident monitor = controller.registerIncident(
                    "Monitor secundario sin senal",
                    "El monitor pierde la senal despues de varios minutos.",
                    Impact.BAJO,
                    Urgency.MEDIA,
                    "Hardware"
            );
            controller.advanceIncident(monitor.getId());

            controller.registerIncident(
                    "Intermitencia de red",
                    "La conexion presenta cortes en la sala de reuniones.",
                    Impact.MEDIO,
                    Urgency.MEDIA,
                    "Red"
            );
            refreshData();
            showStatus("Datos de ejemplo cargados para la demostracion");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void completeSelectedIncident() {
        selectedIncident().ifPresent(incident -> {
            JTextArea solutionArea = new JTextArea(5, 32);
            solutionArea.setLineWrap(true);
            solutionArea.setWrapStyleWord(true);
            int result = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(solutionArea),
                    "Descripcion de la solucion",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result == JOptionPane.OK_OPTION) {
                execute(
                        () -> controller.completeIncident(
                                incident.getId(),
                                solutionArea.getText()
                        ),
                        "Incidencia finalizada con solucion"
                );
            }
        });
    }

    private void execute(Supplier<Incident> operation, String successMessage) {
        try {
            Incident result = operation.get();
            refreshData();
            selectIncident(result.getId());
            showStatus(successMessage);
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void refreshData() {
        if (stateFilter.getItemCount() == 0 || priorityFilter.getItemCount() == 0) {
            return;
        }
        IncidentState state = selectedEnum(stateFilter, IncidentState.class);
        Priority priority = selectedEnum(priorityFilter, Priority.class);
        tableModel.setIncidents(controller.listIncidents(
                searchField.getText(),
                (IncidentController.Scope) scopeCombo.getSelectedItem(),
                state,
                priority
        ));
        updateMetrics();
        if (incidentTable.getSelectedRow() < 0) {
            showIncident(null);
        }
    }

    private void updateMetrics() {
        IncidentController.MetricsSnapshot metrics = controller.metrics();
        totalValue.setText(Integer.toString(metrics.total()));
        openValue.setText(Integer.toString(metrics.open()));
        closedValue.setText(Integer.toString(metrics.closed()));
        throughputValue.setText(Long.toString(metrics.throughput()));
        leadTimeValue.setText(String.format("%.1f min", metrics.averageLeadTimeMinutes()));
        prioritySummary.setText(
                "<html><b>Distribucion por prioridad</b><br><br>"
                        + "Critica: " + metrics.byPriority().getOrDefault(Priority.CRITICA, 0L)
                        + "<br>Alta: " + metrics.byPriority().getOrDefault(Priority.ALTA, 0L)
                        + "<br>Normal: " + metrics.byPriority().getOrDefault(Priority.NORMAL, 0L)
                        + "</html>"
        );
    }

    private void showSelectedIncident() {
        showIncident(currentSelection().orElse(null));
    }

    private void showIncident(Incident incident) {
        boolean selected = incident != null;
        if (!selected) {
            selectedTitle.setText("Seleccione una incidencia");
            detailArea.setText(
                    "Seleccione una fila para consultar sus datos y administrar el flujo."
            );
            advanceButton.setEnabled(false);
            expediteButton.setEnabled(false);
            completeButton.setEnabled(false);
            return;
        }

        selectedTitle.setText(incident.getTitle());
        String solution = incident.getSolutionDescription() == null
                ? "Pendiente"
                : incident.getSolutionDescription();
        String closedAt = incident.getClosedAt() == null
                ? "Pendiente"
                : DATE_FORMAT.format(incident.getClosedAt());
        detailArea.setText(
                "ID: " + incident.getId()
                        + "\nTitulo: " + incident.getTitle()
                        + "\nDescripcion: " + incident.getDescription()
                        + "\nCategoria: " + incident.getCategory()
                        + "\nImpacto / urgencia: " + incident.getImpact()
                        + " / " + incident.getUrgency()
                        + "\nPrioridad: " + incident.getPriority()
                        + "\nEstado: " + IncidentTableModel.formatEnum(
                                incident.getState().name()
                        )
                        + "\nEXPEDITE: " + (incident.isExpedited() ? "Si" : "No")
                        + "\nCreada: " + DATE_FORMAT.format(incident.getCreatedAt())
                        + "\nCerrada: " + closedAt
                        + "\nSolucion: " + solution
        );
        detailArea.setCaretPosition(0);

        advanceButton.setEnabled(
                incident.getState() != IncidentState.EN_VALIDACION
                        && incident.getState() != IncidentState.FINALIZADA
        );
        expediteButton.setEnabled(
                incident.getPriority() == Priority.CRITICA
                        && !incident.isExpedited()
                        && incident.getState() != IncidentState.FINALIZADA
        );
        completeButton.setEnabled(incident.getState() == IncidentState.EN_VALIDACION);
    }

    private Optional<Incident> selectedIncident() {
        Optional<Incident> selected = currentSelection();
        if (selected.isEmpty()) {
            showInformation("Seleccione una incidencia en la tabla.");
        }
        return selected;
    }

    private Optional<Incident> currentSelection() {
        int viewRow = incidentTable.getSelectedRow();
        if (viewRow < 0) {
            return Optional.empty();
        }
        int modelRow = incidentTable.convertRowIndexToModel(viewRow);
        return Optional.of(tableModel.getIncidentAt(modelRow));
    }

    private void selectIncident(String incidentId) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.getIncidentAt(row).getId().equals(incidentId)) {
                int viewRow = incidentTable.convertRowIndexToView(row);
                incidentTable.setRowSelectionInterval(viewRow, viewRow);
                incidentTable.scrollRectToVisible(
                        incidentTable.getCellRect(viewRow, 0, true)
                );
                return;
            }
        }
    }

    private void clearRegistrationForm() {
        titleField.setText("");
        descriptionArea.setText("");
        categoryField.setText("");
        impactCombo.setSelectedItem(Impact.BAJO);
        urgencyCombo.setSelectedItem(Urgency.BAJA);
        titleField.requestFocusInWindow();
    }

    private void showError(RuntimeException exception) {
        String message = exception.getMessage() == null
                ? "Ocurrio un error inesperado"
                : exception.getMessage();
        statusLabel.setText("Error: " + message);
        statusLabel.setForeground(RED);
        JOptionPane.showMessageDialog(
                this,
                message,
                "No se pudo completar la operacion",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showInformation(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "HelpDesk Flow",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(GREEN);
    }

    private static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));
        return panel;
    }

    private static JComponent sectionHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 16, 7, 16));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 17f));
        titleLabel.setForeground(NAVY);
        JLabel subtitleLabel = new JLabel(
                "<html><body style='width:240px'>" + subtitle + "</body></html>"
        );
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 11f));
        subtitleLabel.setForeground(MUTED);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(3));
        header.add(subtitleLabel);
        return header;
    }

    private static int addFormField(
            JPanel form,
            GridBagConstraints constraints,
            int row,
            String label,
            JComponent component
    ) {
        constraints.gridy = row++;
        constraints.insets = new Insets(0, 0, 4, 0);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(MUTED);
        fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 11f));
        form.add(fieldLabel, constraints);

        constraints.gridy = row++;
        constraints.insets = new Insets(0, 0, 10, 0);
        component.setPreferredSize(new Dimension(
                component.getPreferredSize().width,
                Math.max(30, component.getPreferredSize().height)
        ));
        form.add(component, constraints);
        return row;
    }

    private static JButton actionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BLUE),
                new EmptyBorder(8, 13, 8, 13)
        ));
        return button;
    }

    private static JLabel metricValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 17f));
        return label;
    }

    private static DefaultComboBoxModel<Object> filterModel(
            String allLabel,
            Object[] values
    ) {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        model.addElement(allLabel);
        for (Object value : values) {
            model.addElement(value);
        }
        return model;
    }

    private static <T> T selectedEnum(JComboBox<Object> combo, Class<T> type) {
        Object selected = combo.getSelectedItem();
        return type.isInstance(selected) ? type.cast(selected) : null;
    }

    private static void configureLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Swing's default look and feel remains available.
        }
        UIManager.put("control", Color.WHITE);
        UIManager.put("nimbusBase", BLUE);
        UIManager.put("text", NAVY);
    }

    private static final class PriorityRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(
                    table,
                    value,
                    selected,
                    focused,
                    row,
                    column
            );
            if (!selected) {
                String priority = String.valueOf(value);
                component.setForeground(switch (priority) {
                    case "CRITICA" -> RED;
                    case "ALTA" -> AMBER;
                    default -> GREEN;
                });
                component.setBackground(Color.WHITE);
            }
            setFont(getFont().deriveFont(Font.BOLD));
            return component;
        }
    }
}
