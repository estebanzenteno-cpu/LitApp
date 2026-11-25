package mx.lilapp.delegate;

import mx.lilapp.entity.Cliente;
import mx.lilapp.entity.Rol;
import mx.lilapp.entity.Usuario;
import mx.lilapp.exception.NegocioException;
import mx.lilapp.persistence.dao.UsuarioDAO;
import mx.lilapp.persistence.integration.ServiceLocator;
import mx.lilapp.util.PasswordHasher;

import java.util.List;

public class DelegateUsuario {

    public static final String MSG_CORREO_DUPLICADO = "El correo utilizado ya tiene una cuenta asignada";
    public static final String MSG_USUARIO_DUPLICADO = "El nombre de usuario ya está registrado";
    public static final String MSG_ROL_NO_DISPONIBLE = "No se pudo asignar el rol de cliente";

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
        usuario.setContrasena(PasswordHasher.hash(usuario.getContrasena()));
        dao.save(usuario);
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
