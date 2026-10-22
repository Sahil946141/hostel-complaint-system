# Task 13 — Configuration Management Specification

## Server Prerequisites

| Requirement | Detail |
|---|---|
| OS | Ubuntu 22.04 LTS |
| Java | OpenJDK 21 |
| Docker | Docker CE (latest) |
| Docker Compose | v2+ |
| Nginx | Latest stable |
| PostgreSQL | 15 |
| Port 8080 | Backend API |
| Port 3000 | Frontend |
| Port 5432 | PostgreSQL (internal) |
| Port 80/443 | Nginx (reverse proxy) |
| User | `hostelcms` (non-root) |
| App Directory | `/opt/hostel-cms/` |

## Ansible Playbook Summary

File: `ansible/playbook.yml`

Plays:
1. **Install packages** — Java 21, Docker, Nginx, curl, git
2. **Create app user** — `hostelcms` with sudo for Docker
3. **Create directories** — `/opt/hostel-cms/`, `/var/log/hostel-cms/`
4. **Configure Nginx** — reverse proxy config for `/api` → port 8080
5. **Pull Docker image** — `hostelcms/hostel-complaint-system:latest`
6. **Run container** — expose port 8080, pass env vars
7. **Enable services** — Docker, Nginx start on boot
8. **Health check** — verify app responds on port 8080

## Run Ansible Playbook

```bash
# Check connectivity
ansible all -i ansible/inventory.ini -m ping

# Run full provisioning
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml

# Run with verbose output
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml -v

# Dry run (check mode - no changes made)
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --check
```

## Idempotency

Running the playbook **twice** gives the same result:
- Package install → skips if already installed
- Container run → stops old, starts new with same config
- Nginx config → overwrites with same content

## Rollback

```bash
ansible-playbook -i ansible/inventory.ini ansible/rollback.yml
```

Rollback does:
1. Stop current container
2. Pull previous tag (e.g. `v1.0.4`)
3. Start with previous image
