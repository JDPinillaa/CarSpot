package autonoma.main;

import autonoma.models.Carro;
import autonoma.models.vehiculos.CatalogoVehiculos;
import autonoma.views.Cabina;
import java.awt.EventQueue;
import java.util.List;
import javax.swing.JOptionPane;

/** Punto de entrada: presenta el selector de garaje e inicia la cabina. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            List<Carro> vehiculos = CatalogoVehiculos.obtenerVehiculos();
            Carro elegido = vehiculos.get(0);
            if (vehiculos.size() > 1) {
                Object seleccion = JOptionPane.showInputDialog(null,
                        "Selecciona un vehículo", "CarSpot · Garaje",
                        JOptionPane.QUESTION_MESSAGE, null,
                        vehiculos.toArray(), elegido);
                if (seleccion == null) return;
                elegido = (Carro) seleccion;
            }
            new Cabina(elegido).setVisible(true);
        });
    }
}
