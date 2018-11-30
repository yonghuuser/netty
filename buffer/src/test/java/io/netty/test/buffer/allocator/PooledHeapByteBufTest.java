package io.netty.test.buffer.allocator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

/**
 * Created by wuzhsh on 2018/11/21.
 */
public class PooledHeapByteBufTest {

    @Test
    public void test() {
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        ByteBuf buf = allocator.heapBuffer(4 * 1024);
        for (int i = 0; i < 4096; i++)
            buf.writeByte((byte)11);

        for (int i = 0; i< 1000; i++) {
            buf.writeByte((byte)12);
        }
        System.out.println("buf.length: " + buf.writerIndex());
    }
}
