package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIWindow {

    private final ActorRef<UIHandler.UICommand> uiHandler;
    private final JTextArea logAreo;

    private final JTextField inputField;

    public UIWindow(ActorRef<UIHandler.UICommand> uiHandler) {
        this.uiHandler = uiHandler;

        JFrame frame = new JFrame("Home Automation UI");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Layout
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        logAreo = new JTextArea();
        logAreo.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logAreo);

        inputField = new JTextField();
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = inputField.getText();
                if (!command.isBlank()) {
                    uiHandler.tell(new UIHandler.UserInput(command));
                    appendLog(">>> " + command);
                    inputField.setText("");
                }
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logAreo.append(message + "\n");
            logAreo.setCaretPosition(logAreo.getDocument().getLength());
        });
    }
}
