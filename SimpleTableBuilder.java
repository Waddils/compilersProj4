import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Stack;

public class SimpleTableBuilder extends LittleBaseListener {
	private int counter;
	private LinkedHashMap<String, LinkedHashMap<String, String[]>> symbolTablesList;
	private Stack<LinkedHashMap<String, String[]>> scopeStack;
	private String error;
	
	public SimpleTableBuilder() {
		counter = 1;
		symbolTablesList = new LinkedHashMap<String, LinkedHashMap<String, String[]>>();
		scopeStack = new Stack<LinkedHashMap<String, String[]>>();
		error = null;
	}
	
	@Override public void enterProgram(LittleParser.ProgramContext ctx) throws NullPointerException {
		// 1. Make a new symbol table for "Global"
		LinkedHashMap<String, String[]> symbolTable = new LinkedHashMap<String, String[]>();
		// 2. add it to the list of symbol tables
		symbolTablesList.put("GLOBAL", symbolTable);
		// 3. push it to the "scope stack"
		scopeStack.push(symbolTable);
	}
	@Override public void enterString_decl(LittleParser.String_declContext ctx) {
		// 1. extract the name, type, value
		String name = ctx.id().getText();
		String type = "STRING";
		String value = ctx.str().getText();
		
		if(scopeStack.peek().containsKey(name) && error == null) {
			error = name;
		}
		
		// 2. create a new table entry using above info and insert to the table at the top of the stack (i.e., current table)
		String[] entry = {type, value};
		scopeStack.peek().put(name, entry);
	}
	@Override public void exitProgram(LittleParser.ProgramContext ctx) {
		scopeStack.pop();
	}
	@Override public void enterVar_decl(LittleParser.Var_declContext ctx) {
		String type = ctx.var_type().getText();
		String[] names = ctx.id_list().getText().split(",");
		
		// 2. create a new table entry using above info and insert to the table at the top of the stack (i.e., current table)
		String[] entry = {type};
		for(int i = 0; i < names.length; i++) {
			if(scopeStack.peek().containsKey(names[i]) && error == null) {
				error = names[i];
			}
			scopeStack.peek().put(names[i], entry);
		}
	}
	@Override public void enterParam_decl(LittleParser.Param_declContext ctx) {
		// 1. extract the name, type, value
		String name = ctx.id().getText();
		String type = ctx.var_type().getText();
		
		// 2. create a new table entry using above info and insert to the table at the top of the stack (i.e., current table)
		String[] entry = {type};
		scopeStack.peek().put(name, entry);
	}
	@Override public void enterFunc_decl(LittleParser.Func_declContext ctx) {
		// 1. Make a new symbol table for "Global"
		LinkedHashMap<String, String[]> symbolTable = new LinkedHashMap<String, String[]>();
		// 2. add it to the list of symbol tables
		symbolTablesList.put(ctx.id().getText(), symbolTable);
		// 3. push it to the "scope stack"
		scopeStack.push(symbolTable);
	}
	@Override public void exitFunc_decl(LittleParser.Func_declContext ctx) {
		scopeStack.pop();
	}
	@Override public void enterIf_stmt(LittleParser.If_stmtContext ctx) {
		if(ctx.getText().length() > 0) {
			enterBlock_stmt();
		}
	}
	@Override public void exitIf_stmt(LittleParser.If_stmtContext ctx) {
		if(ctx.getText().length() > 0) {
			scopeStack.pop();
		}
	}
	@Override public void enterElse_part(LittleParser.Else_partContext ctx) {
		if(ctx.getText().length() > 0) {
			enterBlock_stmt();
		}
	}
	@Override public void exitElse_part(LittleParser.Else_partContext ctx) {
		if(ctx.getText().length() > 0) {
			scopeStack.pop();
		}
	}
	@Override public void enterWhile_stmt(LittleParser.While_stmtContext ctx) {
		if(ctx.getText().length() > 0) {
			enterBlock_stmt();
		}
	}
	@Override public void exitWhile_stmt(LittleParser.While_stmtContext ctx) {
		if(ctx.getText().length() > 0) {
			scopeStack.pop();
		}
	}
	public void enterBlock_stmt() {
		// 1. Make a new symbol table for "Global"
		LinkedHashMap<String, String[]> symbolTable = new LinkedHashMap<String, String[]>();
		// 2. add it to the list of symbol tables
		String block = "BLOCK " + Integer.toString(counter);
		symbolTablesList.put(block, symbolTable);
		// 3. push it to the "scope stack"
		scopeStack.push(symbolTable);
		counter++;
	}
	public void prettyPrint() {
		if(error != null) {
			System.out.println("DECLARATION ERROR " + error);
			return;
		}

		Iterator<Entry<String, LinkedHashMap<String, String[]>>> tables = symbolTablesList.entrySet().iterator();
		while(tables.hasNext()) {
			Entry<String, LinkedHashMap<String, String[]>> entry = tables.next();
			String tableName = entry.getKey();
			LinkedHashMap<String, String[]> table = entry.getValue();
			if(!tableName.equals("GLOBAL")) {
				System.out.println();
			}
			System.out.println("Symbol table " + tableName);
			Iterator<Entry<String, String[]>> variables = table.entrySet().iterator();
			while(variables.hasNext()) {
				Entry<String, String[]> var = variables.next();
				if(var.getValue().length > 1) {
					System.out.println("name " + var.getKey() + " type " + var.getValue()[0] + " value " + var.getValue()[1]);
				}
				else {
					System.out.println("name " + var.getKey() + " type " + var.getValue()[0]);
				}
			}
		}
	}
}