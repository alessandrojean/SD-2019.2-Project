package br.edu.ufabc.minitrello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import br.edu.ufabc.minitrello.commands.*;

public class App implements Watcher {

  private static final String HOST = "localhost";
  private static final int SESSION_TIMEOUT = 3000;

  private static final List<Command> COMMANDS;

  static {
    COMMANDS = new ArrayList<>();
    COMMANDS.add(new Command("help", "Imprime a ajuda.", List.of(), new CommandHelp(COMMANDS)));
    COMMANDS.add(new Command("exit", "Finaliza a execução.", List.of(), new CommandExit()));
  }

  public static ZooKeeper ZOOKEEPER;

  public static void main(String[] args) throws IOException {
    startZooKeeper();
    setupNode();
    runTitle();
    runPrompt();
  }

  private static void startZooKeeper() {
    try {
      ZOOKEEPER = new ZooKeeper(HOST, SESSION_TIMEOUT, new App());
    } catch (IOException e) {
      System.err.println("[ERRO] Não foi possível iniciar o ZooKeeper.");
      System.exit(-1);
    }
  }

  private static void setupNode() {
    try {
      Stat s = ZOOKEEPER.exists("/minitrello", false);
      if (s == null) {
        ZOOKEEPER.create("/minitrello", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Não foi possível criar /minitrello.");
      closeZooKeeper();
      System.exit(0);
    }
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
  
  public static void closeZooKeeper() {
    try {
      ZOOKEEPER.close();
    } catch (InterruptedException e) {
      System.err.println("[ERRO] Não foi possível fechar o ZooKeeper.");
    }
  }

  @Override
  public void process(WatchedEvent event) {
    if (event.getState() == KeeperState.Disconnected) {
      System.out.println("\nA aplicação foi desconectada do servidor, encerrando.");
      closeZooKeeper();
      System.exit(0);
    }
  }

}
