# 🌿 Branch Policy — Hostel Complaint Management System

This document defines the official branch naming conventions and Git workflow rules for this project.

---

## 📋 Branch Types

| Branch Pattern | Purpose                          | Example                          |
|----------------|----------------------------------|----------------------------------|
| `master`       | Production-ready, stable code    | `master`                         |
| `dev`          | Integration branch for testing   | `dev`                            |
| `feature/*`    | New feature development          | `feature/raise-complaint-api`    |
| `bugfix/*`     | Non-critical bug fixes           | `bugfix/fix-login-validation`    |
| `hotfix/*`     | Emergency production patches     | `hotfix/jwt-token-expiry`        |
| `release/*`    | Release preparation & versioning | `release/v1.0.0`                 |
| `test/*`       | Experimental or testing branches | `test/selenium-integration`      |

---

## 🔄 Git Workflow (Gitflow)

```
master ←── hotfix/*
  ↑
release/* ←── dev ←── feature/*
                  ←── bugfix/*
```

### Standard Flow

1. **Create a feature branch** from `dev`:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feature/your-feature-name
   ```

2. **Develop on the feature branch**, making small atomic commits.

3. **Push and open a Pull Request** to `dev`:
   ```bash
   git push origin feature/your-feature-name
   # Open PR → base: dev, compare: feature/your-feature-name
   ```

4. **Code Review** — at least 1 review approval required.

5. **Merge into `dev`** after approval (squash or merge commit).

6. **Merge `dev` → `master`** only for releases after full testing.

---

## ✍️ Commit Message Convention

Follow the **Conventional Commits** format:

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

### Commit Types

| Type       | When to Use                                 |
|------------|---------------------------------------------|
| `feat`     | New feature                                 |
| `fix`      | Bug fix                                     |
| `docs`     | Documentation changes                       |
| `style`    | Formatting, no logic change                 |
| `refactor` | Code refactoring without feature/bug change |
| `test`     | Adding or fixing tests                      |
| `chore`    | Build tools, config, CI changes             |
| `ci`       | CI/CD pipeline changes                      |

### Examples

```bash
feat(auth): add JWT login endpoint
fix(complaint): handle null description in POST /complaint
docs(readme): update local setup instructions
chore(docker): update docker-compose postgres version
test(selenium): add raise complaint test case
ci(jenkins): add docker build stage to Jenkinsfile
```

---

## 🔒 Branch Protection Rules (apply on GitHub)

For `master` branch:
- ✅ Require pull request before merging
- ✅ Require at least 1 approval
- ✅ Require status checks (Jenkins build) to pass
- ✅ Disallow direct pushes
- ✅ Delete branch after merge

For `dev` branch:
- ✅ Require pull request before merging
- ✅ Require Jenkins build to pass
- ❌ Approval not mandatory (for speed)

---

## 🚫 Rules

- ❌ **Never commit directly to `master`**
- ❌ **Never commit `.env` files** (secrets must stay local)
- ❌ **Never commit `node_modules/` or `target/`**
- ✅ Always write meaningful commit messages
- ✅ Keep branches short-lived (merge within 2–3 days)
- ✅ Delete merged branches to keep repo clean

---

## 📛 Naming Rules

- Use **lowercase** letters only
- Use **hyphens** (`-`) to separate words (no underscores or spaces)
- Keep names **short but descriptive**

✅ Good: `feature/raise-complaint-form`  
❌ Bad: `feature/RaiseComplaintForm`, `feature/raise_complaint_form`, `feature/new`
