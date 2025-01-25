package com.uselessmnemonic.pak;

/**
 * Loads pak-rs in JNI mode.
 */
public final class PakRs {

    static {
        String osArch = System.getProperty("os.arch");
        String libArch;
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            libArch = "x86_64";
        } else {
            String message = String.format("Unsupported architecture %s", osArch);
            throw new RuntimeException(message);
        }

        String osName = System.getProperty("os.name");
        String libName;
        String libExt;
        if (osName.startsWith("Windows")) {
            libName = "pak";
            libExt = ".dll";
        } else if (osName.startsWith("Mac") || osName.contains("Darwin")) {
            libName = "libpak";
            libExt = ".dylib";
        } else {
            libName = "libpak";
            libExt = ".so";
        }

        String jarPath = String.format("%s/%s%s", libArch, libName, libExt);
        URL libUrl = ZStreamRefImpl.class.getClassLoader().getResource(jarPath);
        if (libUrl == null) {
            String message = String.format("Unsupported platform %s/%s", osName, osArch);
            throw new RuntimeException(message);
        }

        try (InputStream libData = libUrl.openStream()) {
            Path tmp = Files.createTempFile(libName, libExt);
            Files.copy(libData, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            System.load(tmp.toString());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private PakRs() {}
}
