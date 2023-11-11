
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;

public class Gui extends javax.swing.JFrame {
    Main m;
    int displayWidth;
    int displayHeight;
    float scale = 1.30f;
    PairData pd;
    final String blank = "blank";
    public final PairData pd_blank = new PairData(blank, new ArrayList<String[]>(),BigDecimal.ZERO,false);
    public final ListPairs lp_blank = new ListPairs(blank);      
    public static int row;
    public static int col;
    
    JFileChooser jfc;
    JPopupMenu jpm_list,jpm_table,jpm_TextField;
    File f;
    
    public Gui(Main m) {
        this.m=m;
        initDisplay();
        initComponents();
        initUi();    
    }
    
    private void initDisplay() {
        DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        if(dm != null && dm.getWidth() > 0 && dm.getHeight() > 0) {
            displayWidth = (int)(dm.getWidth()/scale);
            displayHeight = (int)(dm.getHeight()/scale);
        }else {//Defult
            displayWidth = 800;
            displayHeight = 400; 
        }
    }
    private void initUi() {
        jComboBox1.setModel(Main.exchanges);
        
        if(m.lastExchange != null)
            jComboBox1.setSelectedItem(m.lastExchange);
        else
            jComboBox1.setSelectedIndex(0);
     
        m.setExchange(jComboBox1.getSelectedItem().toString(), false);

        
        addWindowListener(wl);
        pd=pd_blank;        
        updateUi();        
        dce.setClickCountToStart(2);       
        jTable1.setDefaultEditor(Object.class, dce);

        InputMap im = jTable1.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = jTable1.getActionMap();
        //ENTER key
        am.put(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) { 
                jTable1.editCellAt(row,col);
            }
    });
        //TAB
        am.put(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
              if(jTable1.isEditing()) 
              jTable1.getCellEditor().stopCellEditing();
                
            }
    });
     
        
        
        jpm_list = new JPopupMenu();
        jpm_list.add(new AbstractAction("Delete") {
        @Override
        public void actionPerformed(ActionEvent e) {
            deletePair();
        }

        });

        jpm_list.add(new AbstractAction("Add custom fee") {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(pd == null)
                return;
            String custom_Fee_Percentage = JOptionPane.showInputDialog("Enter Custom Fee Percentage to this pair (without %) \n 0 Disable");
            if(custom_Fee_Percentage == null)
                return;
            String pair = jList1.getSelectedValue();
            if(custom_Fee_Percentage.matches("\\d+.*")) {
              BigDecimal fee_Percentage =  BigDecimal.valueOf(Double.parseDouble(custom_Fee_Percentage));
              if(fee_Percentage.signum() > 0)
                  pd.custom_Fee_Percentage = fee_Percentage.movePointLeft(2);
              else
                  pd.custom_Fee_Percentage = BigDecimal.ZERO;
              
              if(m.saveData(pd))
                  Main.refreshData(pd,true);
                
            }else
              messageError("Your input is not an Integer! Please try again");  
                
        }

        });
        
        jpm_table = new JPopupMenu();
        
        jpm_table.add(new AbstractAction("Copy Row") {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            StringBuilder sb = new StringBuilder(1);
            for (int i = 0; i < jTable1.getColumnCount(); i++) {
               if(jTable1.getValueAt(row, col) != null)
                   sb.append(jTable1.getValueAt(row, i).toString());
               if(i < jTable1.getColumnCount()-1)
                   sb.append(",");
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()),null);
        }

        }); 
        
        jpm_table.add(new AbstractAction("Copy Cell") {
        @Override
        public void actionPerformed(ActionEvent e) {
            String s;
            if(jTable1.getValueAt(row, col) != null)
                s = jTable1.getValueAt(row, col).toString();
            else
                s="";
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s),null);
        }

        });   

        jpm_TextField = new JPopupMenu();
        Action copy = new DefaultEditorKit.CopyAction();
        copy.putValue(Action.NAME, "Copy");
        copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
        jpm_TextField.add(copy);
        
        
        all_Buy.setComponentPopupMenu(jpm_TextField);
        all_Sell.setComponentPopupMenu(jpm_TextField);
        total.setComponentPopupMenu(jpm_TextField);
        quantity_Buy.setComponentPopupMenu(jpm_TextField);
        quantity_Sell.setComponentPopupMenu(jpm_TextField);
        total_Quantity.setComponentPopupMenu(jpm_TextField);
        all_fee.setComponentPopupMenu(jpm_TextField);
        average.setComponentPopupMenu(jpm_TextField);
        all_Time_average.setComponentPopupMenu(jpm_TextField);
    }
    
    DefaultCellEditor dce = new DefaultCellEditor(new JTextField()) {

            @Override
            public boolean isCellEditable(EventObject e) {
                if (e instanceof KeyEvent) //stop edit cell without enter button or double click in mouse
                    return false;
                
                   
                return super.isCellEditable(e);
            }
    };
    
    WindowListener wl = new WindowListener() {
            @Override
            public void windowOpened(WindowEvent we) {
                
            }
            
            @Override
            public void windowClosing(WindowEvent we) {
                m.saveExchanges();
                m.saveAllData(true,true);

            }
            
            @Override
            public void windowClosed(WindowEvent we) {
                
            }
            
            @Override
            public void windowIconified(WindowEvent we) {
            }
            
            @Override
            public void windowDeiconified(WindowEvent we) {
            }
            
            @Override
            public void windowActivated(WindowEvent we) {
            }
            
            @Override
            public void windowDeactivated(WindowEvent we) {
            }
        };

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        panel1 = new java.awt.Panel();
        jLabel3 = new javax.swing.JLabel();
        all_Sell = new javax.swing.JTextField();
        all_Buy = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        total = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        quantity_Buy = new javax.swing.JTextField();
        quantity_Sell = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        total_Quantity = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        all_fee = new javax.swing.JTextField();
        average = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        all_Time_average = new javax.swing.JTextField();
        b_import = new javax.swing.JButton();
        button_save = new javax.swing.JButton();
        button_row_up = new javax.swing.JButton();
        button_row_down = new javax.swing.JButton();
        button_new = new javax.swing.JButton();
        button_row_delete = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Main.programName);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(this.displayWidth,this.displayHeight));
        setPreferredSize(new java.awt.Dimension((int)(this.displayWidth*1.2f),(int)(this.displayHeight*1.2f)));

        jList1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jList1MousePressed(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jList1KeyPressed(evt);
            }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jTable1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        jTable1.setModel(pd_blank);
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTable1.setName(""); // NOI18N
        jTable1.setOpaque(false);
        jTable1.setRowHeight(25);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setShowGrid(true);
        jTable1.setSurrendersFocusOnKeystroke(true);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.setUpdateSelectionOnSort(false);
        jTable1.setVerifyInputWhenFocusTarget(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTable1MousePressed(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable1KeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel1.setText("Exchange: ");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jComboBox1.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel3.setText("All Sell:");
        jLabel3.setToolTipText("All Sell Amount - All Fee Sell");

        all_Sell.setEditable(false);
        all_Sell.setBackground(new java.awt.Color(230, 230, 230));
        all_Sell.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        all_Buy.setEditable(false);
        all_Buy.setBackground(new java.awt.Color(230, 230, 230));
        all_Buy.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        all_Buy.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel2.setText("All Buy:");
        jLabel2.setToolTipText("All Buy Amount - All Fee Buy");

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel4.setText("Total:");
        jLabel4.setToolTipText("All Buy + All Sell");

        total.setEditable(false);
        total.setBackground(new java.awt.Color(230, 230, 230));
        total.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel8.setText("Quantity Buy:");

        quantity_Buy.setEditable(false);
        quantity_Buy.setBackground(new java.awt.Color(230, 230, 230));
        quantity_Buy.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        quantity_Sell.setEditable(false);
        quantity_Sell.setBackground(new java.awt.Color(230, 230, 230));
        quantity_Sell.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel9.setText("Quantity Sell:");

        jLabel10.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel10.setText("Total Quantity:");

        total_Quantity.setEditable(false);
        total_Quantity.setBackground(new java.awt.Color(230, 230, 230));
        total_Quantity.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel11.setText("All Fee:");

        all_fee.setEditable(false);
        all_fee.setBackground(new java.awt.Color(230, 230, 230));
        all_fee.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        average.setEditable(false);
        average.setBackground(new java.awt.Color(230, 230, 230));
        average.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel12.setText("Average:");
        jLabel12.setToolTipText("All Buy / Quantity Buy");

        jLabel13.setFont(new java.awt.Font("Liberation Sans", 1, 17)); // NOI18N
        jLabel13.setText("All Time Average:");
        jLabel13.setToolTipText("Total / Total Quantity:");

        all_Time_average.setEditable(false);
        all_Time_average.setBackground(new java.awt.Color(230, 230, 230));
        all_Time_average.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(2, 2, 2)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(total)
                    .addComponent(all_Buy)
                    .addComponent(all_Sell))
                .addGap(15, 15, 15)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addGap(2, 2, 2)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(total_Quantity)
                    .addComponent(quantity_Buy)
                    .addComponent(quantity_Sell))
                .addGap(15, 15, 15)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addGap(2, 2, 2)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(all_Time_average)
                    .addComponent(all_fee)
                    .addComponent(average))
                .addGap(10, 10, 10))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(all_fee, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel11))
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(average, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(all_Time_average, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(quantity_Buy, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel8))
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(quantity_Sell, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(total_Quantity, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(all_Buy, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel2))
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(all_Sell, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(total, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jLabel2.getAccessibleContext().setAccessibleDescription("All Buy Amount - All Fee Buy");

        b_import.setFont(new java.awt.Font("Liberation Sans", 0, 15)); // NOI18N
        b_import.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/import.png"))); // NOI18N
        b_import.setText("Import");
        b_import.setToolTipText("import csv file");
        b_import.setAlignmentY(0.0F);
        b_import.setPreferredSize(new java.awt.Dimension(130, 30));
        b_import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_importActionPerformed(evt);
            }
        });

        button_save.setFont(new java.awt.Font("Liberation Sans", 0, 15)); // NOI18N
        button_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save.png"))); // NOI18N
        button_save.setText("Save");
        button_save.setToolTipText("Save Data");
        button_save.setEnabled(false);
        button_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_saveActionPerformed(evt);
            }
        });

        button_row_up.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/go-up.png"))); // NOI18N
        button_row_up.setToolTipText("Move sellected row up");
        button_row_up.setEnabled(false);
        button_row_up.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_row_upActionPerformed(evt);
            }
        });

        button_row_down.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/go-down.png"))); // NOI18N
        button_row_down.setToolTipText("Move sellected row down");
        button_row_down.setEnabled(false);
        button_row_down.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_row_downActionPerformed(evt);
            }
        });

        button_new.setFont(new java.awt.Font("Liberation Sans", 0, 15)); // NOI18N
        button_new.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add.png"))); // NOI18N
        button_new.setText("New");
        button_new.setToolTipText("New  Sheet");
        button_new.setEnabled(false);
        button_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_newActionPerformed(evt);
            }
        });

        button_row_delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/deleteRow.png"))); // NOI18N
        button_row_delete.setToolTipText("Delete sellected row");
        button_row_delete.setEnabled(false);
        button_row_delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_row_deleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(b_import, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_save, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_new, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(button_row_delete)
                        .addGap(7, 7, 7)
                        .addComponent(button_row_up)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_row_down))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(button_row_up)
                            .addComponent(button_row_down)
                            .addComponent(button_row_delete))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(b_import, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_save, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_new, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    //Pairs List
    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
       if(evt.getValueIsAdjusting())
           setPair();
           jTable1.scrollRectToVisible(jTable1.getCellRect(0, 0, true));
    }//GEN-LAST:event_jList1ValueChanged

    
    //Exchange
    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
       if(evt.getStateChange() == 2) {
           if(jComboBox1.getSelectedItem() == null)
               return;
           if(jComboBox1.getSelectedItem().equals("<Add New - Delete>")) 
                add_Delete_Exchange();
           else {
                clearData();          
                m.setExchange(jComboBox1.getSelectedItem().toString(), false);                
           }
       }
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void button_row_downActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_row_downActionPerformed
        if(pd == null)
            return;
        row = jTable1.getSelectedRow();
        
        if(row < pd.getRowsCount()-1) {
            PairDataUtils.moveRowDown(pd,row);
            row++;
            if(col < 0)
               col=0;
            jTable1.changeSelection(row,col, false, false);
            pd.save=true;
            cntrolButtons();
        }
    }//GEN-LAST:event_button_row_downActionPerformed

    private void button_row_upActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_row_upActionPerformed
        if(pd == null)
            return;
        row = jTable1.getSelectedRow();
        
        if(row > 0 && row < pd.getRowsCount()) {
            PairDataUtils.moveRowUp(pd,row);
            row--;
            if(col < 0)
               col=0;
            jTable1.changeSelection(row,col, false, false);
            pd.save=true;
            cntrolButtons();
        }
    }//GEN-LAST:event_button_row_upActionPerformed

    private void jTable1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyReleased
        
        if(jTable1.getSelectedRow() > -1) {

            row = jTable1.getSelectedRow();
            col = jTable1.getSelectedColumn();

            if(evt.getKeyCode() == KeyEvent.VK_TAB) {
                if(!evt.isShiftDown()) {
                    if(col < jTable1.getColumnCount()-1)
                        col++;
                    else{
                        row++;
                        col=0;
                    }
                }
                jTable1.changeSelection(row,col, false, false);                
                jTable1.editCellAt(row,col);
                
            }else if(evt.getKeyCode() == KeyEvent.VK_SPACE) {
                jTable1.transferFocus();
                col=0;
            }


            cntrolButtons();
        }else {
            jTable1.changeSelection(0,0, false, false);
            jTable1.editCellAt(0,0);
        }
    }//GEN-LAST:event_jTable1KeyReleased

    private void jTable1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MousePressed

        row = jTable1.getSelectedRow();
        col = jTable1.getSelectedColumn();
        cntrolButtons();

        if(evt.isPopupTrigger() && row != -1 && col != -1 ) {
            
            if(jTable1.rowAtPoint(evt.getPoint()) != row || jTable1.columnAtPoint(evt.getPoint()) != col)
                jpm_table.getComponent(1).setEnabled(false);
            else
                jpm_table.getComponent(1).setEnabled(true);
            
            jpm_table.show(evt.getComponent(), evt.getX(), evt.getY());
        }

    }//GEN-LAST:event_jTable1MousePressed

    private void button_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_saveActionPerformed
        if(pd == null || jComboBox1.getSelectedIndex() == -1)
            return;
        pd.save= !m.saveData(pd);
        if(!pd.save){
            clearValues();
            Main.refreshData(pd,true);
            pd_blank.data.clear();
            updateUi();
        }
    }//GEN-LAST:event_button_saveActionPerformed

    private void b_importActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_importActionPerformed
        if(jComboBox1.getSelectedIndex() == -1)
            return;
        if(jfc == null)
            initChooser();

        message("Note","Make sure you choose right exchange , this import csv file from: "+jComboBox1.getSelectedItem().toString());
        
        if(Main.lp != null && !Main.lp.last_Order.equals(m.sub))
        message("Note","Last order date in last import is: "+Main.lp.last_Order+"\n Please make sure next import not include this date or before.");
       
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

            int result =  m.importCsv(jfc.getSelectedFile());

           if(result > -1) { //success
               message("Success","Data imported successfully.\n orders added count: "+result);   
           }else{
               switch (result) { //Error
                   case -1:
                       messageError("Error!\n Error code: "+result);
                       break;                   
                   case -2:
                       messageError("Can't read this file!\n Error code: "+result);
                       break;
                   case -3:
                   case -4:
                   case -5:
                   case -6:
                   case -7:
                   case -8:
                   case -9:
                   case -10:
                       messageError("Data Format Error!\n Error code: "+result);
                       break;
                   default:
                       m.setExchange(jComboBox1.getSelectedItem().toString(), true);
                       messageError("Data Format Error!\n Error code: "+result);
                       break;
               }
           } 
        }
    }//GEN-LAST:event_b_importActionPerformed

    private void button_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_newActionPerformed
          pd_blank.data.clear();
          jTable1.setModel(pd_blank);
          pd=pd_blank;
          jList1.clearSelection();
          clearValues();
          updateUi();
    }//GEN-LAST:event_button_newActionPerformed

    private void jList1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MousePressed
       if(evt.isPopupTrigger() && !jList1.isSelectionEmpty())
       jpm_list.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_jList1MousePressed

    private void jList1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jList1KeyPressed
        if(!jList1.isSelectionEmpty()) {
            if(evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deletePair();
            }else if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
                setPair();

            }
        }
    }//GEN-LAST:event_jList1KeyPressed

    private void button_row_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_row_deleteActionPerformed
        if(pd == null)
            return;
        row = jTable1.getSelectedRow();
        if(row > -1 && row < pd.getRowsCount()) {
            PairDataUtils.removeRow(pd,row);
            if(row > 0)
               row--;
            if(col < 0)
               col=0;
            jTable1.changeSelection(row,col, false, false);
            pd.save=true;
            if(Main.lp != null)
                Main.lp.is_Fee_Calculated=false;
            cntrolButtons();
        }
    }//GEN-LAST:event_button_row_deleteActionPerformed

    public void cntrolButtons () {
        if(pd != null) {
            if(row > -1) {
                button_row_up.setEnabled(row > 0 && row < pd.getRowsCount());
                button_row_down.setEnabled(row < pd.getRowsCount()-1);
                button_row_delete.setEnabled(row < pd.getRowsCount());               
            }
            button_save.setEnabled(pd.save);
            button_new.setEnabled((pd != pd_blank && !pd.save));

        }
           
    }
    public void clearData() {
        clearValues();
        jTable1.setModel(pd_blank);
        jList1.setModel(lp_blank);
        pd=pd_blank;
        updateUi();
        
        
    }
    public void setPair() {
        clearValues();
        m.setPairData(jList1.getSelectedValue());        
        updateUi();    
        // scroll jtable 
    }
    
    public void deletePair() {
        
        int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure delete this pair? \n OK [ENTER]\nCANCEL [ESC]", "Delete Pair", JOptionPane.OK_CANCEL_OPTION);        
        if(dialogResult == JOptionPane.OK_OPTION){
        
            f = new File(m.path+jComboBox1.getSelectedItem()+File.separator+jList1.getSelectedValue()+m.extension);
            if(f.exists()) {
                f.delete();
                m.removeFeeFromPair(pd);
                Main.lp.pairData.remove(jList1.getSelectedIndex());
                clearData();
                jList1.setModel(Main.lp);
            }
        
        }
    }

    
    public void clearValues () {
       all_Buy.setText(null);
       all_Sell.setText(null);
       total.setText(null);
       quantity_Buy.setText(null);
       quantity_Sell.setText(null);
       total_Quantity.setText(null);
       all_fee.setText(null);
       average.setText(null);
       all_Time_average.setText(null);


        
    }

    private void add_Delete_Exchange() {
        jComboBox1.setSelectedIndex(-1);
        jComboBox1.hidePopup();
        String exchange_Name = JOptionPane.showInputDialog("Enter Exchange Name");
        if(exchange_Name == null || exchange_Name.isEmpty()) {         
            jComboBox1.setSelectedItem(m.lastExchange);
            return;
        }
        
        for (ListPairs l : Exchanges.listExchange) {
            if(l.exchangeName.equalsIgnoreCase(exchange_Name)) {
                if(Exchanges.listExchange.size() < 2) {
                    messageError ("This is Last Exchange! , please add new one before delete this exchange");
                    exchange_Name=null;
                    break;
                }
                int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure delete this Exchange? \n OK [ENTER]\nCANCEL [ESC]", "Delete Exchange", JOptionPane.OK_CANCEL_OPTION);
                if(dialogResult == JOptionPane.OK_OPTION){
                    
                    f = new File(m.path+File.separator+l.exchangeName);
                    if(f.exists()) {
                        for (File mf : f.listFiles()) {
                            mf.delete();
                        }
                        f.delete();
                        
                    }
                    Exchanges.listExchange.remove(l);
                    clearData();
                    exchange_Name=null;
                    break;
                }else {
                    jComboBox1.setSelectedItem(m.lastExchange);
                    return;
                }
            }
                
        }
        if(exchange_Name != null && !exchange_Name.isEmpty()) {//Add new
            String exchange_Fee_Currency = JOptionPane.showInputDialog("Enter default Fee Currency \n Example:\n Binance: BNB\n Kucoin: USDT\n");
            if(exchange_Fee_Currency == null || exchange_Fee_Currency.isEmpty())
                return;
            exchange_Fee_Currency = exchange_Fee_Currency.toUpperCase();
            if(exchange_Fee_Currency.matches("[A-Z]+|\\p{Sc}")) {
              
              String exchange_Fee_Percentage = JOptionPane.showInputDialog("Enter default Fee Percentage (without %)\n Example:\n Binance: 0.075% \n Kucoin: 0.1% (Class A) \n Note: We using this value if fee curruncy different form pair example: BTC-USDT and fee in BNB\n you can add custom fee percente for every pair by right click in list");
              if(exchange_Fee_Percentage == null || exchange_Fee_Percentage.isEmpty())
                return;
              if(exchange_Fee_Percentage.matches("[-0-9.]+")) {
                  Main.exchanges.addExchange(exchange_Name, exchange_Fee_Currency, BigDecimal.valueOf(Double.parseDouble(exchange_Fee_Percentage)).movePointLeft(2),m.sub);
                  jComboBox1.setSelectedItem(exchange_Name);
                  m.setExchange(exchange_Name , false);
                  clearData();
              }else
                  messageError("Error! try input Numbers only!");
            }else
                messageError("Error! Accepted values: [A to Z] OR [$,€,¥,£..]!");
        }
        
    }
        
    private void updateUi () {
        
       jTable1.getColumnModel().getColumn(jTable1.getColumnModel().getColumnIndex(Main.hdr_Side)).setMaxWidth(50);
       button_row_up.setEnabled(false);
       button_row_down.setEnabled(false);
       button_row_delete.setEnabled(false);
       if(pd != null) {
            button_save.setEnabled(pd.save);
            button_new.setEnabled((pd != pd_blank && !pd.save));
       }
    }

   
    public static void editCellError (int error, int rowIndex, int columnIndex) { 
        switch (error) {
            case 0:
                messageError("Accepted values is:\n b or BUY\n s or SELL"); //Side
                break;
            case 1:
                messageError("Your input is not an Integer! Please try again"); //Price
                break;
            case 2:
                messageError("Please enter integer with currancy name or symbol [$,€,¥,£..].\n example:\n 0.123456BTC\n 100USDT\n 100$"); //Quantity , Amount , Fee
                break;                
            default:
                return;
        }
        jTable1.requestFocus();      
    }
    
    public static void messageError (String message) {
        
        JOptionPane.showMessageDialog (null, message, "ERROR", JOptionPane.ERROR_MESSAGE);    
    }
    public static void message (String title , String message) {
        
        JOptionPane.showMessageDialog (null, message, title, JOptionPane.INFORMATION_MESSAGE);    
    }

    public void initChooser() {
       jfc = new JFileChooser();
       jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
       jfc.setAcceptAllFileFilterUsed(false);
       jfc.setDialogTitle("Import CSV file");
       jfc.setPreferredSize(new Dimension((int)(displayWidth/scale), (int)(displayHeight/scale)));
       jfc.addChoosableFileFilter(new FileNameExtensionFilter("CSV file .csv", "csv"));   
    }
        
    public boolean confirmSaveData() {
        int dialogResult = JOptionPane.showConfirmDialog(null, "Some changes in data not saved , do you want save it?", "Save Data", JOptionPane.YES_NO_OPTION);        
        return dialogResult == JOptionPane.YES_OPTION;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JTextField all_Buy;
    public static javax.swing.JTextField all_Sell;
    public static javax.swing.JTextField all_Time_average;
    public static javax.swing.JTextField all_fee;
    public static javax.swing.JTextField average;
    public static javax.swing.JButton b_import;
    private javax.swing.JButton button_new;
    private javax.swing.JButton button_row_delete;
    private javax.swing.JButton button_row_down;
    private javax.swing.JButton button_row_up;
    private javax.swing.JButton button_save;
    public static javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    public static javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public static javax.swing.JTable jTable1;
    private java.awt.Panel panel1;
    public static javax.swing.JTextField quantity_Buy;
    public static javax.swing.JTextField quantity_Sell;
    public static javax.swing.JTextField total;
    public static javax.swing.JTextField total_Quantity;
    // End of variables declaration//GEN-END:variables


}

