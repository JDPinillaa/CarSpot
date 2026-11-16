package autonoma.models.motores;

/** Curva inicial del EA888 evo4 del Golf GTI europeo 265 PS. */
public final class MotorGolfGtiMk85 extends Motor {
    public MotorGolfGtiMk85() {
        // 370 Nm entre 1.600 y 4.500 rpm y 195 kW entre 5.250 y 6.500 rpm.
        // Los puntos intermedios de par se derivan de T=P/omega para conservar
        // la meseta de potencia al interpolar. 270 Nm al ralentí y 6.800 rpm
        // son estimaciones; el extremo de par se calibra con pruebas medidas.
        super(
                250,
                195,
                850,
                6800,
                new int[]{850, 1600, 4500, 5250, 5500, 5750, 6000, 6250, 6500, 6800},
                new double[]{270, 370, 370, 354.688, 338.566, 323.846, 310.352, 297.938, 286.479, 270}
        );
    }
}
