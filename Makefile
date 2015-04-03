all: build tous
	@echo ""
build:
	javac -d bin src/*
client:
	java -cp bin Client 0 &
serveur:
	java -cp bin Noeud
rmi:
	cd bin/banque/
	rmiregistry 0 &	
	cd ../..
tous:
	@echo "Lancement rmiregistry et du serveur et du client"
	cd bin	
	rmiregistry 0 &
	sleep 1
	java -cp bin Client 0 &	
	cd ..

# ATTENTION: rmiregistry doit être lancé là où il y a banque.Banque
