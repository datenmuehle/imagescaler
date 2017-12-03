package org.datenmuehle.persistence;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public interface Persistor {
    void write(BufferedImage image, String fileName);
}