package br.edu.ufabc.minitrello.commands;

import java.util.Map;
import br.edu.ufabc.minitrello.App;
import br.edu.ufabc.minitrello.Command;

public class CommandExit implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    App.closeZooKeeper();    
    System.exit(0);
  }

}
