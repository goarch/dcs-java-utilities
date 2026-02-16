package generationpresets;

/**
 * Shared context to hold the active client across utilities.
 */
public class ClientContext {
    private static String currentClient;

    public static void setCurrentClient(String client) {
        currentClient = client;
    }

    public static String getCurrentClient() {
        return currentClient;
    }
}