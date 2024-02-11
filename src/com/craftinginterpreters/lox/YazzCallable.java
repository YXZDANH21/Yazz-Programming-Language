package com.craftinginterpreters.lox;

import java.util.List;

interface YazzCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
