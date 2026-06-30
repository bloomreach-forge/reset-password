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
        <version>8.0.0</version>       
     </dependency>
```
 
The demo project's `conf/context.xml` is pre-configured for [MailHog](https://github.com/mailhog/MailHog), a local SMTP server that captures outbound mail without delivering it.

Start MailHog via Docker:

```bash
docker run -d -p 1025:1025 -p 8025:8025 --name mailhog mailhog/mailhog
```

Or via Homebrew:

```bash
brew install mailhog
MailHog
```

The demo `conf/context.xml` is already configured to use it:

```xml
<Resource name="mail/Session"
  auth="Container"
  type="jakarta.mail.Session"
  mail.smtp.host="localhost"
  mail.smtp.port="1025"
/>
```

Captured emails are viewable at [http://localhost:8025](http://localhost:8025).

---

-> Go to CMS login page.

-> Click on Forgot password, it will take you to the Reset Password page.

-> Enter the username and click on Reset.

-> An email will be sent to MailHog with a link to reset the password. Open [http://localhost:8025](http://localhost:8025) to view it.

-> Click on the link, it will take you to the Reset Password page, enter the new password and click on Reset.

-> Go to the CMS Login page and test the new password.
