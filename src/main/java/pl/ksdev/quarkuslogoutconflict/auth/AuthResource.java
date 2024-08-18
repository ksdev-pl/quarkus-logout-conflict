package pl.ksdev.quarkuslogoutconflict.auth;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.time.Instant;
import java.util.Date;

@Path("auth")
public class AuthResource {
    private final CurrentIdentityAssociation identity;

    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    String authCookieName;

    public AuthResource(CurrentIdentityAssociation identity) {
        this.identity = identity;
    }

    @CheckedTemplate(basePath = "auth")
    public static class Templates {
        public static native TemplateInstance login();
    }

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showLoginPage() {
        return Templates.login();
    }

    @GET
    @Path("logout")
    public Response logout() {
        if (identity.getIdentity().isAnonymous()) {
            throw new UnauthorizedException("Not authenticated");
        }
        final NewCookie removeCookie = new NewCookie.Builder(authCookieName)
            .maxAge(0)
            .expiry(Date.from(Instant.EPOCH))
            .path("/")
            .build();
        return Response.seeOther(URI.create("/auth/login"))
            .cookie(removeCookie)
            .build();
    }
}
