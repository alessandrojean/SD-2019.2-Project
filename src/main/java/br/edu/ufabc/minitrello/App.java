package br.edu.ufabc.minitrello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import br.edu.ufabc.minitrello.command.*;

public class App implements Watcher {

  private static final String HOST = "localhost";
  private static final int SESSION_TIMEOUT = 3000;

  private static final List<Command> COMMANDS;

  static {
    COMMANDS = new ArrayList<>();
    COMMANDS.add(new Command("help", "Imprime a ajuda.", List.of(), new CommandHelp(COMMANDS)));
    COMMANDS.add(new Command("exit", "Finaliza a execução.", List.of(), new CommandExit()));
    COMMANDS.add(new Command("new-task", "Cria uma nova tarefa.", List.of("task"), new CommandNewTask()));
    COMMANDS.add(new Command("list", "Lista todas as tarefas.", List.of(), new CommandList()));
    COMMANDS.add(new Command("work-on", "Começa a trabalhar em uma tarefa, sujeito a bloqueio.", List.of("id"), new CommandWorkOn()));
    COMMANDS.add(new Command("create-review", "Começa uma review, sujeito a bloqueio.", List.of("id"), new CommandCreateReview()));
    COMMANDS.add(new Command("review", "Se junta a uma review, sujeito a bloqueio.", List.of("id"), new CommandReview()));
  }

  public static ZooKeeper ZOOKEEPER;

  public static void main(String[] args) throws IOException {
    startZooKeeper();
    setupNode();
    runTitle();
    runPrompt();
    closeZooKeeper();
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
      createNode("/minitrello");
      createNode("/minitrello/tasks");
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Não foi possível criar os znodes.");
      closeZooKeeper();
      System.exit(0);
    }
  }

  private static void createNode(String root) throws KeeperException, InterruptedException {
    Stat s = ZOOKEEPER.exists(root, false);
    if (s == null) {
      ZOOKEEPER.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
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
      evaluate(command.trim());
      System.out.print("\nMiniTrello> ");
    }
  }

  private static void evaluate(String command) {
    if (!command.startsWith(":")) {
      System.err.println("[ERRO] Os comandos devem começar com ':'.");
      return;
    }

    String commandWithoutArgs = command.indexOf(" ") != -1
        ? command.substring(0, command.indexOf(" "))
        : command;

    Optional<Command> optCmd = COMMANDS.stream()
        .filter(c -> c.getKey().equals(commandWithoutArgs.substring(1)))
        .findFirst();

    if (optCmd.isEmpty()) {
      System.err.println("[ERRO] O comando '" + commandWithoutArgs + "' não existe.");
      return;
    }

    Command cmd = optCmd.get();

    if (cmd.getArgSize() > 0 && command.indexOf(" ") == -1) {
      System.err.println("[ERRO] Nenhum argumento foi providenciado.");
      return;
    }

    String args = cmd.getArgSize() > 0 
        ? command.substring(command.indexOf(" ") + 1)
        : "";
    Map<String, Object> argMap = null;

    try {
      Evaluator eval = new Evaluator(args, cmd.getArgumentNames());
      argMap = eval.scanArguments();
    } catch (Exception e) {
      System.err.println("[ERRO] " + e.getMessage());
      return;
    }

    // Injeta o ZooKeeper nos argumentos.
    argMap.put("ZK", ZOOKEEPER);
    argMap.put("ROOT", "/minitrello");

    cmd.getCallable().run(argMap);
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
