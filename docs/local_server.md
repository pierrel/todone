This is how I generated the keystore that allowed me to run jetty in https

$ keytool -genkey -keystore keystore.jks -alias ssl -keyalg RSA -sigalg SHA256withRSA -validity 365 -keysize 2048
