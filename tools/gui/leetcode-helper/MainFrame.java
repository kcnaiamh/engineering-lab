import java.awt.Cursor;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Vector;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class MainFrame extends JFrame {

    private Container container;
    private JLabel label1, label2;
    private JScrollPane scrollPane1, scrollPane2;
    private JButton clearButton, runButton, stringButton;
    private JTextArea textArea1, textArea2;
    private Vector<Integer> vecSize = new Vector<Integer>();

    private MainFrame() {
        initContainer();
        initLabel();
        initTextArea();
        initButton();
    }

    private void initContainer() {
        container = this.getContentPane();
        container.setLayout(null);
    }

    private void initLabel() {
        label1 = new JLabel("Enter Testcase");
        label1.setBounds(20, 20, 150, 20);
        container.add(label1);

        label2 = new JLabel("Output");
        label2.setBounds(20, 250, 100, 20);
        container.add(label2);
    }

    private void initTextArea() {
        createTextAreas();
    }

    private void createTextAreas() {
        textArea1 = createTextArea();
        scrollPane1 = createScrollPane(textArea1, 20, 50, 560, 180);

        textArea2 = createTextArea();
        scrollPane2 = createScrollPane(textArea2, 20, 280, 560, 180);
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private JScrollPane createScrollPane(JComponent component, int x, int y, int width, int height) {
        JScrollPane scrollPane = new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(x, y, width, height);
        container.add(scrollPane);
        return scrollPane;
    }

    private void initButton() {
        createButtons();
        addActionListenersToButtons();
    }

    private void createButtons() {
        runButton = createButton("Run", 300, 480);
        stringButton = createButton("String", 400, 480);
        clearButton = createButton("Clear", 500, 480);
    }

    private JButton createButton(String label, int x, int y) {
        JButton button = new JButton(label);
        button.setBounds(x, y, 80, 20);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        container.add(button);
        return button;
    }

    private void addActionListenersToButtons() {
        runButton.addActionListener(new LRunButton());
        clearButton.addActionListener(new LClearButton());
        stringButton.addActionListener(new LConvertStringButton());
    }

    String getFromatedText(String s) {
        int cnt = 1;

        if (s.substring(0, 2).equals("[[")) {
            s = s.substring(2, s.length() - 1);
            String res = "";

            int pos = s.indexOf("]"), cur = 0;

            while (pos != -1) {
                res = res.concat(s.substring(cur, pos).replaceAll("[, ]+", " ").concat("\n"));
                cur = s.indexOf("[", pos) + 1;
                if (cur == 0)
                    break;
                pos = s.indexOf("]", cur);
                cnt++;
            }
            vecSize.add(cnt);
            return res;
        }

        else if (s.substring(0, 1).equals("[")) {
            String tmp = s.substring(1, s.length() - 1).replaceAll("[, ]+", " ");

            for (int i = 0; i < tmp.length(); i++)
                if (tmp.charAt(i) == ' ')
                    cnt++;

            vecSize.add(cnt);
            return tmp;
        }

        System.out.print("OTHER!");
        return "";

    }

    class LRunButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String[] testcases = textArea1.getText().split("\n\n");

            String res = "";
            for (String testcase : testcases) {
                String[] lines = testcase.split("\n");
                String tmp = "";
                for (String line : lines)
                    tmp += getFromatedText(line) + "\n";
                res += addSizes() + "\n" + tmp + "\n";
            }
            textArea2.setText(res);
            textArea2.requestFocus();
            textArea2.select(0, textArea2.getText().length());
        }

        public String addSizes() {
            HashSet<Integer> set = new HashSet<>();
            String res = "";

            for (Integer x : vecSize) {
                if (!set.contains(x)) {
                    res += Integer.toString(x) + " ";
                    set.add(x);
                }
            }

            vecSize.clear();
            return res;
        }
    }

    class LClearButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea2.setText("");
        }
    }

    class LConvertStringButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String[] text = textArea2.getText().split("\n\n");

            String res = "";
            for (String tmp : text) {
                res += tmp.substring(0, tmp.indexOf('\n')) + "\n";
                tmp = tmp.substring(tmp.indexOf('\n') + 1, tmp.length());
                res += convertString(tmp) + "\n";
            }
            textArea2.setText(res.strip());
        }

        String convertString(String str) {
            String res = "";
            String[] lines = str.split("\n");

            for (String line : lines) {
                if (line.length() < 3)
                    continue;

                line = line.substring(1, line.length() - 1);
                for (int i = 0; i < line.length(); i++)
                    if (line.charAt(i) == '"') {
                        res += ' ';
                        i += 2;
                    } else {
                        res += line.charAt(i);
                    }
                res += "\n";
            }
            return res;
        }
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setBounds(200, 150, 600, 550);
        mainFrame.setTitle("Leetcode Helper");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }
}
