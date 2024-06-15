import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    int userId;
    String username;
    Set<User> friends;
    Set<User> friendRequests;
    List<Message> messages;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.friends = new HashSet<>();
        this.friendRequests = new HashSet<>();
        this.messages = new ArrayList<>();
    }

    public void receiveMessage(Message message) {
        messages.add(message);
    }
}

class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    String sender;
    String content;
    Date timestamp;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = new Date();
    }
}

public class SocialServer {
    private Map<Integer, User> users;
    private AtomicInteger userIdCounter;

    public SocialServer() {
        users = new HashMap<>();
        userIdCounter = new AtomicInteger(1);
    }

    public int addUser(String username) {
        int userId = userIdCounter.getAndIncrement();
        User user = new User(userId, username);
        users.put(userId, user);
        return userId;
    }

    public void sendFriendRequest(int senderId, int receiverId) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender != null && receiver != null) {
            receiver.friendRequests.add(sender);
            System.out.println(sender.username + " sent a friend request to " + receiver.username);
        }
    }

    public void acceptFriendRequest(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user != null && friend != null && user.friendRequests.contains(friend)) {
            user.friends.add(friend);
            friend.friends.add(user);
            user.friendRequests.remove(friend);
            System.out.println(user.username + " accepted the friend request from " + friend.username);
        }
    }

    public void sendMessage(int senderId, int receiverId, String content) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender != null && receiver != null && sender.friends.contains(receiver)) {
            Message message = new Message(sender.username, content);
            receiver.receiveMessage(message);
            System.out.println(sender.username + " sent a message to " + receiver.username + ": " + content);
        } else {
            System.out.println("Message not sent. Make sure you are friends with the receiver.");
        }
    }

    public List<Message> getMessages(int userId) {
        User user = users.get(userId);
        if (user != null) {
            return user.messages;
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        SocialServer socialNetwork = new SocialServer();

        socialNetwork.addUser("Alice");
        socialNetwork.addUser("Bob");
        socialNetwork.addUser("Charlie");

        try (ServerSocket serverSocket = new ServerSocket(5520)) {
            System.out.println("Social Network Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(socialNetwork, clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private SocialServer socialNetwork;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(SocialServer socialNetwork, Socket socket) {
        this.socialNetwork = socialNetwork;
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String request;
            while ((request = in.readLine()) != null) {
                if (request.equals("registerUser")) {
                    String username = in.readLine();
                    int userId = socialNetwork.addUser(username);
                    out.println(userId);
                } else if (request.equals("sendFriendRequest")) {
                    int senderId = Integer.parseInt(in.readLine());
                    int receiverId = Integer.parseInt(in.readLine());
                    socialNetwork.sendFriendRequest(senderId, receiverId);
                } else if (request.equals("acceptFriendRequest")) {
                    int userId = Integer.parseInt(in.readLine());
                    int friendId = Integer.parseInt(in.readLine());
                    socialNetwork.acceptFriendRequest(userId, friendId);
                } else if (request.equals("sendMessage")) {
                    int senderId = Integer.parseInt(in.readLine());
                    int receiverId = Integer.parseInt(in.readLine());
                    String content = in.readLine();
                    socialNetwork.sendMessage(senderId, receiverId, content);
                } else if (request.equals("getMessages")) {
                    int userId = Integer.parseInt(in.readLine());
                    List<Message> messages = socialNetwork.getMessages(userId);
                    out.println(messages.size()); // Send the number of messages
                    for (Message message : messages) {
                        out.println(message.sender);
                        out.println(message.content);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
