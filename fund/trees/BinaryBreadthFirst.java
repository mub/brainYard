import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * @author mbergens Michael Bergens
 */
public class BinaryBreadthFirst {
    private static class Node {
        public final String id;
        public Node(String let) {
            id = let;
        }
        public Node left;
        public Node right;
        @Override public String toString() {
            return id;
        }
    }

    private static int visitCount = 1;

    private static void visit(final Node node) {
        if((visitCount & (visitCount - 1)) == 0) System.out.println();
        visitCount ++;
        System.out.print(node.id);
    }

    public static void mBreadthFirst(final Node root) {
        Deque<Node> queue = new ArrayDeque<Node>();
        queue.addLast(root);
        while(!queue.isEmpty()) {
            Node n = queue.removeFirst();
            visit(n);
            if(n.left != null) queue.addLast(n.left);
            if(n.right != null) queue.addLast(n.right);
        }
    }

    public static void treePrint(final Node root) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0; i < 4; i++) result.add("");
        printNodes(root, 0, result);

        // full tree
        for(int i = 0; i< result.size(); i++) System.out.println(result.get(i));
    }

    public static void printNodes(Node node, int level, ArrayList<String> result) {
        result.set(level, result.get(level) +  node.id);
        if(node.left != null) printNodes(node.left, level + 1, result);
        if(node.right != null) printNodes(node.right, level + 1, result);
    }

    public static void main(String[] args) {
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        Node e = new Node("E");
        Node f = new Node("F");
        Node g = new Node("G");
        Node h = new Node("H");

        a.left = b;
        a.right = c;

        b.left = d;
        b.right = e;
        c.left = f;
        c.right = g;
        d.left = h;
        treePrint(a);
        System.out.printf("%n---------------%n");
        mBreadthFirst(a);
    }
}
