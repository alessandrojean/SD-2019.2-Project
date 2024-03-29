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
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import br.edu.ufabc.minitrello.command.*;
import br.edu.ufabc.minitrello.executor.Leader;
import br.edu.ufabc.minitrello.util.CommandUtils;

public class App {

  private static final String HOST = "localhost";
  private static final int SESSION_TIMEOUT = 3000;

  private static final List<Command> COMMANDS;

  static {
    COMMANDS = new ArrayList<>();
    COMMANDS.add(new Command("help", "Imprime a ajuda.", List.of(), new CommandHelp(COMMANDS)));
    COMMANDS.add(new Command("exit", "Finaliza a execução.", List.of(), new CommandExit()));
    COMMANDS.add(new Command("new-task", "Cria uma nova tarefa.", List.of("task"), new CommandNewTask()));
    COMMANDS.add(new Command("delete-task", "Deleta uma tarefa.", List.of("id"), new CommandDeleteTask()));
    COMMANDS.add(new Command("list", "Lista todas as tarefas.", List.of(), new CommandList()));
    COMMANDS.add(new Command("work-on", "Começa a trabalhar em uma tarefa, sujeito a bloqueio.", List.of("id"), new CommandWorkOn()));
    COMMANDS.add(new Command("review", "Começa um processo de revisão diário.", List.of("message"), true, new CommandReview()));
    COMMANDS.add(new Command("enter-review", "Se junta a uma review em andamento, sujeito a bloqueio.", List.of(), new CommandEnterReview()));
  }

  private static final List<String> SAFE_KEYS = List.of("help", "exit", "enter-review");

  public static ZooKeeper ZOOKEEPER;
  public static boolean isInReview = false;
  public static boolean amILeader = false;
  public static String myUser = "";
  public static String currentLeader = "";

  private static Watcher WATCHER = event -> {
    try {
      if (event.getState() == KeeperState.Disconnected) {
        System.out.println("\nA aplicação foi desconectada do servidor, encerrando.");
        closeZooKeeper();
        System.exit(0);
      } else if (event.getType() == EventType.NodeCreated 
          && event.getPath().endsWith("review")) {
        isInReview = true;
        watchNode("/minitrello/review");
      } else if (event.getType() == EventType.NodeDeleted
          && event.getPath().endsWith("review")) {
        isInReview = false;
        watchNode("/minitrello/review");
      }
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro no watcher.");
      closeZooKeeper();
      System.exit(-1);
    }
  };

  public static void main(String[] args) throws IOException {
    startZooKeeper();
    setupNode();
    setupElection();
    runTitle();
    runPrompt();
    closeZooKeeper();
  }

  private static void startZooKeeper() {
    try {
      ZOOKEEPER = new ZooKeeper(HOST, SESSION_TIMEOUT, WATCHER);
    } catch (IOException e) {
      System.err.println("[ERRO] Não foi possível iniciar o ZooKeeper.");
      System.exit(-1);
    }
  }

  private static void setupNode() {
    try {
      createNode("/minitrello");
      createNode("/minitrello/tasks");
      createNode("/minitrello/reviewers");
      createNode("/minitrello/election");
      watchNode("/minitrello/review");
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Não foi possível criar os znodes.");
      closeZooKeeper();
      System.exit(0);
    }
  }

  private static void createNode(String root)
      throws KeeperException, InterruptedException {
    Stat s = ZOOKEEPER.exists(root, false);
    if (s == null) {
      ZOOKEEPER.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
  }

  private static void watchNode(String root)
      throws KeeperException, InterruptedException {
    Stat s = ZOOKEEPER.exists(root, true);
    if (s != null) {
      isInReview = true;
    }
  }

  private static void checkLeader() {
    try {
      if (ZOOKEEPER.exists("/minitrello/leader", false) == null) return;

      byte[] leader = ZOOKEEPER.getData("/minitrello/leader", false, null);
      String leaderStr = new String(leader);
      currentLeader = leaderStr;
      amILeader = leaderStr.equals(myUser);
    } catch (KeeperException | InterruptedException e) {
      // Faça nada.
    }
  }

  private static void setupElection() {
    try {
      myUser = CommandUtils.getUser(true);
      Leader leader = new Leader("/minitrello", myUser, ZOOKEEPER);
      leader.elect();
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro no processo de eleição.");
      closeZooKeeper();
      System.exit(-1);
    }
  }

  private static void runTitle() {
    System.out.println("Bem-vindo ao MiniTrello!");
    System.out.println("Digite :help para ajuda e :exit para sair.");
  }

  private static void runPrompt() throws IOException {
    checkLeader();

    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    String leaderStatus = "(L" + (amILeader ? "" : "=" + currentLeader) + ")";
    String status = (isInReview ? " (R)" : "") + " " + leaderStatus;
    String command = "";

    System.out.printf("%nMiniTrello%s> ", status);

    while ((command = reader.readLine()) != null) {
      checkLeader();
      leaderStatus = "(L" + (amILeader ? "" : "=" + currentLeader) + ")";
      status = (isInReview ? " (R)" : "") + " " + leaderStatus;

      evaluate(command.trim());
      System.out.printf("%nMiniTrello%s> ", status);
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
    boolean isSafe = SAFE_KEYS.contains(cmd.getKey());

    if (isInReview && !isSafe) {
      System.out.println("[AVISO] Um processo de revisão está em andamento.");
      System.out.println("[AVISO] Você não poderá usar novos comandos até revisar.");
      System.out.println("[AVISO] Utilize o comando :enter-review para revisar.");
      return;
    }

    if (cmd.getArgSize() > 0 && command.indexOf(" ") == -1) {
      System.err.println("[ERRO] Nenhum argumento foi providenciado.");
      return;
    }

    if (cmd.isLeaderOnly() && !amILeader) {
      System.err.println("[ERRO] Este comando é limitado somente ao líder.");
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

}
