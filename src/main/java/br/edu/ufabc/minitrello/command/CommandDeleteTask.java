package br.edu.ufabc.minitrello.command;

import java.util.Map;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import br.edu.ufabc.minitrello.util.CommandUtils;

public class CommandDeleteTask implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    if (!CommandUtils.argIsNumber(args.get("id"))) {
      System.err.println("[ERRO] O parâmetro 'id' precisa ser um número.");
      return;
    }

    String root = (String) args.get("ROOT");
    ZooKeeper zk = (ZooKeeper) args.get("ZK");
    double id = (double) args.get("id");
    String idPadded = String.format("%010.0f", id);

    try {
      String task = root + "/tasks/task-" + idPadded;
      Stat st = zk.exists(task, false);

      if (st == null) {
        System.err.println("[ERRO] A tarefa especificada não existe.");
        return;
      }

      if (zk.getChildren(task, false).size() > 0) {
        System.err.println("[ERRO] Alguém está trabalhando nesta tarefa.");
        return;
      }

      zk.delete(task, 0);
      System.out.println("Tarefa deletada.");
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro durante o procedimento.");
      e.printStackTrace();
    }
  }

}
