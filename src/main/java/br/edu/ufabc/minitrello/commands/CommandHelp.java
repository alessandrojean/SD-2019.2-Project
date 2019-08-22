package br.edu.ufabc.minitrello.commands;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import br.edu.ufabc.minitrello.Command;

public class CommandHelp implements Command.Call {

  private List<Command> commands;

  public CommandHelp(List<Command> commands) {
    this.commands = commands;
  }

  @Override
  public void run(Map<String, Object> args) {
    String cmdStr = commands.stream()
        .map(c -> c.toString())
        .collect(Collectors.joining("\n"));

    System.out.println(cmdStr);
  }

}
