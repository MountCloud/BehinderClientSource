// 
// Decompiled by Procyon v0.5.36
// 

package net.rebeyond.behinder.utils;

import java.io.IOException;
import java.util.Iterator;
import java.io.InputStream;
import java.util.LinkedList;
import java.io.FilterInputStream;

public class ReplacingInputStream extends FilterInputStream
{
    LinkedList<Integer> inQueue;
    LinkedList<Integer> outQueue;
    final byte[] search;
    final byte[] replacement;
    
    public ReplacingInputStream(final InputStream in, final byte[] search, final byte[] replacement) {
        super(in);
        this.inQueue = new LinkedList<Integer>();
        this.outQueue = new LinkedList<Integer>();
        this.search = search;
        this.replacement = replacement;
    }
    
    private boolean isMatchFound() {
        final Iterator<Integer> inIter = this.inQueue.iterator();
        for (int i = 0; i < this.search.length; ++i) {
            if (!inIter.hasNext() || this.search[i] != inIter.next()) {
                return false;
            }
        }
        return true;
    }
    
    private void readAhead() throws IOException {
        while (this.inQueue.size() < this.search.length) {
            final int next = super.read();
            this.inQueue.offer(next);
            if (next == -1) {
                break;
            }
        }
    }
    
    @Override
    public int read() throws IOException {
        if (this.outQueue.isEmpty()) {
            this.readAhead();
            if (this.isMatchFound()) {
                for (int i = 0; i < this.search.length; ++i) {
                    this.inQueue.remove();
                }
                for (final byte b : this.replacement) {
                    this.outQueue.offer((int)b);
                }
            }
            else {
                this.outQueue.add(this.inQueue.remove());
            }
        }
        return this.outQueue.remove();
    }
}
