# Makefile for Library System

# Variables
COMPOSE_FILE := docker-compose.yml

# Dynamic Service Detection
# 1. Find all subdirectories containing a pom.xml
POM_FILES := $(wildcard */pom.xml)
# 2. Extract the directory names (e.g., "book-service/ discovery-service/")
SERVICES := $(dir $(POM_FILES))

.PHONY: help up down logs

help:
	@echo "Detected Services: $(SERVICES)"
	@echo "Targets:"
	@echo "  make up    -> Compiles JARs (mvn install) and starts Docker containers"
	@echo "  make down  -> Stops Docker and cleans Maven artifacts (mvn clean)"
	@echo "  make logs  -> Follows container logs"

# ----------------------------------------------------------------
# UP: Build JARs -> Start Docker
# ----------------------------------------------------------------
up:
	@echo "Starting Build Process for: $(SERVICES)..."
	@for service in $(SERVICES); do \
		echo ">> Building $$service..."; \
		(cd $$service && mvn clean install -DskipTests) || exit 1; \
	done
	@echo ">> Starting Docker Containers..."
	docker-compose -f $(COMPOSE_FILE) up -d --build

# ----------------------------------------------------------------
# DOWN: Stop Docker -> Clean JARs
# ----------------------------------------------------------------
down:
	@echo ">> Stopping Docker Containers..."
	docker-compose -f $(COMPOSE_FILE) down -v --remove-orphans
	@echo ">> Cleaning Maven Artifacts in: $(SERVICES)..."
	@for service in $(SERVICES); do \
		echo "Cleaning $$service..."; \
		(cd $$service && mvn clean); \
	done

logs:
	docker-compose -f $(COMPOSE_FILE) logs -f