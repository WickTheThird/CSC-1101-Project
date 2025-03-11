JAVAC = javac
JAVA = java
MAIN_CLASS = Main
SRC_FILES = $(wildcard *.java)
CLASS_FILES = $(SRC_FILES:.java=.class)

all: compile run-gui

compile: $(CLASS_FILES)

%.class: %.java
	$(JAVAC) $<

run-gui: compile
	$(JAVA) $(MAIN_CLASS) --gui

run: compile
	$(JAVA) $(MAIN_CLASS)

clean:
	rm -f *.class

.PHONY: all compile run-gui run clean
