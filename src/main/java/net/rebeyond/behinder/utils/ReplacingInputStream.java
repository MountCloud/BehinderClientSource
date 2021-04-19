package net.rebeyond.behinder.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

public class ReplacingInputStream extends FilterInputStream {
   LinkedList inQueue = new LinkedList();
   LinkedList outQueue = new LinkedList();
   final byte[] search;
   final byte[] replacement;

   public ReplacingInputStream(InputStream in, byte[] search, byte[] replacement) {
      super(in);
      this.search = search;
      this.replacement = replacement;
   }

   private boolean isMatchFound() {
      Iterator inIter = this.inQueue.iterator();

      for(int i = 0; i < this.search.length; ++i) {
         if (!inIter.hasNext() || this.search[i] != (Integer)inIter.next()) {
            return false;
         }
      }

      return true;
   }

   private void readAhead() throws IOException {
      while(true) {
         if (this.inQueue.size() < this.search.length) {
            int next = super.read();
            this.inQueue.offer(next);
            if (next != -1) {
               continue;
            }
         }

         return;
      }
   }

   public int read() throws IOException {
      if (this.outQueue.isEmpty()) {
         this.readAhead();
         if (this.isMatchFound()) {
            for(int i = 0; i < this.search.length; ++i) {
               this.inQueue.remove();
            }

            byte[] var5 = this.replacement;
            int var2 = var5.length;

            for(int var3 = 0; var3 < var2; ++var3) {
               byte b = var5[var3];
               this.outQueue.offer(Integer.valueOf(b));
            }
         } else {
            this.outQueue.add(this.inQueue.remove());
         }
      }

      return (Integer)this.outQueue.remove();
   }
}
