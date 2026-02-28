<!-- Copilot / AI agent guidance for my-webapp-project -->
# Repository snapshot

- This is a minimal Maven-based Java WAR webapp. Packaging is `war` (see `pom.xml`).
- Static/dynamic web resources live under `src/main/webapp` (e.g. `index.jsp`).
- Servlet descriptors live in `src/main/webapp/WEB-INF/web.xml` (DTD: Servlet 2.3).

# Big picture for an AI coding agent

- Purpose: small archetype webapp (no Java sources present). The repo builds a WAR artifact which is intended to be deployed to a servlet container (Tomcat/Jetty).
- Key flow: `mvn clean package` -> produces `target/my-webapp-project.war` -> deploy to servlet container's `webapps/` or CI/CD pipeline.
- There are currently no `src/main/java` sources; most edits will be to JSPs, static assets, or `pom.xml`.

# Developer workflows (concrete commands)

- Build the WAR locally:

  - `mvn clean package`
  - To skip tests: `mvn -DskipTests package` (project has only a junit test dependency declared).

- Resulting artifact:

  - `target/my-webapp-project.war` (deploy to Tomcat/Jetty or unzip for inspection).

- Run in Tomcat (manual):

  - Copy `target/my-webapp-project.war` into `<TOMCAT_HOME>/webapps/` and start Tomcat.

# Project-specific conventions and patterns

- Webapp layout follows Maven WAR conventions: `src/main/webapp` is the web root.
- `web.xml` uses an older Servlet 2.3 DOCTYPE — assume legacy container compatibility when changing descriptors.
- Artifact finalName is `my-webapp-project` (see `<finalName>` in `pom.xml`).

# Integration points & dependencies

- Only declared dependency: `junit:junit:3.8.1` with test scope. There are no runtime Java dependencies declared.
- Integration is primarily the WAR artifact -> servlet container. No external services, DBs, or REST clients are present in the repo.

# What to look for when making changes

- If adding servlets or Java classes, create `src/main/java` and update `pom.xml` with needed dependencies and servlet API version.
- When updating `web.xml`, be mindful of the DTD (Servlet 2.3). If modernizing to Servlet 3.x, update the descriptor and ensure the target container supports it.
- For UI changes, edit `src/main/webapp/*.jsp` or add static assets under `src/main/webapp`.

# Useful examples from this repo

- Entry page: `src/main/webapp/index.jsp` (simple HTML greeting).
- Descriptor: `src/main/webapp/WEB-INF/web.xml` (legacy DTD).
- Build definition: `pom.xml` (packaging `war`, `<finalName>` set).

# CI / automation notes

- There are no CI workflows in the repository. A typical pipeline would run `mvn -B -DskipTests package` and then publish or deploy `target/*.war`.

# When you are the AI agent: scope your edits

- Prefer minimal, targeted changes: update JSPs or `pom.xml` only if necessary. If adding Java code, ensure `pom.xml` is updated and add a basic unit test under `src/test/java`.
- Do not assume an embedded container; document any run instructions you introduce (e.g., adding an embedded Tomcat plugin).

# Feedback

If anything in this file is unclear or you'd like the agent to include suggested CI snippets or a containerized run target, say so and I'll update this guidance.
