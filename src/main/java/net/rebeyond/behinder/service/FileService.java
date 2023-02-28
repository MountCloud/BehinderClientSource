package net.rebeyond.behinder.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.service.callback.ICallBack;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class FileService {
   private IShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private static int UPLOAD_BLOCK_SIZE = 30720;
   private static int DOWNLOAD_BLOCK_SIZE = 1048576;
   private static int THREAD_NUM = 10;

   public FileService(IShellService shellService, JSONObject shellEntity, List workList) {
      this.currentShellService = shellService;
      this.shellEntity = shellEntity;
      this.workList = workList;
   }

   public void uploadFile(String localFilePath, String remoteFilePath, ICallBack callBack) throws Exception {
      File file = new File(localFilePath);
      long blockCount = file.length() / (long)UPLOAD_BLOCK_SIZE;
      long remainSize = file.length() % (long)UPLOAD_BLOCK_SIZE;
      if (remainSize > 0L) {
         ++blockCount;
      }

      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUM);
      FileInputStream fis = new FileInputStream(localFilePath);
      FileChannel fileChannel = fis.getChannel();
      List errList = new ArrayList();
      Set errorMsg = new HashSet();
      int[] currentCount = new int[]{0};

      for(int i = 0; (long)i < blockCount; ++i) {
         final int finalI = i;
         final long finalblockCount = blockCount;
         Runnable runner = () -> {
            try {
               ByteBuffer byteBuffer = ByteBuffer.allocate(UPLOAD_BLOCK_SIZE);
               int size;
               synchronized(fileChannel) {
                  fileChannel.position((long)(finalI * UPLOAD_BLOCK_SIZE));
                  size = fileChannel.read(byteBuffer);
               }

               JSONObject responseObj = this.currentShellService.uploadFilePart(remoteFilePath, Arrays.copyOfRange(byteBuffer.array(), 0, size), (long)finalI, (long)UPLOAD_BLOCK_SIZE);
               String status = responseObj.getString("status");
               String msg = responseObj.getString("msg");
               if (!status.equals("success")) {
                  errorMsg.add(msg);
                  executorService.shutdownNow();
                  return;
               }

               int var10002 = currentCount[0]++;
               String progress = Utils.getPercent(currentCount[0], (int) finalblockCount);
               callBack.onSuccess("success", progress);
            } catch (SocketTimeoutException var18) {
               errList.add(finalI);
            } catch (SocketException var19) {
               errList.add(finalI);
            } catch (Exception var20) {
               errList.add(finalI);
               var20.printStackTrace();
            }

         };
         executorService.submit(runner);
      }

      while(errList.size() > 0) {
         final int finalI = (Integer)errList.get(0);
         final long finalblockCount = blockCount;
         Runnable runner = () -> {
            try {
               ByteBuffer byteBuffer = ByteBuffer.allocate(UPLOAD_BLOCK_SIZE);
               int size;
               synchronized(fileChannel) {
                  fileChannel.position((long)(finalI * UPLOAD_BLOCK_SIZE));
                  size = fileChannel.read(byteBuffer);
               }

               JSONObject responseObj = this.currentShellService.uploadFilePart(remoteFilePath, Arrays.copyOfRange(byteBuffer.array(), 0, size), (long)finalI, (long)UPLOAD_BLOCK_SIZE);
               String status = responseObj.getString("status");
               String msg = responseObj.getString("msg");
               if (!status.equals("success")) {
                  errorMsg.add(msg);
                  executorService.shutdownNow();
                  return;
               }

               int var10002 = currentCount[0]++;
               String progress = Utils.getPercent(currentCount[0], (int) finalblockCount);
               callBack.onSuccess("success", progress);
            } catch (SocketTimeoutException var18) {
               var18.printStackTrace();
               errList.add(finalI);
            } catch (SocketException var19) {
               var19.printStackTrace();
               errList.add(finalI);
            } catch (Exception var20) {
               errList.add(finalI);
               var20.printStackTrace();
            }

         };
         executorService.submit(runner);
      }

      executorService.shutdown();
      executorService.awaitTermination(24L, TimeUnit.HOURS);
      if (errorMsg.size() > 0) {
         throw new Exception((String)errorMsg.iterator().next());
      } else {
         try {
            String localHash = Utils.getFileMD5(localFilePath);
            JSONObject resObj = this.currentShellService.checkFileHash(remoteFilePath, localHash);
            if (!resObj.getString("status").equals("success")) {
               throw new Exception("哈希不匹配");
            }
         } catch (InterruptedException var20) {
            var20.printStackTrace();
         }

      }
   }

   public void downloadFile(String localFilePath, String remoteFilePath, long remoteFileSize, ICallBack callBack) throws Exception {
      long blockCount = remoteFileSize / (long)DOWNLOAD_BLOCK_SIZE;
      long remainSize = remoteFileSize % (long)DOWNLOAD_BLOCK_SIZE;
      if (remainSize > 0L) {
         ++blockCount;
      }

      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUM);
      FileOutputStream fos = new FileOutputStream(localFilePath);
      FileChannel fileChannel = fos.getChannel();
      Set errorMsg = new HashSet();
      List errList = new ArrayList();
      int[] currentCount = new int[]{0};

      for(int i = 0; (long)i < blockCount; ++i) {
         final int finalI = i;
         final long finalblockCount = blockCount;
         Runnable runner = () -> {
            try {
               JSONObject responseObj = this.currentShellService.downFilePart(remoteFilePath, (long)finalI, (long)DOWNLOAD_BLOCK_SIZE);
               String status = responseObj.getString("status");
               String msg = responseObj.getString("msg");
               if (!status.equals("success")) {
                  executorService.shutdownNow();
                  errorMsg.add(msg);
                  return;
               }

               byte[] content = Base64.getDecoder().decode(msg);
               int var10002 = currentCount[0]++;
               String progress = Utils.getPercent(currentCount[0], (int) finalblockCount);
               callBack.onSuccess("success", progress);
               synchronized(fileChannel) {
                  fileChannel.position((long)finalI * (long)DOWNLOAD_BLOCK_SIZE);
                  fileChannel.write(ByteBuffer.wrap(content));
               }
            } catch (SocketTimeoutException var19) {
               errList.add(finalI);
            } catch (SocketException var20) {
               errList.add(finalI);
            } catch (Exception var21) {
               errList.add(finalI);
               var21.printStackTrace();
            }

         };
         executorService.submit(runner);
      }

      while(errList.size() > 0) {
         final int finalI = (Integer)errList.get(0);
         final long finalblockCount = blockCount;
         Runnable runner = () -> {
            try {
               JSONObject responseObj = this.currentShellService.downFilePart(remoteFilePath, (long)finalI, (long)DOWNLOAD_BLOCK_SIZE);
               String status = responseObj.getString("status");
               String msg = responseObj.getString("msg");
               if (!status.equals("success")) {
                  executorService.shutdownNow();
                  errorMsg.add(msg);
                  return;
               }

               byte[] content = Base64.getDecoder().decode(msg);
               int var10002 = currentCount[0]++;
               String progress = Utils.getPercent(currentCount[0], (int) finalblockCount);
               callBack.onSuccess("success", progress);
               synchronized(fileChannel) {
                  fileChannel.position((long)finalI * (long)DOWNLOAD_BLOCK_SIZE);
                  fileChannel.write(ByteBuffer.wrap(content));
               }

               errList.remove(finalI);
            } catch (SocketTimeoutException var19) {
               return;
            } catch (SocketException var20) {
               errList.add(finalI);
            } catch (Exception var21) {
               errList.add(finalI);
               var21.printStackTrace();
            }

         };
         executorService.submit(runner);
      }

      executorService.shutdown();
      executorService.awaitTermination(24L, TimeUnit.HOURS);
      if (errorMsg.size() > 0) {
         throw new Exception((String)errorMsg.iterator().next());
      } else {
         try {
            String localHash = Utils.getFileMD5(localFilePath);
            JSONObject resObj = this.currentShellService.checkFileHash(remoteFilePath, localHash);
            if (!resObj.getString("status").equals("success")) {
               throw new Exception("哈希不匹配");
            }
         } catch (InterruptedException var24) {
            var24.printStackTrace();
         } finally {
            fileChannel.close();
            fos.close();
         }

      }
   }

   public static void main(String[] args) {
   }
}
