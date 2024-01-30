import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProcessorGUI {
    private JFrame frame;
    private JTextArea inputTextArea;
    private JScrollPane pipelineScrollPane;
    private JTextArea pipelineTextArea;
    private JScrollPane registersScrollPane;
    private JTextArea registersTextArea;
    private JScrollPane memoryScrollPane;
    private JTextArea memoryTextArea;
    private JButton runButton;
    private Processor processor;

    public ProcessorGUI() {
        processor = new Processor();

        frame = new JFrame("Double Big Harvard Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);

        JLabel inputLabel = new JLabel("Instructions");
        inputLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(inputLabel, BorderLayout.NORTH);

        inputTextArea = new JTextArea();
        inputTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        inputTextArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        int inputAreaHeight = (int) (frame.getHeight() * 0.35);
        inputScrollPane.setPreferredSize(new Dimension(frame.getWidth(), inputAreaHeight));
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new GridLayout(3, 1));
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outputPanel.setBackground(Color.BLACK);

        JPanel pipelinePanel = new JPanel(new BorderLayout());
        JLabel pipelineLabel = new JLabel("Pipeline");
        pipelineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pipelineLabel.setBackground(Color.LIGHT_GRAY);
        pipelineLabel.setOpaque(true);
        pipelinePanel.add(pipelineLabel, BorderLayout.NORTH);
        pipelineTextArea = new JTextArea();
        pipelineTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        pipelineScrollPane = new JScrollPane(pipelineTextArea);
        pipelinePanel.add(pipelineScrollPane, BorderLayout.CENTER);
        outputPanel.add(pipelinePanel);

        JPanel registersPanel = new JPanel(new BorderLayout());
        JLabel registersLabel = new JLabel("Registers");
        registersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        registersLabel.setBackground(Color.LIGHT_GRAY);
        registersLabel.setOpaque(true);
        registersPanel.add(registersLabel, BorderLayout.NORTH);
        registersTextArea = new JTextArea();
        registersTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        registersScrollPane = new JScrollPane(registersTextArea);
        registersPanel.add(registersScrollPane, BorderLayout.CENTER);
        outputPanel.add(registersPanel);

        JPanel memoryPanel = new JPanel(new BorderLayout());
        JLabel memoryLabel = new JLabel("Main Memory");
        memoryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        memoryLabel.setBackground(Color.LIGHT_GRAY);
        memoryLabel.setOpaque(true);
        memoryPanel.add(memoryLabel, BorderLayout.NORTH);
        memoryTextArea = new JTextArea();
        memoryTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        memoryScrollPane = new JScrollPane(memoryTextArea);
        memoryPanel.add(memoryScrollPane, BorderLayout.CENTER);
        outputPanel.add(memoryPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String program = inputTextArea.getText();

                File tempFile;
                try {
                    tempFile = File.createTempFile("program", ".txt");
                    tempFile.deleteOnExit();

                    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                    writer.write(program);
                    writer.close();

                    processor.runProgram(tempFile.getAbsolutePath());

                    pipelineTextArea.setText(processor.getPipelineGUI());
                    registersTextArea.setText(processor.printRegisters());
                    memoryTextArea.setText(processor.printMainMemory());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonPanel.add(runButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(outputPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        
        new ProcessorGUI();
       
    }
}
