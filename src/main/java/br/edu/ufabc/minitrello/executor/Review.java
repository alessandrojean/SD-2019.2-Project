package br.edu.ufabc.minitrello.executor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import static br.edu.ufabc.minitrello.util.CommandUtils.getQueueNumber;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;

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
public class Review {

  private String root;
  private String pathName;
  private ZooKeeper zooKeeper;

  public Review(String root, String pathName, ZooKeeper zooKeeper) {
    this.root = root;
    this.pathName = pathName;
    this.zooKeeper = zooKeeper;
  }

  public void execute() throws KeeperException, InterruptedException {
    final Object barrier = new Object();
    synchronized (barrier) {
      while (true) {
        try {
          // entra na barrier
          String name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
          zooKeeper.create(root + pathName + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
                  CreateMode.EPHEMERAL_SEQUENTIAL);
          
          // TODO  faz algo
          wait(60000);

          //    acaba a review e se for o último deleta o seu znode
          zooKeeper.delete(root + pathName + name, 0);
          List<String> list = zooKeeper.getChildren(root, true);
          if(list.size()>0){
            return;
          }else{
            zooKeeper.delete(root + "/review-", 0);
          }
          
          
        } catch (UnknownHostException e) {
            System.out.println(e.toString());
        }        
      }
    }
  }
}