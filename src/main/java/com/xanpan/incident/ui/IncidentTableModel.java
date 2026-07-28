package com.xanpan.incident.ui;

import com.xanpan.incident.model.Incident;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class IncidentTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Titulo", "Categoria", "Prioridad", "Estado", "EXPEDITE"
    };

    private List<Incident> incidents = new ArrayList<>();

    public void setIncidents(List<Incident> incidents) {
        this.incidents = new ArrayList<>(incidents);
        fireTableDataChanged();
    }

    public Incident getIncidentAt(int modelRow) {
        if (modelRow < 0 || modelRow >= incidents.size()) {
            throw new IllegalArgumentException("Fila de incidencia invalida");
        }
        return incidents.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return incidents.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 5 ? Boolean.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Incident incident = incidents.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> shortId(incident.getId());
            case 1 -> incident.getTitle();
            case 2 -> incident.getCategory();
            case 3 -> incident.getPriority().name();
            case 4 -> formatEnum(incident.getState().name());
            case 5 -> incident.isExpedited();
            default -> "";
        };
    }

    private static String shortId(String id) {
        return id == null || id.length() <= 8 ? id : id.substring(0, 8);
    }

    static String formatEnum(String value) {
        return value == null ? "" : value.replace('_', ' ');
    }
}
