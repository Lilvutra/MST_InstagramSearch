# MST_InstagramSearch
A visual representation of Kruskal's minimum spanning tree algorithm to search for Instagram saved posts with keyword(Since it is hella time-consuming to find a saved post in (my) Saved Post on Instagram



# 📚 Keyword-Centered Minimum Spanning Tree (MST) of Instagram Posts

## 📌 Conceptual Model

### Nodes
Each node represents an individual post relevant to the **input keyword**.  
A post contains:

```json
{
  "id": "post_123",
  "main_keyword": "beach",
  "relevant_words": ["beach", "sunset", "vacation"],
  "caption": "Enjoying the beach at sunset."
}
```

- Posts are **filtered** to include only those where the input keyword matches:
  - the `main_keyword`, or
  - an entry in `relevant_words`.

---

### 🔗 Edges
Edges connect every pair of relevant posts.

#### 🧮 Edge Weights
Weights are computed based on **similarity**:

- If the input keyword `k` == `main_keyword` of post:  
  ➤ `priority += 10`

- If the input keyword `k` is in `relevant_words`:  
  ➤ `priority += (index of k in relevant_words) + 1`  
  *(Words earlier in the list are considered more important)*

Only posts that include the keyword either as `main_keyword` or in `relevant_words` are included in the graph.

---

## ⚙️ Keyword-Centered Tree Construction

### 1. **Input a Keyword**
Example: `"beach"`

### 2. **Filter Relevant Posts**
Include only posts where `"beach"` is the `main_keyword` or in `relevant_words`.

### 3. **Create Graph Nodes**
Each matching post becomes a node.  
Optionally, insert a **virtual keyword node** connected to all post nodes with weight `0`.

### 4. **Compute Edge Weights**
For each pair `(u, v)`, calculate the edge weight as described above.

### 5. **Build Weighted Graph**
Store all edges `(u, v, w)` in a **min-heap** or **priority queue**.

### 6. **Run MST Algorithm**
Use **Kruskal’s algorithm** to construct the **Minimum Spanning Tree** (MST), minimizing total dissimilarity across the graph.

### 7. **Root the Tree Around the Keyword**
- If using a virtual keyword node, the MST will naturally root around it.
- Otherwise, select the most **central** or **high-scoring** post node as the root.

