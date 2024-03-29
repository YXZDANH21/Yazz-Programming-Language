package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>   {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter()   {
        globals.define("clock", new YazzCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("input", new YazzCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    return reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeError(token, "Error reading input from user.");
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }

        });

        globals.define("readFile", new YazzCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                try {
                    String path = arguments.get(0).toString();
                    String contents = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
                    return contents;
                } catch (IOException e) {
                    throw new RuntimeError(token, "Failed to read file: " + e.getMessage());
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("writeFile", new YazzCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                try {
                    String path = arguments.get(0).toString();
                    String contents = arguments.get(1).toString();
                    Files.write(Paths.get(path), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeError(token, "Failed to write to file: " + e.getMessage());
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("appendFile", new YazzCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                try {
                    String path = arguments.get(0).toString();
                    String content = arguments.get(1).toString();
                    Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeError(token, "Failed to append to file: " + e.getMessage());
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });
        globals.define("cap", new YazzCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (arguments.get(0) instanceof String) {
                    String original = (String) arguments.get(0);
                    return original.toUpperCase();
                } else {
                    throw new RuntimeError(token, "Argument must be a string.");
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("uncap", new YazzCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (arguments.get(0) instanceof String) {
                    String original = (String) arguments.get(0);
                    return original.toLowerCase();
                } else {
                    throw new RuntimeError(token, "Argument must be a string.");
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("countChars", new YazzCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (arguments.get(0) instanceof String) {
                    String string = arguments.get(0).toString();
                    return (double) string.length();
                } else {
                    throw new RuntimeError(token, "Argument must be a string.");
                }
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("editChar", new YazzCallable() {
            @Override
            public int arity() {
                return 3;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof String)) {
                    throw new RuntimeError(token, "First argument must be a string.");
                }
                if (!(arguments.get(1) instanceof Double)) {
                    throw new RuntimeError(token, "Second argument must be a number.");
                }
                if (!(arguments.get(2) instanceof String)) {
                    throw new RuntimeError(token, "Third argument must be a string.");
                }

                String originalString = (String) arguments.get(0);
                int position = ((Double) arguments.get(1)).intValue() - 1;
                String newCharacter = (String) arguments.get(2);

                if (position < 0 || position >= originalString.length()) {
                    throw new RuntimeError(token, "Position out of bounds.");
                }

                String newString = originalString.substring(0, position) + newCharacter + originalString.substring(position + 1);
                return newString;
            }

            @Override
            public String toString() {
                return "<native fn for editing characters in a string>";
            }
        });

        globals.define("sqrt", new YazzCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof Double)) {
                    throw new RuntimeError(token, "Argument must be a number.");
                }
                Double number = (Double) arguments.get(0);
                if (number < 0) {
                    throw new RuntimeError(token, "Argument must be non-negative.");
                }
                return Math.sqrt(number);
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("pow", new YazzCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof Double))  {
                    throw new RuntimeError(token, "Argument must be a number.");
                }
                if (!(arguments.get(1) instanceof Double))  {
                    throw new RuntimeError(token, "The power must be a number.");
                }
                Double number = (Double) arguments.get(0);
                Double power = (Double) arguments.get(1);
                return Math.pow(number, power);
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("sin", new YazzCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof Double)) {
                    throw new RuntimeError(token, "Argument must be a number.");
                }
                return Math.round(Math.sin(Math.toRadians((Double) arguments.get(0))) * 10000) / 10000.0;
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }
        });

        globals.define("cos", new YazzCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof Double)) {
                    throw new RuntimeError(token, "Argument must be a number.");
                }
                return Math.round(Math.cos(Math.toRadians((Double) arguments.get(0))) * 10000) / 10000.0;
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }

        });

        globals.define("tan", new YazzCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof Double)) {
                    throw new RuntimeError(token, "Argument must be a number.");
                }
                double angle = Math.toRadians((Double) arguments.get(0));
                // Check for the angle where tan is undefined
                if (Math.abs(Math.cos(angle)) < 1E-9) {
                    throw new RuntimeError(token, "Tangent is undefined for this angle.");
                }
                return Math.round(Math.tan(angle) * 10000) / 10000.0;
            }

            @Override
            public String toString()    {
                return "<native fn>";
            }

        });

        globals.define("round", new YazzCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
                if (!(arguments.get(0) instanceof Double) || !(arguments.get(1) instanceof Double)) {
                    throw new RuntimeError(token, "Argument must be a number.");
                }
                double number = (Double) arguments.get(0);
                int places = ((Double) arguments.get(1)).intValue();
                double scale = Math.pow(10, places);
                return Math.round(number * scale) / scale;
            }

            @Override
            public String toString() {
                return "<native fn for rounding>";
            }
        });


    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements)   {
                execute(statement);
            }
        }   catch   (RuntimeError error)    {
            Yazz.runtimeError(error);
        }
    }


    @Override
    public Object visitLiteralExpr(Expr.Literal expr)   {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr)   {
        Object object = evaluate(expr.object);

        if (!(object instanceof YazzInstance))  {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((YazzInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr)   {
        int distance = locals.get(expr);
        YazzClass superclass = (YazzClass)environment.getAt(distance, "super");

        YazzInstance object = (YazzInstance)environment.getAt(distance - 1, "this");

        YazzFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)   {
        Object right = evaluate(expr.right);

        switch  (expr.operator.type)    {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable
        return null;
    }


    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    public Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null)   {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right)    {
        if (left instanceof Double && right instanceof Double)  return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean)  return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null)  return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double)   {
            String text = object.toString();
            if (text.endsWith(".0"))    {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr)  {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth)  {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment)    {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements)   {
                execute(statement);
            }
        } finally   {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null)    {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof YazzClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null)    {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, YazzFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods)   {
            YazzFunction function = new YazzFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }
        YazzClass klass = new YazzClass(stmt.name.lexeme, (YazzClass)superclass, methods);
        if (superclass != null) {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)   {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt)   {
        YazzFunction function = new YazzFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt)   {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }



    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }


    @Override
    public Void visitReturnStmt(Stmt.Return stmt)   {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition)))  {
            execute(stmt.body);
        }
        return null;
    }


    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null)   {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch  (expr.operator.type)    {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)  {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String)  {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments)    {
            arguments.add(evaluate(argument));
        }
        if (!(callee instanceof YazzCallable))  {
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        YazzCallable function = (YazzCallable)callee;
        if (arguments.size() != function.arity())   {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments, expr.paren); // Added null here due to line number
    }

    @Override
    public Object visitGetExpr(Expr.Get expr)   {
        Object object = evaluate(expr.object);
        if (object instanceof YazzInstance) {
            return ((YazzInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances have properties");
    }

}