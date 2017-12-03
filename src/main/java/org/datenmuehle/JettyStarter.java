package org.datenmuehle;

import org.datenmuehle.persistence.ftp.FtpPersistor;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Properties;

public class JettyStarter {

    int port = 8080;

    private void setPort() {
        try {
            Integer portProp = Integer.parseInt(System.getProperty("server.port"));

            if (portProp != null) port = portProp;

            System.out.println("set port " + port);

        } catch (NumberFormatException e) {
            System.out.println("start with default port " + port);
        }
    }

    public void run() throws Exception {

        ClassLoader classLoader = this.getClass().getClassLoader();

        setPort();

        Server server = new Server(port);

        String realmProps = classLoader.getResource("properties/realm.properties").toExternalForm();
        String webCtx     = classLoader.getResource("WEB-INF").toExternalForm();
        InputStream ftpProps   = classLoader.getResourceAsStream("properties/ftp.properties");

        Properties properties = new Properties();
        properties.load(ftpProps);

        FtpPersistor.setProperties(properties);

        LoginService loginService = new HashLoginService("MyRealm", realmProps);

        server.addBean(loginService);

        // A security handler is a jetty handler that secures content behind a
        // particular portion of a url space. The ConstraintSecurityHandler is a
        // more specialized handler that allows matching of urls to different
        // constraints. The server sets this as the first handler in the chain,
        // effectively applying these constraints to all subsequent handlers in
        // the chain.
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        server.setHandler(security);

        // This constraint requires authentication and in addition that an
        // authenticated user be a member of a given set of roles for
        // authorization purposes.
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[] { "user", "admin" });

        // Binds a url pattern with the previously created constraint. The roles
        // for this constraing mapping are mined from the Constraint itself
        // although methods exist to declare and bind roles separately as well.
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);

        // First you see the constraint mapping being applied to the handler as
        // a singleton list, however you can passing in as many security
        // constraint mappings as you like so long as they follow the mapping
        // requirements of the servlet api. Next we set a BasicAuthenticator
        // instance which is the object that actually checks the credentials
        // followed by the LoginService which is the store of known users, etc.
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);

        // Handler for multiple web apps
        HandlerCollection handlers = new HandlerCollection();


        // Creating the first web application context
        WebAppContext webapp1 = new WebAppContext(webCtx,"/");
        webapp1.setDescriptor(webCtx + "/web.xml");

        //webapp1.setDefaultsDescriptor("src/main/web/WEB-INF/web.xml");
        Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );

        webapp1.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*" );

        webapp1.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        handlers.addHandler(webapp1);

        // Adding the handlers to the server
        security.setHandler(handlers);

        // Starting the Server
        server.start();
        System.out.println("Started!");
        server.join();
    }

    public static void main(String[] args) throws Exception {
        new JettyStarter().run();
    }
}
