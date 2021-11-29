# Git Flow

### The project's overall git flow
1. The `master` branch contains production-ready code only, and is not allowed to push at by anyone except of maintainers and owners;
2. The `development` branch is the one containing code to be tested before merging to `master`;
3. Each issue should be provided with a separate feature branch, called by an issue ID and optionally a brief issue name (e.g. `14-bootstrap-project`);
   The branch should be created from the `development` one;
4. When a developer finishes, he/she creates a PR from the feature branch to `development`. Maintainers or admins review this PR, and eventually perform merging;
    1. When there are conflicts between feature branch and `development`, developer should resolve them by **rebasing** his feature branch on `development` (e.g. `git rebase origin/development`);
        1. Force pushing to the feature branch is allowed;
        2. Direct commits to `development` are **not allowed**;
5. Eventually a maintainer/admin merges `development` into `master`.

### Conventional commits
* Project's commits are enforced to fit the [**conventional commits**](https://www.conventionalcommits.org/en/v1.0.0/#summary) style. Especially, each commit should be provided with a type of work (e.g. `fix`, `feat`, `test` etc), an issue ID and brief description of what was done;
* To support above, project contains a config for a commit linter called **gommit**, which makes checking commit messages automatic; To use it,
    * Download the latest release of Gommit [here](https://github.com/antham/gommit/releases), and install it, so that the `gommit` command could be used from within the console;
    * Add a Git configuration setting so that `gommit` checker could be run as a pre-commit hook:
        * `git config core.hooksPath .githooks`
* You can find examples of commit naming in the `{project-root}/.gommit.toml -> [examples]` section

### Code style & formatting
The project uses a clojure formatter called `cljstyle`. This needs to be installed prior to work.
To install, follow the [below](https://github.com/greglook/cljstyle#installation) instructions.
Generally, you should have `cljstyle` command available from your command line.
You should also have the `core.hooksPath` git config set up (see above), as the `pre-commit` hook is used. 

### Development
* To start development, create a `deployment/local/.env` file 
  from the `deployment/local/.env.template` template, then export 
  the environment variables and run a REPL:
  ```sh
  source ./scripts/setenv-local.sh && cd modules/backend/
  lein repl
  ```
* Run `dev` to load the development namespace:
  ```clojure
  user=> (dev)
  #object[clojure.lang.Namespace ... "dev"]
  ```
* In the `dev` namespace, run `go` to prep and initiate the system.
  This creates a web server at <http://localhost:8080>:
  ```clojure
  user=> (go)
  :initiated
  ```
* In the `dev` namespace, when you make changes to the source code files, 
  use `reset` to reload any modified files and reset the server:
  ```clojure
  user=> (reset)
  :reloading (...)
  :resumed
  ```
* To run tests:
  ```sh
  lein test
  ```
* To clean all compiled files:
  ```sh
  lein clean
  ```
* To create a production build run:
  ```sh
  lein clean
  lein uberjar
  ```
* To run the project application in a containerized environment,
  define the environment variables in the `deployment/local/.env` file,
  than go to the **the project root** and run the command:
  ```sh
  docker compose --file deployment/local/docker-compose.yml up --build
  ```
  To run only Metabase (and PostgreSQL) in a containerized environment:
  ```sh
  docker compose --file deployment/local/docker-compose.yml up --build metabase
  ```
