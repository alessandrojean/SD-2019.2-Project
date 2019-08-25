package br.edu.ufabc.minitrello.command;

import java.util.Map;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import br.edu.ufabc.minitrello.util.CommandUtils;

public class CommandNewTask implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    if (!CommandUtils.argIsString(args.get("task"))) {
      System.err.println("[ERRO] O argumento 'task' deve ser uma string.");
      return;
    }

    String task = (String) args.get("task");
    String root = (String) args.get("ROOT");
    ZooKeeper zk = (ZooKeeper) args.get("ZK");

    try {
      String ret = zk.create(root + "/tasks/task-", task.getBytes(), 
          Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
      System.out.println("Tarefa criada em '" + ret + "'.");
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro ao criar a tarefa.");
      e.printStackTrace();
    }
  }

}
