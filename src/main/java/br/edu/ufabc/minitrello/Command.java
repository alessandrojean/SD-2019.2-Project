package br.edu.ufabc.minitrello;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Representa um comando no REPL.
 */
public class Command {

  public static interface Call {
    void run(Map<String, Object> args);
  }

  private String key;
  private String description;
  private List<String> argumentNames;
  private Call callable;

  public Command(String key, String description, 
      List<String> argumentNames, Call callable) {
    this.key = key;
    this.description = description;
    this.argumentNames = argumentNames;
    this.callable = callable;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getArgumentNames() {
    return argumentNames;
  }

  public void setArgumentNames(List<String> argumentNames) {
    this.argumentNames = argumentNames;
  }

  public Call getCallable() {
    return callable;
  }
  
  public void setCallable(Call callable) {
    this.callable = callable;
  }

  @Override
  public String toString() {
    String args = argumentNames.stream()
        .map(x -> "<" + x + ">")
        .collect(Collectors.joining(" "));

    return String.format(":%s %s\n  %s", key, args, description);        
  }

}