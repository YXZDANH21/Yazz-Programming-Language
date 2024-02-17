package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;


class YazzInstance {
    private YazzClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    YazzInstance(YazzClass klass)   {
        this.klass = klass;
    }

    Object get(Token name)  {
        if (fields.containsKey(name.lexeme))    {
            return fields.get(name.lexeme);
        }

        YazzFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value)  {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString()    {
        return klass.name + " instance";
    }

}
