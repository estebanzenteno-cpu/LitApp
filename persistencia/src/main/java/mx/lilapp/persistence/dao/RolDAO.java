package mx.lilapp.persistence.dao;

import mx.lilapp.entity.Rol;
import mx.lilapp.persistence.persistence.AbstractDAO;
import mx.lilapp.persistence.persistence.HibernateUtil;

public class RolDAO extends AbstractDAO<Rol> {

    /** Indica que este DAO trabaja con la entidad Rol. */
    public RolDAO() {
        super(Rol.class);
    }

    public Rol buscarPorNombre(String nombre) {
        try (var em = HibernateUtil.nuevoEntityManager()) {
            var roles = em.createQuery("SELECT r FROM Rol r WHERE LOWER(r.nombreRol) = :nombre", Rol.class)
                    .setParameter("nombre", nombre).setMaxResults(2).getResultList();
            return roles.size() == 1 ? roles.get(0) : null;
        }
    }

}
