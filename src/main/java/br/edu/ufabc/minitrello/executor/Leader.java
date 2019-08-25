package br.edu.ufabc.minitrello.executor;

import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import br.edu.ufabc.minitrello.App;
import static br.edu.ufabc.minitrello.util.CommandUtils.getQueueNumber;

public class Leader implements Watcher {

  private String root;
  private String user;
  private String pathName;
  private ZooKeeper zooKeeper;
  private final Object mutex = new Object();

  public Leader(String root, String user, ZooKeeper zooKeeper) {
    this.root = root;
    this.user = user;
    this.zooKeeper = zooKeeper;
  }

  public void elect() throws KeeperException, InterruptedException {
    pathName = zooKeeper.create(root + "/election/n-", new byte[0],
        Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    check();
  }

  private void check() {
    synchronized (mutex) {
      try {
        int suffix = getQueueNumber(pathName);

        List<String> list = zooKeeper.getChildren(root + "/election", false);
        Collections.sort(list);

        String minStr = list.get(0);
        int min = getQueueNumber(minStr);

        if (suffix == min) {
          leader();
          return;
        }

        Collections.reverse(list);
        String maxStr = list.stream()
            .filter(s -> getQueueNumber(s) < suffix)
            .findFirst().orElse("none");

        zooKeeper.exists(root + "/election/" + maxStr, this);
      } catch (KeeperException | InterruptedException e) {
        System.err.println("[ERRO] Houve um erro na eleição, finalizando.");
        App.closeZooKeeper();
        System.exit(-1);
      }
    }
  }

  private void leader() throws KeeperException, InterruptedException {
    Stat s = zooKeeper.exists(root + "/leader", false);
    if (s == null) {
      zooKeeper.create(root + "/leader", user.getBytes(),
          Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    } else {
      zooKeeper.setData(root + "/leader", user.getBytes(), 0);
    }
  }

  @Override
  public void process(WatchedEvent event) {
    synchronized (mutex) {
      if (event.getType() == EventType.NodeDeleted) {
        check();
      }
    }
  }
}
