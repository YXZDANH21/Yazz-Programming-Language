package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

class YazzClass implements YazzCallable{
    final String name;
    final YazzClass superclass;
    private final Map<String, YazzFunction> methods;

    YazzClass(String name, YazzClass superclass, Map<String, YazzFunction> methods)  {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    YazzFunction findMethod(String name)    {
        if (methods.containsKey(name))  {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public String toString()    {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token token) {
        YazzInstance instance = new YazzInstance(this);
        YazzFunction initializer = findMethod("init");
        if (initializer != null)    {
            initializer.bind(instance).call(interpreter, arguments, token);
        }
        return instance;
    }

    @Override
    public int arity()  {
        YazzFunction initializer = findMethod("init");
        if (initializer == null)    return 0;
        return initializer.arity();
    }
}
