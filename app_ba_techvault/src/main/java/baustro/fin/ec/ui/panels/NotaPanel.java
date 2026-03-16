package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.NotaDAO;
import baustro.fin.ec.model.Nota;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class NotaPanel extends JPanel {

    private final NotaDAO dao = new NotaDAO();
    private DefaultListModel<Nota> listModel;
    private JList<Nota> noteList;
    private JTextField searchField;
    private JTextArea contentArea;
    private JLabel currentTitle;
    private Nota currentNota = null;

    public NotaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JLabel title = new JLabel("Notas");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico4 = IconManager.getIcon(IconManager.ICON_NOTA, 24);
        if (_ico4 != null && _ico4.getIconWidth() > 1) title.setIcon(_ico4);
        title.setIconTextGap(10);

        JButton btnNew = StyledComponents.addButton("Nueva Nota");
        btnNew.addActionListener(e -> newNota());
        header.add(title, BorderLayout.WEST);
        header.add(btnNew, BorderLayout.EAST);

        // Split: left list / right editor
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(UIConstants.BG_BASE);
        split.setDividerSize(4);
        split.setDividerLocation(260);

        // LEFT: Note list
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(UIConstants.BG_CARD);

        // SearchBar con icono en la barra lateral de notas
        JPanel searchWrap = new JPanel(new BorderLayout(0,0));
        searchWrap.setBackground(UIConstants.BG_SURFACE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER),
                BorderFactory.createEmptyBorder(0,0,0,0)));
        JLabel searchIco = new JLabel();
        searchIco.setOpaque(true); searchIco.setBackground(UIConstants.BG_SURFACE);
        searchIco.setBorder(BorderFactory.createEmptyBorder(0,8,0,4));
        ImageIcon _sico = IconManager.getSmallIcon(IconManager.ICON_SEARCH);
        if(_sico!=null&&_sico.getIconWidth()>1) searchIco.setIcon(_sico);
        else { searchIco.setText("Q"); searchIco.setFont(UIConstants.FONT_SMALL); searchIco.setForeground(UIConstants.TEXT_MUTED); }
        searchField = new JTextField();
        searchField.setBackground(UIConstants.BG_SURFACE); searchField.setForeground(UIConstants.TEXT_MUTED);
        searchField.setCaretColor(UIConstants.TEXT_PRIMARY); searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createEmptyBorder(8,0,8,8));
        searchField.setText("Buscar notas...");
        searchField.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e){ if("Buscar notas...".equals(searchField.getText())){searchField.setText("");searchField.setForeground(UIConstants.TEXT_PRIMARY);}}
            public void focusLost(FocusEvent e){ if(searchField.getText().isEmpty()){searchField.setText("Buscar notas...");searchField.setForeground(UIConstants.TEXT_MUTED);}}
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doSearch(); }
        });
        searchWrap.add(searchIco,BorderLayout.WEST);
        searchWrap.add(searchField,BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        noteList = new JList<>(listModel);
        noteList.setBackground(UIConstants.BG_CARD);
        noteList.setForeground(UIConstants.TEXT_PRIMARY);
        noteList.setFont(UIConstants.FONT_BODY);
        noteList.setSelectionBackground(UIConstants.ACCENT_BLUE);
        noteList.setSelectionForeground(Color.WHITE);
        noteList.setCellRenderer(noteCellRenderer());
        noteList.setFixedCellHeight(72);
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) openNota(noteList.getSelectedValue());
        });

        leftPanel.add(searchWrap, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(noteList) {{ setBorder(null); getViewport().setBackground(UIConstants.BG_CARD); }}, BorderLayout.CENTER);

        // RIGHT: Editor
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(UIConstants.BG_BASE);

        // Editor toolbar
        JPanel editorBar = new JPanel(new BorderLayout(10, 0));
        editorBar.setBackground(UIConstants.BG_CARD);
        editorBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        currentTitle = new JLabel("Seleccione o cree una nota");
        currentTitle.setFont(UIConstants.FONT_HEADING);
        currentTitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel editorBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        editorBtns.setOpaque(false);
        JButton btnSave = StyledComponents.successButton("Guardar");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        JButton btnEdit   = StyledComponents.editButton("Editar Info");
        btnSave.addActionListener(e -> saveCurrentNota());
        btnDelete.addActionListener(e -> deleteCurrentNota());
        btnEdit.addActionListener(e -> editNotaInfo());
        editorBtns.add(btnEdit); editorBtns.add(btnSave); editorBtns.add(btnDelete);
        editorBar.add(currentTitle, BorderLayout.WEST);
        editorBar.add(editorBtns, BorderLayout.EAST);

        contentArea = StyledComponents.styledTextArea(10, 30);
        contentArea.setFont(UIConstants.FONT_BODY);
        contentArea.setBackground(UIConstants.BG_BASE);
        contentArea.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        rightPanel.add(editorBar, BorderLayout.NORTH);
        rightPanel.add(StyledComponents.darkScrollPane(contentArea), BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);

        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private DefaultListCellRenderer noteCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean sel, boolean focus) {
                Nota nota = (Nota) val;

                //  Colores base
                Color bgColor  = sel ? UIConstants.ACCENT_BLUE
                        : (idx % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER);
                Color fgTitle  = sel ? Color.WHITE              : UIConstants.TEXT_BRIGHT;
                Color fgPrev   = sel ? new Color(200, 220, 255) : UIConstants.TEXT_SECONDARY;
                Color fgTag    = sel ? new Color(180, 230, 255) : UIConstants.TEAL_PRIMARY;
                JPanel p = getJPanel(sel, bgColor);

                //  Panel de texto (columna central)
                JPanel textCol = new JPanel();
                textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
                textCol.setOpaque(false);

                // Título
                JLabel lTitle = new JLabel(nota.getTitulo());
                lTitle.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD));
                lTitle.setForeground(fgTitle);
                lTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Preview del contenido — máximo 55 chars, sin saltos de línea
                String raw = nota.getContenido() != null
                        ? nota.getContenido().replaceAll("\\s+", " ").trim() : "";
                String preview = raw.isEmpty() ? "Sin contenido"
                        : (raw.length() > 55 ? raw.substring(0, 55) + "…" : raw);
                JLabel lPrev = new JLabel(preview);
                lPrev.setFont(UIConstants.FONT_SMALL);
                lPrev.setForeground(fgPrev);
                lPrev.setAlignmentX(Component.LEFT_ALIGNMENT);

                textCol.add(lTitle);
                textCol.add(Box.createVerticalStrut(3));
                textCol.add(lPrev);

                // Tags — solo si existen, como pills de texto en la parte inferior
                if (nota.getEtiquetas() != null && !nota.getEtiquetas().isBlank()) {
                    textCol.add(Box.createVerticalStrut(4));
                    JPanel tagRow = getJPanel(sel, nota, fgTag);
                    textCol.add(tagRow);
                }

                p.add(textCol, BorderLayout.CENTER);
                return p;
            }

            private JPanel getJPanel(boolean sel, Nota nota, Color fgTag) {
                JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                tagRow.setOpaque(false);
                tagRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                // Dividir por coma y mostrar cada tag por separado
                for (String tag : nota.getEtiquetas().split(",")) {
                    String t = tag.trim();
                    if (t.isEmpty()) continue;
                    JLabel lt = getJLabel(sel, t, fgTag);
                    tagRow.add(lt);
                }
                return tagRow;
            }

            private static JLabel getJLabel(boolean sel, String t, Color fgTag) {
                JLabel lt = new JLabel("  " + t + "  ");
                lt.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD, 9f));
                lt.setForeground(fgTag);
                lt.setOpaque(false);
                lt.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(fgTag.getRed(), fgTag.getGreen(), fgTag.getBlue(), sel ? 140 : 80), 1, true),
                        BorderFactory.createEmptyBorder(1, 2, 1, 2)));
                return lt;
            }

            private JPanel getJPanel(boolean sel, Color bgColor) {
                Color accentBar= sel ? new Color(255,255,255,80): UIConstants.TEAL_PRIMARY;

                //  Panel principal con barra de acento lateral custom
                JPanel p = new JPanel(new BorderLayout(0, 0)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        // Barra de acento vertical izquierda de 3px
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(accentBar);
                        g2.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);
                        g2.dispose();
                    }
                };
                p.setBackground(bgColor);
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_SUBTLE),
                        BorderFactory.createEmptyBorder(10, 14, 10, 12)));
                return p;
            }
        };
    }

    private void loadData() {
        try {
            List<Nota> notas = dao.findAll();
            listModel.clear();
            notas.forEach(listModel::addElement);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void doSearch() {
        String raw = searchField.getText().trim();
        String q = raw.equals("Buscar notas...") ? "" : raw;
        try {
            List<Nota> result = q.isEmpty() ? dao.findAll() : dao.search(q);
            listModel.clear();
            result.forEach(listModel::addElement);
        } catch (Exception ex) { /* ignore */ }
    }

    private void openNota(Nota nota) {
        if (nota == null) return;
        currentNota = nota;
        currentTitle.setText(nota.getTitulo());
        currentTitle.setForeground(UIConstants.TEXT_PRIMARY);
        contentArea.setText(nota.getContenido());
        contentArea.setCaretPosition(0);
    }

    private void newNota() {
        String tit = JOptionPane.showInputDialog(this, "Título de la nota:");
        if (tit == null || tit.trim().isEmpty()) return;
        Nota n = new Nota();
        n.setTitulo(tit.trim());
        n.setContenido("");
        n.setColor("#FFFFFF");
        try {
            dao.insert(n);
            loadData();
            // Select the new note
            for (int i = 0; i < listModel.size(); i++) {
                if (tit.trim().equals(listModel.get(i).getTitulo())) {
                    noteList.setSelectedIndex(i);
                    break;
                }
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void saveCurrentNota() {
        if (currentNota == null) { JOptionPane.showMessageDialog(this, "No hay nota seleccionada."); return; }
        currentNota.setContenido(contentArea.getText());
        try {
            dao.update(currentNota);
            loadData();
            JOptionPane.showMessageDialog(this, "Nota guardada", "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void deleteCurrentNota() {
        if (currentNota == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar nota \"" + currentNota.getTitulo() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                dao.delete(currentNota.getId());
                currentNota = null;
                currentTitle.setText("Seleccione o cree una nota");
                currentTitle.setForeground(UIConstants.TEXT_SECONDARY);
                contentArea.setText("");
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void editNotaInfo() {
        if (currentNota == null) return;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Editar Info de Nota", true);
        d.setSize(400, 220);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        d.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8,12,8,12); gbc.weightx = 1;

        JTextField fTit  = StyledComponents.styledTextField("Título");
        JTextField fTags = StyledComponents.styledTextField("tag1, tag2, tag3");
        fTit.setText(currentNota.getTitulo());
        fTags.setText(currentNota.getEtiquetas());

        gbc.gridy = 0; gbc.gridwidth = 2; d.add(lbl("Título"), gbc);
        gbc.gridy = 1; d.add(fTit, gbc);
        gbc.gridy = 2; d.add(lbl("Etiquetas (separadas por coma)"), gbc);
        gbc.gridy = 3; d.add(fTags, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(UIConstants.BG_BASE);
        JButton ok = StyledComponents.successButton("Guardar");
        JButton cancel = StyledComponents.cancelButton("Cancelar");
        cancel.addActionListener(e -> d.dispose());
        ok.addActionListener(e -> {
            currentNota.setTitulo(fTit.getText().trim());
            currentNota.setEtiquetas(fTags.getText().trim());
            currentNota.setContenido(contentArea.getText());
            try { dao.update(currentNota); currentTitle.setText(currentNota.getTitulo()); loadData(); d.dispose(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage()); }
        });
        btns.add(ok); btns.add(cancel);

        gbc.gridy = 4; d.add(btns, gbc);
        d.setVisible(true);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY); return l;
    }
}