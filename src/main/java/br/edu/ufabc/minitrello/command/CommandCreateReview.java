package br.edu.ufabc.minitrello.command;

import java.util.Map;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import br.edu.ufabc.minitrello.executor.Review;
import br.edu.ufabc.minitrello.util.CommandUtils;

/*
 * Cria a barreira para revisão
 */

public class CommandCreateReview implements Command.Call {
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
      
            System.out.println("A tarefa existe, tentando iniciar revisão");
            String pathName = zk.create(task + "/review-",
                new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            new Review(task, pathName, zk).execute();
            
          } catch (KeeperException | InterruptedException e) {
            System.err.println("[ERRO] Houve um erro durante o procedimento.");
            e.printStackTrace();
          }
    }
}