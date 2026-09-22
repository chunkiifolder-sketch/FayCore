package chunk.faye.mod_tog.faycore;

/**
 * FayCore Extra Features
 * Single entry point that registers every extra utility feature.
 *
 * To enable, add this single line to FaycoreModClient.onInitializeClient():
 *
 *   FayCoreExtraFeatures.register();
 */
public class FayCoreExtraFeatures {
    public static void register() {
        FayCoreDash.register();
        FayCoreAutoSprint.register();
        FayCoreFly.register();
        FayCoreFullbright.register();
        FayCoreAutoTool.register();
        FayCoreWaypoint.register();
        FayCoreZoom.register();
    }
}
