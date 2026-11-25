package mx.lilapp.persistence.dao;

import mx.lilapp.entity.Usuario;
import mx.lilapp.persistence.persistence.AbstractDAO;

import java.util.List;

public class UsuarioDAO extends AbstractDAO<Usuario> {

    /** Indica que este DAO trabaja con la entidad Usuario. */
    public UsuarioDAO() {
        super(Usuario.class);
    }

    public Usuario findByCorreo(String correo) {
        List<Usuario> resultado = getEntityManager()
                .createQuery("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)", Usuario.class)
                .setParameter("correo", correo)
                .setMaxResults(1)
                .getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Usuario findByNombreUsuario(String nombreUsuario) {
        List<Usuario> resultado = getEntityManager()
                .createQuery("SELECT u FROM Usuario u WHERE LOWER(u.nombreUsuario) = LOWER(:nombreUsuario)", Usuario.class)
                .setParameter("nombreUsuario", nombreUsuario)
                .setMaxResults(1)
                .getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }
}
