package br.edu.ufabc.minitrello.executor;

import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import static br.edu.ufabc.minitrello.util.CommandUtils.getQueueNumber;

public class WorkOn implements Watcher {

  private Integer mutex = 0;
  private String root;
  private String pathName;
  private ZooKeeper zooKeeper;

  public WorkOn(String root, String pathName, ZooKeeper zooKeeper) {
    this.root = root;
    this.pathName = pathName;
    this.zooKeeper = zooKeeper;
  }

  public boolean execute() throws KeeperException, InterruptedException {
    while (true) {
      int suffix = getQueueNumber(pathName);
      // Step 2.
      List<String> list = zooKeeper.getChildren(root, false);
      Collections.sort(list);

      String minStr = list.get(0);
      int min = getQueueNumber(minStr);
      
      if (suffix == min) {
        int taskNo = getQueueNumber(root);
        System.out.println("Agora você pode trabalhar na tarefa " + taskNo + ".");
        return true;
      }

      // Step 4.
      Collections.reverse(list);
      String maxStr = list.stream()
          .filter(s -> getQueueNumber(s) < suffix)
          .findFirst().orElse("none");

      // Step 5.
      Stat s = zooKeeper.exists(root + "/" + maxStr, this);
      if (s != null) break;
    }

    System.out.println();
    System.out.println("Alguém já está trabalhando nesta tarefa.");
    System.out.println("Aguarde a confirmação de quando poderá trabalhar.");
    return false;
  }
  
  @Override
  synchronized public void process(WatchedEvent event) {
    synchronized (mutex) {
      if (event.getType() == Event.EventType.NodeDeleted) {
        try {
          execute();
        } catch (Exception e) {
          System.err.println("[ERRO] O processo de obtenção dos direitos da tarefa falhou.");
          e.printStackTrace();
        }
      }
    }
  }

}