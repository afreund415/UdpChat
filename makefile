
JFLAGS = -cp "./json.jar:./"
JCFLAGS = -g -cp "./json.jar:./"
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JCFLAGS) $*.java

CLASSES = \
 UdpChat.java \
 Server.java \
 Client.java \
 Message.java 

default: classes jar

classes: $(CLASSES:.java=.class)

jar: $(classes)
	jar cfvem UdpChat.jar UdpChat UdpChat.mf -C ./ *.class 


clean:
	$(RM) *.class UdpChat.jar

run:
	java $(JFLAGS) UdpChat
	