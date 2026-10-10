The Web module uses the Core module to expose a JSON HTTP API for storing and organising Songs.
The design is RESTlike, exposing simple CRUD operations and not use cases.
The UI for the project leverages this and composes requests to fulfill its own needs,
without the API needing to be changed.

## Structure

Everything is structured around "resources", which are based on the Core module entities we want to expose an API for.

- `*API` interfaces define the available operations for each resource.
- `*Controller` classes implement the happy-path for these operations, delegating to Core module services, and throw exceptions when errors occur.
- `ErrorHandler` implements the error-handling for the API.

Other classes and their purpose are documented in the `semantic` package(s).