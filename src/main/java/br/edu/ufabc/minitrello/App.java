package br.edu.ufabc.minitrello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import br.edu.ufabc.minitrello.commands.*;

public class App {

  private static final List<Command> COMMANDS;

  static {
    COMMANDS = new ArrayList<>();
    COMMANDS.add(new Command("help", "Imprime a ajuda.", List.of(), new CommandHelp(COMMANDS)));
    COMMANDS.add(new Command("exit", "Finaliza a execução.", List.of(), new CommandExit()));
  }

  public static void main(String[] args) throws IOException {
    runTitle();
    runPrompt();
  }

  private static void runTitle() {
    System.out.println("Bem-vindo ao MiniTrello!");
    System.out.println("Digite :help para ajuda e :exit para sair.");
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    String command = "";

    System.out.print("\nMiniTrello> ");

    while ((command = reader.readLine()) != null) {
      evaluate(command);
      System.out.print("\nMiniTrello> ");
    }
  }

  private static void evaluate(String command) {
    if (!command.startsWith(":")) {
      System.err.println("[ERRO] Os comandos devem começar com ':'.");
      return;
    }

    Optional<Command> optCmd = COMMANDS.stream()
        .filter(c -> c.getKey().equals(command.substring(1)))
        .findFirst();

    if (optCmd.isEmpty()) {
      System.err.println("[ERRO] O comando '" + command + "' não existe.");
      return;
    }

    Command cmd = optCmd.get();
    cmd.getCallable().run(null);
  }

}
