import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class KeywordMSTApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KeywordMSTApp().createAndShowGUI());
    }

    JTextField keywordField;
    JTextArea resultArea;
    GraphPanel graphPanel;


    void createAndShowGUI() {
        JFrame frame = new JFrame("Keyword-Centered MST");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        keywordField = new JTextField(20);
        JButton submitBtn = new JButton("Generate MST");
        resultArea = new JTextArea(20, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        submitBtn.addActionListener(this::handleGenerate);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter Keyword:"));
        inputPanel.add(keywordField);
        inputPanel.add(submitBtn);

        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        graphPanel = new GraphPanel();

        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.SOUTH);
        frame.getContentPane().add(graphPanel, BorderLayout.CENTER);

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
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (nodes == null || edges == null) return;
    
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
    
            int w = getWidth(), h = getHeight();
            int radius = 30;
            int centerX = w / 2, centerY = h / 2;
            double angleStep = 2 * Math.PI / nodes.size();
    
            for (int i = 0; i < nodes.size(); i++) {
                int x = (int) (centerX + 200 * Math.cos(i * angleStep));
                int y = (int) (centerY + 200 * Math.sin(i * angleStep));
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
    



    void handleGenerate(ActionEvent e) {
        String keyword = keywordField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a keyword.");
            return;
        }
    
        List<Post> allPosts = DataSource.getSamplePosts();
        List<Post> relevantPosts = new ArrayList<>();
    
        for (Post post : allPosts) {
            if (post.mainKeyword.equalsIgnoreCase(keyword) || post.relevantWords.contains(keyword)) {
                relevantPosts.add(post);
            }
        }
    
        if (relevantPosts.size() < 2) {
            resultArea.setText("Not enough relevant posts to build a tree.");
            graphPanel.update(null, null); // clear graph
            return;
        }
    
        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>();
        for (int i = 0; i < relevantPosts.size(); i++) {
            for (int j = i + 1; j < relevantPosts.size(); j++) {
                Post u = relevantPosts.get(i);
                Post v = relevantPosts.get(j);
                int weight = computeWeight(u, v, keyword);
                edgeQueue.add(new Edge(u, v, weight));
            }
        }
    
        List<Edge> mst = new ArrayList<>();
        Map<Post, Post> parent = new HashMap<>();
        for (Post post : relevantPosts) parent.put(post, post);
    
        while (!edgeQueue.isEmpty() && mst.size() < relevantPosts.size() - 1) {
            Edge edge = edgeQueue.poll();
            Post rootU = find(parent, edge.u);
            Post rootV = find(parent, edge.v);
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
    
        resultArea.setText(sb.toString());
    
        // Send to graphPanel
        graphPanel.update(relevantPosts, mst);
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

        //count the common related words in relevantWords between post u and v
        for (int i = 0; i < u.relevantWords.size(); i++){
            for (int j = 0; j < v.relevantWords.size(); j++){
                if (u.relevantWords.get(i).equalsIgnoreCase(v.relevantWords.get(j))){
                    score ++;
                    break;
                }
            }
        }
        
        //count the words in post caption that appeared in another post's relevantWords
        String [] uWords = u.caption.split("\\s+");
        String [] vWords = v.caption.split("\\s+"); // split at whitespace

        List<String> uList = Arrays.asList(uWords);
        List<String> vList = Arrays.asList(vWords);

        for (int i = 0; i < uList.size(); i++){
            for (int j = 0; j < v.relevantWords.size(); j++){
                if (uList.get(i).equalsIgnoreCase(v.relevantWords.get(j))){
                    score ++;
                    break;
                }
            }
        }
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

    Post find(Map<Post, Post> parent, Post node) {
        if (parent.get(node) != node) {
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
            return Objects.hash(id);
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

    static class DataSource {
        public static List<Post> getSamplePosts() {
            return List.of(
                new Post("post_1", "summer", List.of("sunset", "swim", "vacation"), "Enjoying the beach at sunset."),
                new Post("post_2", "sunset", List.of("sunset", "ocean", "beach"), "Sunset over the ocean."),
                new Post("post_3", "vacation", List.of("beach", "sunset", "swim"), "Holiday vibes!"),
                new Post("post_4", "forest", List.of("trees", "hike", "sunset"), "Hiking in the forest."),
                new Post("post_5", "beach", List.of("waves", "swim", "sunny"), "Beach day fun!"),

                new Post("post_7", "school", List.of("school", "class", "teacher"), "I will have the exam tomorrow at class"),
                new Post("post_8", "stress", List.of("anxiety", "pressure", "school"), "School pressure causing too much stress"),
                new Post("post_9", "teacher", List.of("class", "school", "education"), "My teacher assigned a difficult project")
                // with keyword "school"
                // 7 - 9  will be more relate (lower) than 7 - 8 
            );
        }
    }
}

