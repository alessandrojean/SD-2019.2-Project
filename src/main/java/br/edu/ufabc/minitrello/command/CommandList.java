package br.edu.ufabc.minitrello.command;

import java.util.List;
import java.util.Map;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public class CommandList implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    String root = (String) args.get("ROOT");
    ZooKeeper zk = (ZooKeeper) args.get("ZK");

    try {
      List<String> tasks = zk.getChildren(root + "/tasks", false);
      if (tasks.size() == 0) {
        System.out.println("Não há nenhuma tarefa criada.");
      } else {
        for (String t : tasks) {
          byte[] body = zk.getData(root + "/tasks/" + t, false, null);
          System.out.println(t + " -> " + new String(body));
        }
      }
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro ao obter a lista de tarefas.");
    }
  }

}
