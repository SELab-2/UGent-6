![badge](https://github.com/SELab-2/UGent-6/actions/workflows/backend_testing.yaml/badge.svg)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

<img src="https://github.com/SELab-2/UGent-6/assets/15960534/bf16e825-2d96-46af-8dc0-12ec03ba545b" width="700">



| Student                                            | E-mailadres                 |
----------------------------------------------------|-----------------------------|
| [Matthias Vaneyck](https://github.com/Matthias-VE)           |  matthias.vaneyck@ugent.be       |
| [Inti Danschutter](https://github.com/Aqua-sc)       |  inti.danschutter@ugent.be    |
| [Arthur Werbrouck](https://github.com/AWerbrouck)  | Arthur.Werbrouck@ugent.be            |
| [Arne Dierick](https://github.com/arnedierick) |  arne.dierick@ugent.be  |
| [Wout  Verdyck](https://github.com/usserwoutV2)       |  wout.verdyck@ugent.be |
| [Floris Kornelis Van Dijken](https://github.com/badduck32)    |   floris.kornelisvandijken@ugent.be  |
| [Tristan Verbeken](https://github.com/TR1VER)       |  tristan.verbeken@ugent.be|


[wiki documentation](https://github.com/SELab-2/UGent-6/wiki)

[api documentation](https://apidog.com/apidoc/project-467959)
## Tree view of the project

- backend
  - app
    - src
      - main/java/com/ugent/pidgeon
        - auth (Authentication related logic)
        - config (Configuration settings and beans)
        - controllers (Web controllers for handling requests)
        - model (models for testing submissions)
        - json (request/response bodies)
        - postgre (Database models and repositories)
        - util (Utility classes and helpers)
      - resources (Configuration files, property files etc.)
      - test/java/com/ugent/pidgeon (Unit and integration tests)
  - database (Database schemas and migrations)
  - db (Database related scripts)
  - web-bff (Express webserver that manages user authentication with cookie sessions)

- frontend
  - public (Static files like images, fonts, and `index.html`)
  - src
    - @types (TypeScript type definitions)
    - assets (Static assets like images and logos used in the app)
    - components (Reusable UI components)
      - common (Commonly used components across the application)
      - forms (Form components, including project form tabs)
      - layout (Components related to layout such as navbars and sidebars)
    - hooks (Custom React hooks)
    - i18n (Internationalization setup, including language files)
    - pages (Component structure for each page)
      - course (Course-related components and logic)
      - projects (Project creation and editing interfaces)
    - providers (Context providers for state management)
    - router (Routing and path management)
    - theme (Styling themes and fonts)
    - util (Utility functions and helpers such as our `apiFetch`)

