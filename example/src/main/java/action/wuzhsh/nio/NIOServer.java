package action.wuzhsh.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by wuzhsh on 8/28/2019.
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        Selector serverSelector = Selector.open();
        Selector clientSelector = Selector.open();

        new Thread(() -> {
            try {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8000));
                serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
                while (true) {
                    // 这里如果用 select()，会发现一直阻塞，客户端连接进来不会继续运行
                    if (serverSelector.select() > 0) {
                        Iterator<SelectionKey> iterator = serverSelector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            if (key.isAcceptable()) {
                                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                                SocketChannel socketChannel = serverChannel.accept();
                                socketChannel.configureBlocking(false);
                                // ***  注意这里，
                                // 如果下面的线程中 调用 clientSelector.select() 一直阻塞的话，
                                // 会导致这里的register方法被阻塞 ***
                                // 如果下面的线程中使用 select(), 这里可以调用clientSelector.wakeup() （ps: 这句话并
                                // 不是意味着wakeup与无参的select()要成对出现）唤醒该 selector，但是由于下面是一个while(true)，
                                // 所以依旧可能会继续进入select()， 导致这句代码阻塞，所以下面使用 select(1)，每个毫秒 selector都会唤醒一次
                                socketChannel.register(clientSelector, SelectionKey.OP_READ);
                                System.out.println("接收到连接：" + socketChannel.socket());
                            }
                            iterator.remove();
                        }
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    if (clientSelector.select(1) > 0) {
                        Iterator<SelectionKey> iterator = clientSelector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey next = iterator.next();
                            System.out.println("发生IO事件::" + next.channel());
                            iterator.remove();
                            if (next.isReadable()) {
                                SocketChannel channel = (SocketChannel) next.channel();
                                ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                                channel.read(byteBuffer);
                                System.out.println(new String(byteBuffer.array()));
                            }
                        }
                    } else {
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
