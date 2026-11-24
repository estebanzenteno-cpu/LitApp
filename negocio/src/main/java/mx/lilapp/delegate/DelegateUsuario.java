package mx.lilapp.delegate;

import mx.lilapp.entity.Usuario;
import mx.lilapp.exception.NegocioException;
import mx.lilapp.persistence.integration.ServiceLocator;
import mx.lilapp.seguridad.Contrasenas;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.sql.SQLException;

public class DelegateUsuario {
    private static final Pattern CORREO = Pattern.compile("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+");

    public void registrarCuentaCliente(String correo, char[] clave) {
        String nombre = correo == null ? "" : correo.strip().toLowerCase(Locale.ROOT);
        if (nombre.length() > 50 || !CORREO.matcher(nombre).matches()) {
            throw new NegocioException("Escribe un correo válido de hasta 50 caracteres.");
        }
        if (clave == null || clave.length < 12 || clave.length > 256) {
            throw new NegocioException("La contraseña debe tener entre 12 y 256 caracteres.");
        }

        var usuarios = ServiceLocator.getInstanceUsuarioDAO();
        if (usuarios.buscarPorNombre(nombre) != null) {
            throw new NegocioException("Ya existe una cuenta con ese correo.");
        }
        var rol = ServiceLocator.getInstanceRolDAO().buscarPorNombre("cliente");
        if (rol == null) {
            throw new IllegalStateException("Falta configurar el rol cliente");
        }
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setRol(rol);
        usuario.setContrasena(Contrasenas.generarHash(clave));
        try {
            usuarios.save(usuario);
        } catch (RuntimeException e) {
            for (Throwable causa = e; causa != null; causa = causa.getCause()) {
                if (causa instanceof SQLException sql && sql.getErrorCode() == 1062) {
                    throw new NegocioException("Ya existe una cuenta con ese correo.");
                }
            }
            throw e;
        }
    }

    /** Registra un usuario nuevo. */
    public void guardar(Usuario usuario) {
        ServiceLocator.getInstanceUsuarioDAO().save(usuario);
    }

    /** Actualiza la informacion de un usuario. */
    public Usuario actualizar(Usuario usuario) {
        return ServiceLocator.getInstanceUsuarioDAO().update(usuario);
    }

    /** Elimina un usuario. */
    public void eliminar(Usuario usuario) {
        ServiceLocator.getInstanceUsuarioDAO().delete(usuario);
    }

    /** Busca un usuario por su id. */
    public Usuario buscarPorId(Integer id) {
        return ServiceLocator.getInstanceUsuarioDAO().find(id);
    }

    /** Devuelve todos los usuarios. */
    public List<Usuario> listar() {
        return ServiceLocator.getInstanceUsuarioDAO().findAll();
    }
    public mx.lilapp.seguridad.RolAcceso autenticar(String nombre, char[] clave) {
        if (nombre.length() > 50 || clave.length > 256) return null;
        Usuario usuario = ServiceLocator.getInstanceUsuarioDAO().buscarPorNombre(nombre);
        if (usuario == null || !mx.lilapp.seguridad.Contrasenas.verificar(clave, usuario.getContrasena())) {
            return null;
        }
        return switch (usuario.getRol().getNombreRol().strip().toLowerCase(java.util.Locale.ROOT)) {
            case "gerente" -> mx.lilapp.seguridad.RolAcceso.GERENTE;
            case "empleado" -> mx.lilapp.seguridad.RolAcceso.EMPLEADO;
            case "cliente" -> mx.lilapp.seguridad.RolAcceso.CLIENTE;
            default -> null;
        };
    }
}
