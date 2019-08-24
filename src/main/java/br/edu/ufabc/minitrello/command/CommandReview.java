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

public class CommandReview implements Command.Call {
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
            }else if (zk.exists(task+"/review-", false) == null){
                System.err.println("[ERRO] A tarefa não está sobre revisão");
            }
      
            System.out.println("A tarefa existe, tentando se juntar revisão");

            new Review(task, "/review-", zk).execute();
            
          } catch (KeeperException | InterruptedException e) {
            System.err.println("[ERRO] Houve um erro durante o procedimento.");
            e.printStackTrace();
          }
    }
}