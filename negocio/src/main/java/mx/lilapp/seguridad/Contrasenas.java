package mx.lilapp.seguridad;

import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Contrasenas {
    private static final int ITERACIONES = 600_000;

    private Contrasenas() {}

    public static boolean verificar(char[] clave, String almacenada) {
        if (almacenada == null) return false;
        String[] partes = almacenada.split(":", -1);
        if (partes.length != 3 || !"pbkdf2-sha256".equals(partes[0])) return false;
        try {
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            return sal.length == 16 && esperado.length == 32
                    && MessageDigest.isEqual(esperado, derivar(clave, sal));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(clave, sal, ITERACIONES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo verificar la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String generarHash(char[] clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        Base64.Encoder base64 = Base64.getEncoder();
        return "pbkdf2-sha256:" + base64.encodeToString(sal) + ":" + base64.encodeToString(derivar(clave, sal));
    }

    public static void main(String[] args) {
        if (System.console() == null) throw new IllegalStateException("Ejecuta desde una terminal interactiva");
        char[] clave = System.console().readPassword("Contraseña (mínimo 12 caracteres): ");
        if (clave == null) return;
        try {
            if (clave.length < 12) throw new IllegalArgumentException("Usa al menos 12 caracteres");
            System.out.println(generarHash(clave));
        } finally {
            Arrays.fill(clave, '\0');
        }
    }
}
