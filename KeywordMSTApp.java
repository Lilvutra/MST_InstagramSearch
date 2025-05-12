

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class KeywordMSTApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KeywordMSTApp().createAndShowGUI());
    }

    JTextField keywordField;
    //JTextArea resultArea;
    GraphPanel graphPanel;
    JTable mstTable;
    DefaultTableModel mstTableModel;


    void createAndShowGUI() {
    // frame
    JFrame frame = new JFrame("Keyword MST");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 600); // Wider to fit all panels
    
    // components
    keywordField = new JTextField(20);
    JButton submitBtn = new JButton("Generate MST");
    submitBtn.addActionListener(this::handleGenerate);
    
    // Clear button
    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(e -> handleClear());

    
    JPanel inputPanel = new JPanel();
    inputPanel.add(new JLabel("Enter Keyword:"));
    inputPanel.add(keywordField);
    inputPanel.add(submitBtn);

    inputPanel.add(clearButton);
    
    //resultArea = new JTextArea(10, 50);
    //resultArea.setEditable(false);
    //JScrollPane scrollPane = new JScrollPane(resultArea);
    
    graphPanel = new GraphPanel();
    
    
    // Adding new posts
    // create panel 
    JPanel addPostPanel = new JPanel(new GridLayout(5,2,5,5));
    addPostPanel.setBorder(BorderFactory.createTitledBorder("Add new post"));
    
    // create text field
    JTextField idField = new JTextField(10);
    JTextField keywordField = new JTextField(10);
    JTextField relevantWordsField = new JTextField(20);
    JTextField captionField = new JTextField(30);

    // Add labels and fields to panel
    addPostPanel.add(new JLabel("Post ID:"));
    addPostPanel.add(idField);
    addPostPanel.add(new JLabel("Main keyword:"));
    addPostPanel.add(keywordField);
    addPostPanel.add(new JLabel("Relevant words:"));
    addPostPanel.add(relevantWordsField);
    addPostPanel.add(new JLabel("Caption"));
    addPostPanel.add(captionField);


    // default table model
    // Set up table for MST edges
    String[] columnNames = { "Node 1", "Node 2", "Weight", "Caption 1", "Caption 2" };
    mstTableModel = new DefaultTableModel(columnNames, 0); // 0 rows initially
    mstTable = new JTable(mstTableModel);
    JScrollPane tableScrollPane = new JScrollPane(mstTable);

    

    // Add button
    JButton addButton = new JButton("Add Post");
    addPostPanel.add(new JLabel(""));
    addPostPanel.add(addButton);
    // add action listener to button 
    addButton.addActionListener(e -> handleAddPost(idField, keywordField, relevantWordsField, captionField));
    
    // Add all panels to frame
    frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
    frame.getContentPane().add(graphPanel, BorderLayout.CENTER);
    //frame.getContentPane().add(scrollPane, BorderLayout.SOUTH);
    frame.getContentPane().add(addPostPanel, BorderLayout.WEST);

    // Replace the previous resultArea with table in SOUTH
    frame.getContentPane().add(tableScrollPane, BorderLayout.SOUTH);

    
    // Set visible after adding all components
    frame.setVisible(true);

    }class GraphPanel extends JPanel {
        List<KeywordMSTApp.Post> nodes;
        List<KeywordMSTApp.Edge> edges;
        Map<KeywordMSTApp.Post, Point> nodeLocations;
    
        void update(List<KeywordMSTApp.Post> nodes, List<KeywordMSTApp.Edge> edges) {
            this.nodes = nodes;
            this.edges = edges;
            this.nodeLocations = new HashMap<>();
            repaint();
        }

        public void clearGraph() {
            this.nodes = Collections.emptyList();
            this.edges = Collections.emptyList();
            repaint();
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (nodes == null || edges == null) return;
    
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
    
            int w = getWidth(), h = getHeight();
            int radius = 48;
            int centerX = w / 2, centerY = h / 2;
            double angleStep = 2 * Math.PI / nodes.size();
    
            for (int i = 0; i < nodes.size(); i++) {
                int x = (int) (centerX + 100 * Math.cos(i * angleStep));
                int y = (int) (centerY + 100 * Math.sin(i * angleStep));
                nodeLocations.put(nodes.get(i), new Point(x, y));
            }
    
            // Draw edges
            g2.setColor(Color.GRAY);
            for (KeywordMSTApp.Edge edge : edges) {
                Point p1 = nodeLocations.get(edge.u);
                Point p2 = nodeLocations.get(edge.v);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                String label = String.valueOf(edge.weight);
                int midX = (p1.x + p2.x) / 2, midY = (p1.y + p2.y) / 2;
                g2.drawString(label, midX, midY);
            }
    
            // Draw nodes
            g2.setColor(Color.CYAN);
            for (KeywordMSTApp.Post node : nodes) {
                Point p = nodeLocations.get(node);
                g2.fillOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
                g2.setColor(Color.BLACK);
                g2.drawString(node.id, p.x - 20, p.y + 5);
                g2.setColor(Color.CYAN);
            }
        }
    }
    
    // method to add new post
    void handleAddPost (JTextField idField, JTextField keywordField, JTextField relevantWorField, JTextField captionField){
        // get values from text field
        // get ID
        String id = idField.getText().trim(); // trim(): remove whitespace at beginning and end of string 
        // get keyword
        String keyword = keywordField.getText().trim();
        // get relevantWords
        String [] relevantWordsArray = relevantWorField.getText().split("\\s+");
        List<String> relevantWords = new ArrayList<>();
        for (int i = 0; i < relevantWordsArray.length; i++){
            relevantWords.add(relevantWordsArray[i]);
        }
        // get caption
        String caption = captionField.getText().trim();

        // create new Post object
        Post newPost = new Post(id, keyword, relevantWords, caption);

        // add this to data source
        DataSource.addPost(newPost);

        // after that, clear field
        idField.setText("");
        keywordField.setText("");
        relevantWorField.setText("");
        captionField.setText("");

        // notification
        JOptionPane.showMessageDialog(null, "Post added successfully");

        // regenerate MST
        if (!this.keywordField.getText().trim().isEmpty()){
            handleGenerate(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "generate"));
        }

    }


    void handleGenerate(ActionEvent e) {
        String keyword = keywordField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a keyword.");
            return;
        }
    
        List<Post> allPosts = DataSource.getSamplePosts();
        List<Post> relevantPosts = new ArrayList<>();
        
        // Filter posts based on the keyword
        for (Post post : allPosts) {
            if (post.mainKeyword.equalsIgnoreCase(keyword) || post.relevantWords.contains(keyword)) {
                relevantPosts.add(post);
                System.out.println("relevantPosts: " + relevantPosts);
                System.out.println("post: " + post);
            }
        }

       
    
        //if (relevantPosts.size() < 2) {
            //resultArea.setText("Not enough relevant posts to build a tree.");
            //graphPanel.update(null, null); // clear graph
            //return;
        //}
        
        // Min-Heap Primary Queue to store edges
        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>();
        for (int i = 0; i < relevantPosts.size(); i++) {
            for (int j = i + 1; j < relevantPosts.size(); j++) {
                Post u = relevantPosts.get(i);
                Post v = relevantPosts.get(j);
                int weight = computeWeight(u, v, keyword);
                edgeQueue.add(new Edge(u, v, weight));
            }
        }

        System.out.println("edgeQueue: " + edgeQueue);
    
        List<Edge> mst = new ArrayList<>();
        Map<Post, Post> parent = new HashMap<>();
        for (Post post : relevantPosts) parent.put(post, post);
        System.out.println("parent: " + parent);
    
        // Kruskal's algorithm
        while (!edgeQueue.isEmpty() && mst.size() < relevantPosts.size() - 1) {
            Edge edge = edgeQueue.poll();
            System.out.println("edge: " + edge);
            System.out.println("parent1: " + parent);
            Post rootU = find(parent, edge.u);
            
            System.out.println("rootU: " + rootU);
            System.out.println("edge.v: " + edge.v);

            Post rootV = find(parent, edge.v);
            System.out.println("rootV: " + rootV);
            System.out.println("edge.u: " + edge.u);

            if (!rootU.equals(rootV)) {
                mst.add(edge);
                parent.put(rootU, rootV);
            }
        }
    
        mst.sort(Comparator.comparingInt(e1 -> e1.weight));
    
        StringBuilder sb = new StringBuilder("MST Edges (in increasing weight):\n");
        for (Edge edge : mst) {
            sb.append(edge).append("\n");
        }
    
        //resultArea.setText(sb.toString());

        // Clear previous table data
        mstTableModel.setRowCount(0);

        // Fill table with MST edges
        for (Edge edge : mst) {
             Object[] row = {
                 edge.u.id,
                 edge.v.id,
                 edge.weight,
                 edge.u.caption,
                 edge.v.caption
             };
             mstTableModel.addRow(row);
         }
 
    
        // Send to graphPanel
        graphPanel.update(relevantPosts, mst);
    }

    private void handleClear() {
        keywordField.setText("");
        mstTableModel.setRowCount(0);
        graphPanel.clearGraph();
    }
    

    int computeWeight(Post u, Post v, String keyword) {
        int score = 0;

        if (u.mainKeyword.equalsIgnoreCase(keyword)) score += 10;
        if (v.mainKeyword.equalsIgnoreCase(keyword)) score += 10;

        //more points for earlier positions
        int position_u = u.relevantWords.indexOf(keyword);
        score += (u.relevantWords.size() - position_u); //higher score for earlier positions

        int position_v = v.relevantWords.indexOf(keyword);
        score += (v.relevantWords.size() - position_v); 

        //count the common related words in relevantwords between post u and v
        for (int i = 0; i < u.relevantWords.size(); i++){
            for (int j = 0; j < v.relevantWords.size(); j++){
                if (u.relevantWords.get(i).equalsIgnoreCase(v.relevantWords.get(j))){
                    score ++;
                    break;
                }
            }
        }
        
        // count the words in post's caption that appeared in another post's relevantWords
        String [] uWords = u.caption.split("\\s+");
        System.out.println("uWords: " + Arrays.toString(uWords));
        String [] vWords = v.caption.split("\\s+"); // split at whitespace
        System.out.println("vWords: " + Arrays.toString(vWords));

        List<String> uList = Arrays.asList(uWords);
        System.out.println("uList: " + uList);
        System.out.println("u.relevantWords: " + u.relevantWords);
        System.out.println("v.relevantWords: " + v.relevantWords);
        List<String> vList = Arrays.asList(vWords);

        // count the words in u's caption that appeared in v's relevantwords 
        for (int i = 0; i < uList.size(); i++){
            System.out.println("i " + i);

            for (int j = 0; j < v.relevantWords.size(); j++){
                System.out.println("j " + j);
                System.out.println("uList.get(i): " + uList.get(i));
                System.out.println("v.relevantWords.get(j): " + v.relevantWords.get(j));
                if (uList.get(i).equalsIgnoreCase(v.relevantWords.get(j))){
                    System.out.println("uList.get(i): " + uList.get(i));
                    System.out.println("v.relevantWords.get(j): " + v.relevantWords.get(j));
                    score ++;
                    break;
                }
            }
        }

        // count the words in v's caption that appeared in u's relevantwords
        for (int i = 0; i < vList.size(); i++){
            for (int j = 0; j < u.relevantWords.size(); j++){
                if (vList.get(i).equalsIgnoreCase(u.relevantWords.get(j))){
                    score ++;
                    break;
                }
            }
        }
        
        // Lower score = more important = lower weight
        return 100 - score;
    }

    // Find the root (or representative) of the set that a given node belongs to
    // If the node is not the root, we recursively find the root and compress the path
    // by making the node point directly to the root
    // This is called path compression
    // This is a common optimization in union-find algorithms
    // It helps to flatten the structure of the tree whenever find is called
    // This makes future queries faster
    // If both endpoints have the same root, they're already connected in the current MST

    
    Post find(Map<Post, Post> parent, Post node) {
        if (parent.get(node) != node) {
            System.out.println("find: " + node + " -> " + parent.get(node));
            parent.put(node, find(parent, parent.get(node)));
        }
        return parent.get(node);
    }

    static class Post {
        String id;
        String mainKeyword;
        List<String> relevantWords;
        String caption;

        public Post(String id, String mainKeyword, List<String> relevantWords, String caption) {
            this.id = id;
            this.mainKeyword = mainKeyword.toLowerCase();
            this.relevantWords = relevantWords;
            this.caption = caption;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Post p) return this.id.equals(p.id);
            return false;
        }

        @Override 
        public int hashCode() {
            return Objects.hash(id); //ha, we dont need to use this
        }

        @Override
        public String toString() {
            return id;
        }
    }

    static class Edge implements Comparable<Edge> {
        Post u, v;
        int weight;

        public Edge(Post u, Post v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }

        @Override
        public String toString() {
            return u.id + " -- " + v.id + " [Weight: " + weight + "]";
        }
    }

    // change to ArrayList to make it mutable to be able to add new Post.
    static class DataSource {
        public static final List<Post> SamplePosts = new ArrayList<>(List.of( // creates mutable list initialized by an immutable List.of
       
        new Post("post_1", "summer", List.of("exam", "swim", "vacation"), "POV: you’re 98% sunscreen and 2% human"),
        new Post("post_2", "coffee", List.of("caffeine", "latte", "morning"), "Me after one sip of coffee: I can fight God"),
        new Post("post_3", "cat", List.of("meow", "nap", "chaos"), "My cat at 3am: parkour. me: pls no."),
        new Post("post_4", "gym", List.of("workout", "gains", "protein"), "Went to the gym. Lifted a dumbbell. Called it a day."),
        new Post("post_5", "meme", List.of("lol", "relatable", "dank"), "If Monday had a face, I’d sue it"),
        new Post("post_6", "food", List.of("lol", "exam", "yum"), "Accidentally meal prepped dessert for 5 days straight"),
        new Post("post_7", "dog", List.of("woof", "walkies", "bork"), "My dog: *eats homework*. Me: he’s just expressing himself."),
        new Post("post_8", "wifi", List.of("offline", "panic", "404"), "No WiFi for 10 mins. Wrote a diary. Met my family. Wild. day"),
        new Post("post_9", "study", List.of("exam", "panic", "procrastinate"), "Studied for 6 hours. Remembered nothing. Respect the grind."),
        new Post("post_10", "fashion", List.of("ootd", "drip", "crocs"), "Today’s vibe: business casual with emotional damage"),
        new Post("post_11", "zombie", List.of("apocalypse", "survival", "lol"), "If zombies come, I’m tripping over my own shoelaces first"),
        new Post("post_12", "sleep", List.of("nap", "zzz", "comfy"), "Me: I’ll sleep early tonight. Also me at 2am: what if pigeons have secret lives?"),
        new Post("post_13", "coffee", List.of("espresso", "vibes", "chaotic"), "Espresso depresso but make it aesthetic"),
        new Post("post_14", "summer", List.of("heatwave", "sweat", "hydration"), "It’s so hot outside I saw a squirrel with a mini fan"),
        new Post("post_15", "internet", List.of("scroll", "doom", "addicted"), "Been scrolling for 3 hours. Found inner peace and a raccoon wearing sunglasses"),
        new Post("post_16", "discrete", List.of("math", "truth_table", "logic"), "Thay Linh said 'P → Q' and suddenly my whole life started making conditional sense 🤯📐"),
        new Post("post_17", "discrete", List.of("proof", "pigeonhole", "braincell"), "Me in Thay Linh's class trying to understand proofs with my one surviving brain cell 🕊️🔍"),
        new Post("post_18", "discrete", List.of("set", "venn", "subset"), "Thay Linh: 'A ⊆ B'. Me: ‘A’ is my sleep, ‘B’ is the homework due yesterday."),
        new Post("post_19", "discrete", List.of("graph", "bipartite", "exam"), "Thay Linh during exams: Is this graph connected? Me: Emotionally? Not really 🧠💔")



            // with keyword "school"
            // 7 - 9  will be more relate (lower) than 7 - 8 
        ));
        public static List<Post> getSamplePosts() {
            return SamplePosts; // mutable list
        }

        public static void addPost(Post newPost){
            SamplePosts.add(newPost);
        }
    }
}


