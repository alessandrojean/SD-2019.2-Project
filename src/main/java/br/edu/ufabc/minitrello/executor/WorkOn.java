package br.edu.ufabc.minitrello.executor;

import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import static br.edu.ufabc.minitrello.util.CommandUtils.getQueueNumber;

/**
 * Cada tarefa só pode ter uma pessoa trabalhando nela por vez.
 * Desta maneira, o WorkOn funciona como um Lock, gerando uma
 * espécie de lista de espera onde cada cliente deve esperar
 * sua vez para poder ser liberado para trabalhar nela.
 * 
 * Enquanto o cliente fica esperando, seu terminal da aplicação
 * é travado, impedindo que ele possa digitar novos comandos
 * enquanto não sair da lista de espera.
 */
public class WorkOn {

  private String root;
  private String pathName;
  private ZooKeeper zooKeeper;

  public WorkOn(String root, String pathName, ZooKeeper zooKeeper) {
    this.root = root;
    this.pathName = pathName;
    this.zooKeeper = zooKeeper;
  }

  public void execute() throws KeeperException, InterruptedException {
    final Object lock = new Object();
    synchronized (lock) {
      while (true) {
        int suffix = getQueueNumber(pathName);
        // Step 2.
        List<String> list = zooKeeper.getChildren(root, false);
        Collections.sort(list);

        String minStr = list.get(0);
        int min = getQueueNumber(minStr);
        
        if (suffix == min) {
          int taskNo = getQueueNumber(root);
          System.out.println();
          System.out.println("Agora você pode trabalhar na tarefa " + taskNo + ".");
          return;
        }

        // Step 4.
        Collections.reverse(list);
        String maxStr = list.stream()
            .filter(s -> getQueueNumber(s) < suffix)
            .findFirst().orElse("none");

        // Step 5.
        Stat s = zooKeeper.exists(root + "/" + maxStr, event -> {
          if (event.getType() == EventType.NodeDeleted) {
            synchronized (lock) {
              lock.notifyAll();
            }
          }
        });

        if (s != null) {
          System.out.println();
          System.out.println("Alguém já está trabalhando nesta tarefa.");
          System.out.println("Você está na lista de espera.");

          lock.wait();
        }
      }
    }
  }

}