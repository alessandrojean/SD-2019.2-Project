package br.edu.ufabc.minitrello.command;

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
  private boolean leaderOnly;
  private Call callable;

  public Command(String key, String description,
      List<String> argumentNames, Call callable) {
    this(key, description, argumentNames, false, callable);
  }

  public Command(String key, String description, 
      List<String> argumentNames, boolean leaderOnly, Call callable) {
    this.key = key;
    this.description = description;
    this.argumentNames = argumentNames;
    this.leaderOnly = leaderOnly;
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

  public boolean isLeaderOnly() {
    return leaderOnly;
  }

  public void setLeaderOnly(boolean leaderOnly) {
    this.leaderOnly = leaderOnly;
  }

  public Call getCallable() {
    return callable;
  }
  
  public void setCallable(Call callable) {
    this.callable = callable;
  }

  public int getArgSize() {
    return argumentNames.size();
  }

  @Override
  public String toString() {
    String args = argumentNames.stream()
        .map(x -> "<" + x + ">")
        .collect(Collectors.joining(" "));
    String leaderMark = leaderOnly ? "[LÍDER] " : "";

    return String.format(":%s %s\n  %s%s", key, args, leaderMark, description);        
  }

}