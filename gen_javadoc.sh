#/bin/sh

javadoc -classpath libs/osmdroid-android-3.0.9.jar -d doc/ \
	-sourcepath src/ com.codigoparallevar.deliver -charset UTF-8
