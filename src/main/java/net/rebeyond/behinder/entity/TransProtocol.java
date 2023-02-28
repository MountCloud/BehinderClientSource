package net.rebeyond.behinder.entity;

import net.rebeyond.behinder.core.ICrypt;
import net.rebeyond.behinder.dao.PrimaryKey;

public class TransProtocol {
   private int id;
   @PrimaryKey
   private String name;
   @PrimaryKey
   private String type;
   private String encode;
   private String decode;
   private ICrypt cryptor;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getEncode() {
      return this.encode;
   }

   public void setEncode(String encode) {
      this.encode = encode;
   }

   public String getDecode() {
      return this.decode;
   }

   public void setDecode(String decode) {
      this.decode = decode;
   }

   public ICrypt getCryptor() {
      return this.cryptor;
   }

   public void setCryptor(ICrypt cryptor) {
      this.cryptor = cryptor;
   }
}
