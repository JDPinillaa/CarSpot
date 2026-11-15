package autonoma.sounds;

import java.net.URL;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/** Reproducción de efectos, mantenida fuera del modelo del vehículo. */
public final class ReproductorAudio {
    public void reproducir(String archivo) {
        try {
            URL recurso = getClass().getResource("/autonoma/sounds/" + archivo);
            if (recurso == null) return;
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(recurso));
            clip.addLineListener(evento -> {
                if (evento.getType() == javax.sound.sampled.LineEvent.Type.STOP) clip.close();
            });
            clip.start();
        } catch (Exception ignorada) {
            // El audio es opcional: la simulación sigue funcionando sin dispositivo de sonido.
        }
    }
}
