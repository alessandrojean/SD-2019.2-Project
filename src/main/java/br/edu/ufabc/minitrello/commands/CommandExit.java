package br.edu.ufabc.minitrello.commands;

import java.util.Map;
import br.edu.ufabc.minitrello.Command;

public class CommandExit implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    System.exit(0);
  }

}
