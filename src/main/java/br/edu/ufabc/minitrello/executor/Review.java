package br.edu.ufabc.minitrello.executor;

import java.util.List;
import java.util.Scanner;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import br.edu.ufabc.minitrello.App;
import br.edu.ufabc.minitrello.util.CommandUtils;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;

/**
 * Quando uma revisão é convocada, no mínimo x pessoas devem estar no 
 * processo realizando a mesma. Caso alguma termine sua revisão, 
 * precisará esperar que as outras também terminem. Com isto,
 * utiliza-se uma Barrier para gerenciar este procedimento.
 */
public class Review {

  private String root;
  private ZooKeeper zooKeeper;
  private String currentUser;
  private final Object mutex = new Object();

  private Watcher watcher;

  public Review(String root, ZooKeeper zooKeeper) {
    this.root = root;
    this.zooKeeper = zooKeeper;
    this.currentUser = CommandUtils.getUser();
    this.watcher = event -> {
      synchronized (mutex) {
        mutex.notify();
      }
    };
  }

  @SuppressWarnings("resource")
  public void execute() throws KeeperException, InterruptedException {
    if (zooKeeper.exists(root + "/reviewers/" + currentUser, false) != null) {
      System.err.println("[ERRO] Você já está revisando em alguma outra instância.");
      return;
    }

    // Entra na barreira.
    enter();

    byte[] message = zooKeeper.getData(root + "/review", false, null);
    String messageStr = new String(message);

    System.out.println("\n[ REVISÃO DIÁRIA ]\n");
    System.out.println(messageStr);

    System.out.println("\nQuando estiver de acordo, digite [Enter].");
    Scanner sc = new Scanner(System.in);
    sc.nextLine();

    // Sai da barreira.
    leave();
  }

  private void enter() throws KeeperException, InterruptedException {
    zooKeeper.create(root + "/reviewers/" + currentUser, new byte[0],
        Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

    synchronized (mutex) {
      while (true) {
        List<String> list = zooKeeper.getChildren(root + "/reviewers", watcher);
        if (list.size() < 2) {
          mutex.wait();
        } else {
          return;
        }
      }
    }
  }

  private void leave() throws KeeperException, InterruptedException {
    zooKeeper.delete(root + "/reviewers/" + currentUser, 0);

    synchronized (mutex) {
      while (true) {
        List<String> list = zooKeeper.getChildren(root + "/reviewers", watcher);
        if (list.size() > 0) {
          mutex.wait();
        } else {
          try {
            // Deleta a revisão se existir e for o último.
            if (zooKeeper.exists(root + "/review", false) != null) {
              zooKeeper.delete(root + "/review", 0);
            }
          } catch (KeeperException e) {
            // Não faz nada.
          }

          App.isInReview = false;

          return;
        }
      }
    }
  }

}
