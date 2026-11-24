package mx.lilapp.persistence.dao;

import mx.lilapp.entity.Usuario;
import mx.lilapp.persistence.persistence.AbstractDAO;
import mx.lilapp.persistence.persistence.HibernateUtil;

public class UsuarioDAO extends AbstractDAO<Usuario> {

    /** Indica que este DAO trabaja con la entidad Usuario. */
    public UsuarioDAO() {
        super(Usuario.class);
    }

    public Usuario buscarPorNombre(String nombre) {
        try (var em = HibernateUtil.nuevoEntityManager()) {
            var usuarios = em.createQuery(
                    "SELECT u FROM Usuario u JOIN FETCH u.rol WHERE LOWER(u.nombreUsuario) = LOWER(:nombre)",
                    Usuario.class).setParameter("nombre", nombre).setMaxResults(2).getResultList();
            return usuarios.size() == 1 ? usuarios.get(0) : null;
        }
    }
}
