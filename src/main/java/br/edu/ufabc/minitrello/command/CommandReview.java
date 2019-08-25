package br.edu.ufabc.minitrello.command;

import java.util.Map;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import br.edu.ufabc.minitrello.executor.Review;
import br.edu.ufabc.minitrello.util.CommandUtils;

/**
 * Cria a barreira para revisão.
 */
public class CommandReview implements Command.Call {

  @Override
  public void run(Map<String, Object> args) {
    if (!CommandUtils.argIsString(args.get("message"))) {
      System.err.println("[ERRO] O parâmetro 'message' precisa ser uma string.");
      return;
    }

    String message = (String) args.get("message");
    String root = (String) args.get("ROOT");
    ZooKeeper zk = (ZooKeeper) args.get("ZK");

    try {
      zk.create(root + "/review", message.getBytes(), 
          Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
      System.out.println("Processo de revisão iniciado.");

      new Review(root, zk).execute();
    } catch (KeeperException | InterruptedException e) {
      System.err.println("[ERRO] Houve um erro durante o procedimento.");
      e.printStackTrace();
    }
  }
}
