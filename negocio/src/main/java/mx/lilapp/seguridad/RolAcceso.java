package mx.lilapp.seguridad;

public enum RolAcceso {
    GERENTE, EMPLEADO, CLIENTE;

    public boolean esInterno() {
        return this == GERENTE || this == EMPLEADO;
    }
}
