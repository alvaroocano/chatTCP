import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class FirstClient extends JFrame {
    private JTextField messageField;
    private JTextArea chatArea;
    private JList<String> userList;
    private String userNickname;
    private PrintWriter writer;
    private Scanner scanner;

    public FirstClient() {
        setTitle("First Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        initComponents();
        connectToServer();
    }

    public void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        userList = new JList<>();
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(100, 0));
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            messageField.setText("");
        }
    }

    private void connectToServer() {
        userNickname = JOptionPane.showInputDialog("Ingrese su nickname:");

        String serverAddress = "localhost";

        try {
            Socket socket = new Socket(serverAddress, 12345);
            scanner = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(userNickname);

            new Thread(() -> {
                try {
                    while (scanner.hasNextLine()) {
                        String serverMessage = scanner.nextLine();
                        handleServerMessage(serverMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("/userlist ")) {
            String[] userArray = message.substring("/userlist ".length()).split(" ");
            SwingUtilities.invokeLater(() -> {
                userList.setListData(userArray);
            });
        } else if (message.equals("/nicknameinuse")) {
            userNickname = JOptionPane.showInputDialog("El nickname ya está en uso. Por favor, elige otro:");
            writer.println(userNickname);
        } else {
            chatArea.append(message + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FirstClient().setVisible(true);
            }
        });
    }
}
