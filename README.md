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
 
add mail session JNDI resource to local context.xml, e.g.:
 
```
    <Resource name="mail/Session" 
      auth="Container" 
      type="javax.mail.Session" 
      mail.smtp.host="127.0.0.1"
      mail.smtp.port= "2525"  
      />
```

___
Download  FakeSMTP from:

[https://github.com/Nilhcem/FakeSMTP](https://github.com/Nilhcem/FakeSMTP)

and run it as:

```bash
java -jar fakeSMTP-2.1-SNAPSHOT.jar -s  -p 2525 -a 127.0.0.1
```
If you encounter issues on Mac, you can try to run it as:

```bash
java --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED -jar fakeSMTP-2.1-SNAPSHOT.jar -s -p 2525 -a 127.0.0.1
```

You may also need to update FakeSMTP to use Java 7 instead of Java 6.
___
-> Go to CMS login page.

-> Click on Forgot password, it will take you to the Reset Password page.

-> Enter the username and click on Reset.

-> An email will be sent to the Fake SMTP Server with a link to reset the password.

-> Click on the link, it will take you to the Reset Password page, enter the new password and click on Reset.

-> Go to the CMS Login page and test the new password.
