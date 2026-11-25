package mx.lilapp.persistence.dao;

import mx.lilapp.entity.Rol;
import mx.lilapp.persistence.persistence.AbstractDAO;

import java.util.List;

public class RolDAO extends AbstractDAO<Rol> {

    /** Indica que este DAO trabaja con la entidad Rol. */
    public RolDAO() {
        super(Rol.class);
    }

    public Rol findByNombre(String nombre) {
        List<Rol> resultado = getEntityManager()
                .createQuery("SELECT r FROM Rol r WHERE LOWER(r.nombreRol) = LOWER(:nombre)", Rol.class)
                .setParameter("nombre", nombre)
                .setMaxResults(1)
                .getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }
}
