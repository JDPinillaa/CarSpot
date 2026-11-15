package autonoma.main;

import autonoma.models.PerfilVehiculo;
import autonoma.persistence.CatalogoVehiculos;
import autonoma.persistence.PersistenciaVehiculos;
import autonoma.views.Cabina;
import java.awt.EventQueue;
import javax.swing.JOptionPane;

/** Punto de entrada: carga la configuración y construye la presentación. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        try {
            CatalogoVehiculos catalogo = PersistenciaVehiculos.cargarCatalogo();
            EventQueue.invokeLater(() -> {
                PerfilVehiculo inicial = catalogo.getPerfilInicial();
                PerfilVehiculo elegido = inicial;
                if (catalogo.getPerfiles().size() > 1) {
                    Object seleccion = JOptionPane.showInputDialog(null,
                            "Selecciona vehículo e interior", "CarSpot · Garaje",
                            JOptionPane.QUESTION_MESSAGE, null,
                            catalogo.getPerfiles().toArray(), inicial);
                    if (seleccion == null) return;
                    elegido = (PerfilVehiculo) seleccion;
                }
                new Cabina(elegido).setVisible(true);
            });
        } catch (Exception error) {
            EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "No se pudo iniciar el simulador: " + error.getMessage(),
                    "Error de configuración", JOptionPane.ERROR_MESSAGE));
        }
    }
}
