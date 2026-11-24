import java.io.*;
import java.util.*;

/**
 * PS No. 16 - Browser History Manager
 *
 * Data structures (all written by hand, no ready-made Stack/Queue classes):
 *  - Doubly Linked List : full history (can be read oldest->newest and newest->oldest)
 *  - Stack (x2)         : back stack and forward stack
 *  - Queue              : the 5 most recently visited pages
 *  - HashMap            : visit count of every URL
 *  - File               : history.txt (loaded at start, saved on change and on exit)
 */
public class bowserHistoryManager {

    // ======================= DATA STRUCTURES =======================

    /** Node of the doubly linked list. */
    static class DNode {
        String url;
        DNode prev, next;

        DNode(String url) {
            this.url = url;
        }
    }

    /** Doubly linked list that stores the complete history. */
    static class DoublyLinkedList {
        private DNode head, tail;
        private int size = 0;

        void addLast(String url) {
            DNode node = new DNode(url);
            if (head == null) {
                head = tail = node;
            } else {
                tail.next = node;
                node.prev = tail;
                tail = node;
            }
            size++;
        }

        void clear() {
            head = tail = null;
            size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        int size() {
            return size;
        }

        DNode getHead() {
            return head;
        }

        DNode getTail() {
            return tail;
        }
    }

    /** Stack (LIFO) built with linked nodes. */
    static class MyStack {
        private static class Node {
            String data;
            Node below;

            Node(String data, Node below) {
                this.data = data;
                this.below = below;
            }
        }

        private Node top;

        void push(String data) {
            top = new Node(data, top);
        }

        String pop() {
            if (top == null) {
                return null;
            }
            String value = top.data;
            top = top.below;
            return value;
        }

        boolean isEmpty() {
            return top == null;
        }

        void clear() {
            top = null;
        }
    }

    /** Queue (FIFO) built with linked nodes. */
    static class MyQueue {
        private static class Node {
            String data;
            Node next;

            Node(String data) {
                this.data = data;
            }
        }

        private Node front, rear;
        private int size = 0;

        void enqueue(String data) {
            Node node = new Node(data);
            if (rear == null) {
                front = rear = node;
            } else {
                rear.next = node;
                rear = node;
            }
            size++;
        }

        String dequeue() {
            if (front == null) {
                return null;
            }
            String value = front.data;
            front = front.next;
            if (front == null) {
                rear = null;
            }
            size--;
            return value;
        }

        int size() {
            return size;
        }

        void clear() {
            front = rear = null;
            size = 0;
        }

        List<String> toList() {
            List<String> list = new ArrayList<>();
            Node n = front;
            while (n != null) {
                list.add(n.data);
                n = n.next;
            }
            return list;
        }
    }

    // ======================= BACKEND =======================

    /** All browser logic (no menu printing here). */
    static class BrowserHistoryManager {
        private static final String FILE_NAME = "history.txt";
        private static final int RECENT_LIMIT = 5;

        private final DoublyLinkedList history = new DoublyLinkedList();
        private final MyStack backStack = new MyStack();
        private final MyStack forwardStack = new MyStack();
        private final MyQueue recentPages = new MyQueue();
        private final Map<String, Integer> visitCount = new HashMap<>();
        private String currentPage = null;

        BrowserHistoryManager() {
            loadFromFile();
        }

        /** A URL is valid only if it is not empty, has no spaces and contains a dot. */
        boolean isValidUrl(String url) {
            return url != null && !url.trim().isEmpty()
                    && !url.trim().contains(" ") && url.trim().contains(".");
        }

        /** Records a page in history, visit count and recent queue. */
        private void record(String url) {
            history.addLast(url);
            visitCount.put(url, visitCount.getOrDefault(url, 0) + 1);
            recentPages.enqueue(url);
            if (recentPages.size() > RECENT_LIMIT) {
                recentPages.dequeue();
            }
        }

        /** Visit a new page. Returns false if the URL is invalid. */
        boolean visit(String url) {
            if (!isValidUrl(url)) {
                return false;
            }
            url = url.trim();
            if (currentPage != null) {
                backStack.push(currentPage);
            }
            forwardStack.clear();              // visiting a new page clears forward history
            currentPage = url;
            record(url);
            saveToFile();
            return true;
        }

        /** Go back one page. Returns false if there is no previous page. */
        boolean back() {
            if (backStack.isEmpty()) {
                return false;
            }
            forwardStack.push(currentPage);
            currentPage = backStack.pop();
            return true;
        }

        /** Go forward one page. Returns false if there is no forward page. */
        boolean forward() {
            if (forwardStack.isEmpty()) {
                return false;
            }
            backStack.push(currentPage);
            currentPage = forwardStack.pop();
            return true;
        }

        String getCurrentPage() {
            return currentPage;
        }

        Map<String, Integer> getVisitCounts() {
            return visitCount;
        }

        DoublyLinkedList getHistory() {
            return history;
        }

        List<String> getRecentPages() {
            return recentPages.toList();
        }

        void clearAll() {
            history.clear();
            backStack.clear();
            forwardStack.clear();
            recentPages.clear();
            visitCount.clear();
            currentPage = null;
            saveToFile();
        }

        // ---------- file handling ----------

        void saveToFile() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
                DNode p = history.getHead();
                while (p != null) {
                    pw.println(p.url);
                    p = p.next;
                }
            } catch (IOException e) {
                System.out.println("Error: could not save history -> " + e.getMessage());
            }
        }

        private void loadFromFile() {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (isValidUrl(line)) {
                        if (currentPage != null) {
                            backStack.push(currentPage);   // earlier pages go to back stack
                        }
                        currentPage = line;                // last page in file = current page
                        record(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error: could not load history -> " + e.getMessage());
            }
        }
    }

    // ======================= FRONTEND (console) =======================

    private static void printMenu() {
        System.out.println();
        System.out.println("=========== BROWSER HISTORY MANAGER ===========");
        System.out.println("1. Visit a page");
        System.out.println("2. Back");
        System.out.println("3. Forward");
        System.out.println("4. Show current page");
        System.out.println("5. Show history (oldest to newest)");
        System.out.println("6. Show visit counts");
        System.out.println("7. Clear history");
        System.out.println("8. Show recent pages (queue, last 5)");
        System.out.println("9. Show history (newest to oldest)");
        System.out.println("0. Exit");
        System.out.println("===============================================");
        System.out.print("Enter your choice: ");
    }

    private static String currentOrNone(BrowserHistoryManager manager) {
        return manager.getCurrentPage() == null ? "none" : manager.getCurrentPage();
    }

    private static void showHistory(BrowserHistoryManager manager, boolean newestFirst) {
        DoublyLinkedList list = manager.getHistory();
        if (list.isEmpty()) {
            System.out.println("History is empty.");
            return;
        }
        System.out.println("--- History (" + list.size() + " entries) ---");
        int i = 1;
        if (newestFirst) {
            DNode p = list.getTail();
            while (p != null) {
                System.out.println(i++ + ". " + p.url);
                p = p.prev;
            }
        } else {
            DNode p = list.getHead();
            while (p != null) {
                System.out.println(i++ + ". " + p.url);
                p = p.next;
            }
        }
    }

    private static void showVisitCounts(BrowserHistoryManager manager) {
        Map<String, Integer> counts = manager.getVisitCounts();
        if (counts.isEmpty()) {
            System.out.println("No pages visited yet.");
            return;
        }
        System.out.println("--- Visit counts ---");
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue() + " visit(s)");
        }
    }

    private static void showRecent(BrowserHistoryManager manager) {
        List<String> recent = manager.getRecentPages();
        if (recent.isEmpty()) {
            System.out.println("No recent pages.");
            return;
        }
        System.out.println("--- Recent pages (oldest to newest) ---");
        for (int i = 0; i < recent.size(); i++) {
            System.out.println((i + 1) + ". " + recent.get(i));
        }
    }

    public static void main(String[] args) {
        BrowserHistoryManager manager = new BrowserHistoryManager();
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            if (!sc.hasNextLine()) {
                manager.saveToFile();
                break;
            }
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter URL: ");
                    String url = sc.hasNextLine() ? sc.nextLine() : "";
                    if (manager.visit(url)) {
                        System.out.println("Visited: " + manager.getCurrentPage());
                        System.out.println("Current page: " + manager.getCurrentPage());
                    } else {
                        System.out.println("Invalid URL. Enter a URL like example.com.");
                    }
                    break;

                case "2":
                    if (manager.back()) {
                        System.out.println("Back to: " + manager.getCurrentPage());
                    } else {
                        System.out.println("No previous page.");
                        System.out.println("Current page: " + currentOrNone(manager));
                    }
                    break;

                case "3":
                    if (manager.forward()) {
                        System.out.println("Forward to: " + manager.getCurrentPage());
                    } else {
                        System.out.println("No forward page.");
                        System.out.println("Current page: " + currentOrNone(manager));
                    }
                    break;

                case "4":
                    if (manager.getCurrentPage() == null) {
                        System.out.println("No page is open.");
                    } else {
                        System.out.println("Current page: " + manager.getCurrentPage());
                    }
                    break;

                case "5":
                    showHistory(manager, false);
                    break;

                case "6":
                    showVisitCounts(manager);
                    break;

                case "7":
                    manager.clearAll();
                    System.out.println("History cleared.");
                    break;

                case "8":
                    showRecent(manager);
                    break;

                case "9":
                    showHistory(manager, true);
                    break;

                case "0":
                    manager.saveToFile();
                    running = false;
                    System.out.println("History saved to history.txt. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 0-9.");
            }
        }
        sc.close();
    }
}