# Secure
To run this locally, use [mkcert](This is how to run this locally so that your browser).

1. `brew install mkcert`
2. `mkcert -install`
3. `mkcert -pkcs12 localhost` - Or the name of the local host


# Insecure
This only works when you're making an explicitly-insecure server. This is used in testing. This will not work with many browsers.

$ keytool -genkey -keystore keystore.jks -alias ssl -keyalg RSA -sigalg SHA256withRSA -validity 365 -keysize 2048

# On Codespaces
You must start the server (https or not), find the port forwarding rule in the PORTS tab, right click the row of the application (port 3000), and click "Make public."

Also use that url in the Azure application authentication section (it should be https).
