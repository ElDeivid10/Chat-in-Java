import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField textField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(8, 40);
    private JList<String> userList = new JList<>();
    private DefaultListModel<String> userModel = new DefaultListModel<>();
    private String userName;

    public ChatClient(String serverAddress) throws Exception {
        this.userName = JOptionPane.showInputDialog(frame, "Enter your name:", "Name", JOptionPane.PLAIN_MESSAGE);
        if (this.userName == null || this.userName.isBlank()) {
            System.exit(0); // Exit if no name is provided
        }

        socket = new Socket(serverAddress, 59001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        textField.setEditable(false);
        messageArea.setEditable(false);
        userList.setModel(userModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(new JScrollPane(userList), BorderLayout.WEST);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });

        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null) {
                        openPrivateChat(selectedUser);
                    }
                }
            }
        });
    }

    private void run() throws IOException {
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(userName);
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("USERLIST")) {
                updateUsersList(line.substring(8));
            } else if (line.startsWith("PRIVATE")) {
                handlePrivateMessage(line.substring(8));
            }
        }
    }

    private void updateUsersList(String users) {
        userModel.clear();
        for (String user : users.split(",")) {
            userModel.addElement(user);
        }
    }

    private void handlePrivateMessage(String message) {
        String[] parts = message.split(":", 2);
        String fromUser = parts[0];
        String privateMessage = parts[1];

        JOptionPane.showMessageDialog(frame, privateMessage, "Private message from " + fromUser, JOptionPane.INFORMATION_MESSAGE);
    }

    private void openPrivateChat(String user) {
        String message = JOptionPane.showInputDialog(frame, "Enter message for " + user + ":");
        if (message != null && !message.isBlank()) {
            out.println("PRIVATE " + user + ":" + message);
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient("192.168.137.156");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}