rem "C:\Program Files\j2sdk1.4.2_12\bin\keytool" -genkey -alias HexaWar
rem "C:\Program Files\j2sdk1.4.2_12\bin\keytool" -selfcert -alias hexawar
"C:\Program Files\j2sdk1.4.2_12\bin\jarsigner" -signedjar dist/HexaWarApplet.jar dist/HexaWarApp.jar hexawar