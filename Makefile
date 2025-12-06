# Makefile for Library System

# Variables
COMPOSE_FILE := docker-compose.yml
SERVICE_DIR := book-service
SCRIPT := ./free_ports.sh

.PHONY: help build up down clean logs

help:
	@echo "Available targets:"
	@echo "  make up      - Frees ports, builds the application, and starts Docker containers"
	@echo "  make down    - Stops and removes Docker containers"
	@echo "  make build   - Builds the Java application (Maven)"
	@echo "  make clean   - Cleans Maven artifacts and Docker resources"
	@echo "  make logs    - Follows container logs"

build:
	@echo "Building the application..."
	cd $(SERVICE_DIR) && mvn clean install

up:
	@echo "Preparing to start services..."
	@chmod +x $(SCRIPT)
	@$(SCRIPT)
	@$(MAKE) build
	@echo "Starting Docker containers..."
	docker-compose -f $(COMPOSE_FILE) up -d --build

down:
	docker-compose -f $(COMPOSE_FILE) down

clean:
	cd $(SERVICE_DIR) && mvn clean
	docker-compose -f $(COMPOSE_FILE) down -v --rmi local --remove-orphans

logs:
	docker-compose -f $(COMPOSE_FILE) logs -f
