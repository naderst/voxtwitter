/*
    Vox Twitter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtwitter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtwitter.app;

import java.util.ArrayList;

/**
 * La clase AppMain es una abstracción para manejar el flujo de la App
 */
public class AppMain {
    private MainActivity mainActivity;
    private TwitterManager twitter;

    AppMain(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        twitter = new TwitterManager(mainActivity);
    }

    /**
     * Evento que se ejecuta cuando la aplicación está lista para empezar
     */
    public void onInit() {

    }
}
