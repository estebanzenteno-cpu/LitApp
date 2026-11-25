package mx.lilapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.lilapp.exception.NegocioException;
import mx.lilapp.integration.ServiceFacadeLocator;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("crearCuentaUI")
@RequestScoped
public class CrearCuentaBeanUI {
    private static final Logger LOGGER = Logger.getLogger(CrearCuentaBeanUI.class.getName());
    private String correo;
    private String telefono;
    private String contrasena;
    private String confirmacion;

    public String registrar() {
        String claveIngresada = contrasena;
        String confirmacionIngresada = confirmacion;
        contrasena = null;
        confirmacion = null;

        if (correo == null || correo.isBlank() || telefono == null || telefono.isBlank()
                || claveIngresada == null || claveIngresada.isBlank()
                || confirmacionIngresada == null || confirmacionIngresada.isBlank()) {
            return error("Favor de llenar todos los campos.");
        }
        if (!claveIngresada.equals(confirmacionIngresada)) {
            return error("Las contraseñas no coinciden.");
        }

        char[] clave = claveIngresada.toCharArray();
        try {
            ServiceFacadeLocator.getInstanceFacadeUsuario().registrarCuentaCliente(correo, telefono, clave);
        } catch (NegocioException e) {
            return error(e.getMessage());
        } catch (RuntimeException | LinkageError e) {
            LOGGER.log(Level.WARNING, "No se pudo crear la cuenta de cliente", e);
            return error("No se pudo crear la cuenta. Inténtalo más tarde.");
        } finally {
            Arrays.fill(clave, '\0');
        }

        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage("Cuenta creada. Ya puedes iniciar sesión."));
        return "/login?faces-redirect=true";
    }

    private String error(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
        return null;
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getConfirmacion() { return confirmacion; }
    public void setConfirmacion(String confirmacion) { this.confirmacion = confirmacion; }
}
