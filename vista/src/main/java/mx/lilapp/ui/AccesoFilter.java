package mx.lilapp.ui;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import mx.lilapp.seguridad.RolAcceso;

import java.io.IOException;

@WebFilter("*.xhtml")
public class AccesoFilter implements Filter {
    @Override
    public void doFilter(ServletRequest solicitud, ServletResponse respuesta, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) solicitud;
        HttpServletResponse response = (HttpServletResponse) respuesta;
        String ruta = request.getServletPath();
        if (ruta.startsWith("/jakarta.faces.resource/")) {
            chain.doFilter(request, response);
            return;
        }
        HttpSession session = request.getSession(false);
        RolAcceso rol = session == null ? null : (RolAcceso) session.getAttribute(LoginBeanUI.ROL_SESION);
        String destino = rol == null ? "/login.xhtml" : rol.esInterno() ? "/index.xhtml" : "/panel-cliente.xhtml";
        response.setHeader("Cache-Control", "no-store");
        if (ruta.equals(destino) || (rol == null && "/crear-cuenta.xhtml".equals(ruta))) {
            chain.doFilter(request, response);
        } else if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write("<partial-response><redirect url=\""
                    + request.getContextPath() + destino + "\"/></partial-response>");
        } else {
            response.sendRedirect(request.getContextPath() + destino);
        }
    }
}
