package auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LoginGuard {

  private LoginGuard() {}

  public static boolean requireAccess() {
    LoginManager lm = LoginManager.getInstance();
    if (!lm.isLoginEnabled()) return true;
    if (lm.isAuthenticated()) {
      lm.touch();
      return true;
    }
    return showLoginDialog();
  }

  private static boolean showLoginDialog() {
    try {
      FXMLLoader loader = new FXMLLoader(LoginGuard.class.getResource("/views/Login.fxml"));
      Parent root = loader.load();

      // 🔥 Quitamos el "420, 520" fijo para que la ventana mida justo lo que mide la tarjeta
      Scene scene = new Scene(root); 

      Stage stage = new Stage();
      stage.setTitle("Iniciar Sesión");
      stage.setScene(scene);
      stage.initModality(Modality.WINDOW_MODAL);
      stage.setResizable(false);

      Window owner = Stage.getWindows().stream().filter(Window::isFocused).findFirst().orElse(null);
      
      if (owner != null) {
        stage.initOwner(owner);
        
        // 🔥 INYECTAMOS EL CSS DEL DASHBOARD (Para que el botón sea verde y respete el tema)
        if (owner.getScene() != null) {
            scene.getStylesheets().addAll(owner.getScene().getStylesheets());
            
            if (owner.getScene().getRoot().getStyleClass().contains("dark-theme")) {
                root.getStyleClass().add("dark-theme");
            }
        }
        
        stage.setOnShown(ev -> {
          stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
          stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
        });
      } else {
        stage.centerOnScreen();
      }

      // 🔥 Ajustamos el Stage al tamaño exacto del contenido
      stage.sizeToScene(); 

      stage.showAndWait();

      Object result = stage.getUserData();
      return result instanceof Boolean && (Boolean) result;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
