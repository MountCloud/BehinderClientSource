package net.rebeyond.behinder.dao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.rebeyond.behinder.entity.AlreadyExistException;
import net.rebeyond.behinder.utils.Utils;

public class BaseDao {
   private ShellManager shellManager;
   protected Connection connection;

   public BaseDao() {
      try {
         this.connection = DBClient.getConnection();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public static void main(String[] args) throws Exception {
   }

   private List getUniqueKey(Class clazz) {
      List list = new ArrayList();
      Field[] var3 = clazz.getDeclaredFields();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Field field = var3[var5];
         if (field.getDeclaredAnnotation(PrimaryKey.class) != null) {
            list.add(field);
         }
      }

      return list;
   }

   public int addEntity(Object entity) throws Exception {
      Class clazz = entity.getClass();
      String table = clazz.getSimpleName();
      List uniqueKeyList = new ArrayList();
      String insertSQL = String.format("insert into %s", table);
      String keySQL = "(";
      String valueSQL = " values(";
      List valueList = new ArrayList();
      Field[] var9 = clazz.getDeclaredFields();
      int var10 = var9.length;

      Field uniqueField;
      for(int var11 = 0; var11 < var10; ++var11) {
         uniqueField = var9[var11];
         uniqueField.setAccessible(true);
         if (uniqueField.getDeclaredAnnotation(PrimaryKey.class) != null) {
            uniqueKeyList.add(uniqueField);
         }

         if (!uniqueField.getName().equals("id") && uniqueField.get(entity) != null) {
            keySQL = keySQL + uniqueField.getName() + ",";
            valueSQL = valueSQL + "?,";
            valueList.add(uniqueField.get(entity));
         }
      }

      keySQL = keySQL.substring(0, keySQL.length() - 1) + ")";
      valueSQL = valueSQL.substring(0, valueSQL.length() - 1) + ")";
      insertSQL = insertSQL + keySQL + valueSQL;
      String findSQL = String.format("select count(*) from %s where ", table);
      String whereSQL = "";

      for(Iterator var17 = uniqueKeyList.iterator(); var17.hasNext(); whereSQL = whereSQL + uniqueField.getName() + "=? and ") {
         uniqueField = (Field)var17.next();
      }

      whereSQL = whereSQL + " 1=1";
      findSQL = findSQL + whereSQL;
      PreparedStatement statement = this.connection.prepareStatement(findSQL);

      for(int i = 0; i < uniqueKeyList.size(); ++i) {
         Field field = (Field)uniqueKeyList.get(i);
         if (Integer.TYPE.equals(field.getType())) {
            statement.setInt(i + 1, field.getInt(entity));
         } else if (String.class.equals(field.getType())) {
            statement.setString(i + 1, field.get(entity).toString());
         } else if (Timestamp.class.equals(field.getType())) {
            statement.setTimestamp(i + 1, (Timestamp)field.get(entity));
         }
      }

      int count = statement.executeQuery().getInt(1);
      statement.close();
      if (count > 0) {
         throw new AlreadyExistException(String.format("该%s已存在", table));
      } else {
         statement = this.connection.prepareStatement(insertSQL);

         for(int i = 0; i < valueList.size(); ++i) {
            Object value = valueList.get(i);
            if (Integer.class.equals(value.getClass())) {
               statement.setInt(i + 1, (Integer)value);
            } else if (String.class.equals(value.getClass())) {
               statement.setString(i + 1, value.toString());
            } else if (Timestamp.class.equals(value.getClass())) {
               statement.setTimestamp(i + 1, (Timestamp)value);
            }
         }

         count = statement.executeUpdate();
         statement.close();
         return count;
      }
   }

   public int delEntity(Object entity, HashMap[] params) throws Exception {
      Class clazz = entity.getClass();
      String table = clazz.getSimpleName();
      String delSQL = String.format("delete from %s where id= ?", table);
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setInt(1, (Integer)this.getField(entity, "id"));
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   public int delEntity(Object entity) throws Exception {
      Class clazz = entity.getClass();
      String table = clazz.getSimpleName();
      String delSQL = String.format("delete from %s where id= ?", table);
      PreparedStatement statement = this.connection.prepareStatement(delSQL);
      statement.setInt(1, (Integer)this.getField(entity, "id"));
      int num = statement.executeUpdate();
      statement.close();
      return num;
   }

   private Object getField(Object object, String name) throws Exception {
      Class clazz = object.getClass();
      Field field = clazz.getDeclaredField(name);
      field.setAccessible(true);
      return field.get(object);
   }

   public void setField(Object object, String name, Object value) throws Exception {
      Class clazz = object.getClass();
      Field field = clazz.getDeclaredField(name);
      field.setAccessible(true);
      if (value == null && (field.getType() == Integer.class || field.getType() == Integer.TYPE)) {
         value = 0;
      }

      if (field.getType() == Timestamp.class) {
         if (value == null) {
            value = Utils.getCurrentDate();
         } else {
            value = Utils.stringToTimestamp(value.toString());
         }
      }

      field.set(object, value);
   }
}
