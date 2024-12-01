package org.example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.prefs.Preferences;

public class SwingUtils {
    public static void resizeAndSaveWindowState(Preferences prefs, Component comp, String sizeKey, String locationKey) {
        Point pos = objFromPrefs(prefs, locationKey, Point.class);
        if (pos != null) {
            comp.setLocation(pos);
        }

        Dimension size = objFromPrefs(prefs, sizeKey, Dimension.class);
        if (size != null) {
            comp.setSize(size);
        }

        comp.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                objToPrefs(prefs, locationKey, comp.getLocation());
            }

            @Override
            public void componentResized(ComponentEvent e) {
                objToPrefs(prefs, sizeKey, comp.getSize());
            }
        });
    }

    private static <T> T objFromPrefs(Preferences prefs, String key, Class<T> clazz) {
        byte[] serializedObj = prefs.getByteArray(key, null);
        T result = null;
        if (serializedObj != null) {
            try (var in = new ObjectInputStream(new ByteArrayInputStream(serializedObj))) {
                result = clazz.cast(in.readObject());
            } catch (IOException | ClassCastException | ClassNotFoundException e) {
                // Ignored.
            }
        }

        return result;
    }

    private static void objToPrefs(Preferences prefs, String key, Object obj) {
        try (var bOut = new ByteArrayOutputStream(); var objOut = new ObjectOutputStream(bOut)) {
            objOut.writeObject(obj);
            prefs.putByteArray(key, bOut.toByteArray());
        } catch (IOException ioe) {
            // Ignored.
        }
    }

}
