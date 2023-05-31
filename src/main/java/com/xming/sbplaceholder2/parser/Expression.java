package com.xming.sbplaceholder2.parser;

import com.xming.sbplaceholder2.parser.type.SBInst;
import com.xming.sbplaceholder2.parser.type.entrust.EntrustInst;
import com.xming.sbplaceholder2.parser.type.entrust.EntrustTool;

import java.util.Arrays;
import java.util.HashMap;

public class Expression implements Cloneable {
    static final String[] symbols = {"+", "-", "*", "/", "%", ">=", "<=", ">", "<", "==", "!=", "&&", "||", "!"};
    static final HashMap<String, Expression> cache = new HashMap<>();
    int max_length = 4;
    EntrustInst[] entrust = new EntrustInst[max_length + 1];
    String[] operator = new String[max_length];

    static public Expression getExpression(Parser parser) {
        if (cache.containsKey(parser.getRaw_expression())) return cache.get(parser.getRaw_expression());
        Expression new_expression = new Expression(parser);
        cache.put(parser.getRaw_expression(), new_expression);
        return new_expression;
    }
    public Expression() {}
    private Expression(Parser parser) {
        String rawExpression = parser.getRaw_expression();
        int object_count = 0;
        int start_pos = 0;
        boolean in_string = false;
        boolean in_bracket = false;
        for (int i = 0; i < rawExpression.length(); i++) {
            if (rawExpression.charAt(i) == '\'' ||
                    rawExpression.charAt(i) == '\"') in_string = !in_string;
            if (in_string) continue;
            if (rawExpression.charAt(i) == '(' ||
                    rawExpression.charAt(i) == ')') in_bracket = !in_bracket;
            if (in_bracket) continue;
            for (String symbol : symbols) {
                if (i + symbol.length() > rawExpression.length()) continue;
                if (rawExpression.subSequence(i, i + symbol.length()).equals(symbol)) {
                    object_count += 1;
                    if (object_count > max_length) {
                        max_length *= 2;
                        this.operator = Arrays.copyOf(this.operator, this.operator.length * 2);
                        this.entrust = Arrays.copyOf(this.entrust, this.entrust.length * 2 + 1);
                    }

                    this.operator[object_count - 1] = symbol;
                    this.entrust[object_count - 1] = EntrustTool.parse(parser, rawExpression.substring(start_pos, i));
                    start_pos = i + symbol.length();
                }
            }
        }
        this.entrust[object_count] = EntrustTool.parse(parser, rawExpression.substring(start_pos));
    }
    public SBInst<?>[] execute() {
        SBInst<?>[] result = new SBInst<?>[entrust.length];
        for (int i = 0; i < entrust.length; i++) {
            result[i] = entrust[i].execute();
        }
        return result;
    }
    @Override
    public Expression clone() {
        Expression expression = new Expression();
        expression.max_length = this.max_length;
        expression.entrust = Arrays.copyOf(this.entrust, this.max_length + 1);
        expression.operator = Arrays.copyOf(this.operator, this.max_length);
        return expression;
    }
}
