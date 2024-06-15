import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SocialClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5520);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("****Welcome to the Social Network Client!****");
            int userId = -1;

            while (true) {
                System.out.println("Menus:");
                System.out.println("1. Register as a new user");
                System.out.println("2. Send Friend Request");
                System.out.println("3. Accept Friend Request");
                System.out.println("4. Send Message");
                System.out.println("5. View Messages");
                System.out.println("6. Exit");
                System.out.println();

                System.out.print("Enter your choice: ");
                Scanner scanner = new Scanner(System.in);
                if (scanner.hasNextInt()) {
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            if (userId != -1) {
                                System.out.println("You are already registered.");
                            } else {
                                System.out.print("Enter your username: ");
                                String username = scanner.nextLine();
                                out.println("registerUser");
                                out.println(username);

                                String response = in.readLine();
                                userId = Integer.parseInt(response);
                                System.out.println("Registered successfully. Your user ID is " + userId);
                                System.out.println();
                            }
                            break;

                        case 2:
                            if (userId == -1) {
                                System.out.println("Please register first.");
                            } else {
                                System.out.print("Enter the user ID of the friend you want to send a request to: ");
                                if (scanner.hasNextInt()) {
                                    int friendId = scanner.nextInt();
                                    scanner.nextLine();
                                    out.println("sendFriendRequest");
                                    out.println(userId);
                                    out.println(friendId);
                                    out.flush();
                                    System.out.println("Request Sent");
                                    System.out.println();
                                } else {
                                    System.out.println("Invalid input. Please enter a valid integer.");
                                    scanner.nextLine(); // Consume the invalid input
                                }
                            }
                            break;

                        case 3:
                            if (userId == -1) {
                                System.out.println("Please register first.");
                            } else {
                                System.out.print("Enter the user ID of the friend you want to accept the request from: ");
                                int friendId = scanner.nextInt();
                                out.println("acceptFriendRequest");
                                out.println(userId);
                                out.println(friendId);
                                out.flush();
                                System.out.println("Request Accepted");
                                System.out.println();
                            }
                            break;

                        case 4:
                            if (userId == -1) {
                                System.out.println("Please register first.");
                            } else {
                                System.out.print("Enter the user ID of the friend you want to send a message to: ");
                                int friendId = scanner.nextInt();
                                out.println("sendMessage");
                                out.println(userId);
                                out.println(friendId);
                                out.flush();

                                System.out.print("Enter your message: ");
                                String message = scanner.next();
                                out.println(message);
                                out.flush();
                                System.out.println("Message Sent");
                                System.out.println();
                            }
                            break;

                        case 5:
                            if (userId == -1) {
                                System.out.println("Please register first.");
                            } else {
                                out.println("getMessages");
                                out.println(userId);
                                out.flush();

                                int messageCount = Integer.parseInt(in.readLine());
                                System.out.println("Your messages:");
                                for (int i = 0; i < messageCount; i++) {
                                    String sender = in.readLine();
                                    String content = in.readLine();
                                    System.out.println(sender + ": " + content);
                                    System.out.println();
                                }
                            }
                            break;

                        case 6:
                            System.out.println("Goodbye!");
                            return;
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid integer.");
                    scanner.nextLine(); // Consume the invalid input
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
