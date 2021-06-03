
JFLAGS = -cp "./json-20201115.jar:./"
JCFLAGS = -g -cp "./json-20201115.jar:./"
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JCFLAGS) $*.java

CLASSES = \
 UdpChat.java \
 Server.java \
 Client.java \
 Message.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

run:
	java $(JFLAGS) UdpChat