package net.rebeyond.behinder.entity;

public class Test {
   public static void main(String[] args) {
      (new Thread(() -> {
         System.out.println("vvvvv");
      })).start();
   }
}
