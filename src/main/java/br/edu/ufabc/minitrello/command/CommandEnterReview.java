package br.edu.ufabc.minitrello.command;

import java.util.Map;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import br.edu.ufabc.minitrello.executor.Review;

/**
 * Cria a barreira para revisão
 */
public class CommandEnterReview implements Command.Call {
  @Override
  public void run(Map<String, Object> args) {
    String root = (String) args.get("ROOT");
    ZooKeeper zk = (ZooKeeper) args.get("ZK");

    try {
      if (zk.exists(root + "/review", false) == null) {
        System.err.println("[ERRO] Não há um processo de revisão em andamento.");
        return;
      }

      new Review(root, zk).execute();
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro durante o procedimento.");
      e.printStackTrace();
    }
  }
}
