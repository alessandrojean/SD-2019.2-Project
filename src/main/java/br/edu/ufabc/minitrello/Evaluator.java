package br.edu.ufabc.minitrello;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {

  private String currentString;
  private List<String> argNames;
  private Map<String, Object> arguments = new HashMap<>();
  private int start = 0;
  private int current = 0;
  private int argI = 0;

  public Evaluator(String args, List<String> argNames) {
    this.currentString = args;
    this.argNames = argNames;
  }

  public Map<String, Object> scanArguments() throws Exception {
    while (!isAtEnd() && argI < argNames.size()) {
      start = current;
      scanToken();
      argI++;
    }

    return arguments;
  }

  private void scanToken() throws Exception {
    char c = advance();
    if (isDigit(c)) {
      number();
    } else if (c == '"') {
      string();
    } else if (c != ' ' && c != '\r' && c != '\t') {
      throw new Exception("Argumento inválido.");
    }
  }

  private void number() {
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) advance();
    }

    double value = Double.parseDouble(currentString.substring(start, current));
    addArgument(value);
  }

  private void string() throws Exception {
    while ((peek() != '"' || peekPrevious() == '\\') && !isAtEnd())
      advance();

    if (isAtEnd())
      throw new Exception("String não terminada.");

    advance();
    String value = currentString.substring(start + 1, current - 1);
    addArgument(value.replace("\\n", "\n").replace("\\\"", "\""));
  }

  private char advance() {
    current++;
    return currentString.charAt(current - 1);
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return currentString.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= currentString.length()) return '\0';
    return currentString.charAt(current + 1);
  }

  private char peekPrevious() {
    return currentString.charAt(current - 1);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= currentString.length();
  }

  private void addArgument(Object value) {
    arguments.put(argNames.get(argI), value);
  }

}