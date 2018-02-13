/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.experiments;

import com.panayotis.gnuplot.GNUPlotException;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.FileDataSet;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.swing.JPlot;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.experiments.settings.iWASFGA_Settings;
import jmetal.metaheuristics.iwasfga.iWASFGA;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.AchievementScalarizingFunction;
import jmetal.util.JMException;
import jmetal.util.ReferencePoint;
import jmetal.util.kmeans.KMeans;
import jmetal.util.kmeans.Point;
import jmetal.needed.ValuePath;

/**
 *
 * @author ruben
 */
public class iWASFGAStudy extends javax.swing.JFrame {

    final char SEPARATOR_INDEX_IN_NAME = '-';
    final String[] FILE_OF_WEIGHTS_FOR_KMEANS = new String[]{"300W-KSIMPLEX", "364W-KSIMPLEX", "330W-KSIMPLEX", "462W-KSIMPLEX"};
    final String OUTPUT_FOLDER = "./iWASFGAStudy";
    final String PARETO_FRONTS_FOLDER = "data/paretoFronts/";
    final int DEFAULT_POPULATION_SIZE_FOR_2D = 200;
    final int DEFAULT_GENERATIONS_NUMBER_FOR_2D = 300;
    final int DEFAULT_POPULATION_SIZE_FOR_3D = 300;
    final int DEFAULT_GENERATIONS_NUMBER_FOR_3D = 400;
    final int DECIMALS = 4;

    private Map<String, JLabel> jLabel = new HashMap<>();
    private Map<String, MyComponents.FloatJSlider> floatJSlider = new HashMap<>();
    private SolutionSet[] solutions;
    private double[] idealPoint, nadirPoint;
    private iWASFGA algorithm;

    JavaPlot javaPlot1;

    private boolean initExperiment(String problemName, String paretoFrontFilePath, String folderForOutputFiles) throws JMException, CloneNotSupportedException {
        Object[] problemParams = {"Real"};
        Problem problem = (new ProblemFactory()).getProblem(problemName, problemParams);
        MetricsUtil paretoFrontInformation = new MetricsUtil();
        boolean result = false;
        int objectivesNumber = ((Integer) jSpinnerObjectivesNumber.getValue()).intValue();
        String weightsDirectory = new String("data/weights");
        String weightsFileName = new String(((Integer) jSpinnerSolutionsNumber.getValue()).intValue() + "W." + objectivesNumber + "D");

        //Common configuration        
        HashMap parameters = new HashMap();
        parameters.put("populationSize_", ((Integer) jSpinnerPopulationSize.getValue()).intValue());
        parameters.put("numberOfWeights_", ((Integer) jSpinnerSolutionsNumber.getValue()).intValue());
        parameters.put("generations_", ((Integer) jSpinnerGenerationsNumber.getValue()).intValue());
        parameters.put("folderForOutputFiles_", folderForOutputFiles);
        parameters.put("weightsDirectory_", weightsDirectory);
        parameters.put("weightsFileName_", weightsFileName);
        parameters.put("allowRepetitions_", true);
        parameters.put("normalization_", true);
        parameters.put("estimatePoints_", false);

        if (new File(paretoFrontFilePath).exists()) {
            double[][] paretoFront = paretoFrontInformation.readFront(paretoFrontFilePath);

            idealPoint = paretoFrontInformation.getMinimumValues(paretoFront, objectivesNumber);
            nadirPoint = paretoFrontInformation.getMaximumValues(paretoFront, objectivesNumber);

            parameters.put("referencePoint_", new ReferencePoint(idealPoint));
            parameters.put("asf_", new AchievementScalarizingFunction(null, nadirPoint, idealPoint));

            result = true;
        } else if (problemName.contains("DTLZ")) {
            if (problemName.equals("DTLZ1")) {
                idealPoint = new double[objectivesNumber];
                nadirPoint = new double[objectivesNumber];

                for (int i = 0; i < objectivesNumber; i++) {
                    idealPoint[i] = 0;
                    nadirPoint[i] = 0.5;
                }

                parameters.put("referencePoint_", new ReferencePoint(idealPoint));
                parameters.put("asf_", new AchievementScalarizingFunction(null, nadirPoint, idealPoint));
                result = true;
            } else if (problemName.equals("DTLZ2")) {
                idealPoint = new double[objectivesNumber];
                nadirPoint = new double[objectivesNumber];

                for (int i = 0; i < objectivesNumber; i++) {
                    idealPoint[i] = 0;
                    nadirPoint[i] = 1;
                }
                parameters.put("referencePoint_", new ReferencePoint(idealPoint));
                parameters.put("asf_", new AchievementScalarizingFunction(null, nadirPoint, idealPoint));
                result = true;
            }
        }

        if (result) {
            try {
                algorithm = (iWASFGA) (new iWASFGA_Settings(problemName, ((Integer) jSpinnerObjectivesNumber.getValue()).intValue()).configure(parameters));
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
    }

    /**
     * Creates new form Ventana
     */
    public iWASFGAStudy() {
        beforeInitComponents();

        initComponents();

        this.setLocationRelativeTo(null);

        this.jPanelReferencePoint.removeAll();

        try {
            initExperiment((String) jComboBoxProblemName.getSelectedItem(), PARETO_FRONTS_FOLDER + jComboBoxProblemName.getSelectedItem() + "." + jSpinnerObjectivesNumber.getValue().toString() + "D.pf", OUTPUT_FOLDER);
        } catch (JMException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        }

        jSpinnerSolutionsNumber.setValue(((Integer) jSpinnerObjectivesNumber.getValue()).intValue() * 2);
        jSpinnerPopulationSize.setValue(200);
        jSpinnerGenerationsNumber.setValue(300);

        loadReferencePointUI(Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()), ((Integer) jSpinnerSolutionsNumber.getValue()).intValue(), idealPoint, nadirPoint);
        algorithm.setInputParameter("referencePoint", getReferencePoint());

        //Load problem's pareto optimal front
        javaPlot1.set("term", "png size " + (jPlot1.getWidth() - 1) + ", " + (jPlot1.getHeight() - 1));
        javaPlot1.set("grid", "");

        plotParetoFront(jPlot1);
        plotReferencePoint(jPlot1);
        repaintPlot(jPlot1);

        jTextAreaLog.append("Application started successfully ;-)");
        redirectSystemStreams();
    }

    private void beforeInitComponents() {
        try {
            InputStream is;
            is = new FileInputStream("data/iWASFGA.config");
            Properties prop = new Properties();
            prop.load(is);

            String parameter = prop.getProperty("GNUPLOT_PATH");
            if (new File(parameter).exists()) {
                javaPlot1 = new com.panayotis.gnuplot.JavaPlot(parameter);
                javaPlot1.setGNUPlotPath(parameter);
            } else {
                try {
                    javaPlot1 = new com.panayotis.gnuplot.JavaPlot("c:\\gnuplot\\bin\\gnuplot.exe");
                    javaPlot1.setGNUPlotPath("c:\\gnuplot\\bin\\gnuplot.exe");
                } catch (GNUPlotException ex) {
                    JOptionPane.showMessageDialog(null, "It is not possible to find GNUPlot in the system."
                            + "\n\n"
                            + "You have to check if it is installed and the information in the configuration file 'data\\iWASFGA.config' is correct."
                            + "\n\n"
                            + "If you need help you can read the 'README.txt' file.", "iWASFGA: Problems with GNUPLot.", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }

                JOptionPane.showMessageDialog(null,
                        "The file '" + parameter + "' does not exist."
                        + "\n\n"
                        + "If GNUPlot.exe is not in the default path ('C:\\gnuplot\\bin\\gnuplot.exe'), the application will fail."
                        + "\n"
                        + "In this case, you must install GNUPlot and modify adequately the file 'data\\iWASFGA.config' using your favourite text editor.",
                        "iWASFGA: Problems with GNUPLot?", JOptionPane.WARNING_MESSAGE);
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "The configuration file 'data\\iWASFGA.config' does not exist.\n\nGNUPlot.exe must be in the default path (c:\\gnuplot\\bin\\gnuplot.exe).", "iWASFGA: Problems with the configuration file.", JOptionPane.WARNING_MESSAGE);
            try {
                javaPlot1 = new com.panayotis.gnuplot.JavaPlot("c:\\gnuplot\\bin\\gnuplot.exe");
                //javaPlot1.setGNUPlotPath("c:\\gnuplot\\bin\\gnuplot.exe");
            } catch (GNUPlotException ex1) {
                JOptionPane.showMessageDialog(null, "It is not possible to find GNUPlot in the system."
                        + "\n\n"
                        + "You have to check if it is installed and the information in the configuration file 'data\\iWASFGA.config' is correct."
                        + "\n\n"
                        + "If you need help you can read the 'README.txt' file.", "iWASFGA: Problems with GNUPLot.", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        } catch (IOException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        }

        javaPlot1.setPersist(false);
        javaPlot1.setTerminal(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelReferencePoint = new javax.swing.JPanel();
        jLabel0 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        floatJSlider2 = new MyComponents.FloatJSlider();
        jLabel6 = new javax.swing.JLabel();
        jPanelActions = new javax.swing.JPanel();
        jButtonStart = new javax.swing.JButton();
        jButtonNextIteration = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPaneSolutions = new javax.swing.JScrollPane();
        jTableSolutions = new javax.swing.JTable();
        jScrollPaneLog = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jPanelPlot = new javax.swing.JPanel();
        jPlot1 = new com.panayotis.gnuplot.swing.JPlot(javaPlot1);
        jPanelProblemConfiguration = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxProblemName = new javax.swing.JComboBox();
        jSpinnerObjectivesNumber = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        jPanelAlgorithmConfiguration = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jSpinnerSolutionsNumber = new javax.swing.JSpinner();
        jSpinnerGenerationsNumber = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jSpinnerPopulationSize = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Interactive WASF-GA");
        setIconImages(null);
        setResizable(false);

        jPanelReferencePoint.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Reference point", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanelReferencePoint.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanelReferencePoint.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanelReferencePoint.setPreferredSize(new java.awt.Dimension(100, 325));
        jPanelReferencePoint.setLayout(new java.awt.GridLayout(2, 4));

        jLabel0.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel0.setText("objective");
        jLabel0.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel0.setName("jLabel0"); // NOI18N
        jPanelReferencePoint.add(jLabel0);

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("value");
        jPanelReferencePoint.add(jLabel13);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("ideal");
        jPanelReferencePoint.add(jLabel1);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("-");
        jPanelReferencePoint.add(jLabel2);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("nadir");
        jPanelReferencePoint.add(jLabel3);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("jLabel4");
        jPanelReferencePoint.add(jLabel4);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("jLabel4");
        jPanelReferencePoint.add(jLabel14);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("jLabel5");
        jPanelReferencePoint.add(jLabel5);
        jPanelReferencePoint.add(floatJSlider2);

        jLabel6.setText("jLabel6");
        jPanelReferencePoint.add(jLabel6);

        jPanelActions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Solution process", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanelActions.setLayout(new java.awt.GridLayout(1, 0));

        jButtonStart.setText("Start");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });
        jPanelActions.add(jButtonStart);

        jButtonNextIteration.setText("Next iteration");
        jButtonNextIteration.setEnabled(false);
        jButtonNextIteration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextIterationActionPerformed(evt);
            }
        });
        jPanelActions.add(jButtonNextIteration);

        jButton2.setLabel("Exit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanelActions.add(jButton2);

        jScrollPaneSolutions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Solutions", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jScrollPaneSolutions.setAutoscrolls(true);
        jScrollPaneSolutions.setMaximumSize(new java.awt.Dimension(100, 100));
        jScrollPaneSolutions.setPreferredSize(new java.awt.Dimension(300, 200));

        jTableSolutions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableSolutions.setEnabled(false);
        jTableSolutions.setMaximumSize(new java.awt.Dimension(2147483647, 100000076));
        jTableSolutions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableSolutions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableSolutionsMousePressed(evt);
            }
        });
        jScrollPaneSolutions.setViewportView(jTableSolutions);

        jScrollPaneLog.setBorder(javax.swing.BorderFactory.createTitledBorder("Log"));

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setColumns(20);
        jTextAreaLog.setForeground(new java.awt.Color(0, 0, 102));
        jTextAreaLog.setLineWrap(true);
        jTextAreaLog.setRows(5);
        jScrollPaneLog.setViewportView(jTextAreaLog);

        jPanelPlot.setBorder(javax.swing.BorderFactory.createTitledBorder("Plot"));

        jPlot1.setBackground(new java.awt.Color(255, 255, 255));
        jPlot1.setAutoscrolls(true);
        jPlot1.setJavaPlot(javaPlot1);

        javax.swing.GroupLayout jPlot1Layout = new javax.swing.GroupLayout(jPlot1);
        jPlot1.setLayout(jPlot1Layout);
        jPlot1Layout.setHorizontalGroup(
            jPlot1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPlot1Layout.setVerticalGroup(
            jPlot1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelPlotLayout = new javax.swing.GroupLayout(jPanelPlot);
        jPanelPlot.setLayout(jPanelPlotLayout);
        jPanelPlotLayout.setHorizontalGroup(
            jPanelPlotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPlot1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelPlotLayout.setVerticalGroup(
            jPanelPlotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPlot1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanelProblemConfiguration.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Problem's configuration", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText("Problem name:");
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel7.setName("jLabel0"); // NOI18N

        jComboBoxProblemName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ZDT1", "ZDT2", "ZDT3", "ZDT4", "ZDT6", "DTLZ1", "DTLZ2", "DTLZ3", "DTLZ4", "DTLZ5", "DTLZ6", "DTLZ7", "WFG1", "WFG2", "WFG3", "WFG4", "WFG5", "WFG6", "WFG7", "WFG8", "WFG9" }));
        jComboBoxProblemName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxProblemNameActionPerformed(evt);
            }
        });

        jSpinnerObjectivesNumber.setModel(new javax.swing.SpinnerNumberModel(2, 2, 6, 1));
        jSpinnerObjectivesNumber.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerObjectivesNumberStateChanged(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Objectives number:");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel10.setName("jLabel0"); // NOI18N

        javax.swing.GroupLayout jPanelProblemConfigurationLayout = new javax.swing.GroupLayout(jPanelProblemConfiguration);
        jPanelProblemConfiguration.setLayout(jPanelProblemConfigurationLayout);
        jPanelProblemConfigurationLayout.setHorizontalGroup(
            jPanelProblemConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProblemConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSpinnerObjectivesNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBoxProblemName, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelProblemConfigurationLayout.setVerticalGroup(
            jPanelProblemConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProblemConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelProblemConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxProblemName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerObjectivesNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelAlgorithmConfiguration.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Algorithm's configuration", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Number of solutions:");
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel8.setName("jLabel0"); // NOI18N

        jSpinnerSolutionsNumber.setModel(new javax.swing.SpinnerNumberModel(4, 1, 50, 1));

        jSpinnerGenerationsNumber.setModel(new javax.swing.SpinnerNumberModel(100, 100, 10000, 100));
        jSpinnerGenerationsNumber.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerGenerationsNumberStateChanged(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("Number of generations:");
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel9.setName("jLabel0"); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Population size:");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel11.setName("jLabel0"); // NOI18N

        jSpinnerPopulationSize.setModel(new javax.swing.SpinnerNumberModel(50, 50, 1000, 50));
        jSpinnerPopulationSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerPopulationSizeStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelAlgorithmConfigurationLayout = new javax.swing.GroupLayout(jPanelAlgorithmConfiguration);
        jPanelAlgorithmConfiguration.setLayout(jPanelAlgorithmConfigurationLayout);
        jPanelAlgorithmConfigurationLayout.setHorizontalGroup(
            jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAlgorithmConfigurationLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinnerPopulationSize)
                    .addComponent(jSpinnerSolutionsNumber)
                    .addComponent(jSpinnerGenerationsNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelAlgorithmConfigurationLayout.setVerticalGroup(
            jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAlgorithmConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jSpinnerSolutionsNumber)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jSpinnerPopulationSize)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAlgorithmConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel9)
                    .addComponent(jSpinnerGenerationsNumber))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneSolutions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addComponent(jPanelReferencePoint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelActions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelAlgorithmConfiguration, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelPlot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelProblemConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(jScrollPaneLog, javax.swing.GroupLayout.DEFAULT_SIZE, 952, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelAlgorithmConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelReferencePoint, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPaneSolutions, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelActions, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelProblemConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelPlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonNextIterationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextIterationActionPerformed
        final iWASFGAStudy study = this;

        Thread thr = new Thread() {
            @Override
            public void run() {
                if (new File((PARETO_FRONTS_FOLDER + jComboBoxProblemName.getSelectedItem() + "." + jSpinnerObjectivesNumber.getValue().toString() + "D.pf")).exists()
                        || jComboBoxProblemName.getSelectedItem().equals("DTLZ1")
                        || jComboBoxProblemName.getSelectedItem().equals("DTLZ2")) {
                    if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) > 2) {
                        String weightsFileName = jSpinnerSolutionsNumber.getValue() + "W." + jSpinnerObjectivesNumber.getValue() + "d";
                        algorithm.setInputParameter("weightsFileName", weightsFileName);
                        generateWeightsVectorsWithKmeans((String) algorithm.getInputParameter("weightsDirectory"), (String) algorithm.getInputParameter("weightsFileName"));
                    }

                    jTextAreaLog.append("\n- Iteration run using reference point " + getReferencePoint().toString(DECIMALS) + ".");

                    try {
                        solutions = algorithm.doIteration(solutions[0], getReferencePoint(), ((Integer) jSpinnerSolutionsNumber.getValue()).intValue());
                    } catch (JMException ex) {
                        Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    readSolutions(solutions[1]);

                    jPlot1.getJavaPlot().getPlots().clear();
                    if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) < 3) {
                        plotParetoFront(jPlot1);
                        plotSolutions(solutions[1], jPlot1);
                        plotReferencePoint(jPlot1);
                    } else {
                        plotValuePath(solutions[1], true);
                    }

                    repaintPlot(jPlot1);
                    setTickLabels();
                } else {
                    JOptionPane.showMessageDialog(study, "The " + jSpinnerObjectivesNumber.getValue().toString() + " objectives " + jComboBoxProblemName.getSelectedItem() + " problem does not exist.");
                }
            }
        };
        thr.start();
        thr.yield();
    }//GEN-LAST:event_jButtonNextIterationActionPerformed

    private ReferencePoint getReferencePoint() {
        ReferencePoint result;

        double[] values = new double[floatJSlider.size()];
        for (int i = 0; i < floatJSlider.size(); i++) {
            values[i] = floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).getFloatValue();
        }

        result = new ReferencePoint(values);

        return result;
    }

    private double roundWithPrecision(double number, int numberOfDigits) {
        return (Math.rint(number * Math.pow(10, numberOfDigits)) / Math.pow(10, numberOfDigits));
    }

    private ReferencePoint loadReferencePointUI(int numberOfObjectives, int numberOfSolutions, double[] idealPoint, double[] nadirPoint) {
        //We show the information for the solutions in a table                     
        ReferencePoint result = new ReferencePoint(numberOfObjectives);
        String[] names;
        double referenceValue;
        Double[][] data;
        int i;
        JLabel lbl;
        MyComponents.FloatJSlider fs;
        DefaultTableModel tm;

        floatJSlider.clear();
        jPanelReferencePoint.removeAll();

        data = new Double[numberOfSolutions][numberOfObjectives + 1];

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 100;
        c.weighty = 100;

        this.jPanelReferencePoint.setLayout(gbl);

        Font newLabelFont = new Font(jLabel0.getFont().getName(), Font.BOLD, jLabel0.getFont().getSize());

        lbl = new JLabel("objective");
        lbl.setVisible(true);
        lbl.setEnabled(true);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setName("jLabelObjective");
        lbl.setFont(newLabelFont);
        c.gridx = 0;
        c.gridy = 0;
        jPanelReferencePoint.add(lbl, c);
        lbl = new JLabel("value");
        lbl.setVisible(true);
        lbl.setEnabled(true);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setName("jLabelSelecteValue");
        lbl.setFont(newLabelFont);
        c.gridx = 1;
        jPanelReferencePoint.add(lbl, c);
        lbl = new JLabel("ideal");
        lbl.setVisible(true);
        lbl.setEnabled(true);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setName("jLabelIdeal");
        lbl.setFont(newLabelFont);
        c.gridx = 2;
        jPanelReferencePoint.add(lbl, c);
        lbl = new JLabel("");
        lbl.setVisible(true);
        lbl.setEnabled(true);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setName("jLabelValue");
        lbl.setFont(newLabelFont);
        c.gridx = 3;
        jPanelReferencePoint.add(lbl, c);
        lbl = new JLabel("nadir");
        lbl.setVisible(true);
        lbl.setEnabled(true);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setName("jLabelNadir");
        lbl.setFont(newLabelFont);
        c.gridx = 4;
        jPanelReferencePoint.add(lbl, c);

        c.gridy++;
        names = new String[numberOfObjectives + 1];
        names[0] = "Solution";
        for (i = 0; i < numberOfObjectives; i++) {
            names[i + 1] = "f " + (i + 1);

            referenceValue = ((Math.abs(nadirPoint[i]) - idealPoint[i]) / 2) + idealPoint[i];

            //We show the information for the reference point  
            lbl = new JLabel("f" + (i + 1));
            lbl.setVisible(true);
            lbl.setEnabled(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setName("jLabelObjective" + SEPARATOR_INDEX_IN_NAME + i);
            c.gridx = 0;
            jPanelReferencePoint.add(lbl, c);

            lbl = new JLabel(Double.toString(roundWithPrecision(referenceValue, DECIMALS)));
            lbl.setVisible(true);
            lbl.setEnabled(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setName("jLabelSelectedValue" + SEPARATOR_INDEX_IN_NAME + i);
            jLabel.put(lbl.getName(), lbl);
            c.gridx = 1;
            jPanelReferencePoint.add(lbl, c);

            lbl = new JLabel(Double.toString(roundWithPrecision(idealPoint[i], DECIMALS)));
            lbl.setVisible(true);
            lbl.setEnabled(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setName("jLabelMinimum" + SEPARATOR_INDEX_IN_NAME + i);
            jLabel.put(lbl.getName(), lbl);
            c.gridx = 2;
            jPanelReferencePoint.add(lbl, c);

            fs = new MyComponents.FloatJSlider();

            fs.setName("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i);
            fs.setFloatPrecision(DECIMALS);
            fs.setFloatMinimum(Double.valueOf(idealPoint[i]).floatValue());
            fs.setFloatMaximum(Double.valueOf(nadirPoint[i]).floatValue());

            fs.setFloatValue(new Double(referenceValue).floatValue());

            fs.setVisible(true);
            fs.setEnabled(true);
            floatJSlider.put(fs.getName(), fs);
            fs.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    String name = new String(((MyComponents.FloatJSlider) e.getSource()).getName());
                    String indexOfComponent = name.substring(name.lastIndexOf('-') + 1);
                    jLabel.get("jLabelSelectedValue" + SEPARATOR_INDEX_IN_NAME + indexOfComponent).setText(Float.toString(floatJSlider.get(name).getFloatValue()));
                }
            });
            c.gridx = 3;
            jPanelReferencePoint.add(fs, c);

            lbl = new JLabel(Double.toString(roundWithPrecision(nadirPoint[i], DECIMALS)));
            lbl.setVisible(true);
            lbl.setEnabled(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setName("jLabelMaximum" + SEPARATOR_INDEX_IN_NAME + i);
            jLabel.put(lbl.getName(), lbl);
            c.gridx = 4;
            jPanelReferencePoint.add(lbl, c);
            c.gridy++;

            result.set(i, Float.valueOf(fs.getFloatValue()).doubleValue());
        }

        tm = new DefaultTableModel(data, names);
        jTableSolutions.setModel(tm);
        jTableSolutions.repaint();

        jPanelReferencePoint.revalidate();
        jPanelReferencePoint.repaint();

        return result;
    }

    private void readSolutions(SolutionSet ss) {
        //We show the information for the solutions in a table                               
        String[] names;
        Object[][] data;
        int i, j;
        DefaultTableModel tm;

        names = new String[ss.get(0).numberOfObjectives() + 1];
        names[0] = "Solution";
        for (i = 0; i < ss.get(0).numberOfObjectives(); i++) {
            names[i + 1] = "f " + (i + 1);
        }

        data = new Object[ss.size()][ss.get(0).numberOfObjectives() + 1];
        for (i = 0; i < ss.size(); i++) {
            for (j = 0; j < ss.get(i).numberOfObjectives() + 1; j++) {
                if (j == 0) {
                    data[i][j] = "S" + (i + 1);
                } else {
                    data[i][j] = roundWithPrecision(ss.get(i).getObjective(j - 1), DECIMALS);
                }
            }
        }
        
        tm = new DefaultTableModel(data, names);
        jTableSolutions.setModel(tm);
        jTableSolutions.repaint();

        jPanelReferencePoint.revalidate();
        jPanelReferencePoint.repaint();
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTableSolutionsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableSolutionsMousePressed
        // TODO add your handling code here:
        /*
         Hashtable tickLabel;
         JLabel jLabelTick;
         Integer minIntValue, maxIntValue;
         Float floatNumber, minFloatValue, maxFloatValue;

         if (jTableSolutions.getValueAt(jTableSolutions.getSelectedRow(), jTableSolutions.getSelectedColumn()) != null) {
         for (int i = 1; i < jTableSolutions.getColumnCount(); i++) {
         minIntValue = floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + (i-1)).getMinimum();
         maxIntValue = floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + (i-1)).getMaximum();
         floatNumber = Float.valueOf(jTableSolutions.getValueAt(jTableSolutions.getSelectedRow(), i).toString());
         minFloatValue = floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + (i-1)).getFloatMinimum();
         maxFloatValue = floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + (i-1)).getFloatMaximum();

         //Double value = roundWithPrecision((Double) jTableSolutions.getValueAt(jTableSolutions.getSelectedRow(), i), DECIMALS);                
         jLabelTick = new JLabel(jTableSolutions.getValueAt(jTableSolutions.getSelectedRow(), 0).toString());
         jLabelTick.setFont(new Font("", ITALIC, 11));
         tickLabel = new Hashtable();
         tickLabel.put(new Integer(scaleFloatToIntValue(floatNumber, minIntValue, maxIntValue, minFloatValue, maxFloatValue)), jLabelTick);                
                
         floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + (i-1)).setLabelTable(tickLabel);
         }
         }
         */
    }//GEN-LAST:event_jTableSolutionsMousePressed

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        final iWASFGAStudy study = this;

        Thread thr = new Thread() {
            @Override
            public void run() {
                boolean isPossibleToExecute = true;

                try {
                    isPossibleToExecute = initExperiment((String) jComboBoxProblemName.getSelectedItem(), PARETO_FRONTS_FOLDER + jComboBoxProblemName.getSelectedItem() + "." + jSpinnerObjectivesNumber.getValue().toString() + "D.pf", OUTPUT_FOLDER);
                } catch (JMException ex) {
                    Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (isPossibleToExecute) {
                    if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) > 2) {
                        generateWeightsVectorsWithKmeans((String) algorithm.getInputParameter("weightsDirectory"), (String) algorithm.getInputParameter("weightsFileName"));
                    }

                    algorithm.setInputParameter("referencePoint", getReferencePoint());
                    jTextAreaLog.append("\n- iWASFGA executed using reference point " + getReferencePoint().toString(DECIMALS) + ".");
                   
                    try {
                        solutions = algorithm.executeFirstIteration();
                    } catch (JMException ex) {
                        Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    readSolutions(solutions[1]);

                    jPlot1.getJavaPlot().getPlots().clear();
                    if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) < 3) {
                        plotParetoFront(jPlot1);
                        plotSolutions(solutions[1], jPlot1);
                        plotReferencePoint(jPlot1);
                    } else {
                        plotValuePath(solutions[1], true);
                    }

                    //changeSelection(0, 0, false, false);
                    repaintPlot(jPlot1);

                    setTickLabels();
                    jButtonNextIteration.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(study, "The " + jSpinnerObjectivesNumber.getValue().toString() + " objectives " + jComboBoxProblemName.getSelectedItem() + " problem does not exist.");
                }
            }
        };
        thr.start();
        thr.yield();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jSpinnerPopulationSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerPopulationSizeStateChanged
        // TODO add your handling code here:
        int previousValue = ((Integer) jSpinnerSolutionsNumber.getValue()).intValue();

        if (previousValue <= ((Integer) jSpinnerPopulationSize.getValue()).intValue()) {
            jSpinnerSolutionsNumber.setModel(new SpinnerNumberModel(previousValue, 1, ((Integer) jSpinnerPopulationSize.getValue()).intValue(), 1));
        } else {
            jSpinnerSolutionsNumber.setModel(new SpinnerNumberModel(((Integer) jSpinnerObjectivesNumber.getValue()).intValue() * 2, 1, ((Integer) jSpinnerPopulationSize.getValue()).intValue(), 1));
        }

        jButtonNextIteration.setEnabled(false);
    }//GEN-LAST:event_jSpinnerPopulationSizeStateChanged

    private void jSpinnerGenerationsNumberStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerGenerationsNumberStateChanged
        // TODO add your handling code here:
        jButtonNextIteration.setEnabled(false);
    }//GEN-LAST:event_jSpinnerGenerationsNumberStateChanged

    private void jSpinnerObjectivesNumberStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerObjectivesNumberStateChanged
        boolean result;

        Integer objectivesNumber = (Integer) jSpinnerObjectivesNumber.getValue();
        String selectedProblem = (String) jComboBoxProblemName.getSelectedItem();
        ComboBoxModel<String> cbm;

        switch (objectivesNumber) {
            case 2:
                cbm = new DefaultComboBoxModel<>(new String[]{
                    "ZDT1", "ZDT2", "ZDT3", "ZDT4", "ZDT6",
                    "DTLZ1", "DTLZ2", "DTLZ3", "DTLZ4", "DTLZ5", "DTLZ6", "DTLZ7",
                    "WFG1", "WFG2", "WFG3", "WFG4", "WFG5", "WFG6", "WFG7", "WFG8", "WFG9"
                });

                jSpinnerPopulationSize.setValue(DEFAULT_POPULATION_SIZE_FOR_2D);
                jSpinnerGenerationsNumber.setValue(DEFAULT_GENERATIONS_NUMBER_FOR_2D);
                break;

            case 3:
                cbm = new DefaultComboBoxModel<>(new String[]{
                    "DTLZ1", "DTLZ2", "DTLZ3", "DTLZ4", "DTLZ5", "DTLZ6", "DTLZ7",
                    "WFG1", "WFG2", "WFG3", "WFG4", "WFG5", "WFG6", "WFG7", "WFG8", "WFG9"
                });

                jSpinnerPopulationSize.setValue(DEFAULT_POPULATION_SIZE_FOR_3D);
                jSpinnerGenerationsNumber.setValue(DEFAULT_GENERATIONS_NUMBER_FOR_3D);
                break;

            case 4:
            case 5:
            case 6:
                cbm = new DefaultComboBoxModel<>(new String[]{
                    "DTLZ1", "DTLZ2"
                });
                break;

            default:
                cbm = new DefaultComboBoxModel<>(new String[]{});
        }

        jComboBoxProblemName.setModel(cbm);
        jComboBoxProblemName.setSelectedIndex(0);

        result = setExperimentForNewProblem();
    }//GEN-LAST:event_jSpinnerObjectivesNumberStateChanged

    private void jComboBoxProblemNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxProblemNameActionPerformed

        setExperimentForNewProblem();
    }//GEN-LAST:event_jComboBoxProblemNameActionPerformed

    private void showDialog(final Component component, final String title, final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(component, message, title, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void setTickLabels() {
        for (int i = 0; i < (int) jSpinnerObjectivesNumber.getValue(); i++) {
            Hashtable tickLabel = new Hashtable();
            tickLabel.put(new Integer(scaleFloatToIntValue(
                    floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).getFloatValue(),
                    floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).getMinimum(),
                    floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).getMaximum(),
                    floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).getFloatMinimum(),
                    floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).getFloatMaximum())),
                    new JLabel("|"));

            floatJSlider.get("floatJSlider" + SEPARATOR_INDEX_IN_NAME + i).setLabelTable(tickLabel);
        }
    }

    private void plotSolutions(SolutionSet ss, JPlot jplot) {
        PlotStyle myPlotStyle = new PlotStyle();
        myPlotStyle.setStyle(Style.POINTS);
        myPlotStyle.setPointType(6);
        myPlotStyle.setPointSize(2);
        myPlotStyle.setLineWidth(2);
        myPlotStyle.set("linecolor", "1");

        DataSetPlot dataSetPlot = new DataSetPlot(ss.writeObjectivesToMatrix());
        dataSetPlot.setTitle("Solutions");
        dataSetPlot.setPlotStyle(myPlotStyle);

        jplot.getJavaPlot().addPlot(dataSetPlot);
    }

    private void plotValuePath(SolutionSet ss, boolean plotReferencePoint) {
        int numberOfObjectives = ss.get(0).numberOfObjectives();
        double[][] data = ValuePath.getSolutionsForPlot(ss);
        double minimumValue = Double.MAX_VALUE, maximumValue = Double.MIN_VALUE;

        for (int i = 0; i < numberOfObjectives; i++) {
            if (idealPoint[i] < minimumValue) {
                minimumValue = idealPoint[i];
            }

            if (nadirPoint[i] > maximumValue) {
                maximumValue = nadirPoint[i];
            }
        }

        //Plot information
        jPlot1.getJavaPlot().set("xlabel", "'objectives'");
        jPlot1.getJavaPlot().set("ylabel", "'values'");
        jPlot1.getJavaPlot().set("xtics", "1");

        //It shows a vertical box for each objective                
        jPlot1.getJavaPlot().set("xrange", "[0.5 to " + (numberOfObjectives + 0.5) + "]");
        jPlot1.getJavaPlot().set("yrange", "[" + (minimumValue - 0.1) + " to " + (maximumValue + 0.1) + "]");
        jPlot1.getJavaPlot().set("key", "outside bottom");

        for (int i = 1; i <= numberOfObjectives; i++) {
            jPlot1.getJavaPlot().set("object " + i, "rect from " + (i - 0.1) + "," + idealPoint[i - 1] + " to " + (i + 0.1) + "," + nadirPoint[i - 1]);
        }

        //It shows the solutions
        int solutionIndex = 0;
        for (int i = 0; i < data.length; i = i + numberOfObjectives) {
            double[][] localData = new double[numberOfObjectives][2];
            for (int j = 0; j < numberOfObjectives; j++) {
                localData[j][0] = data[i + j][0];
                localData[j][1] = data[i + j][1];
            }

            //It paints each objective in each vertical box                       
            PlotStyle myPlotStyle = new PlotStyle();
            myPlotStyle.setStyle(Style.LINESPOINTS);
            myPlotStyle.set("linecolor", String.valueOf(solutionIndex));
            myPlotStyle.setPointType(6);
            myPlotStyle.setPointSize(2);
            myPlotStyle.setLineWidth(2);

            DataSetPlot dataSetPlot = new DataSetPlot(localData);
            dataSetPlot.setPlotStyle(myPlotStyle);
            dataSetPlot.setTitle("s" + String.valueOf(solutionIndex + 1));

            jPlot1.getJavaPlot().addPlot(dataSetPlot);

            solutionIndex++;
        }

        //It shows the reference point
        if (plotReferencePoint) {
            PlotStyle myPlotStyle2 = new PlotStyle();
            myPlotStyle2.setStyle(Style.LINESPOINTS);
            myPlotStyle2.set("linecolor", "rgb 'black'");
            myPlotStyle2.setPointType(2);
            myPlotStyle2.setPointSize(2);
            myPlotStyle2.setLineWidth(2);
            myPlotStyle2.setLineType(5);
            DataSetPlot dataSetPlot = new DataSetPlot(ValuePath.getReferencePointForPlot(getReferencePoint()));
            dataSetPlot.setPlotStyle(myPlotStyle2);
            dataSetPlot.setTitle("");
            jPlot1.getJavaPlot().addPlot(dataSetPlot);
        }

        //System.out.println(jplot.getJavaPlot().getCommands());
    }

    private void plotValuePath() {
        int numberOfObjectives = Integer.valueOf(this.jSpinnerObjectivesNumber.getValue().toString());
        double minimumValue = Double.MAX_VALUE, maximumValue = Double.MIN_VALUE;

        jPlot1.getJavaPlot().getPlots().clear();

        javaPlot1 = new JavaPlot(null, javaPlot1.getGNUPlotPath(), javaPlot1.getTerminal(), false);
        javaPlot1.set("term", "png size " + (jPlot1.getWidth() - 1) + ", " + (jPlot1.getHeight() - 1));
        javaPlot1.set("grid", "");
        javaPlot1.setPersist(false);
        jPlot1.setJavaPlot(javaPlot1);
        jPanelPlot.setBorder(new TitledBorder("Plot for " + jComboBoxProblemName.getSelectedItem() + " problem"));

        for (int i = 0; i < numberOfObjectives; i++) {
            if (idealPoint[i] < minimumValue) {
                minimumValue = idealPoint[i];
            }

            if (nadirPoint[i] > maximumValue) {
                maximumValue = nadirPoint[i];
            }
        }

        //Plot information
        jPlot1.getJavaPlot().set("xlabel", "'objectives'");
        jPlot1.getJavaPlot().set("ylabel", "'values'");
        jPlot1.getJavaPlot().set("xtics", "1");

        //It shows a vertical box for each objective                
        jPlot1.getJavaPlot().set("xrange", "[0.5 to " + (numberOfObjectives + 0.5) + "]");
        jPlot1.getJavaPlot().set("yrange", "[" + (minimumValue - 0.1) + " to " + (maximumValue + 0.1) + "]");
        for (int i = 1; i <= numberOfObjectives; i++) {
            jPlot1.getJavaPlot().set("object " + i, "rect from " + (i - 0.1) + "," + idealPoint[i - 1] + " to " + (i + 0.1) + "," + nadirPoint[i - 1]);
        }

        //It shows the reference point
        PlotStyle myPlotStyle2 = new PlotStyle();
        myPlotStyle2.setStyle(Style.LINESPOINTS);
        myPlotStyle2.set("linecolor", "rgb 'black'");
        myPlotStyle2.setPointType(2);
        myPlotStyle2.setPointSize(2);
        myPlotStyle2.setLineWidth(2);
        myPlotStyle2.setLineType(10);

        DataSetPlot dataSetPlot = new DataSetPlot(ValuePath.getReferencePointForPlot(getReferencePoint()));
        dataSetPlot.setPlotStyle(myPlotStyle2);
        dataSetPlot.setTitle("Reference point");
        jPlot1.getJavaPlot().addPlot(dataSetPlot);
    }

    private void plotReferencePoint(JPlot jplot) {
        double[][] rp = new double[1][(int) jSpinnerObjectivesNumber.getValue()];
        rp[0] = getReferencePoint().toDouble();

        PlotStyle myPlotStyle = new PlotStyle();
        myPlotStyle.setStyle(Style.POINTS);
        myPlotStyle.setPointType(2);
        myPlotStyle.setPointSize(2);
        myPlotStyle.setLineWidth(2);
        myPlotStyle.set("linecolor", "3");

        DataSetPlot dataSetPlot = new DataSetPlot(rp);
        dataSetPlot.setTitle("Reference point");
        dataSetPlot.setPlotStyle(myPlotStyle);

        jplot.getJavaPlot().addPlot(dataSetPlot);
    }

    private void generateWeightsVectorsWithKmeans(String weightsDirectory, String weightsFileName) {
        if (!(new File(weightsDirectory + File.separator + weightsFileName).exists())) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(weightsDirectory + File.separator + weightsFileName, false);
                KMeans kMeans = new KMeans(weightsDirectory + File.separator + FILE_OF_WEIGHTS_FOR_KMEANS[((Integer) (jSpinnerObjectivesNumber.getValue())).intValue() - 3] + "." + Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) + "D", ((Integer) jSpinnerSolutionsNumber.getValue()).intValue());
                java.util.List<Point> centroids = kMeans.getCentroids();

                Collections.sort(centroids);

                for (int i = 0; i < ((Integer) jSpinnerSolutionsNumber.getValue()).intValue(); i++) {
                    fw.write(centroids.get(i).toStringForFileFormat() + "\n");
                }
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private boolean setExperimentForNewProblem() {
        boolean isPossibleToExecute = true;

        try {
            isPossibleToExecute = initExperiment((String) jComboBoxProblemName.getSelectedItem(), PARETO_FRONTS_FOLDER + jComboBoxProblemName.getSelectedItem() + "." + jSpinnerObjectivesNumber.getValue().toString() + "D.pf", OUTPUT_FOLDER);
        } catch (JMException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (isPossibleToExecute) {
            loadReferencePointUI(Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()), ((Integer) jSpinnerSolutionsNumber.getValue()).intValue(), idealPoint, nadirPoint);
            algorithm.setInputParameter("referencePoint", getReferencePoint());

            jPlot1.getJavaPlot().getPlots().clear();

            if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) < 3) {
                plotParetoFront(jPlot1);
                plotReferencePoint(jPlot1);
            } else {
                plotValuePath();
            }

            repaintPlot(jPlot1);

            jButtonNextIteration.setEnabled(false);
        } else {
            showDialog(this, "Problem's configuration error:", "The " + jSpinnerObjectivesNumber.getValue().toString() + " objectives " + jComboBoxProblemName.getSelectedItem() + " problem does not exist.");
        }

        return isPossibleToExecute;
    }

    private void plotParetoFront(JPlot jplot) {
        PlotStyle myPlotStyle = new PlotStyle();
        myPlotStyle.setStyle(Style.POINTS);
        myPlotStyle.setPointType(6);
        myPlotStyle.setPointSize(1);
        myPlotStyle.set("linecolor", "0");

        try {
            FileDataSet fds = new FileDataSet(new File(PARETO_FRONTS_FOLDER + jComboBoxProblemName.getSelectedItem() + "." + jSpinnerObjectivesNumber.getValue().toString() + "D.pf"));
            DataSetPlot testDataSetPlot = new DataSetPlot(fds);
            testDataSetPlot.setTitle("Pareto front");
            testDataSetPlot.setPlotStyle(myPlotStyle);

            if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) == 2) {
                javaPlot1 = new JavaPlot(null, javaPlot1.getGNUPlotPath(), javaPlot1.getTerminal(), false);
                javaPlot1.set("term", "png size " + (jplot.getWidth() - 1) + ", " + (jplot.getHeight() - 1));
                javaPlot1.set("grid", "");
                javaPlot1.setPersist(false);
                jplot.setJavaPlot(javaPlot1);
                jplot.getJavaPlot().addPlot(testDataSetPlot);

                jplot.getJavaPlot().set("xlabel", "'f1'");
                jplot.getJavaPlot().set("ylabel", "'f2'");
                jPanelPlot.setBorder(new TitledBorder("Plot for " + jComboBoxProblemName.getSelectedItem() + " problem"));
            } else if (Integer.valueOf(jSpinnerObjectivesNumber.getValue().toString()) == 3) {
                javaPlot1 = new JavaPlot(null, javaPlot1.getGNUPlotPath(), javaPlot1.getTerminal(), true);
                javaPlot1.set("term", "png size " + (jplot.getWidth() - 1) + ", " + (jplot.getHeight() - 1));
                javaPlot1.set("grid", "");
                javaPlot1.setPersist(false);
                jplot.setJavaPlot(javaPlot1);
                jplot.getJavaPlot().addPlot(testDataSetPlot);

                jplot.getJavaPlot().set("xlabel", "'f1'");
                jplot.getJavaPlot().set("ylabel", "'f2'");
                jplot.getJavaPlot().set("zlabel", "'f3'");
                jPanelPlot.setBorder(new TitledBorder("Plot for " + jComboBoxProblemName.getSelectedItem() + " problem"));
            } else {
                jPanelPlot.setBorder(new TitledBorder("Plot not available"));
            }
        } catch (IOException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(iWASFGAStudy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void repaintPlot(JPlot jplot) {
        jplot.plot();
        jplot.repaint();
    }

    private Integer scaleFloatToIntValue(Float floatNumber, Integer minIntValue, Integer maxIntValue, Float minFloatValue, Float maxFloatValue) {
        Integer result;
        result = new Integer(minIntValue + (int) (((maxIntValue - minIntValue) * ((floatNumber - minFloatValue) * 100.0) / (maxFloatValue - minFloatValue)) / 100));

        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(iWASFGAStudy.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(iWASFGAStudy.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(iWASFGAStudy.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(iWASFGAStudy.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new iWASFGAStudy().setVisible(true);
            }
        });
    }

    //The following codes set where the text get redirected. In this case, jTextArea1    
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jTextAreaLog.append(text);
            }
        });
    }

    //Followings are The Methods that do the Redirect, you can simply Ignore them. 
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private MyComponents.FloatJSlider floatJSlider2;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonNextIteration;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JComboBox jComboBoxProblemName;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelActions;
    private javax.swing.JPanel jPanelAlgorithmConfiguration;
    private javax.swing.JPanel jPanelPlot;
    private javax.swing.JPanel jPanelProblemConfiguration;
    private javax.swing.JPanel jPanelReferencePoint;
    private com.panayotis.gnuplot.swing.JPlot jPlot1;
    private javax.swing.JScrollPane jScrollPaneLog;
    private javax.swing.JScrollPane jScrollPaneSolutions;
    private javax.swing.JSpinner jSpinnerGenerationsNumber;
    private javax.swing.JSpinner jSpinnerObjectivesNumber;
    private javax.swing.JSpinner jSpinnerPopulationSize;
    private javax.swing.JSpinner jSpinnerSolutionsNumber;
    private javax.swing.JTable jTableSolutions;
    private javax.swing.JTextArea jTextAreaLog;
    // End of variables declaration//GEN-END:variables
}
