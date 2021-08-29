This is how I generated the keystore that allowed me to run jetty in https

$ keytool -genkey -keystore keystore.jks -alias ssl -keyalg RSA -sigalg SHA256withRSA -validity 365 -keysize 2048

# On Codespaces
You must start the server (https or not), find the port forwarding rule in the PORTS tab, right click the row of the application (port 3000), and click "Make public."

Also use that url in the Azure application authentication section (it should be https).