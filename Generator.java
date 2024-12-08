import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class Generator {
    private static boolean FloatVar = false;
    private static int temp = 1;
    private static Node var;

    public static void newNode(Node root, Node varRoot, SimpleTableBuilder stb) {
        var = varRoot;
    	
        ArrayList<String> irCode = new ArrayList<>();
        
        //Generate
        irCode.add(";IR code");
        generateIR(root, irCode, stb);
        irCode.add(";RET");
        irCode.add(";tiny code");
        
        for (String line : irCode) {
        	System.out.println(line);
        }
        
        FloatVar = false;
        temp = 0;
        ArrayList<String> tiny = new ArrayList<>();
        
        generate3AC(root, tiny, stb);
        tiny.add("sys halt");
        for (String line : tiny) {
            System.out.println(line);
        }
    }
    
    private static void generateIR(Node current, List<String> irCode, SimpleTableBuilder stb) {
        if (current == null) {
            return;
        }

        switch (current.getType()) {
        	case "FUNC":
        		irCode.add(";LABEL " + current.getName());
        		irCode.add(";LINK");
        		break;
        	case "FLOAT":
                FloatVar = true;
                break;
            case "INT":
                break;
            case "STRING":
                break;
            case "ASSIGN":
                generateAssignmentIR(current, irCode, stb);
                break;
            case "READ":
                generateReadIR(current, irCode, stb);
                break;
            case "WRITE":
                generateWriteIR(current, irCode, stb);
                break;
            default:
                break;
        }

        for (int i = 0; i < current.count(); i++) {
            generateIR(current.getChild(i), irCode, stb);
        }
    }

    private static void generateAssignmentIR(Node current, List<String> irCode, SimpleTableBuilder stb) {
        String value = current.getValue().replaceAll("\\(|\\)", ""); // Remove parentheses
        String name = current.getName();
    
        if (!Pattern.compile("[+\\-*/]").matcher(value).find()) {
            String temp = getNextIRTemp();
            if (FloatVar) {
                irCode.add(";STOREF " + value + " " + temp);  // Use STOREF for floating-point values
                irCode.add(";STOREF " + temp + " " + name);  // Always use STOREI to handle both types
            } else {
                irCode.add(";STOREI " + value + " " + temp);  // Use STOREI for integer values
                irCode.add(";STOREI " + temp + " " + name);  // Always use STOREI to handle both types
            }
            
        } else {
            // Split at the boundary between operators and operands
            String[] parts = value.split("\\s*(?=[+\\-*/])|(?<=[+\\-*/])\\s*");
    
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
    
            if (FloatVar) { // float math
                handleFloatAssignmentIR(parts, name, irCode);
            } else { // int math
                handleIntAssignmentIR(parts, name, irCode);
            }
        }
    }

    private static void handleFloatAssignmentIR(String[] parts, String name, List<String> irCode) {
        String temp = getNextIRTemp();
        String part2 = parts[2];

        if(isNumeric(part2)) {
    		irCode.add(";STOREF " + part2 + " " + temp);
    		part2 = temp;
    		temp = getNextIRTemp();
    	}

        switch (parts[1]) {
            case "+":
                irCode.add(";ADDF " + parts[0] + " " + part2 + " " + temp);
                break;
            case "-":
                irCode.add(";SUBF " + parts[0] + " " + part2 + " " + temp);
                break;
            case "*":
                irCode.add(";MULTF " + parts[0] + " " + part2 + " " + temp);
                break;
            case "/":
                irCode.add(";DIVF " + parts[0] + " " + part2 + " " + temp);
                break;
            default:
                throw new IllegalArgumentException("Invalid operator: " + parts[1]);
        }
        irCode.add(";STOREF " + temp + " " + name);
    }

    private static void handleIntAssignmentIR(String[] parts, String name, List<String> irCode) {
    	String temp = getNextIRTemp();
        String part2 = parts[2];

        if(isNumeric(part2)) {
    		irCode.add(";STOREI " + part2 + " " + temp);
    		part2 = temp;
    		temp = getNextIRTemp();
    	}

        switch (parts[1]) {
            case "+":
                irCode.add(";ADDI " + parts[0] + " " + part2 + " " + temp);
                break;
            case "-":
                irCode.add(";SUBI " + parts[0] + " " + part2 + " " + temp);
                break;
            case "*":
                irCode.add(";MULTI " + parts[0] + " " + part2 + " " + temp);
                break;
            case "/":
                irCode.add(";DIVI " + parts[0] + " " + part2 + " " + temp);
                break;
            default:
                throw new IllegalArgumentException("Invalid operator: " + parts[1]);
        }
        irCode.add(";STOREI " + temp + " " + name);
    }



    private static void generateReadIR(Node current, List<String> irCode, SimpleTableBuilder stb) {
        String[] variables = current.getValue().split(",");
        for (String variable : variables) {
            String type = current.getType();
            if (FloatVar) {
                irCode.add(";READF " + variable);
            } else {
                irCode.add(";READI " + variable);
            }
        }
    }

    private static void generateWriteIR(Node current, List<String> irCode, SimpleTableBuilder stb) {
        String[] values = current.getValue().split(",");
        String type = current.getType();
        for (String value : values) {
            if (value.equals("newline")) {
                irCode.add(";WRITES newline");
            } else {
                if (FloatVar) {
                    irCode.add(";WRITEF " + value);
                } else {
                    irCode.add(";WRITEI " + value);
                }
            }
        }
    }
    
    //Tiny Code methods
    
    public static void generate3AC(Node current, ArrayList<String> tiny, SimpleTableBuilder stb) {
        if (current == null) {
            return;
        }

        switch (current.getType()) {
            case "FLOAT":
                FloatVar = true;
                tiny.add("var " + current.getName());
                break;
            case "INT":
                tiny.add("var " + current.getName());
                break;
            case "STRING":
                tiny.add("str " + current.getName() + " " + current.getValue());
                break;
            case "ASSIGN":
                generateAssignment3AC(current, tiny, stb);
                break;
            case "READ":
                generateRead3AC(current, tiny, stb);
                break;
            case "WRITE":
                generateWrite3AC(current, tiny, stb);
                break;
            default:
                break;
        }

        for (int i = 0; i < current.count(); i++) {
            generate3AC(current.getChild(i), tiny, stb);
        }
    }

    private static void generateAssignment3AC(Node current, ArrayList<String> tiny, SimpleTableBuilder stb) {
    	String value = current.getValue().replaceAll("\\(|\\)", ""); // Remove parentheses
        String name = current.getName();
    
        if (!Pattern.compile("[+\\-*/]").matcher(value).find()) {
        	String temp = getNextTemp();
            tiny.add("move " + value + " " + temp);
            tiny.add("move " + temp + " " + name);
        } else {
            // Split at the boundary between operators and operands
            String[] parts = value.split("\\s*(?=[+\\-*/])|(?<=[+\\-*/])\\s*");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
    
            if (FloatVar) { // float math
                handleFloatAssignment(parts, name, tiny);
            } else { // int math
                handleIntAssignment(parts, name, tiny);
            }
        }
    }

    private static void handleFloatAssignment(String[] parts, String name, ArrayList<String> tiny) {
    	String temp = getNextTemp();
    	String part2 = parts[2];

    	if(isNumeric(part2)) {
    		tiny.add("move " + part2 + " " + temp);
    		part2 = temp;
    		temp = getNextTemp();
    	}
    	tiny.add("move " + parts[0] + " " + temp);
        switch (parts[1]) {
            case "+":
                tiny.add("addr " + part2 + " " + temp);
                break;
            case "-":
                tiny.add("subr " + part2 + " " + temp);
                break;
            case "*":
                tiny.add("mulr " + part2 + " " + temp);
                break;
            case "/":
                tiny.add("divr " + part2 + " " + temp);
                break;
            default:
                throw new IllegalArgumentException("Invalid operator: " + parts[1]);
        }
        tiny.add("move " + temp + " " + name);
    }

    private static void handleIntAssignment(String[] parts, String name, ArrayList<String> tiny) {
    	String temp = getNextTemp();
    	String part2 = parts[2];

    	if(isNumeric(part2)) {
    		tiny.add("move " + part2 + " " + temp);
    		part2 = temp;
    		temp = getNextTemp();
    	}
    	tiny.add("move " + parts[0] + " " + temp);
        switch (parts[1]) {
            case "+":
                tiny.add("addi " + part2 + " " + temp);
                break;
            case "-":
                tiny.add("subi " + part2 + " " + temp);
                break;
            case "*":
                tiny.add("muli " + part2 + " " + temp);
                break;
            case "/":
                tiny.add("divi " + part2 + " " + temp);
                break;
            default:
                throw new IllegalArgumentException("Invalid operator: " + parts[1]);
        }
        tiny.add("move " + temp + " " + name);
    }

    private static void generateRead3AC(Node current, ArrayList<String> tiny, SimpleTableBuilder stb) {
    	String[] values = current.getValue().split(",");
        for (String value : values) {
        	Node iterNode = var;
        	boolean check = false;
            do {
            	if(check) {
            		iterNode = iterNode.getChild(0);
            	}
            	else {
            		check = true;
            	}
            	if(value.equals(iterNode.name)) {
            		break;
            	}
            } while(iterNode.count() != 0);
        	
        	String type = iterNode.getType();
            if (type.equals("FLOAT")) {
                tiny.add("sys readr " + value);
            } else {
                tiny.add("sys readi " + value);
            }
        }
    }

    private static void generateWrite3AC(Node current, ArrayList<String> tiny, SimpleTableBuilder stb) {
        String[] values = current.getValue().split(",");
        for (String value : values) {
        	Node iterNode = var;
        	boolean check = false;
            do {
            	if(check) {
            		iterNode = iterNode.getChild(0);
            	}
            	else {
            		check = true;
            	}
            	if(value.equals(iterNode.name)) {
            		break;
            	}
            } while(iterNode.count() != 0);
        	
        	String type = iterNode.getType();
            if (type.equals("FLOAT")) {
                tiny.add("sys writer " + value);
            } else if (type.equals("STRING")) {
                tiny.add("sys writes " + value);
            } else {
                tiny.add("sys writei " + value);
            }
        }
    }

    private static String getNextTemp() {
        return "r" + temp++;
    }
    
    private static String getNextIRTemp() {
    	return "$T" + temp++;
    }
    
    private static boolean isNumeric(String s) {
    	if (s == null) {
    		return false;
    	}
    	try {
    		double d = Double.parseDouble(s);
    	} catch (NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
}