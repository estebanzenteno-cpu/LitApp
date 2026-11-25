package mx.lilapp.delegate;

import mx.lilapp.entity.Cliente;
import mx.lilapp.entity.Rol;
import mx.lilapp.entity.Usuario;
import mx.lilapp.exception.NegocioException;
import mx.lilapp.persistence.dao.UsuarioDAO;
import mx.lilapp.persistence.integration.ServiceLocator;
import mx.lilapp.seguridad.Contrasenas;
import mx.lilapp.seguridad.RolAcceso;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DelegateUsuario {

    public static final String MSG_CORREO_DUPLICADO = "El correo utilizado ya tiene una cuenta asignada";
    public static final String MSG_USUARIO_DUPLICADO = "El nombre de usuario ya está registrado";
    public static final String MSG_ROL_NO_DISPONIBLE = "No se pudo asignar el rol de cliente";

    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{10}$");

    public void registrarCuentaCliente(String correo, char[] clave) {
        registrarCuentaCliente(correo, "0000000000", clave);
    }

    public void registrarCuentaCliente(String correo, String telefono, char[] clave) {
        String nombre = correo == null ? "" : correo.strip().toLowerCase(Locale.ROOT);
        if (!CORREO.matcher(nombre).matches()) {
            throw new NegocioException("Escribe un correo válido.");
        }
        if (telefono == null || !TELEFONO.matcher(telefono.strip()).matches()) {
            throw new NegocioException("Favor de introducir un telefono con 10 caracteres numéricos");
        }
        if (clave == null || clave.length < 10) {
            throw new NegocioException("La contraseña debe tener al menos 10 caracteres.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setCorreo(nombre);
        usuario.setTelefono(telefono.strip());
        usuario.setContrasena(new String(clave));
        registrar(usuario);
    }

    public void registrar(Usuario usuario) {
        UsuarioDAO dao = ServiceLocator.getInstanceUsuarioDAO();

        if (dao.findByNombreUsuario(usuario.getNombreUsuario()) != null) {
            throw new NegocioException(MSG_USUARIO_DUPLICADO);
        }
        if (dao.findByCorreo(usuario.getCorreo()) != null) {
            throw new NegocioException(MSG_CORREO_DUPLICADO);
        }

        Rol rolCliente = ServiceLocator.getInstanceRolDAO().findByNombre("cliente");
        if (rolCliente == null) {
            throw new NegocioException(MSG_ROL_NO_DISPONIBLE);
        }

        usuario.setRol(rolCliente);
        usuario.setCliente(obtenerCliente(usuario));
        char[] clave = usuario.getContrasena().toCharArray();
        try {
            usuario.setContrasena(Contrasenas.generarHash(clave));
        } finally {
            Arrays.fill(clave, '\0');
        }
        dao.save(usuario);
    }

    public RolAcceso autenticar(String nombre, char[] clave) {
        if (nombre == null || nombre.isBlank() || clave == null || clave.length == 0) {
            return null;
        }

        Usuario usuario = ServiceLocator.getInstanceUsuarioDAO().findByNombreUsuario(nombre.strip());
        if (usuario == null || !Contrasenas.verificar(clave, usuario.getContrasena())) {
            return null;
        }

        String rol = usuario.getRol().getNombreRol().strip().toLowerCase(Locale.ROOT);
        return switch (rol) {
            case "gerente" -> RolAcceso.GERENTE;
            case "empleado" -> RolAcceso.EMPLEADO;
            case "cliente" -> RolAcceso.CLIENTE;
            default -> null;
        };
    }

    private Cliente obtenerCliente(Usuario usuario) {
        Cliente cliente = ServiceLocator.getInstanceClienteDAO().findByCorreo(usuario.getCorreo());
        if (cliente != null) {
            return cliente;
        }

        String[] partes = usuario.getNombreUsuario().trim().split("\\s+");
        cliente = new Cliente();
        cliente.setNombres(partes[0]);
        cliente.setApellidoPaterno(partes.length > 1 ? partes[1] : partes[0]);
        if (partes.length > 2) {
            cliente.setApellidoMaterno(partes[2]);
        }
        cliente.setCorreo(usuario.getCorreo());
        cliente.setTelefono(usuario.getTelefono());
        ServiceLocator.getInstanceClienteDAO().save(cliente);
        return cliente;
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
}
