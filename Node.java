import java.util.ArrayList;

public class Node {

    String type;
    String name;
    String value;
    int counter;
    ArrayList<Node> child = new ArrayList<>();

    public Node(String type, String name, String value){
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }

    public void addChild(Node child){
        this.child.add(child);
        counter++;
    }

    public int count(){
        return counter;
    }

    public Node getChild(int index){
        return child.get(index);
    }
}