# Reset Password Plugin

The Reset Password plugin provides basic reset password functionality. 


# Documentation (GitHub Pages)

Documentation is available at [bloomreach-forge.github.io/reset-password/](https://bloomreach-forge.github.io/reset-password/)

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn -Pgithub.pages clean site
```

The output is in the ```/docs``` directory; push it and GitHub Pages will serve the site automatically. 

For rendering documentation on non-master branches, use the normal site command so the output will be in the ```/target``` 
and therefore ignored by Git.

 > mvn clean site:site


 # Development: email testing
 Use essentials plugin for testing:
 
 ```xml
     <dependency>
        <groupId>org.bloomreach.forge.resetpassword</groupId>
        <artifactId>reset-password-essentials</artifactId>
        <version>7.0.0</version>       
     </dependency>
```
 
The demo `context.xml` is pre-configured to use `localhost:2525`. Start a local SMTP trap before running the demo:

**Mailpit** (recommended — web UI at `http://localhost:8025`):

```bash
brew install mailpit
mailpit --smtp 0.0.0.0:2525
```

**Docker (no install):**

```bash
docker run --rm -p 2525:25 -p 8025:8025 axllent/mailpit
```

If you need to point a different project at a local mail session, add this JNDI resource to its `context.xml`:

```xml
<Resource name="mail/Session"
  auth="Container"
  type="jakarta.mail.Session"
  mail.smtp.host="127.0.0.1"
  mail.smtp.port="2525"/>
```

---

1. Go to the CMS login page and click **Forgot password**.
2. Enter a username and click **Reset** — the email will be captured by Mailpit.
3. Open `http://localhost:8025`, find the email, and click the reset link.
4. Enter and confirm the new password, then click **Reset**.
5. Return to the CMS login page and verify the new password works.
