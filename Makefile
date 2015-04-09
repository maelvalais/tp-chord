all: build
	@echo ""
build:
	javac -d bin src/*
tous:

# ATTENTION: rmiregistry doit être lancé là où il y a banque.Banque
