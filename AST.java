public class AST extends LittleBaseListener {
    private Node root;
    private Node currentNode;
    private Node varRoot = null;
    private SimpleTableBuilder stb;

    public AST(SimpleTableBuilder stb) {
        this.stb = stb;
    }

    @Override
    public void enterProgram(LittleParser.ProgramContext ctx) {
        root = new Node("GLOBAL", null, null);
        currentNode = root;
    }

    @Override
    public void enterString_decl(LittleParser.String_declContext ctx) {
        String name = ctx.id().getText();
        String type = "STRING";
        String value = ctx.str().getText();
        addNode(type, name, value);
    }

    @Override
    public void enterVar_decl(LittleParser.Var_declContext ctx) {
        String type = ctx.var_type().getText();
        String[] names = ctx.id_list().getText().split(",");
        for (String name : names) {
            Node previousNode = addNode(type, name, null);
            if (varRoot == null) {
            	varRoot = previousNode;
            }
        }
    }

    @Override
    public void enterFunc_decl(LittleParser.Func_declContext ctx) {
        String functionName = ctx.id().getText();
        addNode("FUNC", functionName, null);
    }

    @Override
    public void enterAssign_stmt(LittleParser.Assign_stmtContext ctx) {
        String id = ctx.assign_expr().id().getText();
        String expr = ctx.assign_expr().expr().getText();
        addNode("ASSIGN", id, expr);
    }

    @Override
    public void enterRead_stmt(LittleParser.Read_stmtContext ctx) {
        addNode("READ", null, ctx.id_list().getText());
    }

    @Override
    public void enterWrite_stmt(LittleParser.Write_stmtContext ctx) {
    	addNode("WRITE", null, ctx.id_list().getText());
    }

    private Node addNode(String scope, String name, String value) {
        currentNode.addChild(new Node(scope, name, value));
        Node previousNode = currentNode;
        currentNode = currentNode.getChild(currentNode.count() - 1);
        return previousNode;
    }

    public Node getRoot() {
        return root;
    }
    
    public Node getVarRoot() {
    	return varRoot;
    }
}