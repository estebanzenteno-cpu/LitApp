package mx.lilapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import mx.lilapp.integration.ServiceFacadeLocator;
import mx.lilapp.seguridad.RolAcceso;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("loginUI")
@RequestScoped
public class LoginBeanUI {
    public static final String ROL_SESION = "litapp.rol";
    private static final Logger LOGGER = Logger.getLogger(LoginBeanUI.class.getName());
    private String usuario;
    private String contrasena;

    public String iniciarSesion() {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isBlank()) {
            contrasena = null;
            return error("Favor de llenar todos los campos.");
        }
        char[] clave = contrasena.toCharArray();
        contrasena = null;
        RolAcceso rol;
        try {
            rol = ServiceFacadeLocator.getInstanceFacadeUsuario().autenticar(usuario.strip(), clave);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "No se pudo iniciar sesión", e);
            return error("No se pudo iniciar sesión. Inténtalo más tarde.");
        } finally {
            Arrays.fill(clave, '\0');
        }
        if (rol == null) return error("Usuario o contraseña incorrectos.");

        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        request.getSession().setAttribute(ROL_SESION, rol);
        request.changeSessionId();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage("Inicio de sesión exitoso."));
        return (rol.esInterno() ? "/index" : "/panel-cliente") + "?faces-redirect=true";
    }

    public String cerrarSesion() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }

    public boolean isGerente() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ROL_SESION)
                == RolAcceso.GERENTE;
    }

    private String error(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
        return null;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
