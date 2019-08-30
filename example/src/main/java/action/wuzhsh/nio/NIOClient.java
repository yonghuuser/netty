package action.wuzhsh.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by wuzhsh on 8/28/2019.
 */
public class NIOClient {

    public static void main(String[] args) throws IOException {
        /*Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 8000));
        socket.getOutputStream().write("aaaaaaaaaaaa".getBytes());*/
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8000));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        a:
        while (true) {
            if (selector.select(1) > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    iterator.remove();
                    if (next.isConnectable()) {
                        SocketChannel channel = (SocketChannel) next.channel();
                        channel.finishConnect();
                        System.out.println(channel.socket() + " 连接完成");
                        channel.register(selector, SelectionKey.OP_WRITE);
                    }
                    if (next.isWritable()) {
                        SocketChannel channel = (SocketChannel) next.channel();
                        System.out.println(channel + "可写");
                        channel.write(ByteBuffer.wrap("aaaaaaaaaaa".getBytes()));
                        next.interestOps(0);
                        channel.close();
                        break a;
                    }
                }
            }
        }

    }
}
