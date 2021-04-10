package serverside.view.elements;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.GridLayout;

public class ControlPanel extends JPanel {
    JButton startButton;
    JButton stopButton;

    public ControlPanel() {
        setLayout(new GridLayout(1, 2));

        startButton = new JButton("Запустить сервер");
        stopButton = new JButton("Остановить сервер");

        add(startButton);
        add(stopButton);
    }

    public JButton getStartButton() { return startButton; }

    public JButton getStopButton() { return stopButton; }
}
