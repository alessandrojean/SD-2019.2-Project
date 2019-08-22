package br.edu.ufabc.minitrello.command;

import java.util.Map;
import br.edu.ufabc.minitrello.App;

public class CommandExit implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    App.closeZooKeeper();    
    System.exit(0);
  }

}
